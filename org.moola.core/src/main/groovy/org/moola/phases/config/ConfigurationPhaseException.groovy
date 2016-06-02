package org.moola.phases.config

/**
 * Indicates an error in the configuration phase - especially compilation and static type checking errors. *
 */
class ConfigurationPhaseException
	extends RuntimeException {

	public ConfigurationPhaseException(String message){
		super(message)
	}	
	
}
