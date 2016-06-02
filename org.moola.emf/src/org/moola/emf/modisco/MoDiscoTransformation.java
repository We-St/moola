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
package org.moola.emf.modisco;


import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.eclipse.modisco.java.discoverer.DiscoverJavaModelFromJavaProject;
import org.moola.emf.EmfModel;
import org.moola.emf.EmfModelFactory;

import org.moola.dsl.core.ModelFactory;
import org.moola.dsl.core.Operation;
import org.moola.dsl.core.Signature;

public class MoDiscoTransformation extends Operation {
	
	private final static String RESULT_NAME = "result";
	
	protected DiscoverJavaModelFromJavaProject javaDiscoverer;
	
	protected EmfModelFactory modelFactory;
	protected Resource result;
	protected IJavaProject targetProject;
	
	public String project = "";
	public boolean deepAnalysis = false;
	public boolean serialize = false;
	
	public MoDiscoTransformation() { 
		this.modelFactory =  (EmfModelFactory) ModelFactory.INSTANCE;
	}
	
	@Override
	public void postConfig() {
		try {
			this.javaDiscoverer = this.createDiscoverer();
		} catch(Exception ex) {
			throw new RuntimeException("Could not initialize MoDisco Java discoverer", ex);
		}
	}

	@Override
	protected Signature provideSignature() {
		Signature spec = new Signature();
		spec.addInput("String", "project");
		spec.addOutput("Object", RESULT_NAME);
		return spec;
	}
	
	@Override
	protected void execute() {
		try {
			System.out.println("** Starting MoDisco: " + this.javaDiscoverer);

			Object inProject = this.getInput("project");
			if(inProject != null && inProject.toString() != ""){
				this.project = inProject.toString();
			}
			
			this.result = new XMIResourceImpl();
			this.result.setURI(URI.createURI("file:/TEMP/URI"));
			this.modelFactory.getResourceSet().getResources().add(this.result);

			this.targetProject = this.createProject();
			this.javaDiscoverer.setTargetModel(this.result); // set the resource so we have it in our ResourceSet
			this.javaDiscoverer.discoverElement(this.targetProject, new NullProgressMonitor());
						
			EmfModel model = (EmfModel) this.modelFactory.create();
			model.setContent(result);
			this.emit(RESULT_NAME, model);
		} catch(CoreException ex) {
			throw new RuntimeException("Could not initialize Java project", ex);
		} catch (DiscoveryException ex) {
			throw new RuntimeException("Could not discover project", ex);
		}
	}
	
	protected DiscoverJavaModelFromJavaProject createDiscoverer() {
		DiscoverJavaModelFromJavaProject discoverer = new DiscoverJavaModelFromJavaProject();
		discoverer.setDeepAnalysis(deepAnalysis);
		discoverer.setSerializeTarget(serialize);
		return discoverer;
	}
	
	protected IJavaProject createProject() throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IWorkspace workspace = root.getWorkspace();

		JavaProjectFactory.init(workspace);
		IJavaProject javaProject = JavaProjectFactory.getJavaProject(this.project);
		javaProject.open(new NullProgressMonitor());
		return javaProject;
	}

}
