package org.moola.emf.atl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IExtractor;
import org.eclipse.m2m.atl.core.IInjector;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.ModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFExtractor;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;

public class AtlRunner {
	
	protected Map<IModel, String> outModels;
	protected Map<String, IReferenceModel> metaModels = new HashMap<>();
	protected ILauncher atlLauncher;
	protected ModelFactory modelFactory;
	protected IInjector injector;
	protected IExtractor extractor;

	public boolean refinement = false;
	public String transformation = "";
	public Map<String, Object> atlOptions = new HashMap<>();
	public Map<String, Object> atlLibraries = new HashMap<>();

	public AtlRunner() {
		atlLauncher = new EMFVMLauncher();
		modelFactory = new EMFModelFactory();
		injector = new EMFInjector();
		extractor = new EMFExtractor();
		metaModels = new HashMap<>();
		outModels = new HashMap<>();
		
		// init launcher with empty settings. Can later be specified during actual launch
		atlLauncher.initialize(new HashMap<String, Object>());
	}
		

	// ***********************************************************************
	// * MetaModel registration
	// ***********************************************************************

	public void metaModel(String name, Resource resource) {
		IReferenceModel metamodel;
		try {
			metamodel = modelFactory.newReferenceModel();
			
			// registers the Resource ALSO in the ModelFactory (or the RS of it) 
			// ^- we do not need this since all Resources are loaded by some RS apart from the one in the ModelFactory 
			//    but we need the referenceModel
			populateModel(metamodel, resource);

			metaModels.put(name, metamodel);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create metamodel for " + name, e);
		}
	}
	
	
	// ***********************************************************************
	// * Model registration
	// ***********************************************************************

	public void input(String name, Resource resource, String conformsTo) {	
		IModel model = this.createModel(conformsTo);
		populateModel(model, resource);
		atlLauncher.addInModel(model, name, conformsTo);
	}

	//ToDo: add outputName? What happens if refinement with multiple input but only one output? Match by type?
	public void inout(String name, Resource resource, String conformsTo) {
		IModel model = this.createModel(conformsTo);
		populateModel(model, resource);
		atlLauncher.addInOutModel(model, name, conformsTo);
		outModels.put(model, name);
	}	
	
	public void output(String name, String conformsTo) {
		IModel model = this.createModel(conformsTo);
		atlLauncher.addOutModel(model, name, conformsTo);
		outModels.put(model, name);
	}	
	
	protected IModel createModel(String conformsTo) {
		try {
			if(! metaModels.containsKey(conformsTo)) {
				throw new RuntimeException("Metamodel not loaded: " + conformsTo);
			}
			IReferenceModel metamodel = metaModels.get(conformsTo);
			IModel m = modelFactory.newModel(metamodel);
			return m;
		} catch (ATLCoreException e) {
			throw new RuntimeException("Cannot create model for metamodel" + conformsTo, e);
		}
	}
	
	protected void populateModel(IModel model, Resource resource) {
		((EMFInjector) injector).inject(model, resource);
	}
	
	
	// ***********************************************************************
	// * Execution
	// ***********************************************************************

	protected void addRefinementModel() throws ATLCoreException {
		IReferenceModel refiningTraceMetamodel;
		IModel refiningTraceModel;

		refiningTraceMetamodel = modelFactory.getBuiltInResource("RefiningTrace.ecore");
		refiningTraceModel = modelFactory.newModel(refiningTraceMetamodel);
		atlLauncher.addOutModel(refiningTraceModel, "refiningTrace", "RefiningTrace");
	}
	
	public Map<String, Resource> execute() {
		try {
			if(refinement) {
				addRefinementModel();
			}
			
			registerLibraries();
			launchATL();
			return extractOutModels();
		} catch (Exception e) {
			throw new RuntimeException("Error during ATL transformation", e);
		}
	}
	
	protected void registerLibraries(){
		for(String name : atlLibraries.keySet()){
			Object library = atlLibraries.get(name);
			atlLauncher.addLibrary(name, library);
		}
	}
	
	protected void launchATL() throws FileNotFoundException {
		IProgressMonitor monitor = new NullProgressMonitor();
		FileInputStream file = new FileInputStream(transformation);
	
		atlLauncher.launch(ILauncher.RUN_MODE, monitor, atlOptions, file);
	}
	
	protected Map<String, Resource> extractOutModels() throws ATLCoreException {
		Map<String, Resource> result = new HashMap<>();
		for(IModel outModel : outModels.keySet()) {
			String outputName = outModels.get(outModel);
			if(outModel instanceof EMFModel){
				EMFModel model = (EMFModel) outModel;
				result.put(outputName, model.getResource());
			}
		}
		return result;
	}
}
