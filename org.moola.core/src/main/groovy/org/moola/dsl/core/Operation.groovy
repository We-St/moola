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

import java.util.List
import java.util.concurrent.locks.ReentrantLock;

import org.moola.logging.ILogger
import org.moola.logging.LoggerFactory
import groovy.lang.Closure

class Operation {
	
	public String name = ""

	protected ILogger log
	protected PathFactory pathFactory
	protected LockFactory lockFactory
	protected Signature signature
	protected def outputs = [:]
	protected def inputs = [:]
	protected Closure work
	protected List<Closure> preActions = []
	protected List<Closure> postActions = []
	
	private boolean useCustomSignature = false
	
	/**
	 * Creates a new transformation
	 */
	Operation() {
		this.signature = new Signature()
	}
	
	/**
	 * Sets the log level for this operation. If nothing is set, the project log level will be taken.
	 * @param level The new log level
	 */
	void loglevel(String level) {
		this.log.setLogLevel(level)
	}
	
	/**
	 * Gets the signature of this operation
	 * @return The signature
	 */
	public Signature getSignature(){
		return this.signature
	}
	
	/**
	 * Can be overridden in subclasses to describe the operation's interface.
	 * @return A signature defining the operation's interface.
	 */
	protected Signature provideSignature() { 
		return this.signature; 
	}
		
	/**
	 * A shortcut method to set the inputs on the signature for this operation
	 * @param inputs The expected input values (name and type)
	 */
	protected void expects(inputs){
		signature.inputs(inputs);
		useCustomSignature = true
	}
	
	/**
	 * A shortcut method to set the outputs on the signature for this operation
	 * @param inputs The expected output values (name and type)
	 */
	protected void returns(outputs){
		signature.outputs(outputs);
		useCustomSignature = true
	}
	
	/**
	 * Allows settings a pre-action closure from within the configuration block
	 * @param preAction The pre-action closure
	 */
	protected void before(Closure preAction){
		this.preActions.add(preAction)
	}
	
	/**
	 * Allows settings the work closure from within the configuration block
	 * @param work The work closure
	 */
	protected void task(Closure work){
		this.work = work
	}
	
	/**
	 * Allows settings a post-action closure from within the configuration block
	 * @param preAction The post-action closure
	 */
	protected void after(Closure preAction){
		this.postActions.add(preAction)
	}
	
	/**
	 * Resolves the path against the root directory of this execution
	 * @param path The path to resolve
	 * @return The resolved path
	 */
	String path(String path){
		return pathFactory.resolve(path)
	}
	
	/**
	 * Configures this operation
	 */
	public void init() {
		this.init(null);
	}

	/**
	 * Configures this operation by invoking a config closure in its scope
	 * @param configClosure The closure doing the configuration
	 */
	public void init(Closure config) {
		if(config != null) {
			config.delegate = this
			config.resolveStrategy = Closure.DELEGATE_FIRST
			config()
		}
		this.postConfig()
		
		if( ! this.useCustomSignature ){
			this.signature = this.provideSignature()
		}
	}
	
	/**
	 * This method is executed after the operation has been fully configured.
	 * Can be overridden to perform start-up actions in sub classes
	 */
	protected void postConfig() { }
		
	protected void prepareRun() { }
	
	/**
	 * This method can be used to specify a list of resources that should be locked
	 * before the operation is executed. The locks will be released after
	 * the operation has finished.
	 */
	protected List<Object> getResourcesToLock() { 
		return []
	}
	
	/**
	 * Retrieves an ordered list of locks for the shared resources in this operation.
	 * @return An ordered list of locks.
	 */
	protected List<Object> getLocks() {
		def resources = this.getResourcesToLock()
		if(resources == null || resources.size() == 0) {
			return []
		}
		return this.lockFactory.getLocks(resources)
	}
	
	/**
	 * Acquires all locks in the order they are specified.
	 * @param locks The list of locks to acquire.
	 */
	protected void acquireLocks(List<ReentrantLock> locks) {
		for(ReentrantLock lock : locks) {
			lock.lock()
		}
	}
	
	/**
	 * Releases all locks in the order they are specified.
	 * @param locks The list of locks to release.
	 */
	protected void releaseLocks(List<ReentrantLock> locks) {
		for(ReentrantLock lock : locks) {
			try {				
				lock.unlock()
			} catch(IllegalMonitorStateException ex) {
				// Ignore this exception, since we work on list of locks. Be sure
				// to unlock all locks before exiting from this method.
			}
		}
	}
	
	/**
	 * Runs the operation by executing all pre- and post-actions.
	 * Execution order:
	 * 1) pre-actions in order of their definition
	 * 2) work item (if defined) or (overridden) execute() method
	 * 3) post-actions in order of their definition
	 */
	public def run() {
		this.prepareRun()
		this.executePreActions()
		
		def locks = this.getLocks()
		try {
			this.acquireLocks(locks)
			
			// execute work, if defined
			if(work != null) {
				work.delegate = this
				work.resolveStrategy = Closure.DELEGATE_FIRST
				work()
			} else {
				this.execute()
			}
			
			this.executePostActions()
			return this.assembleOutput()
		} finally {
			this.releaseLocks(locks)
		}
	}
	
	/**
	 * Assembles the outputs in order of appearance in the specification 
	 * @return The list of outputs of this operation
	 */
	private def assembleOutput(){
		def values = []
		if( signature == null ){
			return values; 
		}
		signature.outputs.eachWithIndex { definition, idx ->
			def key = definition.name;
			if(outputs.containsKey(key)){
				def value = outputs.get(key)
				log.verbose "assembling output for $definition.name: $value"
				values.add(value)
			} else {
				log.warning "no output for $definition.name"
				values.add(null)
			}
		}
		log.verbose "Outputs are $values"
		if(values.size() == 1){
			return values[0] // directly return single result
		} else {
			return new OperationResult(values)
		}
	}
	
	/**
	 * Defines an output value by name and value
	 * @param name The name of the output value
	 * @param value The value
	 */
	protected void emit(String name, def value){
		this.outputs[name] = value
	}
	
	/**
	 * Sets the input values for this operation
	 * @param args The list of input values
	 */
	public void setInput(args){
		if(this.signature == null){
			return;
		}
		this.signature.inputs.eachWithIndex { definition, idx ->
			log.verbose "Adding input for $definition.name: ${args[idx]}"
			this.inputs[definition.name] = args[idx]
		}
	}
	
	/**
	 * Gets an input value by name
	 * @param name The name of the input value
	 * @return The value
	 */
	protected def getInput(String name){
		return this.inputs[name]
	}
	
	/**
	 * Executes all pre-actions in the order of their definition.
	 * Can be overridden in sub classes to change pre-action execution
	 */
	protected void executePreActions() {
		preActions.each() { action ->
			action.delegate = this
			action.resolveStrategy = Closure.DELEGATE_FIRST
			action()
		}
	}
	
	/**
	 * Executes all post-actions in the order of their definition.
	 * Can be overridden in sub classes to change post-action execution
	 */
	protected void executePostActions() {
		postActions.each() { action ->
			action.delegate = this
			action.resolveStrategy = Closure.DELEGATE_FIRST
			action() 
		}
	}
	
	/**
	 * Executes the main activity of this operation. Can be overridden in sub operations
	 * to specify specialized execution (e.g. call special libraries). Is not executed, if the work
	 * delegate is defined
	 */
	protected void execute() { }
	
}
