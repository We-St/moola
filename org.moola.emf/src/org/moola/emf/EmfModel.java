package org.moola.emf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;

import org.moola.dsl.core.Model;
import org.moola.logging.ILogger;
import org.moola.logging.LoggerFactory;

public class EmfModel
	extends Model<Resource> {

	private static final int FLUSH_LIMIT_SHIFT = 16;
	private static final Integer FLUSH_LIMIT = Integer.valueOf(1 << FLUSH_LIMIT_SHIFT);
	
	protected ResourceSet resourceSet;
	
	public EmfModel() {
	}
	
	public EmfModel(ResourceSet resourceSet) {
		this();
		this.resourceSet = resourceSet;
	}
	
	public URI getUri() {
		return URI.createURI(this.path);
	}
		
	@Override
	public void load() {
		URI uri = this.getUri();
		synchronized (this.resourceSet) {
			this.content = this.resourceSet.getResource(uri, true);	
		}
	}

	@Override
	public void save()  {
		try {
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(XMLResource.OPTION_FLUSH_THRESHOLD, FLUSH_LIMIT);
			options.put(XMLResource.OPTION_USE_FILE_BUFFER, Boolean.TRUE);
			
			URI uri = this.getUri();
			this.content.setURI(uri);
			
			synchronized (this.resourceSet) {
				long start = System.nanoTime();
				this.content.save(options);
				long end = System.nanoTime();
				double savingTime = ((end - start) / 1000 / 1000 / 1000);
				System.out.println( "Saving " + name + " took " + savingTime );
			}
		} catch (IOException ex) {
			throw new RuntimeException("Could not save model " + this.name, ex);
		}
	}
	
}
