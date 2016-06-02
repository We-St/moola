package org.moola.emf.atl.emftvm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.m2m.atl.emftvm.EmftvmFactory;
import org.eclipse.m2m.atl.emftvm.ExecEnv;
import org.eclipse.m2m.atl.emftvm.Metamodel;
import org.eclipse.m2m.atl.emftvm.Model;
import org.eclipse.m2m.atl.emftvm.util.DefaultModuleResolver;
import org.eclipse.m2m.atl.emftvm.util.ExecEnvPool;
import org.eclipse.m2m.atl.emftvm.util.TimingData;
import org.moola.emf.EmfModelFactory;
import org.moola.emf.atl.AtlDetails;
import org.moola.emf.atl.AtlExecStrategie;
import org.moola.emf.atl.AtlParser;

import org.moola.dsl.core.ModelFactory;
import org.moola.logging.ILogger;
import org.moola.logging.LoggerFactory;

public class EmftvmAtlExecStrategie implements AtlExecStrategie {

	protected String path;
	protected String module;
	
	protected DefaultModuleResolver moduleResolver;
	protected ExecEnv exec;
	protected EmfModelFactory modelFactory;
	protected ILogger log;
	
	public Map<String, Resource> models;

	protected ExecEnvPoolRepository poolRepo;
	protected ExecEnvPool pool;


	public EmftvmAtlExecStrategie(String path, String module, ExecEnvPoolRepository poolRepo, ILogger log){
		this.path = path;
		this.module = module;
		this.poolRepo = poolRepo;
		this.log = log;
		this.models = new HashMap<>();
		this.modelFactory = (EmfModelFactory) ModelFactory.INSTANCE;
		this.pool = this.poolRepo.get(path, module);
	}
	
	private ExecEnv getExecEnv(){
		if(exec != null){
			return exec;
		}
		exec = pool.getExecEnv();
		return exec;
	}
	
	@Override
	public AtlDetails parseDetails() {
		AtlParser parser = new AtlParser();
		File file = new File(this.path.replace("file:/", "") + "/" + this.module + ".atl");
		if(file.exists()){
			return parser.extractDetails(file.getAbsolutePath());
		} else {
			throw new RuntimeException("No file found at " + file.getAbsolutePath());
		}
	}

	@Override
	public void init() {
		//this.exec = EmftvmFactory.eINSTANCE.createExecEnv();
	}

	@Override
	public void addMetaModel(String name, Resource resource) {
		this.poolRepo.registerMetaModel(pool, name, resource);
	}

	@Override
	public void addInputModel(String name, Resource resource, String type) {
		this.models.put(name, resource);

		Model model = EmftvmFactory.eINSTANCE.createModel();
		model.setResource(resource);
		
		
		this.getExecEnv().registerInputModel(name, model);
	}

	@Override
	public void addInoutModel(String name, Resource resource, String type) {
		this.models.put(name, resource);

		Model model = EmftvmFactory.eINSTANCE.createModel();
		model.setResource(resource);
		this.getExecEnv().registerInOutModel(name, model);
	}

	@Override
	public void addOutputModel(String name, String type) {
		ResourceSet resourceSet = modelFactory.getResourceSet();
		
		Model outModel = EmftvmFactory.eINSTANCE.createModel();
		outModel.setResource(resourceSet.createResource(URI.createURI("temp.xmi")));
		this.getExecEnv().registerOutputModel(name, outModel);
	}

	@Override
	public Map<String, Resource> run(boolean refinement) {
		execEmftvm();
		Map<String, Resource> result = collectOutputs();

		pool.returnExecEnv(getExecEnv());
		return result;
	}

	
	public final static Object ATL_LOCK_OBJ = new Object();

	private void execEmftvm(){
		log.verbose("Starting Emftvm with metamodels: " + this.exec.getMetaModels());
				
		//ResourceSet resourceSet = modelFactory.getResourceSet();
		//synchronized (ATL_LOCK_OBJ) {
		//	this.moduleResolver = new DefaultModuleResolver(this.path, resourceSet);
		//	exec.loadModule(moduleResolver, this.module);

			TimingData td = new TimingData();
			td.finishLoading();
			getExecEnv().run(td);
			td.finish();
		//}
	}

	private Map<String, Resource> collectOutputs(){
		Map<String, Resource> result = new HashMap<>();
		Map<String, Model> outputs = exec.getOutputModels();
		Map<String, Model> inouts = exec.getInoutModels();
		
		for(String name : outputs.keySet()){
			Model output = outputs.get(name);
			result.put(name, output.getResource());
		}
		for(String name : inouts.keySet()){
			Model output = inouts.get(name);
			result.put(name, output.getResource());
		}
		
		log.verbose("Found " + result.size() + " (in)output models");
		return result;
	}
}
