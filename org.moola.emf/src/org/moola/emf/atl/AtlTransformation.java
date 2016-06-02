package org.moola.emf.atl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class AtlTransformation extends AbstractAtlTransformation {

	protected AtlRunner atl;
	protected Map<String, Object> atlOptions;
	protected Map<String, Object> atlLibraries;
	
	public AtlTransformation() { 
		this.atlOptions = new HashMap<>();
		this.atlLibraries = new HashMap<>();
	}

	@Override
	protected AtlExecStrategie getStrategie() {
		this.src = this.path(this.src).replace("file:/", ""); // TODO: remove hack
		log.verbose("Full path for ATL file of " + this.name + " is: " + this.src);
		return new DefaultAtlExecStrategie(this.src, atlOptions, atlLibraries);
	}
	
	public void option(String option, Object value){
		atlOptions.put(option, value);
	}
	
	public void library(String name, Object library){
		if(library instanceof String){ // assume path
			String path = library.toString();
			path = this.path(path).replace("file:/", "");
			try {
				log.verbose("Adding library with from " + path);
				InputStream lib = new FileInputStream(path);
				atlLibraries.put(name, lib);
			} catch (FileNotFoundException e) {
				log.error("Cannot add library", e);
			}
		} else {
			atlLibraries.put(name, library);
		}
	}
	
}
