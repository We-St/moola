package org.moola.emf.atl;

import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;

public interface AtlExecStrategie {

	AtlDetails parseDetails();
	
	void init();
	void addMetaModel(String name, Resource metaModel);
	void addInputModel(String name, Resource model, String type);
	void addInoutModel(String name, Resource model, String type);
	void addOutputModel(String name, String type);

	Map<String, Resource> run(boolean refinement);
	
}
