package org.moola.dsl.core

/**
 * Groups the result of an operation if the operation returns more than one result value.
 * @author Stefan Weghofer
 */
class OperationResult {

	def list = []
	
	public OperationResult(def list){
		this.list = list
	}
	
	public def value(int index){
		return list[index]
	}
	
}

