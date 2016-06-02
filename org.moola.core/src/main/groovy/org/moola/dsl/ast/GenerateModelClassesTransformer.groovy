package org.moola.dsl.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.CompilePhase
import org.moola.dsl.core.ModelFactory

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class GenerateModelClassesTransformer implements ASTTransformation  {

	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		def transformer = new ModelClassesTransformer(source);
		source.getAST().getStatementBlock().visit(transformer);
		for (Object method : source.getAST().getMethods()) {
			MethodNode methodNode = (MethodNode) method;
			methodNode.getCode().visit(transformer);
		}
	}
	
	private class ModelClassesTransformer extends CodeVisitorSupport {
		
		private final static OPERATION_NAME = "modeltypes"
		private SourceUnit sourceUnit
		private ModuleNode module
		
		public ModelClassesTransformer(SourceUnit sourceUnit){
			this.sourceUnit = sourceUnit
			this.module = sourceUnit.AST
		}
		
		@Override
		public void visitMethodCallExpression(MethodCallExpression call) {
			doVisitMethodCallExpression(call);
			super.visitMethodCallExpression(call);
		}
		
		private void doVisitMethodCallExpression(MethodCallExpression call) {
			if(!AstHelper.isInstanceCall(OPERATION_NAME, call) || !AstHelper.hasTupleArgument(call)){
				return
			}
			
			def tuple = (TupleExpression) call.arguments
			def mapEntries = AstHelper.extractMapItems(tuple)
			mapEntries.each { item ->
				def className = AstHelper.getValue(item.keyExpression)
				def newClass = createClass(className)
				addClassToModule(newClass)
			}
			
//			println "Available classes in Moola script: "
//			module.getClasses().each {
//				println "-) " + it
//			}
		}
		
		
		/**
		 * Adds the class to the current module
		 * @param newClass The class to add
		 */
		private void addClassToModule(newClass){
			module.addClass(newClass)
		}
				
		
		/**
		 * Creates a new class node with the given name
		 * @param className The fully qualified name of the ClassNode
		 * @return A class with the given name
		 */
		private ClassNode createClass(String className) {
	 
			new AstBuilder().buildFromSpec {
				classNode(className, ClassNode.ACC_PUBLIC) {
					classNode ModelFactory.modelClass
					interfaces { classNode GroovyObject }
					mixins { }
				}
			}.first()
	 
		}
				
	}
	
}
