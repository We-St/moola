package org.moola.phases.exec

import java.util.List;

/**
 * The awaitable result of a call to the parallel keyword
 * @author Stefan Weghofer
 */
class ParallelResult {

	public List<MoolaThread> threads = []
	
	public ParallelResult() {}
	
	public void add(MoolaThread th){
		threads.add(th)
	}

	private void await(){
		threads.each { t -> t.join() }
		
		def exceptions = getThreadExceptions()
		if(exceptions.size() == 1){
			throw exceptions.get(0)
		}
		if(exceptions.size() > 0){
			throw new ParallelExecutionException(exceptions)
		}
	}
	
	private List<Exception> getThreadExceptions(){
		return threads.findAll({ t -> t.hasErrors() }).collect({ t -> t.executionException })
	}
	
}
