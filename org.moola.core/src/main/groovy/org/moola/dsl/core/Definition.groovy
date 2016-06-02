package org.moola.dsl.core

/**
 * Defines an interface element
 */
class Definition {
	
	Class type
	String name
	
	@Override
	public String toString() {
		return type.toString() + ": " + name
	}
	
}

