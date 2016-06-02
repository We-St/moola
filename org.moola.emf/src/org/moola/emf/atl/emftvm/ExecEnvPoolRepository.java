package org.moola.emf.atl.emftvm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.emftvm.EmftvmFactory;
import org.eclipse.m2m.atl.emftvm.Metamodel;
import org.eclipse.m2m.atl.emftvm.util.DefaultModuleResolverFactory;
import org.eclipse.m2m.atl.emftvm.util.ExecEnvPool;
import org.eclipse.m2m.atl.emftvm.util.ModuleResolverFactory;

public class ExecEnvPoolRepository {

	protected Map<String, ExecEnvPool> pools;
	protected Map<ExecEnvPool, Set<String>> registeredMetaModels;
	
	public ExecEnvPoolRepository(){
		this.pools = new HashMap<>();
		this.registeredMetaModels = new HashMap<>();
	}
	
	public ExecEnvPool get(String path, String module){
		if(this.pools.containsKey(module)){
			return this.pools.get(module);
		} else {
			final ModuleResolverFactory mrf = new DefaultModuleResolverFactory(path);
			ExecEnvPool pool = new ExecEnvPool();
			pool.setModuleResolverFactory(mrf);
			pool.loadModule(module);
			this.pools.put(module, pool);
			return pool;
		}
	}

	
	public void registerMetaModel(ExecEnvPool pool, String name, Resource resource) {
		if(!registeredMetaModels.containsKey(pool)){
			registeredMetaModels.put(pool, new HashSet<>());
		}
		if(registeredMetaModels.get(pool).contains(name)){
			return; // skipping meta model registration
		}
		
		Metamodel metaModel = EmftvmFactory.eINSTANCE.createMetamodel();
		metaModel.setResource(resource);
		pool.registerMetaModel(name, metaModel);
		registeredMetaModels.get(pool).add(name);
	}
	
}
