package org.moola.emf.atl;

import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;

public class DefaultAtlExecStrategie implements AtlExecStrategie {

	protected AtlRunner atl;
	protected String path;
	protected Map<String, Object> atlOptions;
	protected Map<String, Object> atlLibraries;
	
	public DefaultAtlExecStrategie(String path, Map<String, Object> atlOptions, Map<String, Object> atlLibraries) {
		this.path = path;
		this.atlOptions = atlOptions;
		this.atlLibraries = atlLibraries;
	}
	
	@Override
	public AtlDetails parseDetails() {
		AtlParser parser = new AtlParser();
		return parser.extractDetails(this.path);
	}

	@Override
	public void init() {
		String compiledTrafo = this.path.replace(".atl", ".asm");
		this.atl = new AtlRunner();
		this.atl.transformation = compiledTrafo;
		this.atl.atlOptions = this.atlOptions;
		this.atl.atlLibraries = this.atlLibraries;
	}

	@Override
	public void addMetaModel(String name, Resource metaModel) {
		this.atl.metaModel(name, metaModel);
	}

	@Override
	public void addInputModel(String name, Resource model, String type) {
		this.atl.input(name, model, type);	
	}

	@Override
	public void addInoutModel(String name, Resource model, String type) {
		this.atl.inout(name, model, type);	
	}

	@Override
	public void addOutputModel(String name, String type) {
		this.atl.output(name, type);
	}

	@Override
	public Map<String, Resource> run(boolean refinement) {
		this.atl.refinement = refinement;
		return this.atl.execute();
	}

}
