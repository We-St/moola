package org.moola.phases.exec

import org.moola.dsl.core.Operation;

class ParallelExecutionException
	extends RuntimeException {
		
	List<Exception> exceptions
			
	public ParallelExecutionException(List<Exception> exceptions){
		super()
		this.exceptions = exceptions
	}
	
}
