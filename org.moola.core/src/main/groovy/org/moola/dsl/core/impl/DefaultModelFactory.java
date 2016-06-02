package org.moola.dsl.core.impl;

import org.moola.dsl.core.Model;
import org.moola.dsl.core.ModelFactory;

public class DefaultModelFactory
	extends ModelFactory {

	@Override
	public Model<?> create() {
		return new DefaultModel();
	}

}
