package org.moola.emf.ocl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.Query;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.SendSignalAction;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.helper.OCLHelper;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;

public class OclQueryExecutor {

	
	public static void execute(String queryString, Resource resource){
		try {
			OCLExpression<EClassifier> query = null;

		    // Model umlModel = (Model) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.MODEL);

		    
		    
		    // create an OCL instance for Ecore
		    OCL<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, org.eclipse.ocl.ecore.Constraint, EClass, EObject> ocl;
		    ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE, resource);

		    
		    // create an OCL helper object
		    OCLHelper<EClassifier, EOperation, EStructuralFeature, org.eclipse.ocl.ecore.Constraint> helper = ocl.createOCLHelper();
		    
		    // set the OCL context classifier
		    //helper.setContext(UMLPackage.eINSTANCE.getModel());
		    helper.setContext(EcorePackage.eINSTANCE.getEModelElement()); // getEClass()?
		    
		    // query = helper.createQuery("self.name");
		    query = helper.createQuery(queryString);
		    Query<EClassifier, EClass, EObject> eval = ocl.createQuery(query);
		    
		    // print query result
		    Object result = eval.evaluate(resource.getContents().get(0));
		    System.out.println(result);
		    System.out.println(result.getClass());
		    
		    // record success
		} catch (ParserException e) {
		    // record failure to parse
			e.printStackTrace();
		    System.err.println(e.getLocalizedMessage());
		}
	}
	
}
