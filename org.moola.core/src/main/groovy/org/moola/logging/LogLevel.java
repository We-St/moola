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

package org.moola.logging;

public enum LogLevel {
	
	Verbose(0),
	Info(1),
	Warning(2),
	Error(3),
	Fatal(4);

	private Integer severity;

	LogLevel(int severity) {
		this.severity = severity;
	}

	public boolean isEqualOrWorseThan(LogLevel other) {
		return this.severity >= other.severity;
	}
	
	public static LogLevel parse(String level){
		String desiredLogLvl = level.toLowerCase();
		for(LogLevel lvl : LogLevel.values()) {
			String lvlName = lvl.name().toLowerCase();
			if(lvlName.equals(desiredLogLvl)){
				return lvl;
			}
		}
		throw new RuntimeException("Invalid loglevel: " + level);
	}
	
}
