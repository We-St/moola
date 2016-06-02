/*******************************************************************************
 * Copyright (coffee) 2014 Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Stefan Weghofer (Vienna University of Technology) - DSL and petri nets implementation
 * Alexander Bergmayr (Vienna University of Technology) - initial API and implementation
 *
 * Initially developed in the context of ARTIST EU project www.artist-project.eu
 *******************************************************************************/

package org.moola.dsl.core.impl;

import java.util.HashMap;
import java.util.Map;

import org.moola.dsl.core.Operation;

public final class OperationRegistry {

	public Map<String, Class<? extends Operation>> registry;
	
	public OperationRegistry() {
		registry = new HashMap<>();
	}
	
	public void register(String key, Class<? extends Operation> trafo) {
		registry.put(key.toLowerCase(), trafo);
	}

	public boolean has(String key) {
		return key != null && registry.containsKey(key.toLowerCase());
	}
	
	public Class<? extends Operation> resolve(String key) {
		if(key == null || !registry.containsKey(key.toLowerCase())){
			throw new RuntimeException("No operation registered for type " + key + ". Please check you dependencies");
		}
		return registry.get(key.toLowerCase());
	}
}
