package org.moola.plugin.standard

import org.moola.dsl.core.Operation
import org.moola.dsl.core.Signature;

class ExecOperation extends Operation {

	protected String command;
	
	@Override
	protected Signature provideSignature() {
		return new Signature() // neither in- nor outputs
	}
	
	@Override
	protected void execute() {
		if(command == null || command == ""){
			throw new RuntimeException("No command specified for exec operation")
		}
		def path = this.resolvePath(".")
		this.log.verbose("Executing $command on path $path")
		
		def outWriter = new StringWriter()
		def errWriter = new StringWriter()
		def process = null
		
		process = command.execute([], new File(path))
		process.waitForProcessOutput( outWriter, errWriter )
		
		def exitStatus = process.exitValue()
		def output = outWriter.toString()
		def error = errWriter.toString()
		
		if(exitStatus != 0){
			this.log.error(error)
			throw new RuntimeException("Error during command execution")
		} else {
			this.log.verbose(output)
		}
	}
	
	public void command(String command){
		this.command = command
	}
	
}
