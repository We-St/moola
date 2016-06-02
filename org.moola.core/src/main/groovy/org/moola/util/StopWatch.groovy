package org.moola.util

import org.apache.commons.lang3.time.StopWatch as ApacheStopWatch;

/**
 * A simple wrapper around the Apache Commons StopWatch
 * @author Stefan Weghofer
 */
class StopWatch {

	static StopWatch start(){
		return new StopWatch()
	}
	
	ApacheStopWatch stopWatch
	
	public StopWatch(){
		stopWatch = new ApacheStopWatch()
		stopWatch.start()
	}
	
	public long stop(){
		stopWatch.stop()
		return stopWatch.getTime()
	}
	
}
