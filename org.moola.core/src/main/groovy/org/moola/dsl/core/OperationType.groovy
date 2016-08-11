package org.moola.dsl.core

import java.util.List

import org.moola.Process;
import org.moola.dsl.core.impl.OperationRegistry;
import groovy.lang.Closure;

//TODO: rename to OperationDefinition
class OperationType {

	Process process
	String name
	String type
	Closure configClosure
		
	public OperationType(Process process){
		this.process = process
	}
	
	public Operation createInstance(){
		def operationClass = this.getOperationClass()
		def operation = operationClass.newInstance()
		operation.log = process.log
		operation.pathFactory = process.pathFactory
		operation.lockFactory = process.lockFactory
		operation.name = this.name;
		if(configClosure != null) {
			operation.init(this.configClosure)
		}
		return operation
	}
	
	private Class<Operation> getOperationClass(){
		if(type != null && type != ""){
			def typeKnown = process.operationRegistry.has(type)
			if(!typeKnown){
				throw new RuntimeException("Unknown operation type " + type)
			}
			def opClass = process.operationRegistry.resolve(type)
			return opClass
		} else {
			return Operation
		}
	}
	
}
