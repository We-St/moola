package org.moola.plugin

import org.moola.dsl.core.Operation
import org.moola.Process

abstract class Plugin {
	
	abstract String getName()
	abstract String getVersion()
	abstract void applyTo(Process process)
	
	@Override
	public String toString(){
		return "${getName()} (${getVersion()})"
	}
	
}
