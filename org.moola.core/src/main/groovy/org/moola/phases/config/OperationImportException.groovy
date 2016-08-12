package org.moola.phases.config

/**
 * Indicates an error during the import of an operation.
 * @author Stefan Weghofer
 */
class OperationImportException
	extends RuntimeException {

	public OperationImportException(String message){
		super(message)
	}	
}
