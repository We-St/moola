/*******************************************************************************
 * Copyright (coffee) 2014 Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Stefan Weghofer (Vienna University of Technology) - DSL and petri nets implementation
 * Alexander Bergmayr (Vienna University of Technology) - initial API and implementation
 *
 * Initially developed in the context of ARTIST EU project www.artist-project.eu
 *******************************************************************************/

package org.moola

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.moola.logging.ILogger
import org.moola.logging.LogLevel
import org.moola.logging.LoggerFactory
import org.moola.phases.config.ConfigurationPhase;
import org.moola.phases.config.ConfigurationPhaseException;
import org.moola.phases.exec.ExecutionPhase;
import org.moola.phases.exec.OperationExecutionException;
import org.moola.phases.exec.ParallelExecutionException;
import org.moola.plugin.Plugin
import org.moola.dsl.core.Model
import org.moola.dsl.core.ModelFactory;
import org.moola.dsl.core.Operation;
import org.moola.dsl.core.OperationType;
import org.moola.dsl.core.PathFactory
import org.moola.dsl.core.impl.OperationRegistry;
import org.moola.plugin.PluginRegistry
import org.moola.plugin.standard.StandardPlugin;
import org.moola.util.FileHelper;
import org.moola.util.StopWatch;;

class Process {
	
	public final static String DEFAULT_PROJECT_NAME = "Moola Process"
	public final static String JAR_EXTENSION = ".jar"
	public final static String DEFAULT_LIB_PATH = "mlibs"
	
	static {
		PluginRegistry.add(new StandardPlugin()) //TODO: move?
	}
	
	
	OperationRegistry operationRegistry
	ModelFactory modelFactory
	PathFactory pathFactory
	LoggerFactory loggerFactory;
	
	protected ILogger log
	protected ProcessSettings settings
	protected String scriptContent
	
	protected List<Model> models = []
	protected List<OperationType> operationTypes = []
	protected Closure orchestration
	
	
	/**
	 * Creates a new process with an empty script and empty settings
	 */
	public Process(){
		this("", new ProcessSettings())
	}
	
	/**
	 * Creates a new process with script and settings
	 * @param scriptContent The script content
	 * @param settings The settings
	 */
	public Process(String scriptContent, ProcessSettings settings){
		this.scriptContent = scriptContent
		this.settings = settings
	}
	
	
	/**
	 * Sets the logger factory of the Moola process and updates the logger
	 * @param loggerFactory The logger factory
	 */
	public void setLoggerFactory(LoggerFactory loggerFactory){
		this.loggerFactory = loggerFactory;
		this.log = loggerFactory.create();	
	}
	
	
	/**
	 * Applies a plugin to the current process
	 * @param name The name of the plugin to apply
	 */
	void applyPlugin(String name){
		Plugin plugin = PluginRegistry.get(name)
		applyPlugin(plugin)
	}
	
	/**
	 * Applies a plugin to the current process
	 * @param plugin The plugin to apply
	 */
	void applyPlugin(Plugin plugin){
		log.verbose("Applying plugin $plugin to current process")
		plugin.applyTo(this)
	}
	
	
	/**
	 * Executes the current process. This includes running the configuration and execution phase
	 */
	public void run() {
		try {
			StopWatch stopWatch = StopWatch.start()

			if(settings.logToSystemOut){
				System.setOut(loggerFactory.getPrintStream())
			}
			log.info("Starting Moola process")
			
			configureProcess()
			executeProcess()
			
			log.info("Finished script in " + (stopWatch.stop() / 1000.0) + "s")
		} catch(ConfigurationPhaseException ex) {
			log.error(ex.getMessage())
			throw ex
		} catch(OperationExecutionException ex) {
			log.error("An error occured during the execution of " + ex.getOperation().getName())
			log.error(ex.getInner())
			throw ex
		} catch(ParallelExecutionException ex) {
			log.error("Exceptions occured during parallel execution")
			for(Exception inner : ex.getExceptions()){
				log.error(inner)
			}
			throw ex
		} catch(Exception ex) {
			log.error("Unexpected error in Moola process", ex)
			throw ex
		}
	}

	private void configureProcess() {
		ConfigurationPhase configPhase = new ConfigurationPhase(this);
		configPhase.execute();
	}
	
	private void executeProcess() {
		ExecutionPhase execPhase = new ExecutionPhase(this);
		execPhase.execute();
	}
	
	
	/**
	 * Registers a model type for the current process
	 * @param name The name of the type
	 * @param path The details describing the type (e.g. path to metamodel)
	 */
	void registerModelType(String name, String path){
		this.modelFactory.registerType(name, path)
	}
	
	
	/**
	 * Registers a model for the current process
	 * @param model The model to register
	 */
	void registerModel(Model model){
		this.models.add(model)
	}
	
	
	/**
	 * Registers an operation type for the current process
	 * @param opType The operation type to register
	 */
	void registerOperationType(OperationType opType){
		this.operationTypes.add(opType)
	}
	
	
	/**
	 * Sets the orchestration code for the process
	 * @param orchestration The closure describing the orchestration
	 */
	void setOrchestration(Closure orchestration){
		this.orchestration = orchestration
	}
}
