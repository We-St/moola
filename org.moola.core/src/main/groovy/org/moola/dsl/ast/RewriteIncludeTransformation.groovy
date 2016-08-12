package org.moola.dsl.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.CompilePhase

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class RewriteIncludeTransformation implements ASTTransformation  {

	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		def transformer = new RewriteIncludeTransformer(source);
		source.getAST().getStatementBlock().visit(transformer);
		for (Object method : source.getAST().getMethods()) {
			MethodNode methodNode = (MethodNode) method;
			methodNode.getCode().visit(transformer);
		}
	}
	
	private class RewriteIncludeTransformer extends CodeVisitorSupport {
		
		private final static METHOD_NAME = "include"
		private SourceUnit sourceUnit
		private ModuleNode module
		
		public RewriteIncludeTransformer(SourceUnit sourceUnit){
			this.sourceUnit = sourceUnit
			this.module = sourceUnit.AST
		}
		
		@Override
		public void visitMethodCallExpression(MethodCallExpression call) {
			this.doVisitMethodCallExpression(call);
			super.visitMethodCallExpression(call);
		}
		
		private void doVisitMethodCallExpression(MethodCallExpression call){
			if(call.method.text != METHOD_NAME || !AstHelper.hasTupleArgument(call)){
				return
			}
			
			def arguments = call.arguments as ArgumentListExpression
			def newArguments = new ArgumentListExpression()
			for(Expression expr : arguments.getExpressions()){
				if(expr instanceof VariableExpression){
					def vexpr = (VariableExpression) expr
					newArguments.addExpression(new ConstantExpression(vexpr.name))
				} else {
					throw new RuntimeException("Invalid include statement")
				}
			}
			call.arguments = newArguments
		}
				
	}
	
}
