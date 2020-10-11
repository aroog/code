package oogre.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oog.itf.IObject;
import oogre.refinements.tac.BaseOperation;
import oogre.refinements.tac.OType;
import oogre.refinements.tac.SetOType;
import oogre.refinements.tac.TypePair;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * TOEBI: Add more state here: this is your big lattice (\sigma)
 * 
 * TODO: rename: AnalysisContext? RefinementContext?
 * TODO: make it a singleton. unless there are clear benefits to using a functional style?
 */
public class OOGContext {
	
	private OType selectedTyping;
	private OType mainDefaultTyping;
	private boolean isRefined = false;
	private boolean isHeuristicOwnedRan = false;
	private boolean isHeuristicPDRan = false;
	private boolean isHeuristicOwnerRan = false;
	
	// XXX. Delete PF!
	private  HashMap<TypePair, ArrayList<TypePair>> typeToFormalParameterMap = new HashMap<TypePair, ArrayList<TypePair>>();
	
	private Map<IObject, HashSet<IObject>> PC = new HashMap<IObject, HashSet<IObject>>();
	
	private Map<String, Map<String,String>> classToFields = new HashMap<String, Map<String,String>>();
	
	private Map<String,String> typeToSuperType = new HashMap<String, String>();
	
	private Set<IMethodBinding> calledMethodSet = new HashSet<IMethodBinding>();
	
	private Map<String, List<String>> classToPublicMethods = new HashMap<String, List<String>>();
	
	private Map<String,Map<String,Set<String>>> classToFinalFields = new HashMap<String,Map<String,Set<String>>>();
	
	private Map<Variable, Variable> tempToFinalVars = new HashMap<Variable, Variable>();
	
	private String currentReceiverName;
	
	//A map from an enclosing class to a map form the type of a final field that has been receiver in an adaptation to its identifier: encClass -> (fieldType -> fieldName)
	private Map<String,Map<String,String>> receiversAsFinalFields = new HashMap<String, Map<String,String>>(); 
	
	private Set<Variable> encapsulatedVars = new HashSet<Variable>();
	
	private Set<Variable> loigcalPartVars = new HashSet<Variable>();
	
	/**
	 * A map from all the binding.key to the SourceVariable (locals and parameters) or TACVariable (fields) or TACMethod (method return) variables
	 */
	private Map<String,Variable> allBindingKeyToVariableMap = new HashMap<String, Variable>();

	public Map<IObject, HashSet<IObject>> getPC() {
		return PC;
	}

	private static OOGContext instance = null;
	
	private boolean isAllowed = true;
	
	private List<BaseOperation> completedOprs = new ArrayList<BaseOperation>();
	
	private List<BaseOperation> moreInfoNeededRefs = new ArrayList<BaseOperation>();
	
	/**
	 * A mapping from an overridden method to its set of overriding methods
	 */
	private Map<String, Set<IMethodBinding>> overriddenToOverridingMap = new HashMap<String, Set<IMethodBinding>>();
	/**
	 * A mapping from the list of parameters of an overridden method to the set of list of parameters of its of overriding methods
	 */
	private Map<String,Set<List<IVariableBinding>>> overriddenToOverridingParametersMap = new HashMap<String, Set<List<IVariableBinding>>>();
	
	/**
	 * A set of all the overridden methods
	 */
	private Set<IMethodBinding> overriddenMethods = new HashSet<IMethodBinding>();
	
	/**
	 * A mapping from the list of parameters of an overridden method to the list of its parameters
	 */
	private Map<String,List<IVariableBinding>> overriddenToParametersMap = new HashMap<String, List<IVariableBinding>>();
	
	/**
	 * Keep track of all the n.PD's that get created
	 */
	private Set<String> nPDModifiers = new HashSet<String>();
	
	/**
	 * A map from a synthetic new expression variable to its left hand side variable (field or local) 
	 */
	private Map<Variable,Variable> newExprVarToLHSVar = new HashMap<Variable,Variable>();
	
	private OOGContext() {
	}

	public static OOGContext getInstance() {
		if (instance == null) {
			instance = new OOGContext();
		}
		return instance;
	}
	
	public void addNewExprVarToLhsVar(Variable newExprVar, Variable lhsVar){
		this.newExprVarToLHSVar.put(newExprVar, lhsVar);
	}
	
	public Variable getLhsVar(Variable newExprVar){
		return this.newExprVarToLHSVar.get(newExprVar);
	}
	
	public Map<Variable,Variable> getNewToLhsMap(){
		return this.newExprVarToLHSVar;
	}
	
	public void addOverridingMethod(IMethodBinding overriddenMethod, IMethodBinding overridingMethod){
		Set<IMethodBinding> overridingMethods = overriddenToOverridingMap.get(overriddenMethod.getKey());
		if(overridingMethods==null){
			overridingMethods= new HashSet<IMethodBinding>();
		}
		overridingMethods.add(overridingMethod);
		overriddenToOverridingMap.put(overriddenMethod.getKey(), overridingMethods);
	}
	
	public Set<IMethodBinding> getOverridingMethods(IMethodBinding overriddenMethod){
		return overriddenToOverridingMap.get(overriddenMethod.getKey());
	}
	
	public void addOverridenMethods(IMethodBinding overriddenMethod){
		this.overriddenMethods.add(overriddenMethod);
	}
	
	public Set<IMethodBinding> getOverridenMethods(){
		return this.overriddenMethods;
	}
	
	public void addOverriddenMethodParameters(String overriddenMethBinding, List<IVariableBinding> params){
		this.overriddenToParametersMap.put(overriddenMethBinding, params);
	}
	
	public List<IVariableBinding> getOverriddenMethodParameters(String overriddenMethBinding){
		return this.overriddenToParametersMap.get(overriddenMethBinding);
	}
	
	public void addOverridingMethodParams(IMethodBinding overriddenMethod, List<IVariableBinding> overridingParamList){
		Set<List<IVariableBinding>> paramLists = overriddenToOverridingParametersMap.get(overriddenMethod.getKey());
		if(paramLists==null){
			paramLists = new HashSet<List<IVariableBinding>>();
		}
		paramLists.add(overridingParamList);
		overriddenToOverridingParametersMap.put(overriddenMethod.getKey(), paramLists);
	}
	
	public Set<List<IVariableBinding>> getOverridingMethodsParams(IMethodBinding overriddenMethod){
		return overriddenToOverridingParametersMap.get(overriddenMethod.getKey());
	}
	
	public void addNPDModifier(String nPDModifier){
		this.nPDModifiers.add(nPDModifier);
	}
	
	public Set<String> getnPDModifiers(){
		return this.nPDModifiers;
	}
	
	public void clearNPDModifiers(){
		this.nPDModifiers.clear();
	}
	
	public boolean isTempVariableFinal(Variable tempVariable){
		return tempToFinalVars.get(tempVariable)!=null;
	}
	
	public void putfinalTempVariable(Variable tempVariable, Variable finalVariable){
		tempToFinalVars.put(tempVariable, finalVariable);
	}
	
	public Variable getAllBindingKeyToVariableMap(String binding){
		return this.allBindingKeyToVariableMap.get(binding);
	}
	
	public void putAllBindingKeyToVariableMap(String binding, Variable variable){
		this.allBindingKeyToVariableMap.put(binding, variable);
	}
	
	public ArrayList<TypePair> getPFMapping(TypePair typePiar) {
		return typeToFormalParameterMap.get(typePiar);
	}

	public void putPFMapping(TypePair typePiar, ArrayList<TypePair> typePairList) {
		typeToFormalParameterMap.put(typePiar, typePairList);
	}
	
	public boolean isAllowed() {
    	return isAllowed;
    }

	public HashMap<TypePair, ArrayList<TypePair>> getPF() {
		return typeToFormalParameterMap;
	}

	public Map<String,String> getClassToFields(String c) {
		return classToFields.get(c);
	}

	public void setClassToFields(String className, Map<String,String> fieldAndType) {
		this.classToFields.put(className, fieldAndType);
	}
	
	public Map<String, Map<String, String>> getClassToFields(){
		return classToFields;
	}
	
	public String getSuperType(String typeName){
		return typeToSuperType.get(typeName);
	}

	public void setSuperType(String typeName, String superTypeName){
		typeToSuperType.put(typeName, superTypeName);
	}
	
	public Map<String,String> getSuperTypeMap(){
		return typeToSuperType;
	}

	public boolean isAMethodCalled(IMethodBinding method) {
		return calledMethodSet.contains(method);
	}

	public void addACalledMethod(IMethodBinding method) {
		this.calledMethodSet.add(method);
	}
	
	public void setSelectedTyping(String type, String owner, String alpha){
		this.selectedTyping = new OType(type, owner, alpha);
	}
	
	// NOTE: OType is immutable; so the callers cannot modify the object
	public OType getSelectedTyping(){
		return this.selectedTyping;
	}

	public void setPC(Map<IObject, HashSet<IObject>> pC) {
    	PC = pC;
    }
	
	public boolean isRefined() {
		return isRefined;
	}

	public void setRefined(boolean isRefined) {
		this.isRefined = isRefined;
	}
	
	public void addEncapsulatedVariable(Variable var){
		this.encapsulatedVars.add(var);
	}
	
	public void removeEncapsulatedVariable(Variable var){
		this.encapsulatedVars.remove(var);
	}
	
	public Set<Variable> getEncapsulatedVars(){
		return this.encapsulatedVars;
	}
	
	public void addLogicalPartVariable(Variable var){
		this.loigcalPartVars.add(var);
	}
	
	public Set<Variable> getLogicalPartVars(){
		return this.loigcalPartVars;
	}
	
	public void removeLogicalPartVariable(Variable var){
		this.loigcalPartVars.remove(var);
	}
	
	public boolean isHeuristicOwnedRan() {
		return isHeuristicOwnedRan;
	}

	public void setHeuristicOwnedRan(boolean isHeuristicOwnedRan) {
		this.isHeuristicOwnedRan = isHeuristicOwnedRan;
	}

	public boolean isHeuristicPDRan() {
		return isHeuristicPDRan;
	}

	public void setHeuristicOwnerRan(boolean isHeuristicOwnerRan) {
		this.isHeuristicOwnerRan = isHeuristicOwnerRan;
	}
	
	public boolean isHeuristicOwnerRan() {
		return isHeuristicOwnerRan;
	}

	public void setHeuristicPDRan(boolean isHeuristicPDRan) {
		this.isHeuristicPDRan = isHeuristicPDRan;
	}
	
	public List<String> getClassPublicMethods(String qualifiedName){
		return this.classToPublicMethods.get(qualifiedName);
	}

	public void setClassPublicMethods(String qualifiedName, List<String> methodNames){
		this.classToPublicMethods.put(qualifiedName, methodNames);
	}
	
	public OType getMainDefaultTyping() {
		return mainDefaultTyping;
	}

	public void setMainDefaultTyping(String owner, String alpha) {
		this.mainDefaultTyping = new OType("",owner,alpha);
	}
	
	public void initializeFinalFieldsList(String className){
		Map<String,Set<String>> finalFields = new HashMap<String, Set<String>>();
		classToFinalFields.put(className, finalFields);
	}
	
	public Map<String,Set<String>> getFinalFields(String className){
		return classToFinalFields.get(className);
	}
	
	public void addAFinalField(String className, String fieldType, String fieldName){
		Set<String> identifiersSet = new HashSet<String>();
		if(classToFinalFields.get(className).get(fieldType)!=null){
			identifiersSet = classToFinalFields.get(className).get(fieldType);
		}
		identifiersSet.add(fieldName);
		Map<String,Set<String>> finalFields = new HashMap<String, Set<String>>();
		finalFields = classToFinalFields.get(className);
		finalFields.put(fieldType,identifiersSet);
		classToFinalFields.put(className, finalFields);
	}
	
	public boolean hasFinalField(String className){
		boolean result = false;
		if(classToFinalFields.get(className)!= null && classToFinalFields.get(className).size()>0){
			result = true;
		}
		return result;
	}
	
	public boolean isfinal(String className, String fieldType, String fieldName){
		boolean result = false;
		Map<String,Set<String>> finalFieldsOfClass = null;
		finalFieldsOfClass = classToFinalFields.get(className);
		Set<String> finalVarsOfType = new HashSet<String>();
		// XXX. TOEBI: Clean this up. NPE.
		if(finalFieldsOfClass!=null){
			finalVarsOfType = finalFieldsOfClass.get(fieldType);
			if(finalVarsOfType!=null && finalVarsOfType.contains(fieldName)){
				result = true;
			}
		}
		return result;
	}
	
	public void initializeRcvsAsFinFlds(String className){
		Map<String,String> finalFieldsMap = new HashMap<String,String>();
		receiversAsFinalFields.put(className, finalFieldsMap);
	}
	
	public Map<String,String> getRcvsAsFinFlds(String className){
		Map<String,String> finalFields = new HashMap<String,String>();
		finalFields = receiversAsFinalFields.get(className);
		return finalFields;
	}
	
	public void addRcvsAsFinFlds(String className, String fieldType, String fieldName){
		Map<String,String> finalFields = new HashMap<String,String>();
		finalFields = receiversAsFinalFields.get(className);
		finalFields.put(fieldType, fieldName);
		receiversAsFinalFields.put(className, finalFields);
	}
	
	//Do the same as the previous method but for a set (intermediate methods in adaptation process)
	//XXX. should I remove old oTypes form new set? No, because for the next refinements we need those typings.
	public Set<OType> substRcvInSet(Set<OType> typingSet, String receiver){
		StringBuilder typing = new StringBuilder();
		typing.append(receiver);
		typing.append(".");
		typing.append("PD");
		Set<OType> newTypingSet = new SetOType<OType>();
		newTypingSet.addAll(typingSet);
		for (OType oType : typingSet) {
			if(oType.getOwner().contains("PD") && oType.getAlpha().contains("PD")){
				OType newTyping = new OType(typing.toString(),typing.toString());
				newTypingSet.add(newTyping);
			}
			else if(oType.getOwner().contains("PD") && !oType.getAlpha().contains("PD")){
				OType newTyping = new OType(typing.toString(),oType.getAlpha());
				newTypingSet.add(newTyping);
			}
			else if(!oType.getOwner().contains("PD") && oType.getAlpha().contains("PD")){
				OType newTyping = new OType(oType.getOwner(),typing.toString());
				newTypingSet.add(newTyping);
			}
		}
		return newTypingSet;
	}
	
	public boolean constainsAny(Set<OType> typingSet){
		boolean result = false;
		for (OType oType : typingSet) {
			if(oType.getOwner().contains("any") || oType.getAlpha().contains("any")){
				result = true;
			}
		}
		return result;
	}
	
	public String getCurrentReceiverName() {
		return currentReceiverName;
	}

	public void setCurrentReceiverName(String currentRcvName) {
		this.currentReceiverName = currentRcvName;
	}
	
	// Reset all the state
	// XXX. This is not getting called!
	public void reset() {
		this.selectedTyping = null;
		this.typeToFormalParameterMap.clear();
		this.PC.clear();
		this.classToFields.clear();
		this.typeToSuperType.clear();
		this.calledMethodSet.clear();
		this.isAllowed = true;
	}

	public List<BaseOperation> getCompletedOprs() {
		return completedOprs;
	}
	
	public boolean addCompletedOperation(BaseOperation opr) {
		return completedOprs.add(opr);
	}
	
	public List<BaseOperation> getMoreInfoNeededRefs() {
		return moreInfoNeededRefs;
	}
	
	public boolean addMoreInfoNeededRefinement(BaseOperation opr) {
		return moreInfoNeededRefs.add(opr);
	}
	
	/**
	 * gets a method declaration and generates its signature and returns it as a string
	 * @param methodDeclaration
	 * @return
	 * XXX. Make consistent with the other version
	 */
	public String getSignature(MethodDeclaration methodDeclaration) {
		return getSignature(methodDeclaration.resolveBinding());
	}
	
	// R::C::m(C1...Cn)
	// XXX. Add return type
	// XXX. Add class names to avoid clashes between: C1::m and C2::m
	public String getSignature(IMethodBinding methodBinding) {
		StringBuilder builder = new StringBuilder();
		// Cannot use this. Due to inheritance.
		// builder.append(methodBinding.getDeclaringClass().getQualifiedName());
		// builder.append("::");
		builder.append(methodBinding.getName());
		builder.append(".");
		ITypeBinding[] parameters = methodBinding.getParameterTypes();
		for (ITypeBinding typeBinding : parameters) {
			builder.append(typeBinding.getQualifiedName());
			builder.append(".");
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
	
	/**
	 * Given a variable, if it a s field, return the name of the declaring/enclosing class.  
	 * 
	 * If this returns NULL, it is NOT a field. So don't try to look it up as such...
	 * @param variable
	 * @return
	 */
	public String getFieldEnclosingType(Variable variable) {
		String fieldEnclosingType = "";

		if (variable instanceof SourceVariable) {
			SourceVariable srcReceiver = (SourceVariable) variable;
			IVariableBinding varBinding = srcReceiver.getBinding();

			if (varBinding.isField()) {
				ITypeBinding declaringClass = varBinding.getDeclaringClass();

				if (declaringClass != null) {
					fieldEnclosingType = declaringClass.getQualifiedName();
				}
				else {
					int debug = 0;
					debug++;
				}
			}
			// It's a local variable, then it does not have a declaring class
			else {
				int debug = 0;
				debug++; // XXX. TOEBI: Set breakpoint here

				return getVariableEnclosingClass(varBinding);
			}
		}
		// When accessing an inherited field from a superclass, gotta find its declaring class
		// Seems like there is no way to do this in TAC; gotta go down to AST
		else if (variable instanceof TempVariable) {
			TempVariable tmp = (TempVariable) variable;

			ASTNode node2 = tmp.getNode();
			if (node2 instanceof SimpleName) {
				SimpleName sName = (SimpleName) node2;
				IBinding resolveBinding = sName.resolveBinding();
				if (resolveBinding instanceof IVariableBinding) {
					IVariableBinding varBinding = (IVariableBinding) resolveBinding;
					if (varBinding.isField()) {
						ITypeBinding declaringClass = varBinding.getDeclaringClass();
						if (declaringClass != null) {
							fieldEnclosingType = declaringClass.getQualifiedName();
						}
						else {
							int debug = 0;
							debug++;
						}
					}
					// It's a local variable...
					else {
						int debug = 0;
						debug++; // XXX. TOEBI: Set breakpoint here

						return getVariableEnclosingClass(varBinding);
					}
				}
			}
			else if (node2 instanceof CastExpression) {
				CastExpression castExpr = (CastExpression) node2;
				Expression expression = castExpr.getExpression();
				if (expression instanceof SimpleName) {
					SimpleName sName = (SimpleName) expression;
					IBinding resolveBinding = sName.resolveBinding();
					if (resolveBinding instanceof IVariableBinding) {
						IVariableBinding varBinding = (IVariableBinding) resolveBinding;
						if (varBinding.isField()) {
							ITypeBinding declaringClass = varBinding.getDeclaringClass();
							if (declaringClass != null) {
								fieldEnclosingType = declaringClass.getQualifiedName();
							}
							else {
								int debug = 0;
								debug++;
							}
						}
						// It's a local variable...
						else {
							int debug = 0;
							debug++; // XXX. TOEBI: Set breakpoint here

							return getVariableEnclosingClass(varBinding);
						}
					}
				}
			}
			else if (node2 instanceof FieldAccess) {
				FieldAccess feildAccessExpr = (FieldAccess) node2;
				IVariableBinding fieldBinding = feildAccessExpr.resolveFieldBinding();
				fieldEnclosingType = fieldBinding.getDeclaringClass().getQualifiedName();
			}
		}
		return fieldEnclosingType;
	}
	
	public String getVariableEnclosingClass(IVariableBinding varBinding) {
		String type = "";

		IMethodBinding declaringMethod = varBinding.getDeclaringMethod();
		if (declaringMethod != null) {
			ITypeBinding declaringClass = declaringMethod.getDeclaringClass();
			if (declaringClass != null) {
				type = declaringClass.getQualifiedName();
			}
		}

		return type;
	}
}
