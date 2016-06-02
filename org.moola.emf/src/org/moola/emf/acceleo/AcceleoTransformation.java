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
package org.moola.emf.acceleo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.acceleo.common.internal.utils.workspace.AcceleoWorkspaceUtil;
import org.eclipse.acceleo.engine.service.AbstractAcceleoGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.moola.emf.EmfModel;
import org.moola.emf.EmfModelFactory;

import org.moola.dsl.core.Model;
import org.moola.dsl.core.ModelFactory;
import org.moola.dsl.core.Operation;
import org.moola.dsl.core.Signature;


public class AcceleoTransformation extends Operation {
	
	protected Class<AbstractAcceleoGenerator> generatorClass;
	protected AbstractAcceleoGenerator generator;
	
	public String project;
	public String jar;
	public String main;
	public String target;
	public Object model;
	public List<Object> arguments;
	public List<String> propertyFiles;
	
	public AcceleoTransformation() {
		this.arguments = new ArrayList<>();
		this.propertyFiles = new ArrayList<>();
	}
	
	@Override
	protected Signature provideSignature() {
		Signature spec = new Signature();
		spec.addInput("Object", "model"); // ToDo: change to String.class here ?
		spec.addOutput("String", "fullPath");
		return spec;
	}

	
	@Override
	protected void prepareRun() {
		// resolve target
		this.target = this.path(this.target).replace("file:/", "");
		log.info("Target for " + this.name + " is " + this.target);
		
		if(this.model == null){
			this.model = this.getInput("model");
		}

		// register Acceleo project
		if(project != null && project != ""){
			this.generatorClass = loadFromProject();
		} else if(jar != null && jar != ""){
			this.generatorClass = loadFromJar();
		} else {
			throw new RuntimeException("Please supply the 'jar' or 'project' parameter for Acceleo operations");
		}
		
		try {
			// create generator and initialize it
			this.generator = this.generatorClass.newInstance();
			this.configureGenerator();
		} catch (Exception e) {
			throw new RuntimeException("Could not initialize Acceleo generator", e);
		}
	}

	private static Set<String> registeredProjects = new HashSet<>();
	
	@SuppressWarnings({ "restriction", "unchecked" })
	protected Class<AbstractAcceleoGenerator> loadFromProject(){
		if(!registeredProjects.contains(this.project)){
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(this.project);
			//AcceleoWorkspaceUtil.INSTANCE.addWorkspaceContribution(project);
			registeredProjects.add(this.project);
		}
		return null; //(Class<AbstractAcceleoGenerator>) AcceleoWorkspaceUtil.INSTANCE.getClass(this.main, false);
	}

	@SuppressWarnings("unchecked")
	protected Class<AbstractAcceleoGenerator> loadFromJar(){
		String path = this.path(jar);
		try {
			System.out.println("**** loaded Acceleo from JAR file");
			
			URL[] urls = { new URL("jar:" + path + "!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls, this.getClass().getClassLoader());
			return (Class<AbstractAcceleoGenerator>) cl.loadClass(this.main);
		} catch(Exception ex){
			throw new RuntimeException("Cannot load " + main + " from " + jar, ex);
		}
	}

	protected void configureGenerator() throws IOException {
		File outputPath;
		outputPath = new File(this.target);
		
		// configure input model
		if(this.model instanceof EmfModel) {
			EmfModel model = (EmfModel) this.model;
			EObject eObject = model.getContent().getContents().get(0);
			this.generator.initialize(eObject, outputPath, this.arguments);
		} else if(this.model instanceof String){
			String modelUri = this.path(this.model.toString());
			this.generator.initialize(URI.createURI(modelUri), outputPath, arguments);
		} else {
			throw new RuntimeException("No model specified for Acceleo transformation");
		}

		// Set property files.
		for(String filePath : this.propertyFiles) {
			this.generator.addPropertiesFile(filePath);
		}
	}
	
	@Override
	public void execute() {
		try {
			this.generator.doGenerate(new BasicMonitor());
			
			Model<String> result = new Model<String>();
			result.setContent(this.target);
			this.emit("fullPath", result);
		} catch (IOException e) {
			this.log.error(e);
			throw new RuntimeException("Acceleo transformation failed", e);
		}
	}
	
}
