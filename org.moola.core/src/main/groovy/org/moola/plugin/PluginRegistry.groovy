package org.moola.plugin

class PluginRegistry {

	/**
	 * Default plugins are registered to all Moola processes 
	 */
	static List<Plugin> defaultPlugins = []
	
	static Plugin get(String name){
		return defaultPlugins.find { p -> p.name == name }
	}
	
	static void add(Plugin plugin){
		defaultPlugins.add(plugin)
	}
	
}
