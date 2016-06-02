package org.moola.phases.exec

import java.util.List;

import org.moola.dsl.core.Operation
import org.moola.dsl.core.OperationType
import org.moola.logging.ILogger
import org.moola.logging.LoggerFactory

/**
 * Executes an operation on a given set of inputs
 * @author Stefan Weghofer
 */
class OperationExecutor {
	
	private Operation operation
	private List arguments = []
	private ILogger log;
	
	public OperationExecutor(Operation operation, def arguments, ILogger log){
		this.operation = operation
		this.arguments = arguments
		this.log = log
	}
	
	def execute(){
		try {			
			operation.setInput(arguments)
			return operation.run()
		} catch(Exception ex){
			throw new OperationExecutionException(operation, ex);
		}
	}

}
