package org.moola

import java.net.MalformedURLException;
import java.net.URL;

import org.moola.dsl.core.LockFactory
import org.moola.dsl.core.impl.DefaultModelFactory;
import org.moola.dsl.core.impl.DefaultPathFactory;
import org.moola.dsl.core.impl.OperationRegistry;
import org.moola.logging.LoggerFactory
import org.moola.plugin.Plugin;
import org.moola.plugin.PluginRegistry;
import org.moola.util.FileHelper;

/**
 * This factory helps in creating and correctly configuring Moola processes
 * @author Stefan Weghofer
 */
class ProcessFactory {
	
	/**
	 * Creates a new process by specifying the file path to the Moola file
	 * @param file The Moola file
	 * @return A new Moola process
	 */
	public static Process create(File file){
		String scriptContent = FileHelper.readContents(file)
		return create(file.getName(), file, scriptContent, Process.DEFAULT_LIB_PATH, System.out)
	}
	
	/**
	 * Creates a new process by specifying the Moola script as String
	 * @param content The script content
	 * @return A new Moola process
	 */
	public static Process create(String content){
		return create(Process.DEFAULT_PROJECT_NAME, new File("."), content, Process.DEFAULT_LIB_PATH, System.out)
	}
	
	/**
	 * 
	 * @param projectName
	 * @param file
	 * @param scriptContent
	 * @param libraryPath
	 * @param outStream
	 * @return
	 */
	public static Process create(String projectName, File file, String scriptContent, String libraryPath, PrintStream outStream){
		String projectPath = FileHelper.getFileLocation(file)
		URL[] libraries = getLibraries(libraryPath)
		
		ProcessSettings settings = new ProcessSettings()
		settings.setPath(projectPath) 
		settings.setTitle(projectName)
		settings.setMoolaFile(file.getAbsolutePath())
		settings.setLibraries(libraries)
		
		Process process = new Process(scriptContent, settings)
		process.setPathFactory(new DefaultPathFactory(file.toURI().toString()))
		process.setOperationRegistry(new OperationRegistry())
		process.setModelFactory(new DefaultModelFactory())
		process.setLoggerFactory(new LoggerFactory(outStream))
		process.setLockFactory(new LockFactory())
		
		for(Plugin plugin : PluginRegistry.getDefaultPlugins()){
			process.applyPlugin(plugin)
		}
		return process;
	}

	private static URL[] getLibraries(String libraryPath) {
		try {
			return FileHelper.getFileUrls(libraryPath, Process.JAR_EXTENSION);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error while including libraries", e);
		}
	}
	
}
