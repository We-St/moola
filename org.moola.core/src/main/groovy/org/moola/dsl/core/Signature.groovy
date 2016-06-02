/*******************************************************************************
 * Copyright (coffee) 2014 Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Stefan Weghofer (Vienna University of Technology) - DSL and petri nets implementation
 * Alexander Bergmayr (Vienna University of Technology) - initial API and implementation
 *
 * Initially developed in the context of ARTIST EU project www.artist-project.eu
 *******************************************************************************/

package org.moola.dsl.core

import java.util.HashSet;
import java.util.List;

import groovy.lang.Closure;

/**
 * Defines a transformation in terms of input and output elements
 */
class Signature {

	List<Definition> expects;
	List<Definition> returns;
	
	public Signature() {
		expects = [];
		returns = [];
	}
	
	/**
	 * Adds a new input to the specification
	 * @param type The type of the input
	 * @param name The name of the input
	 */
	void addInput(String type, String name) {
		expects.add( new Definition( type: type, name: name ) )
	}
	
	/**
	 * Gets the inputs
	 * @return The inputs
	 */
	List<Definition> getInputs(){
		return this.expects
	}
	
	/**
	 * Adds a new output to the specification
	 * @param type The type of the output
	 * @param name The name of the output
	 */
	void addOutput(String type, String name) {
		returns.add( new Definition( type: type, name: name ) )
	}
	
	
	/**
	 * Gets the outputs
	 * @return The outputs
	 */
	List<Definition> getOutputs(){
		return this.returns
	}
	
	
	/**
	 * Inits the specification by invoking the closure on it
	 * @param initClosure The initialization closure
	 */
	void init(Closure initClosure) {
		initClosure.delegate = this
		initClosure.resolveStrategy = Closure.DELEGATE_FIRST
		initClosure.run()
	}
	
	/**
	 * Adds an input definition for each element in the map
	 * @param args A map consisting of key-value pairs. The key is the name of the input, the value the model type
	 */
	void inputs(args){
		def newInputs = args.collect { k, v -> [name: k, type: v] as Definition}
		expects.addAll newInputs
	}
	
	/**
	 * Adds an output definition for each element in the map
	 * @param args A map consisting of key-value pairs. The key is the name of the input, the value the model type
	 */
	void outputs(args){
		def newOutputs = args.collect { k, v -> [name: k, type: v] as Definition}
		returns.addAll newOutputs
	}
	
	/**
	 * Gets a list of all types involved in this spec
	 * @return A list of all types in this spec
	 */
	HashSet<String> getAllTypes(){
		HashSet<String> types = [];
		expects.each { d -> types.add(d.type) }
		returns.each { d -> types.add(d.type) }
		return types;
	}
	
	/**
	 * Gets the input definition with the given name
	 * @param name The name of the definition
	 * @return The definition
	 */
	Definition getInputDefinition(String name){
		return expects.find { it.name == name }
	}
	
	/**
	 * Gets the output definition with the given name
	 * @param name The name of the definition
	 * @return The definition
	 */
	Definition getOutputDefinition(String name){
		return returns.find { it.name == name }
	}
	
	@Override
	public String toString() {
		String ret = ""
		expects.each { ret += "(in -> $it)" }
		returns.each { ret += "(out -> $it)" }
		return ret
	}
	
}
