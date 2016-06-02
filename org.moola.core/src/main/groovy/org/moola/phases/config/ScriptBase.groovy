package org.moola.phases.config

import java.util.List
import org.moola.phases.exec.ExecutionContext
import org.moola.dsl.core.Model
import org.moola.dsl.core.Operation
import org.moola.dsl.core.OperationType
import org.moola.dsl.core.PathFactory
import org.moola.logging.ILogger
import org.moola.logging.LogLevel
import groovy.lang.Script
import org.moola.Process
import org.moola.ProcessSettings

/**
 * The base class for parsing a Moola script
 * @author Stefan Weghofer
 */
class ScriptBase extends Script {
	
	protected Process process
	protected ProcessSettings proj = null
	protected def models = [:]
	protected def operationTypes = [:]
	
	public ScriptBase() { }
	
	
	/**
	 * Initializes the ScriptBase instance with a Moola process
	 * @param process
	 */
	public void init(Process process){
		this.process = process
		this.proj = process.settings
	}
	
	
	/**
	 * Runs the script 
	 */
	public Object run() {
		return null;
	}
	

	/**
	 * Applies the plugin with the given name to the current Moola process
	 * @param pluginName The name of the plugin
	 */
	void apply(String pluginName){
		process.applyPlugin(pluginName)
	}
	
	
	/**
	 * Sets the model types
	 * @param values The model types as map
	 */
	void modeltypes(values){
		values.each { key, value ->
			def path = resolvePath(value)
			this.process.registerModelType(key, path)
		}
	}
	
	
	/**
	 * Creates a new model
	 * @param args The arguments of the model
	 * @param configClosure A configuration closure
	 */
	void model(args) {
		def name = args["name"]
		if(name == null || name == ""){
			throw new RuntimeException("Invalid name for model: $name") //ToDo: make sure it is a valid Java method name
		}
		if(models[name] || operationTypes[name]){
			throw new RuntimeException("Name $name already in use for other model or operation")
		}
		def path = args["path"]
		if(path == null || path == ""){
			throw new RuntimeException("Invalid path for model $name: $path")
		}
		def type = args["type"]
		if(type == null || !(type instanceof Class)){
			throw new RuntimeException("Invalid type for model $name: $type")
		}
		
		Model<?> model = type.newInstance() // ModelFactory.INSTANCE.create()
		model.name = name
		model.path = resolvePath(path)
		model.content = args["content"]
		models[name] = model
		process.registerModel(model)
	}


	/**
	 * Defines a new transformation
	 * @param args The arguments specifying name and type of transformation
	 * @param configClosure An optional closure used for configuration of the new trafo
	 * @return An object representing the transformation
	 */
	def operation(args, @DelegatesTo(Operation) configClosure) {
		def opType = new OperationType(this.process)
		opType.name = args.name
		opType.type = args.type
		opType.configClosure = configClosure
		operationTypes[args.name] = opType
		process.registerOperationType(opType)
	}
	
	def operation(args) {
		this.operation(args, null)
	}
		
	
	/**
	 * Sets the closure that defines the orchestration
	 */
	void run(@DelegatesTo(ExecutionContext) closure) {
		process.setOrchestration(closure)
	}
	
	void run(String message) {
		process.setOrchestration({})
	}
	
	void merge(@DelegatesTo(ExecutionContext) closure) {
		process.setOrchestration(closure)
	}
	
		
	/**
	 * Sets the log level for the project and the default log level for all subsequent logger instances
	 * @param level The new log level
	 */
	void loglevel(String level) {
		LogLevel lvl = LogLevel.parse(level)
		process.loggerFactory.setLogLevel(lvl)
		process.log.setLogLevel(lvl)
	}
		

	/**
	 * Resolves the path against the root directory of this execution
	 * @param path The path to resolve
	 * @return The resolved path
	 */
	String resolvePath(String path){
		return process.pathFactory.resolve(path)
	}
	
}
