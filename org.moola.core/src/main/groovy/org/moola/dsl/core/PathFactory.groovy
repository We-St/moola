package org.moola.dsl.core

/**
 * Helps resolving paths
 * @author Stefan Weghofer
 */
interface PathFactory {

	/**
	 * Resolves the resolve path against the base path. If the resolve path is absolute, it is simple returned.
	 * @param basePath The base path. If it is absolute, the result will definitely be absolute as well.
	 * @param resolvePath The path to resolve. If absolute, it is returned directly.
	 * @return The resolved path
	 */
	String resolve(String basePath, String resolvePath);
	
	
	/**
	 * Same as resolve(), but called against the default base path. The default path can be set via setDefaultBasePath().
	 * @param resolvePath The path to resolve. If absolute, it is returned directly.
	 * @return The resolved path
	 */
	String resolve(String resolvePath);
	
	
	/**
	 * Sets the default base path
	 * @param defaultBasePath The default base path
	 */
	void setDefaultBasePath(String defaultBasePath);
	
	
	/**
	 * Gets the default base path
	 * @return The default base path
	 */
	String getDefaultBasePath();
	
}
