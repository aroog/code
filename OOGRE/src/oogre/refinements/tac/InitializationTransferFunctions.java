package oogre.refinements.tac;

import java.lang.reflect.Modifier;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.analysis.RefinementLatticeOps;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.Variable;

public class InitializationTransferFunctions extends AbstractingTransferFunction<OOGContext> {
	
	private TM tm;

	public InitializationTransferFunctions(TM tm) {
		super();
		this.tm = tm;
	}
	
	public TM getTM() {
    	return tm;
    }

	@Override
	public OOGContext createEntryValue(MethodDeclaration method) {
		OOGContext context = OOGContext.getInstance();
		ITypeBinding mdeclaringClass = method.resolveBinding().getDeclaringClass();
		if(mdeclaringClass != null) {
			// Create TAC variable for each field of the declaring class
			for(final IVariableBinding field : mdeclaringClass.getDeclaredFields() ) {
				if( (field.getModifiers() & Modifier.STATIC) != 0)
					// skip static fields
					continue;
				
				final TACVariable fieldVar = new TACVariable(field);
				IVariableBinding fieldBinding = fieldVar.getVarDecl();
				context.putAllBindingKeyToVariableMap(fieldBinding.getKey(), fieldVar);
				
				Variable fieldBindingInMap = this.tm.getVarBindingMap(fieldBinding);
				if(fieldBindingInMap==null){
					this.tm.putVarBindingMap(field, fieldVar);
					
				}
				String enclosingTypeName = mdeclaringClass.getQualifiedName();
				Set<OType> fieldTypingSet = this.tm.getTypeMapping(fieldVar);
				
				if(fieldTypingSet==null){
					boolean isMainClass = enclosingTypeName.equals(Config.MAINCLASS);
					fieldTypingSet = this.tm.initTypeMapping(isMainClass,fieldVar);
					this.tm.putTypeMapping(fieldVar, fieldTypingSet);
				}
			}
		}

		// Create TAC variable for the method itself
		IMethodBinding methodBinding = method.resolveBinding();
		ITypeBinding methodReturnType = methodBinding.getReturnType();
		if( /*!Modifier.isStatic(methodBinding.getModifiers()) &&*/ !methodReturnType.isPrimitive() ){
			final TACMethod methodVar = new TACMethod(methodBinding);
			context.putAllBindingKeyToVariableMap(methodBinding.getKey(), methodVar);
			Variable methodBindingInMap = this.tm.getVarBindingMap(methodBinding);
			if(methodBindingInMap==null){
				this.tm.putVarBindingMap(methodBinding, methodVar);
			}
			String methodEncClassName = mdeclaringClass.getQualifiedName();
			Set<OType> methodTypingSet = this.tm.getTypeMapping(methodVar);
			if(methodTypingSet==null){
				boolean isMainClass = methodEncClassName.equals(Config.MAINCLASS);
				methodTypingSet = this.tm.initTypeMapping(isMainClass,methodVar);
				this.tm.putTypeMapping(methodVar, methodTypingSet);
			}
		}

		return context;
	}

	@Override
	public ILatticeOperations<OOGContext> getLatticeOperations() {
	    return new RefinementLatticeOps();
	}
	
	@Override
	public OOGContext transfer(SourceVariableDeclaration instr, OOGContext value) {
		
		SourceVariable declaredVariable = instr.getDeclaredVariable();
		IVariableBinding binding = declaredVariable.getBinding();
		value.putAllBindingKeyToVariableMap(binding.getKey(), declaredVariable);
		String enclosingClass = "";
		if(binding.isParameter()){
			enclosingClass = declaredVariable.getBinding().getDeclaringMethod().getDeclaringClass().getQualifiedName();
			this.tm.addBindingKeyToVariable(binding.getKey(), declaredVariable);
		}
		else if(binding.isField()){
			enclosingClass = declaredVariable.getBinding().getDeclaringClass().getQualifiedName();
		}
		else{
			enclosingClass = declaredVariable.getBinding().getDeclaringMethod().getDeclaringClass().getQualifiedName();
		}
		Set<OType> declaredVariableTypingSet = this.tm.getTypeMapping(declaredVariable);
		if(declaredVariableTypingSet==null){
			boolean isMainClass = enclosingClass.equals(Config.MAINCLASS);
			declaredVariableTypingSet = this.tm.initTypeMapping(isMainClass,declaredVariable);
			this.tm.putTypeMapping(declaredVariable, declaredVariableTypingSet);
		}

		return value;
	}

}
