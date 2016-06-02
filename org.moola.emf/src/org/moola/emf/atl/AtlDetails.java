package org.moola.emf.atl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The result of an Atl extraction
 */
public class AtlDetails {
	
	private Map<String, String> metaModels;
	private Map<String, String> inputModels;
	private Map<String, String> outputModels;
	private boolean refinement;
	
	public AtlDetails(){
		metaModels = new HashMap<>();
		inputModels = new HashMap<>();
		outputModels = new HashMap<>();
	}

	public Map<String, String> getMetaModels() {
		return metaModels;
	}

	public void setMetaModels(Map<String, String> metaModels) {
		this.metaModels = metaModels;
	}

	public Map<String, String> getInputModels() {
		return inputModels;
	}

	public void setInputModels(Map<String, String> inputModels) {
		this.inputModels = inputModels;
	}

	public Map<String, String> getOutputModels() {
		return outputModels;
	}

	public void setOutputModels(Map<String, String> outputModels) {
		this.outputModels = outputModels;
	}

	public boolean isRefinement() {
		return refinement;
	}

	public void setRefinement(boolean refinement) {
		this.refinement = refinement;
	}

	
	public List<String> getInputModelNames(){
		List<String> result = new ArrayList<>();
		for(String name : inputModels.keySet()){
			result.add(name);
		}
		return result;
	}
	
	public List<String> getOutputModelNames(){
		List<String> result = new ArrayList<>();
		for(String name : outputModels.keySet()){
			result.add(name);
		}
		return result;
	}
}
