package org.moola.logging;

import java.io.OutputStream;
import java.io.PrintStream;

import org.moola.logging.impl.StreamLogger;

/**
 * This factory can be used to create logger instances to log in trafos, the project, etc.
 * @author Stefan Weghofer
 */
public class LoggerFactory {

	private PrintStream outStream;
	private LogLevel defaultLogLevel;
	
	public LoggerFactory(PrintStream outStream) {
		if(outStream == null){
			throw new RuntimeException("Missing argument outStream");
		} 
		this.defaultLogLevel = LogLevel.Info;
		this.outStream = outStream;
	}
	
	public LoggerFactory() {
		this(System.out);
	}
	
	/**
	 * Configures this logger factory
	 * @param targetStream The output stream used for logging
	 */
	public void configure(OutputStream targetStream){
		outStream = new PrintStream(targetStream);
	}
	
	/**
	 * Returns the print stream used for this factory.
	 */
	public PrintStream getPrintStream(){
		return outStream;
	}
	
	/**
	 * Sets the default log level for all new loggers
	 * @param level The new default log level
	 */
	public void setLogLevel(LogLevel level){
		defaultLogLevel = level;
	}
	
	/**
	 * Gets the default log level for this factory
	 * @return The default log level
	 */
	public LogLevel getLogLevel(){
		return defaultLogLevel;
	}
	
	/**
	 * Creates a new logger. Multiple loggers can be used in one Moola instance
	 * @return A new logger
	 */
	public ILogger create(){
		return new StreamLogger(outStream, defaultLogLevel);
	}
	
}
