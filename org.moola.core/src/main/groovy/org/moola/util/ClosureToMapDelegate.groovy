package org.moola.util

import groovy.lang.Closure;

/**
 * This class can be used as delegate for closures, especially closures for configuration. It will store all "method argument" pairs inside the
 * closure code as key-value pairs in a map. Only stores a key-value pair if the method is called with exactly one parameter.
 * @author Stefan Weghofer
 */
class ClosureToMapDelegate {
	
	def properties = [:]

	
	def methodMissing(String name, args) {
		if(args.size() != 1){
		  throw new RuntimeException("Unexpected number of arguments for $name")
		}
		properties[name] = args.iterator()[0]
	}

	
	/**
	 * Extracts the key-value pairs set in this closure
	 * @param closure The closure to execute
	 * @return A map holding the key-value pairs
	 */
	public static def evaluate(Closure closure)	{
		def delegate = new ClosureToMapDelegate();
		
		closure.delegate = delegate
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.run()
		
		return delegate.properties;
	}
}
