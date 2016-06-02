package org.moola.phases.exec

import java.util.List;

import groovy.lang.Closure;

class ParallelExecutor {

	private List<Closure> execPaths = []
	private ParallelResult result
	
	public ParallelExecutor(Closure... execPaths){
		this.execPaths.addAll(execPaths)
		this.result = new ParallelResult()
	}
	
	public def run(){
		execPaths.each { executePath(it) }
		return result
	}
	
	private void executePath(Closure path){
		MoolaThread thread = new MoolaThread(path, result)
		thread.start()
		result.add(thread)
	}
	
	
}
