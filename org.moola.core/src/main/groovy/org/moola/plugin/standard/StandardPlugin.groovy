package org.moola.plugin.standard

import org.moola.Process
import org.moola.plugin.Plugin

class StandardPlugin extends Plugin {
	
	String getName(){
		return "Moola Standard Plugin"
	}
	
	String getVersion(){
		return "0.0.1"	
	}
	
	void applyTo(Process process){
		process.operationRegistry.register("Exec", ExecOperation)
	}
	
}
