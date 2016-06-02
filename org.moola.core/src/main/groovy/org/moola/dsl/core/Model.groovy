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

package org.moola.dsl.core


/**
 * Represents a model in the transformation chain. Models are loaded on demand during execution.
 * @author Stefan Weghofer
 * @param <T> Describes the payload of this model instance
 */
class Model<T> {
	
	protected String name = ""
	protected String path = ""
	protected T content;
	protected boolean loadOnDemand = true;
			
	
	public Model() { }
	
	
	public void load() {}
	public void save() {}
	
	
	public T getContent(){
		if(content == null){	
			if(loadOnDemand){
				this.load();
			}
		}
		return content
	}
	
	public void setContent(T content){
		this.content = content
	}
	
	public String getName(){
		return this.name
	}
	
	public void setName(String name){
		this.name = name
	}
	
	public String getPath(){
		return this.path
	}
	
	public void setPath(String path){
		this.path = path
	}
	
}
