package oogre.refinements.tac;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;

import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.analysis.RefinementLatticeOps;
import oogre.utils.Utils;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public class InitializationTransferFunctions extends AbstractingTransferFunction<OOGContext> {
	
	private TM tm;
	private OOGContext context = OOGContext.getInstance();

	public InitializationTransferFunctions(TM tm) {
		super();
		this.tm = tm;
	}
	
	public TM getTM() {
    	return tm;
    }

	@Override
	public OOGContext createEntryValue(MethodDeclaration method) {
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
					fieldTypingSet = this.tm.initTypeMapping(isMainClass,fieldVar, false, false);
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
				methodTypingSet = this.tm.initTypeMapping(isMainClass,methodVar, false, true);
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
		boolean isLent = false;
		boolean isUnique = false;
		SourceVariable declaredVariable = instr.getDeclaredVariable();
		IVariableBinding binding = declaredVariable.getBinding();
		ITypeBinding typebinding = binding.getType();
		if(!typebinding.isPrimitive()){
			value.putAllBindingKeyToVariableMap(binding.getKey(), declaredVariable);
			String enclosingClass = "";
			if(binding.isParameter()){
				enclosingClass = declaredVariable.getBinding().getDeclaringMethod().getDeclaringClass().getQualifiedName();
				this.tm.addBindingKeyToVariable(binding.getKey(), declaredVariable);
				isLent = true;
			}
			else if(binding.isField()){
				enclosingClass = declaredVariable.getBinding().getDeclaringClass().getQualifiedName();
			}
			else{
				enclosingClass = declaredVariable.getBinding().getDeclaringMethod().getDeclaringClass().getQualifiedName();
				isLent = true;
				isUnique = true;
			}
			Set<OType> declaredVariableTypingSet = this.tm.getTypeMapping(declaredVariable);
			if(declaredVariableTypingSet==null){
				boolean isMainClass = enclosingClass.equals(Config.MAINCLASS);
				declaredVariableTypingSet = this.tm.initTypeMapping(isMainClass,declaredVariable,isLent,isUnique);
				this.tm.putTypeMapping(declaredVariable, declaredVariableTypingSet);
			}
		}

		return value;
	}

	@Override
	public OOGContext transfer(NewObjectInstruction instr, OOGContext value) {
		ITypeBinding instantiatedType = instr.resolveInstantiatedType();
		ASTNode node = instr.getNode();
		ITypeBinding newExprEnclosingClassBinding = Utils.findEnclosingClassBinding(node);
		IMethodBinding newExprEnclosingMethodBinding = Utils.findEnclosingMethodBinding(node);
		if(node instanceof ClassInstanceCreation){
			ClassInstanceCreation cicNode = (ClassInstanceCreation)node;
			IMethodBinding constructorBinding = cicNode.resolveConstructorBinding();
			final TACNewExpr newExprVar = new TACNewExpr(constructorBinding, newExprEnclosingClassBinding, newExprEnclosingMethodBinding, instantiatedType);
			context.putAllBindingKeyToVariableMap(constructorBinding.getKey(), newExprVar);
			Variable newExprVarInMap = this.tm.getVarBindingMap(constructorBinding);
			
			// Build a mapping from the synthetic new expression variable to its left hand side variable
			Variable lhsVariable = instr.getTarget();
			value.addNewExprVarToLhsVar(newExprVar, lhsVariable);
			
			if(newExprVarInMap==null){
				this.tm.putVarBindingMap(constructorBinding, newExprVar);
			}
			String newExprEncClassName = newExprEnclosingClassBinding.getQualifiedName();
			Set<OType> newExprTypingSet = this.tm.getTypeMapping(newExprVar);
			if(newExprTypingSet==null){
				boolean isMainClass = newExprEncClassName.equals(Config.MAINCLASS);
				newExprTypingSet = this.tm.initTypeMapping(isMainClass,newExprVar, false,true);
				this.tm.putTypeMapping(newExprVar, newExprTypingSet);
			}
		}
		return value;
	}
	
	/**
	 * Field write TF, just to update the mapping from a synthetic new expression variable to the actual field variable
	 * In the TF for new expression, the mapping is from a synthetic new expression to a temporary variable that represents the left hand side
	 * Only field write TF knows that the left hand side of the new expression is assigned to what field variable
	 */
	@Override
	public OOGContext transfer(StoreFieldInstruction instr, OOGContext value) {
		Variable rhsVar = instr.getSourceOperand();
		Map<Variable, Variable> newToLhsMap = value.getNewToLhsMap();
		for (java.util.Map.Entry<Variable, Variable> entry : newToLhsMap.entrySet()) {
			Variable lhsVar = entry.getValue();
			if(lhsVar.equals(rhsVar)){
				IVariableBinding resolveFieldBinding = instr.resolveFieldBinding().getVariableDeclaration();
				Variable fieldVariable = this.tm.getVarBindingMap(resolveFieldBinding);
				value.addNewExprVarToLhsVar(entry.getKey(), fieldVariable);
			}
		}
		return value;
	}

	/**
	 * Assignment TF, just to update the mapping from a synthetic new expression variable to the local variable
	 * In the TF for new expression, the mapping is from a synthetic new expression to a temporary variable that represents the left hand side
	 * Only field the Assignment TF knows that the left hand side of the new expression is assigned to what local variable
	 */
	@Override
	public OOGContext transfer(CopyInstruction instr, OOGContext value) {
		Variable rhsVar = instr.getOperand();
		Variable lhsVar = instr.getTarget();
		Map<Variable, Variable> newToLhsMap = value.getNewToLhsMap();
		for (java.util.Map.Entry<Variable, Variable> entry : newToLhsMap.entrySet()) {
			Variable newExprLhsVar = entry.getValue();
			if(rhsVar.equals(newExprLhsVar)){
				value.addNewExprVarToLhsVar(entry.getKey(), lhsVar);
			}
		}
		return value;
	}

}
