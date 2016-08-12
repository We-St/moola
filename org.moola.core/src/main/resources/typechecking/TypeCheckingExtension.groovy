import org.moola.dsl.core.Definition
import org.moola.dsl.core.OperationResult
import org.moola.dsl.core.Operation
import org.moola.dsl.core.OperationType
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.moola.dsl.core.OperationType
import org.moola.Process
import org.moola.phases.config.TypeCheckSpace


Process process = TypeCheckSpace.getProcess()

unresolvedVariable { var ->
	
	process.operationTypes.each { OperationType ot ->
		if(ot.name == var.name){
			storeType(var, classNodeFor(OperationType))
			handled = true
		}
	}
	
	// TODO: change later
	if(var.name == var.name.toUpperCase()){
		storeType(var, classNodeFor(OperationType))
		handled = true
	}
		
	if(!handled){
		// println "(type checking): " + var.getType()
		storeType(var, classNodeFor(Object))
		handled = true
	}
}



methodNotFound { receiver, name, argList, argTypes, call ->
	def opType = process.operationTypes.find { ot -> ot.name == name }
	
	if(opType != null){
		return createMethod(opType)
	}
	
	if(name == "emit"){
		return newMethod(name, Object)
	}
		
	newMethod(name, Object)
}



MethodNode createMethod(OperationType opType){
	int ACC_PUBLIC = 0x0001
	Operation op = opType.createInstance()
	
	// derive return type of newly created method
	def returnType;
	def outputTypes = op.getSignature().getOutputs()
	switch(outputTypes.size()){
		case 0:
			returnType = Void.TYPE
			break
		case 1:
			returnType = outputTypes[0].type
			break;
		default:
			returnType = OperationResult.class
	}
	
	return (new AstBuilder().buildFromSpec {
            method(opType.name, ACC_PUBLIC, returnType) {
                parameters {
					op.signature.inputs.each { Definition d ->
						parameter "${d.name}": d.type
					}
                }
                exceptions {}
				
				// The method node is not added to the AST, so any code in it will never be executed. 
				// Only its signature is used for type checking.
                block { 
                    returnStatement {
                        constant '<irrelevant>' 
                    }
                }
                annotations {}
            }
        }).first()
}
