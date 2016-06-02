package org.moola.phases.config

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import org.moola.Process

class TypeCheckSpace {

	private static Lock lock
	private static Process process
	
	static {
		lock = new ReentrantLock()
	} 
	
	public static Process getProcess(){
		return process
	}
	
	public static void setProcess(Process proc){
		process = proc
	}
	
}
