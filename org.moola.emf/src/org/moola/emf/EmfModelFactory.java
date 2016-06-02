package org.moola.emf;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
import org.eclipse.m2m.atl.emftvm.impl.resource.EMFTVMResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

import org.moola.dsl.core.Model;
import org.moola.dsl.core.ModelFactory;

public class EmfModelFactory
	extends ModelFactory {
	
	protected Map<String, Resource> loadedMetaModels;
	protected ResourceSet resourceSet;
	
	public EmfModelFactory(){
		this.initResourceFactory();
		
		this.loadedMetaModels = new HashMap<>();
		this.resourceSet = this.createResourceSet();
	}
	
	private void initResourceFactory(){
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		
		Map<String, Object> etfm = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		if ( ! etfm.containsKey("*")) {
			etfm.put("*", new XMIResourceFactoryImpl());
		}
	}
	
	private ResourceSet createResourceSet(){
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("emftvm", new EMFTVMResourceFactoryImpl());
		
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		UMLResourcesUtil.init(this.resourceSet);
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);

		// from the Acceleo generated main classes
		Map<URI, URI> uriMap = resourceSet.getURIConverter().getURIMap();
		URI uri = URI.createURI("platform:/plugin/org.eclipse.uml2.uml.resources/");
		uriMap.put(URI.createURI(UMLResource.LIBRARIES_PATHMAP), uri.appendSegment("libraries").appendSegment(""));
		uriMap.put(URI.createURI(UMLResource.METAMODELS_PATHMAP), uri.appendSegment("metamodels").appendSegment(""));
		uriMap.put(URI.createURI(UMLResource.PROFILES_PATHMAP), uri.appendSegment("profiles").appendSegment(""));
		uri = URI.createURI("platform:/plugin/eu.artist.migration.umlprofilestore/");
		uriMap.put(URI.createURI("pathmap://UML_PROFILE_STORE/PROFILES/jpa2_profile.profile.uml"), uri
				.appendSegment("umlprofiles").appendSegment(""));
		
		
		Map<Object, Object> loadOptions = resourceSet.getLoadOptions();
		loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
		loadOptions.put(XMLResource.OPTION_EXTENDED_META_DATA, new BasicExtendedMetaData());
		return resourceSet;
	}
	
	@Override
	public void registerType(String name, String path) {
		super.registerType(name, path);
		
		if(path.equals(UMLPackage.eNS_URI)){
			Resource metamodel = this.resourceSet.getResource(URI.createURI(UMLPackage.eNS_URI), true);
			this.loadedMetaModels.put(name, metamodel);
			return; //TODO: refactor!
		}

		URI uri = URI.createURI(path);
		Resource metamodel = this.resourceSet.getResource(uri, true);
		this.registerMetaModel(metamodel);
		this.loadedMetaModels.put(name, metamodel);
	}
	
	private void registerMetaModel(Resource metamodel){
		for(EObject eObject : metamodel.getContents()){
			if(eObject instanceof EPackage){
				EPackage p = (EPackage) eObject;
				String nsURI = p.getNsURI();
				if (nsURI == null) {
					nsURI = p.getName();
					p.setNsURI(nsURI);
				}
				this.resourceSet.getPackageRegistry().put(nsURI, p);	
			}
		}
	}
	
	@Override
	public Model<?> create() {
		return new EmfModel(this.resourceSet);
	}
	
	public Resource getMetamodel(String name){
		return this.loadedMetaModels.get(name);
	}
	
	public ResourceSet getResourceSet(){
		return this.resourceSet;
	}
	
}
