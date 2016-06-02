package org.moola.dsl.ast

import java.util.List;

import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression;

class AstHelper {
	
	private final static OPERATION_TARGET = "this"
	
	
	/**
	 * Determines whether the method call is to an instance method of the passed name
	 * @param methodName The target name of the method call
	 * @param call The method call in the AST tree
	 * @return True if the method call is to the method name, false otherwise
	 */
	public static boolean isInstanceCall(String methodName, MethodCallExpression call){
		def isOperation = call.method.text == methodName
		def isOnThis = call.objectExpression.text == OPERATION_TARGET
		return isOperation && isOnThis
	}
	
	
	/**
	 * Checks if the method has arguments
	 * @param call The method call in the AST tree
	 * @return True if the method call has at least one argument, false otherwise
	 */
	public static boolean hasArguments(MethodCallExpression call){
		def arguments = call.arguments
		if(arguments == null){
			return false
		}
		if(arguments instanceof TupleExpression){
			def tupleExpr = (TupleExpression) arguments
			return tupleExpr.size() > 0
		}
		// Unexpected expression
		return false
	}
	
	
	/**
	 * Checks if the only argument of this method call is a tuple
	 * @param call The method call in the AST tree
	 * @return True if the only argument is a tuple, false otherwise
	 */
	public static boolean hasTupleArgument(MethodCallExpression call){
		def arguments = call.arguments
		if(arguments == null){
			return false
		}
		if(!(arguments instanceof TupleExpression)){
			return false
		}
		return true
	}
	
	
	/**
	 * Extracts the MapEntryExpressions from a tuple, e.g. from a method call 
	 * @param tuple The tuple expression
	 * @return A list of MapEntryExpressions
	 */
	public static List<MapEntryExpression> extractMapItems(TupleExpression tuple){
		if(tuple.size() == 0){
			return []
		}
		if(!(tuple.getExpression(0) instanceof MapExpression)){
			return [] //throw new RuntimeException("Cannot extract map items from tuple")
		}
		
		def firstItem = (MapExpression) tuple.getExpression(0)
		def result = new ArrayList<MapEntryExpression>(firstItem.mapEntryExpressions) // copy list to avoid unwanted modification
		return result
	}
	
	
	
	/**
	 * Gets the value of the expression, if it can be determined at compile-time. So far, this is only supported for
	 * ConstantExpressions.
	 * @param expr The (constant) expression in the AST tree
	 * @return The value of the expression
	 */
	public static Object getValue(Expression expr){
		if(!(expr instanceof ConstantExpression)){
			throw new RuntimeException("Cannot extract value from non-constant expression")
		}
		return expr.value
	}
	
	
	/**
	 * Returns the last argument of the method call, if it is a closure. Closures are often the last argument, since the Groovy
	 * syntax allows them to be appended to the method call
	 * @param call THe method call in the AST tree
	 * @return The closure, if it is the last argument or null otherwise
	 */
	public static ClosureExpression getClosureArgument(MethodCallExpression call){
		if(!hasArguments(call)){
			return null
		}
		
		def arguments = (TupleExpression) call.arguments
		def lastArgument = arguments.expressions.last()
		if(lastArgument instanceof ClosureExpression){
			return lastArgument
		}
		return null
	}
	
}
