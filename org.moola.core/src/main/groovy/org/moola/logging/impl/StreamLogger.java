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

package org.moola.logging.impl;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.moola.logging.ILogger;
import org.moola.logging.LogLevel;

public class StreamLogger
	implements ILogger {
	
	protected LogLevel logLevel;
	protected PrintStream writer; // PrintStream is synchronized internally

	public StreamLogger(PrintStream stream, LogLevel logLevel){
		this.logLevel = logLevel;
		this.writer = stream;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	public LogLevel getLogLevel() {
		return this.logLevel;
	}
	
	public PrintStream getStream(){
		return this.writer;
	}
	
	public void log(LogLevel level, String message) {
		try {
			if(!level.isEqualOrWorseThan(this.logLevel)){
				return;
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			
			String logItem = "";
			logItem += dateFormat.format(new Date());
			logItem += "\t" + String.format("%03d", Thread.currentThread().getId());
			logItem += "\t" + level.toString();
			logItem += "\t" + message;

			this.writer.println(logItem);
			this.writer.flush();
		} catch(Exception ex){
			System.out.println("Logger not available");
			ex.printStackTrace();
		}
	}
	
	public synchronized void log(LogLevel level, Exception ex) {
		try {
			if(!level.isEqualOrWorseThan(this.logLevel)){
				return;
			}
			
			ex.printStackTrace(this.writer);
		} catch(Exception e){
			System.out.println("Logger not available");
		}
	}

	@Override
	public void verbose(String message) {
		this.log(LogLevel.Verbose, message);
	}

	@Override
	public void info(String message) {
		this.log(LogLevel.Info, message);
	}

	@Override
	public void warning(String message) {
		this.log(LogLevel.Warning, message);
	}

	@Override
	public void error(String message) {
		this.log(LogLevel.Error, message);
	}

	@Override
	public void error(Exception ex) {
		this.log(LogLevel.Error, ex);
	}

	@Override
	public void error(String message, Exception ex) {
		this.log(LogLevel.Error, message);
		this.log(LogLevel.Error, ex);
	}
	
}
