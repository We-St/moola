package org.moola.emf.atl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can be used to extract information from *.atl files such as meta models, model names and types
 * and refinement flags.
 */
public class AtlParser {

	private Pattern metaModelPattern = Pattern.compile("\\s*--\\s*(@path|@nsURI)\\s*[a-zA-Z0-9_]+\\s*=.*");
	private Pattern meatModelDetailsPattern = Pattern.compile("\\s*[a-zA-Z0-9]+\\s*=.*");
	private Pattern createPattern = Pattern.compile("\\s*create\\s*(.*?)(from|refining)(.*?);");
	private Pattern createDetailsPattern = Pattern.compile("\\s*[a-zA-Z0-9_]+\\s*:\\s*[a-zA-Z0-9_]+\\s*");
	
	/**
	 * Extracts meta models and names of input/output models
	 * @param file The ATL file to parse
	 */
	public AtlDetails extractDetails(String file){
		AtlDetails details = new AtlDetails();
		
		List<String> lines = readLines(file);
		for(String line : lines){
	    	if (isMetaModelDef(line))
	    	{
	    		Tuple<String, String> def = this.getMetaModelDef(line);
	    		details.getMetaModels().put(def.value1, def.value2);
	    	}
	    	else {
	    		if( isModelDef(line) ){
	    			List<Tuple<String, String>> models;
	    			
	    			boolean refinement = isRefinement(line);
	    			details.setRefinement(refinement);
	    			
	    			models = extractInputModels(line);
	    			for(Tuple<String, String> def : models){
			    		details.getInputModels().put(def.value1, def.value2);
	    			}
	    			
	    			models = extractOutputModels(line);
	    			for(Tuple<String, String> def : models){
			    		details.getOutputModels().put(def.value1, def.value2);
	    			}
	    		}
	    	}
		}
		return details;
	}
	
	private List<String> readLines(String file) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line = null;
		    while ((line = br.readLine()) != null) {
	    	   lines.add(line);
		    }
		    return lines;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean isMetaModelDef(String line) {
		Matcher matcher = metaModelPattern.matcher(line);
		return matcher.find();
	}

	private Tuple<String, String> getMetaModelDef(String line){
		Matcher detailsMatch = meatModelDetailsPattern.matcher(line);
		if(detailsMatch.find()){
    		String details = detailsMatch.group(0).trim();
    		String[] parts = details.split("=");
    		String name = parts[0].trim();
    		String path = parts[1].trim();
    		return new Tuple<>(name, path);
		}
		throw new RuntimeException("No details match found");
	}

	private boolean isModelDef(String line) {
		Matcher matcher = createPattern.matcher(line);
		return matcher.find();
	}

	private List<Tuple<String, String>> extractInputModels(String line){
		String part = line;
		if(isRefinement(line)){
			part = line.split("refining")[1];
		} else {
			part = line.split("from")[1];
		}
		return getModelDefs(part);
	}
	
	private List<Tuple<String, String>> extractOutputModels(String line){
		String part = line;
		if(isRefinement(line)){
			part = line.split("refining")[0];
		} else {
			part = line.split("from")[0];
		}
		return getModelDefs(part);
	}
	
	private List<Tuple<String, String>> getModelDefs(String line){
		List<Tuple<String,String>> result = new ArrayList<>();
		Matcher modelMatches = createDetailsPattern.matcher(line);
		while(modelMatches.find()){
			String modelMatch = modelMatches.group(0).trim();
			String[] parts = modelMatch.split(":");
			String modelName = parts[0].trim();
			String modelType = parts[1].trim();
			result.add(new Tuple<>(modelName, modelType));
		}
		return result;
	}
	
	private boolean isRefinement(String line){
		return line.contains("refining");
	}
	
	private class Tuple<T,V> {
		private T value1;
		private V value2;
		
		public Tuple(T value1, V value2){
			this.value1 = value1;
			this.value2 = value2;
		}
	}
	
}
