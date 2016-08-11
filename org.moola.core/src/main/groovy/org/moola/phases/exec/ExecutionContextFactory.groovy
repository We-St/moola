package org.moola.phases.exec

import org.moola.Process;
import org.moola.dsl.core.Model
import org.moola.dsl.core.OperationType
import org.moola.logging.ILogger

class ExecutionContextFactory {

	static ExecutionContext create(Process process) {
		ILogger log = process.loggerFactory.create()
		
		ExecutionContext context = new ExecutionContext()
		context.work = process.orchestration
		context.pathFactory = process.pathFactory
		context.lockFactory = process.lockFactory
		context.log = process.loggerFactory.create()
				
		// register models
		def models = [:]
		process.models.each { model ->
			def propName = model.name.capitalize()
			models[propName] = model
			context.metaClass."get$propName" = {
				return models[propName]
			}
			context.metaClass."set$propName" = { newModel ->				
				models[propName] = newModel
			}
		}
		
		// register trafos
		process.operationTypes.each { OperationType opType ->
			def operationName = opType.name  		
			context.metaClass."$operationName" = { ...args ->
				def operation = opType.createInstance()
				def exec = new OperationExecutor(operation, args, log)
				return exec.execute()
			}
			
		}
		
		return context;
	}
	
}
