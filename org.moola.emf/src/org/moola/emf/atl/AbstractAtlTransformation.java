package org.moola.emf.atl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.moola.emf.EmfModel;
import org.moola.emf.EmfModelFactory;

import org.moola.dsl.core.Definition;
import org.moola.dsl.core.ModelFactory;
import org.moola.dsl.core.Operation;
import org.moola.dsl.core.Signature;

public abstract class AbstractAtlTransformation extends Operation {
	
	public String src = "";
	
	protected AtlDetails details;
	protected EmfModelFactory modelFactory;
	protected Map<String, String> inoutMap;
	
	protected AtlExecStrategie strategie;
	
	public AbstractAtlTransformation(){
		this.modelFactory = (EmfModelFactory) ModelFactory.INSTANCE;
		this.inoutMap = new HashMap<>();
	}
	
	@Override
	protected void postConfig() {
		try {
			this.strategie = this.getStrategie();
			
			this.details = strategie.parseDetails();
			log.verbose("Running " + this.name + " as refinement: " + this.details.isRefinement());
		} catch(Exception ex){
			log.warning("Could not read ATL details of " + this.name + " from " + this.src);
			log.warning("Reason: " + ex.getMessage());
			throw ex;
		}
	}
	
	protected abstract AtlExecStrategie getStrategie();
	
	
	@Override
	protected Signature provideSignature() {
		if(this.signature != null){
			return this.signature; // Take user defined signature if it exists.
		}
		
		Map<String, String> inModels = this.details.getInputModels();
		Map<String, String> outModels = this.details.getOutputModels();

		Signature spec = new Signature();
		spec.inputs(inModels);	
		spec.outputs(outModels);
		return spec;
	}
	
	@Override
	protected void prepareRun() {
		log.verbose("Pre-run for trafo " + this.name + ": " + this.signature);
		
		strategie.init();
		registerMetaModels();
		registerInputs();
		registerInouts();
		registerOutputs();
	}


	public static Object lock = new Object();
	protected void registerMetaModels(){
		Set<String> types = this.signature.getAllTypes();
		synchronized(lock){	
			for(String type : types){
				Resource metaModel = this.modelFactory.getMetamodel(type);
				this.strategie.addMetaModel(type, metaModel);
				log.verbose("Registering metamodel " + type + " with " + metaModel);
			}
		}
	}
	
	protected void registerInputs(){
		List<Definition> inModels = this.getInputModels();
		for(Definition def : inModels){
			Object model = this.getInput(def.getName());
			
			log.verbose("Start registering input model " + def.getName());	
			if(model instanceof EmfModel){
				Resource resource = ((EmfModel) model).getContent();
				//strategie.addInputModel(def.getName(), resource, def.getType());
				log.verbose("Registering input model " + def.getName() + " with " + resource + ". IsEmpty: " + resource.getContents().isEmpty());
			} else {
				throw new RuntimeException("Expected model input. Got: " + model.getClass().getCanonicalName());
			}
		}
	}
	
	private List<Definition> getInputModels(){
		List<Definition> inputs;
		
		// In refinement mode, ignore the first X input models (X = number of outputs), since they are considered inouts.
		if(this.details.isRefinement()){ 
			inputs = new ArrayList<Definition>();
			
			int outputSize = this.signature.getOutputs().size();
			for(Definition def : this.signature.getInputs()){
				if(outputSize > 0){
					outputSize -= 1;
				} else {
					inputs.add(def);
				}
			}
		} else {
			inputs = this.signature.getInputs();
		}
		
		return inputs;
	}
	
	protected void registerInouts(){
		List<Definition> inModels = this.getInoutModels();
		for(Definition def : inModels){
			Object model = this.getInput(def.getName());
			
			if(model instanceof EmfModel){
				Resource resource = ((EmfModel) model).getContent();
				//strategie.addInoutModel(def.getName(), resource, def.getType());
				log.verbose("Registering inout model " + def.getName() + " with " + resource + ". IsEmpty: " + resource.getContents().isEmpty());
			} else {
				throw new RuntimeException("Expected model input. Got: " + model.getClass().getCanonicalName());
			}
		}
	}

	private List<Definition> getInoutModels(){
		List<Definition> inouts = new ArrayList<Definition>();
		
		// In outs only when refinement mode is activated.
		if(this.details.isRefinement()){ 
			List<Definition> outputModels = this.signature.getOutputs();
			int index = 0;

			List<Definition> inputModels = this.signature.getInputs();
			for(Definition def : inputModels){
				if(index < outputModels.size()){
					inouts.add(def);
					
					Definition outputDef = outputModels.get(index);
					inoutMap.put(def.getName(), outputDef.getName());
					log.verbose("Connecting input " + def.getName() + " to output " + outputDef.getName());
					index += 1;
				}
			}
		}
		
		return inouts;
	}
	
	protected void registerOutputs() {
		if(this.details.isRefinement()){
			return;
		}
		List<Definition> outputModels = this.signature.getOutputs();
		for(Definition def : outputModels){
			//strategie.addOutputModel(def.getName(), def.getType());
			log.verbose("Registering output model " + def.getName());
		}
	}

	@Override
	protected void execute() {
		Map<String, Resource> outputs;
		
		outputs = strategie.run(this.details.isRefinement());
		checkOutputTypes(outputs);
		emitOutputs(outputs);
	}
	
	protected void checkOutputTypes(Map<String, Resource> outputs){
		// TODO: implement
	}

	protected void emitOutputs(Map<String, Resource> outputs){
		for(String name : outputs.keySet()){
			String target = name;
			if(inoutMap.containsKey(name)){
				target = inoutMap.get(name);
				log.verbose("Applying inout map to " + name + ". Is now " + inoutMap.get(name));
			}
			
			//String type = this.getSpec().getOutputDefinition(target).getType();
			Resource content = outputs.get(name);
				
			EmfModel outModel = (EmfModel) ModelFactory.INSTANCE.create();
			outModel.setContent(content);
			//outModel.setType(type);
			
			log.verbose("Emitting " + target + " with " + outModel.toString());
			this.emit(target, outModel);
		}
	}
	
	public void src(String src){
		this.src = src;
	}
}
