package org.moola.dsl.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation
class ScriptMerger implements ASTTransformation {

	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		def transformer = new ScriptMergerTransformer();
		source.getAST().getStatementBlock().visit(transformer);
	}
	
	private class ScriptMergerTransformer extends CodeVisitorSupport {
		
		private final static String RUN_CLOSURE_NAME = "merge"
				
		public ScriptMergerTransformer(){ }
		
		@Override
		public void visitMethodCallExpression(MethodCallExpression call) {
			doVisitMethodCallExpression(call);
			super.visitMethodCallExpression(call);
		}
		
		private void doVisitMethodCallExpression(MethodCallExpression call) {
			if(!AstHelper.isInstanceCall(RUN_CLOSURE_NAME, call) || !AstHelper.hasArguments(call)){
				return
			}
			
			call.arguments = ScriptSeparator.CACHE
		}
			
	}
}
