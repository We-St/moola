package org.moola.dsl.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.moola.logging.ILogger

@GroovyASTTransformation
class ModelNameTransformation implements ASTTransformation {

	
	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		def transformer = new ModelNameTransformer();
		source.getAST().getStatementBlock().visit(transformer);
		for (Object method : source.getAST().getMethods()) {
			MethodNode methodNode = (MethodNode) method;
			methodNode.getCode().visit(transformer);
		}
	}
	
	private class ModelNameTransformer extends CodeVisitorSupport {
		
		private final static String NAME_FIELD = "name"
		private final static String MODEL_NAME = "model"
				
		@Override
		public void visitMethodCallExpression(MethodCallExpression call) {
			doVisitMethodCallExpression(call);
			super.visitMethodCallExpression(call);
		}
		
		private void doVisitMethodCallExpression(MethodCallExpression call) {
			if(!AstHelper.isInstanceCall(MODEL_NAME, call) || !AstHelper.hasArguments(call)){
				return
			}
			
			def argList = (TupleExpression) call.getArguments()
			def arg = argList.getExpressions().get(0)
			
			if(arg instanceof MethodCallExpression){
				def nestedCall = (MethodCallExpression) arg
				transformNestedMethodCall(call, nestedCall)
			}
		}

		
		private void transformNestedMethodCall(MethodCallExpression modelCall, MethodCallExpression nestedCall) {
			def nestedMethod = nestedCall.method
			def modelName = ""
			if(nestedMethod instanceof ConstantExpression){
				modelName = nestedMethod.value
			}
			
			// create map
			def keyExpr = new ConstantExpression(NAME_FIELD)
			def valueExpr = new ConstantExpression(modelName)
			def nameExpr = new MapEntryExpression(keyExpr, valueExpr)
			
			def mapExpr = new MapExpression()
			mapExpr.mapEntryExpressions.add(nameExpr)
			addNestedMethodCallArguments(nestedCall, mapExpr);
			
			def originalArguments = (TupleExpression) modelCall.arguments
            originalArguments.expressions.clear()
			originalArguments.expressions.add(mapExpr)
			
			def configClosure = AstHelper.getClosureArgument(nestedCall)
			if(configClosure != null){
				originalArguments.expressions.add(configClosure)
			}
		}
		
		private void addNestedMethodCallArguments(MethodCallExpression nestedCall, MapExpression mapExpr){
			if(!AstHelper.hasArguments(nestedCall)){
				return
			}
			// add arguments of nested call to new call (e.g. forward 'type' and other params)
			def arguments = AstHelper.extractMapItems(nestedCall.arguments)
			mapExpr.mapEntryExpressions.addAll(arguments);
		}
					
	}
}
