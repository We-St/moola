package org.moola.dsl.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation
class ScriptSeparator implements ASTTransformation {

	public static ArgumentListExpression CACHE;
	
	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		def transformer = new ScriptSeparatorTransformer();
		source.getAST().getStatementBlock().visit(transformer);
	}
	
	private class ScriptSeparatorTransformer extends CodeVisitorSupport {
		
		private final static String RUN_CLOSURE_NAME = "run"
				
		public ScriptSeparatorTransformer(){ }
		
		@Override
		public void visitMethodCallExpression(MethodCallExpression call) {
			doVisitMethodCallExpression(call);
			super.visitMethodCallExpression(call);
		}
		
		private void doVisitMethodCallExpression(MethodCallExpression call) {
			if(!AstHelper.isInstanceCall(RUN_CLOSURE_NAME, call) || !AstHelper.hasArguments(call)){
				return
			}
						
			CACHE = call.arguments // save for use in merger
			call.arguments = new ArgumentListExpression(new ConstantExpression("Hello from AST! :)"))
		}
				
	}
}
