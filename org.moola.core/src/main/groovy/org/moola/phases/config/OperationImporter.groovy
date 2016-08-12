package org.moola.phases.config

import java.util.List

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.CompilePhase

import org.moola.dsl.ast.GenerateModelClassesTransformer
import org.moola.dsl.ast.LoggingCompilationCustomizer
import org.moola.dsl.ast.ModelNameTransformation
import org.moola.dsl.ast.OperationNameTransformation
import org.moola.dsl.ast.ScriptMerger
import org.moola.dsl.ast.ScriptSeparator
import org.moola.dsl.ast.UnrollMultiAssignmentTransformation
import org.moola.dsl.core.OperationType
import org.moola.dsl.core.PathFactory
import org.moola.dsl.core.impl.DefaultPathFactory
import groovy.lang.GroovyShell
import groovy.transform.TypeChecked
import org.moola.util.FileHelper
import org.moola.Process
import org.moola.ProcessFactory;
import org.moola.Launcher


class OperationImporter {
	
	private String path
	private ScriptBase base
	
	public OperationImporter(String path, ScriptBase base) {
		this.base = base
		this.path = this.base.process.pathFactory.resolve(path + ".moola")
	}
	
	void include(String... operationTypeNames) {
		def opTypeNames = []
		operationTypeNames.each { opTypeNames.add(it) }
		executeScript(opTypeNames)
	}
	
	void executeScript(List<String> opTypeNames) {
		try {
			File file = new File(URI.create(this.path))
			if(!file.exists()) {
				throw new OperationImportException("Cannot find file to import from: ${this.path}.");
			}
			
			Process process = ProcessFactory.create(file)
			CompilerConfiguration compilerConfig = createCompilerConfiguration()
			GroovyShell shell = prepareGroovyShell(this.base.classLoader, compilerConfig);
			gatherArtifacts(process, shell, compilerConfig)
			copyArtifacts(this.base.process, process, opTypeNames)
		} catch(MultipleCompilationErrorsException ex){
			throw new ConfigurationPhaseException(ex.getMessage())
		}
	}
	
	private GroovyShell prepareGroovyShell(ClassLoader cl, CompilerConfiguration cc){
		Binding binding = new Binding()
		return new GroovyShell(cl, binding, cc)
	}
	
	private void gatherArtifacts(Process process, GroovyShell shell, CompilerConfiguration cc){
		ScriptBase script = (ScriptBase) shell.parse(process.scriptContent);
		script.init(process, shell.getClassLoader())
		script.run()
	}
	
	private CompilerConfiguration createCompilerConfiguration(){
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.setScriptBaseClass("org.moola.phases.config.ScriptBase");
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new ModelNameTransformation()));
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new OperationNameTransformation()));
	}
	
	private void copyArtifacts(Process baseProcess, Process importedProcess, List<String> opTypesToImport) {
		for(String opTypeToImport in opTypesToImport) {
			def opType = importedProcess.operationTypes.find { it.name == opTypeToImport }
			
			if(opType != null) {
				baseProcess.registerOperationType(opType)
			} else {
				throw new OperationImportException("Cannot find operation $opTypeToImport to import.");
			}
		}
	}
}
