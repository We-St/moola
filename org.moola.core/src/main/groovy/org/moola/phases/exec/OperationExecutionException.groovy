package org.moola.phases.exec

import org.moola.dsl.core.Operation

/**
 * This exception is thrown whenever an operation cannot be executed
 */
class OperationExecutionException 
	extends RuntimeException {
	
	Operation operation
	Exception inner
		
	public OperationExecutionException(Operation operation, Exception inner){
		super(inner)
		this.operation = operation
		this.inner = inner
	}
}
