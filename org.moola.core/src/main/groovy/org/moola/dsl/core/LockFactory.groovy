package org.moola.dsl.core

import java.util.concurrent.locks.ReentrantLock

/**
 * This class can be used to create a total order over shared resources for deadlock prevention.
 * 
 * @author Stefan Weghofer
 */
class LockFactory {

	private List<ReentrantLock> lockOrder
	private Map<Object, ReentrantLock> locks
	
	public LockFactory() {
		this.locks = new HashMap<>()
		this.lockOrder = new ArrayList<>()
	}
	
	/**
	 * Retrieves a list of locks for the provided input resources. The locks need to be requested 
	 * in the order returned from this method.
	 * @param inputs The list of input resources.
	 * @return A sorted list of locks.
	 */
	public synchronized List<ReentrantLock> getLocks(List<Object> inputs) {
		def inputLocks = []
		
		for(Object input : inputs) {
			if(!this.locks.containsKey(input)) {
				def lock = new ReentrantLock()
				this.locks.put(input, lock)
				this.lockOrder.add(lock)
			}
			inputLocks.add(this.locks.get(input))
		}
		
		return inputLocks.sort { this.lockOrder.indexOf(it) }
	}
}
