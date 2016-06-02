package org.moola.emf.validate;

import org.moola.dsl.core.Operation;
import org.moola.dsl.core.Signature;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.moola.emf.EmfModel;
import org.eclipse.emf.common.util.*;

public class ValidateOperation extends Operation {

	@Override
	protected Signature provideSignature() {
		Signature spec = new Signature();
		spec.addInput("EmfModel", "input");
		spec.addOutput("Boolean", "valid");
		return spec;
	}
	
	@Override
	public void execute() {
		try {
			EmfModel model = (EmfModel) this.getInput("input");
			Resource content = model.getContent();
			for(EObject item : content.getContents()){
				Diagnostic d = Diagnostician.INSTANCE.validate(item);
				System.out.println(d.getCode() + " " + d.getMessage() + " " + d.getSeverity() + " " + d.getData());
			}
			emit("valid", true);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException("Validation failed", e);
		}
	}
	
}
