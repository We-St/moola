package org.moola.phases.exec

class MoolaThread 
	extends Thread {
	
	Closure action
	Object delegate
	Exception executionException	
	
	public MoolaThread(Closure action, Object delegate){
		this.action = action
		this.delegate = delegate
	}
		
	public void run() {
		try {
			action.delegate = delegate // TODO: change to isolated objects so threads cannot influence each other
			action.resolveStrategy = Closure.DELEGATE_FIRST
			action()
		} catch(Exception ex){
			executionException = ex
		}
	}
	
	public boolean hasErrors(){
		return executionException != null
	}
	
}
