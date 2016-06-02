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
package org.moola.emf.atl.emftvm;

import org.moola.emf.atl.AbstractAtlTransformation;
import org.moola.emf.atl.AtlExecStrategie;

public class AtlEmftvmTransformation extends AbstractAtlTransformation {

	protected String module;
	
	private static ExecEnvPoolRepository poolRepo;

	static {
		 poolRepo = new ExecEnvPoolRepository();
	}
	
	public AtlEmftvmTransformation() { }

	@Override
	protected AtlExecStrategie getStrategie() {
		this.src = this.path(this.src);
		log.verbose("Full path for ATL file of " + this.name + " is: " + this.src);
		return new EmftvmAtlExecStrategie(this.src, this.module, poolRepo, this.log);
	}
	
}
