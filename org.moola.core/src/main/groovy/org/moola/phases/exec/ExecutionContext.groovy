package org.moola.phases.exec

import org.moola.dsl.core.LockFactory
import org.moola.dsl.core.Model
import org.moola.dsl.core.ModelFactory
import org.moola.dsl.core.PathFactory
import org.moola.logging.ILogger

class ExecutionContext {

	Closure work
	ILogger log
	PathFactory pathFactory
	LockFactory lockFactory
	
	public ExecutionContext(){ }
	
	void run() {
		work.delegate = this
		work.resolveStrategy = Closure.DELEGATE_ONLY // to prevent overlapping variable scopes. Alternatively use DELEGATE_FIRST
		work.run()
	}

	/**
	 * Prints the question to the console and awaits user input
	 * @param question The question
	 * @return The answer read from the console
	 */
	def ask(String question){
		def answer = readLine (question.trim() + " ")
		return answer
	}
	
	private String readLine(String format, Object... args) throws IOException {
		if (System.console() != null) {
			return System.console().readLine(format, args);
		}
		System.out.print(String.format(format, args));
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		return reader.readLine();
	}
	
	/**
	 * Loads a model by specifying name, type and path. This can be used when the models to load are
	 * not known in the configuration phase (e.g. when user input should be considered or for file iterations)
	 * @param args The map of arguments
	 * @param model A new model instance
	 */
	Model<?> load(args){
		if(args.path == null || args.type == null || args.name == null){
			throw new RuntimeException("Path, type and name is required to load a model")
		}
		def path = pathFactory.resolve(args.path)
		log.verbose "Loading ${args.name} from ${path} as ${args.type}"
		def model = ModelFactory.INSTANCE.create()
		model.name = args.name
		model.type = args.type
		model.path = path
		return model
	}
	
	/**
	 * Helper method in the run closure to save models
	 * @param model The model to save
	 */
	void save(Model<?> model){
		model.save()
	}
	
	/**
	 * Helper method in the run closure to save models
	 * @param model The model to save
	 * @param path The path to save the model to
	 */
	void save(Model<?> model, String path){
		model.path = pathFactory.resolve(path)
		save( model )
	}
	
	/**
	 * Helper method in the run closure to save models
	 * @param model The models to save
	 */
	void save(List<Model<?>> models){
		models.each { 
			save(it)
		}
	}
	
	/**
	 * Terminates the moola process with an error message
	 */
	void abort(String error){
		log.error error
		System.exit(1);
	}
	
	/**
	 * Runtime properties for helper variables
	 */
	def runtimeProperties = [:]
	
	def propertyMissing(String name) {
		//log.verbose "missing call for $name"
		if( runtimeProperties[ name ] != null ) {
			runtimeProperties[ name ]
		}
	}

	def propertyMissing(String name, def value) {
		runtimeProperties[ name ] = value
	}
	
	def parallel(Closure... execPaths){
		def exec = new ParallelExecutor(execPaths)
		return exec.run()
	}
	
	/**
	 * Waits until the model resolves
	 * @param model The model to wait on
	 */
	void await(model){
		if(model instanceof ParallelResult){
			((ParallelResult) model).await();
		}
	}
	
	/**
	 * Log helper. TODO: do we need them in ExecContext (and basically everywhere) as well? Maybe use mixins?
	 */
	
	void verbose(String msg){
		log.verbose(msg)
	}
	
	void info(String msg){
		log.info(msg)
	}
	
	void warn(String msg){
		log.warning(msg)
	}
	
	void error(String msg){
		log.error(msg)
	}
}
