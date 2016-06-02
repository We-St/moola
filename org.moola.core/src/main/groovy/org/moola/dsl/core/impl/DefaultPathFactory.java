package org.moola.dsl.core.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.moola.dsl.core.PathFactory;


public class DefaultPathFactory 
	implements PathFactory {

	protected String defaultBasePath;
	
	public DefaultPathFactory(String defaultBasePath){
		this.defaultBasePath = defaultBasePath;
	}
	
	@Override
	public String resolve(String basePath, String resolvePath) {
		try {
			URI base = new URI(basePath);
			URI toResolve = new URI(resolvePath);
			return base.resolve(toResolve).toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Cannot resolve URIs", e);
		}
	}

	@Override
	public String resolve(String resolvePath) {
		return this.resolve(this.defaultBasePath, resolvePath);
	}

	@Override
	public void setDefaultBasePath(String defaultBasePath) {
		this.defaultBasePath = defaultBasePath;
	}

	@Override
	public String getDefaultBasePath() {
		return this.defaultBasePath;
	}

}
