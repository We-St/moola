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

package org.moola;

import java.io.File;

/**
 * This class can be used to launch a Moola process from the command line
 * @author Stefan Weghofer
 */
public class Launcher {

	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				System.out.println("Please specify a path to the Moola file");
			}

			String path = args[0];
			Launcher launcher = new Launcher(new File(path));
			launcher.run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private File file;
	
	public Launcher(File file) {
		this.file = file;
	}
	
	public Launcher(String filePath) {
		this.file = new File(filePath);
	}
	
	public void run(){
		Process process = ProcessFactory.create(file);
		process.run();
	}
	
}
