package org.moola.dsl.core;

import java.util.Map;

/**
 * Used to load and resolve models for a given Moola execution
 * @author Stefan Weghofer
 */
public abstract class ModelFactory {
	
	public static ModelFactory INSTANCE;
	public static Class modelClass = Model

	protected Map<String, String> modelTypes;
	
	public ModelFactory(){
		this.modelTypes = new HashMap<String, String>()
	}
	
	public void registerType(String name, String path) {
		if(modelTypes.containsKey(name)){
			throw new RuntimeException("Model type $name already registered with $path")
		}
		modelTypes.put(name, path)
	}
	
	public String getType(String name){
		if(! modelTypes.containsKey(name)){
			throw new RuntimeException("Unkown model type $name")
		}
		return modelTypes.get(name)
	}
	
	public Map<String, String> getTypes(){
		return modelTypes;	
	}
	
	abstract Model<?> create();
	
}
