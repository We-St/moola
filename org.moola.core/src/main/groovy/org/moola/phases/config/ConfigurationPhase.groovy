package org.moola.phases.config

import java.io.IOException
import java.net.URL

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
import org.moola.dsl.core.PathFactory
import org.moola.dsl.core.impl.DefaultPathFactory
import groovy.lang.GroovyShell
import groovy.transform.TypeChecked
import org.moola.util.FileHelper
import org.moola.Process
import org.moola.Launcher

/**
 * Executes the configuration phase. This encompasses parsing the script from a file or text, gathering all Moola artifacts (operations, models, model types)
 * to execute the script and finally type check it.
 * @author Stefan Weghofer
 */
class ConfigurationPhase {
	
	private Process process

	public ConfigurationPhase(Process process){
		this.process = process
	}
	
	public void execute() {
		try {
			CompilerConfiguration compilerConfig = createCompilerConfiguration()
			GroovyShell shell = prepareGroovyShell(compilerConfig);
			gatherArtifacts(process, shell, compilerConfig)
			gatherOrchestration(process, shell, compilerConfig)
		} catch(MultipleCompilationErrorsException ex){
			throw new ConfigurationPhaseException(ex.getMessage())
		}
	}
	
	private GroovyShell prepareGroovyShell(CompilerConfiguration cc){
		ClassLoader libraryLoader = this.createLibraryClassLoader()
		Binding binding = new Binding()
		
		GroovyClassLoader defLoader = new GroovyClassLoader(libraryLoader, cc)
		return new GroovyShell(defLoader, binding, cc)
	}
	
	private ClassLoader createLibraryClassLoader(){
		try {
			ClassLoader base = Launcher.class.getClassLoader();
			return new URLClassLoader(this.process.settings.libraries, base);
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
	
	private void gatherArtifacts(Process process, GroovyShell shell, CompilerConfiguration cc){
		TypeCheckSpace.setProcess(process) // share process with TypeChecked DSL
		
		ScriptBase script = (ScriptBase) shell.parse(process.scriptContent);
		script.init(process)
		script.run()
	}
	
	private CompilerConfiguration createCompilerConfiguration(){
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.setScriptBaseClass("org.moola.phases.config.ScriptBase");
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new GenerateModelClassesTransformer()));
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new ModelNameTransformation()));
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new OperationNameTransformation()));
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new ScriptSeparator()));
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(TypeChecked, extensions:['typechecking/TypeCheckingExtension.groovy']));
		//cc.addCompilationCustomizers(new LoggingCompilationCustomizer(CompilePhase.INSTRUCTION_SELECTION, System.out));
	}
	
	private void gatherOrchestration(Process process, GroovyShell shell, CompilerConfiguration cc){
				
		// for the orchestration script, we need to bring in the previously removed run closure via ScriptMerger
		// HOWEVER, we need to reuse the same instance of CompilerConfiguration for the class loading to work
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new ScriptMerger()));
		cc.addCompilationCustomizers(new ASTTransformationCustomizer(new UnrollMultiAssignmentTransformation(process)));
		// cc.addCompilationCustomizers(new LoggingCompilationCustomizer(CompilePhase.INSTRUCTION_SELECTION, System.out));
		
		def scriptText =
		""" // the whole closure should be overwritten by ScriptMerger
			merge { 
				println 'If you can see this, the AST merging encountered a problem.'
			}
		""";
		ScriptBase script = (ScriptBase) shell.parse(scriptText);
		script.init(process)
		script.run()
	}

}
