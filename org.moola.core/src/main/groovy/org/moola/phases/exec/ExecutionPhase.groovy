package org.moola.phases.exec

import org.moola.Process;
import org.moola.logging.ILogger
import org.moola.logging.LoggerFactory

class ExecutionPhase {
	
	private Process process
	
	public ExecutionPhase(Process process){
		this.process = process
	} 

	public void execute() {
		ExecutionContext context = ExecutionContextFactory.create(this.process)
		context.run()
	}
		
}
