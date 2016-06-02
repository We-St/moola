package org.moola.dsl.ast

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.ast.ClassHelper

import org.moola.dsl.core.OperationResult
import org.moola.dsl.core.Operation
import org.moola.dsl.core.OperationType
import org.moola.Process

/**
 * Allows multi assignments to be used in Moola. Multi assignments are not usable in type checked Groovy code. This AST unrolls multi 
 * assignments and allows for their use in Moola. Example:
 * 
 * (a, b) = operation(c, d)
 * 
 * is transformed to
 * 
 * temp = operation(c, d) // temp is an instance of MultiResult
 * a = temp.value0
 * b = temp.value1
 * 
 * 
 * @author Stefan Weghofer
 *
 */
@GroovyASTTransformation
class UnrollMultiAssignmentTransformation implements ASTTransformation {

	protected Process process
	
	public UnrollMultiAssignmentTransformation(Process process){
		this.process = process
	}
	
	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		def opTypes = process.operationTypes
		def transformer = new UnrollMultiAssignmentTransformer(opTypes);
		source.getAST().getStatementBlock().visit(transformer)
	}
	
	private class UnrollMultiAssignmentTransformer extends CodeVisitorSupport {
		
		private final static String RUN_CLOSURE_NAME = "merge"
		private static Random random = new Random()
		
		private List<OperationType> availableOperations
				
		public UnrollMultiAssignmentTransformer(List<OperationType> availableOperations){
			this.availableOperations = availableOperations
		}
		
		
		//TODO: implement unrolling for DeclarationExpression. It has a isMultipleAssignmentDeclaration method (!!!)
		
		@Override
		public void visitBlockStatement(BlockStatement block) {	
			// loop from last to first to allow for concurrent modifications of the statements in checkBinaryExpression()
			for(int i = block.statements.size() - 1; i >= 0; i--){
				def stmt = block.statements[i];
				if(stmt instanceof TryCatchStatement){
					continue;
				}
				if(stmt.expression instanceof BinaryExpression){
					checkBinaryExpression(block, stmt)
				}
			}
			super.visitBlockStatement(block);
		}
		
		
		private void checkBinaryExpression(BlockStatement block, Statement binaryStmt) {
			def binaryExpr = binaryStmt.expression as BinaryExpression
			if(!isMultiAssignmentExpression(binaryExpr)){
				return
			}						
			if(!isOperationCall(binaryExpr.rightExpression)){
				return
			}
					
			def tupleExpr = (TupleExpression) binaryExpr.leftExpression
			def opType = getOperationType(binaryExpr.rightExpression)
			def tempVariable = createTempVariableName(opType)
			
			// declare a new temporary variable and assign the operation method call result to it
			def tempVarExpr = new VariableExpression(tempVariable, ClassHelper.make(OperationResult))
			tempVarExpr.setSourcePosition(binaryStmt)
			binaryExpr.leftExpression = tempVarExpr

			int idx = block.statements.indexOf(binaryStmt)
			Operation op = opType.createInstance()
			
			// add an assignment for each variable in the tuple
			int i = 0;
			idx += 1
			getVariableNames(tupleExpr).each { variableName, variableType ->
				//println "--- created $variableName ($variableType) = ${tempVariable}.value${i} (${outputDefinition.type})"
				def outputDefinition = op.getSignature().getOutputs().get(i)
				def stmt = this.createAssignment(variableName, variableType, binaryStmt, tempVariable, i, outputDefinition.type);
				block.statements.add(idx + i, stmt)
				i += 1
			}
		}
		
		private boolean isMultiAssignmentExpression(BinaryExpression binaryExpr){
			if(binaryExpr.operation.text != "="){
				return false // only consider variable assignments
			}
			if(!(binaryExpr.rightExpression instanceof MethodCallExpression)){
				return false // only consider binary expressions with a method call on the right side
			}
			if(!(binaryExpr.leftExpression instanceof TupleExpression)){
				return false // only consider variable assignments to tuples
			}
			return true
		}
		
		private boolean isOperationCall(Expression expression){
			MethodCallExpression methodCall = (MethodCallExpression) expression
			return availableOperations.any { it.name == methodCall.method.text }
		}
		
		private OperationType getOperationType(Expression expression){
			MethodCallExpression methodCall = (MethodCallExpression) expression
			return availableOperations.find { it.name == methodCall.method.text }
		}
		
		private String createTempVariableName(OperationType opType){
			return "temp_" + opType.name + "_" + randomInt() + "_" + System.currentTimeMillis()
		}
		
		private int randomInt(){
			return Math.abs(random.nextInt())
		}
		
		private Map<String, ClassNode> getVariableNames(TupleExpression tupleExpr){
			def variables = [:]
			tupleExpr.each { item ->
				if(item instanceof VariableExpression){
					def variableExpr = item as VariableExpression
					variables.put(variableExpr.name, variableExpr.getType())
				}
			}
			return variables
		}
		
		/**
		 * Creates an assignment like:
		 * {var} = ({clazz}) {tempVar}.value({i})
		 */
		private def createAssignment(String var, ClassNode varType, Statement refNode, String tempVar, int i, def clazz){
			def clazzNode = ClassHelper.make(clazz)
			def varExpr = new VariableExpression(var, varType)
			def token = new Token(Types.ASSIGN, "=", 10, 10)
			def objExpr = new VariableExpression(tempVar, ClassHelper.make(OperationResult))
			def argExpr = new ArgumentListExpression(new ConstantExpression(i))
			def methodNameExpr = new ConstantExpression("value")
			def methodCall = new MethodCallExpression(objExpr, methodNameExpr, argExpr)
			
			def castExpr = new CastExpression(clazzNode, methodCall)
			castExpr.setSourcePosition(refNode)
			
			def assignExpr = new BinaryExpression(varExpr, token, castExpr)
			def stmt = new ExpressionStatement(assignExpr)
			stmt.setSourcePosition(stmt)
			
			return stmt
		}
	}
}