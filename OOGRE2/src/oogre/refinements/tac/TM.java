package oogre.refinements.tac;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oogre.analysis.Config;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * Encapsulate the TM into a separate object:
 * - records the commands to do proper rollbacks
 * 
 * XXX. Encapsulate the HashMap completely..
 * 
 * XXX. Encapsulate the set too
 * 
 * XXX. Create a separate class for Set<OType>
 * 
 * XXX. Make TM an inner class of OOGContext
 */
public class TM {
	
	public static final TMKey KEY_INITIAL = new TMKey(TMSolutionType.INITIAL, 0);
	
	// Stage at which TM may get discarded
	public enum TMPhase {InitialRunTFs, CheckEqualityConstraints, TypeCheck, RespectPreviousRefs, FindNextTM}
	
	// XXX. Avoid exposing the Set<OType> too!
	/**
	 * Map of Variable to AnalysisResult: Variables here consists of ones provided by TAC.
	 */
	// XXX. To avoid iterating over typeMapping, may need to maintain an "index"
	//  String (BindingKey) -> Variable
	private Map<Variable, Set<OType>> typeMapping = new HashMap<Variable, Set<OType>>();
	
	// A mapping from String (BindingKey) -> Variable to look up parameters of a method as variables
	private Map<String, Variable> bindingKeyToVariable = new HashMap<String, Variable>();
	
	// XXX. Move this elsewhere since this need not be copied...
	// A map from an IBinding to corresponding synthetic TAC variable for FIELDs and RETURNS only
	// IVariableBinding -> Field Variable
	// IMethodBinding -> Method Return Variable
	private Map<String, Variable> varBindingMap = new HashMap<String, Variable>();
	
	// Set if this TM leads to RefinementUnsupportedException
	public boolean isDiscarded = false;
	
	public boolean moreInfoNeeded = false;
	
	private TMKey key;

	public String discardReason;
	
	// Store the .toString() of the expression (ASTNode or TAC instruction) that lead to TM being discarded
	public String discardExpression;
	
	public TMPhase discardPhase;
	
//	private static List<TM> allTMs = new ArrayList<TM>();
//	
//	public static List<TM> getAllTMs() {
//    	return allTMs;
//    }

	private TM() {
	    super();
	    
//	    TM.allTMs.add(this);
	    this.discardPhase = TMPhase.InitialRunTFs;
    }
	
	public TM(TMKey key) {
		this();
		this.key = key;
	}
	
	public TMKey getKey() {
		return key;
	}


	public TM(TMKey key, Map<Variable, Set<OType>> typeMapping) {
		this(key);
	    this.typeMapping = typeMapping;
    }

	public String getBindingKey(Variable v) {
		if(v instanceof SourceVariable) {
			return ((SourceVariable)v).getBinding().getKey();
		}
		else if (v instanceof TACVariable) {
			return ((TACVariable)v).getVarDecl().getKey();
		}
		else if (v instanceof TACMethod) {
			return ((TACMethod)v).getMethDecl().getKey();
		}
		
		return null;
	}
	
	/**
	 * Returns a mutable set!
	 * @param au
	 * @return
	 */
	public Set<OType> getTypeMapping(Variable variable) {
		Set<OType> qualifiersSet = typeMapping.get(variable);
		
//		boolean containsKey = typeMapping.containsKey(variable);
//		if(!containsKey) {
//			for (Entry<Variable, Set<OType>> entry : typeMapping.entrySet()) {
//				if (getBindingKey(entry.getKey()).equals(getBindingKey(variable))) {
//					analysisResult = entry.getValue();
//					break;
//				}
//			}
//		}

		return qualifiersSet;
	}
	
	public Set<OType> getReadOnlyTypeMapping(Variable variable) {
		Set<OType> qualifiersSet = new HashSet<OType>(typeMapping.get(variable));
		return Collections.unmodifiableSet(qualifiersSet);
	}
	
	public Set<OType> getAnalysisResult(Variable variable) {
		return typeMapping.get(variable);
	}
	
	public void putTypeMapping(Variable var, Set<OType> qualifiersSet) {
		typeMapping.put(var, qualifiersSet);
    }

	// Return read-only view to avoid modifying TM
	public TM getReadOnlyView(){
		Map<Variable, Set<OType>> readOnlyMap = (Map<Variable, Set<OType>>) Collections.unmodifiableMap(typeMapping);
		return new TM(getKey(), readOnlyMap);
	}
	
	public void clearUndoStack() {
		// XXX. No longer using the undo stack		
		// this.undoStack.clear();
	}

	/*
	 * Direct write to hashmap; no rollback!
	 * 
	 * putTM(x, S) should have the following semantics:
	 * S = TM[x] (read)
	 * It should be an update *inplace*, i.e., overwrite whatever the old entry for x was: 
	 * TM' = TM[x -> S]
	 * 
	 * NOT:
	 * TM' = TM[x -> (TM[x] \cup S)]
	 * 
	 * We use overwrite semantics; NOT union semantics
	 * 
	 */
/*	public void put(AnnotatableUnit au, Set<OType> newAUTypingSet) {
		Set<OType> setOTypes = typeMapping.get(au);
		if (setOTypes == null) {
			setOTypes = new Set<OType>();
			typeMapping.put(au, setOTypes);
		}
		// Important: Overwrite semantics; NOT union semantics
		setOTypes.clear(); 
		if(newAUTypingSet != null ) {
			setOTypes.addAll(newAUTypingSet);
		}
    }*/

	/*
	 * Direct write to hashmap; no rollback!
	 * Overwrite semantics
	 */
//	public void put(AnnotatableUnit au, Set<OType> newAUTypingSet) {
//		// Always overwrite!
//		Set<OType> setOTypes = new Set<OType>();
//		setOTypes.addAll(newAUTypingSet);
//		typeMapping.put(au, setOTypes);
//    }

	public void clear() {
		typeMapping.clear();
    }

	// XXX. NASTY smell: copying part of the object. 
	// XXX. Will this work for empty TM object!
	// TODO: Encapsulate: return TM object
	// XXX. Why using copy constructor!
	// XXX. Does it make sense to NOT copy the undo stack?
	// XXX. Rename: just copy()
	public TM copyTypeMapping(TMKey key) {
		if(this.isDiscarded) {
			int debug = 0; debug++;
		}
		TM copiedTM = new TM(key);
		for (Entry<Variable,Set<OType>> entry : typeMapping.entrySet()) {
			// NOTE: not copying temporaries. Is this OK?
			Variable var = entry.getKey();
			Set<OType> setOTypes = entry.getValue();
			Set<OType> copiedTypings = new HashSet<OType>();
			if(setOTypes != null ) {
				// XXX. Here, getting oType == null
				for (OType oType : setOTypes) {
					OType copiedOType = new OType(oType.getOwner(), oType.getAlpha(), oType.getInner());
					copiedTypings.add(copiedOType);
				}
			}
			copiedTM.putTypeMapping(var, copiedTypings);
		}
		// Copy variable bindings
//		for (Entry<IBinding, Variable> entry : varBindingMap.entrySet()) {
//			// NOTE: not copying temporaries. Is this OK?
//			IBinding bindingKey = entry.getKey();
//			Variable variable = entry.getValue();
//			copiedTM.varBindingMap.put(bindingKey, variable);
//		}
		
		copiedTM.varBindingMap = this.varBindingMap;
		copiedTM.bindingKeyToVariable = this.bindingKeyToVariable;
		
		return copiedTM;
	}

	// TODO: Encapsulate: take 2 TM objects
	// TODO: Convert to equal method on "this" object
	// Do not call it equals.
	// XXX. Gotta make this much more efficient... 
	public static boolean isEqual(TM oldTMo, TM newTMo) {
		// XXX. Why not use the underlying raw sets?
		// TM oldTM = oldTMo.getReadOnlyView();
		// TM newTM = newTMo.getReadOnlyView();
		if(oldTMo.getSize()!=newTMo.getSize()){
			return false;
		}
		
    	for (Entry<Variable,Set<OType>> oldEntry : oldTMo.entrySet()) {
    		Variable oldKey = oldEntry.getKey();
    		Set<OType> oldValue = oldEntry.getValue();
//    		for (Entry<AnnotatableUnit,Set<OType>> newEntry: newTM.entrySet()) {
//    			AnnotatableUnit newKey = newEntry.getKey();
//    			// XXX. Why not replace second set iteration with key lookup?!
//    			if(oldKey.equals(newKey)){
    				Set<OType> newValue = newTMo.getTypeMapping(oldKey);
    				//Set<OType> newValue = newEntry.getValue();
    				// NOTE: Relies on Set<OType>.equals(...)
    				if(!oldValue.equals(newValue)){
    					return false;
    				}
//    			}
//    		}
    	}
    	return true;
//		return oldTMo.equals(newTMo);
   }

	public int getSize() {
	    return typeMapping.size();
    }

	// This is read-only
	/**
	 * @deprecated Should avoid using this directly. 
	 * @return
	 */
	public Set<Variable> keySet() {
	    return typeMapping.keySet();
    }

	// This is read-only
	/**
	 * @deprecated Should avoid using this directly. 
	 * @return
	 */	
	public Set<Entry<Variable, Set<OType>>> entrySet() {
	    return typeMapping.entrySet();
    }
	
	@Override
	public String toString() {
		return "TM [typeMapping=" + typeMapping + ", isDiscarded="
				+ isDiscarded + ", key=" + key + "]";
	}

	/**
     * The goal is to pick the highest ranked typing for each AU.
     * This is the same strategy that SaveAnnotations uses.
     * 
     * @return a copy of this input TM, with modifications
     * 
     */
    public TM pickHighestRanked(RankingStrategy ranking){
    	TM copyTM = copyTypeMapping(getKey());
    	
    	for (Map.Entry<Variable, Set<OType>> tmEntry : copyTM.entrySet()) {
    //		for (Entry<AnnotatableUnit, Set<OType>> tmEntry : copyTM.entrySet()) {
    		// XXX. Which AU to pick? Any?! The first one? We may be picking an unimportant AU!
    		// How do we identify the *important* AUs?
    		// Maybe pick an AU that's either src or dst of refinement
    		// OR a type that occurs inside type of src or dst of refinement
    		Variable var = tmEntry.getKey();
    		Set<OType> setOTypes = tmEntry.getValue();
    		
    		if (setOTypes.size() == 1 ) {
    			continue;
    		}
    		// We should not get here...
    		if ( setOTypes.size() == 0 ) {
    			//throw new IllegalStateException("Trying to use a discarded TM. Fix the caller");
    		}
    		
    		if (setOTypes.size() > 1) {
    			// Pick the highest ranked typing
    			// XXX. Pass the previously attempted ones
    			OType selectedType = ranking.pickFromSet(setOTypes, null);
    			
    			if(selectedType != null ) {
    				// Commit to one typing for one more AU
    				setOTypes.clear();
    				setOTypes.add(selectedType);
    				// Modify the TM
    				copyTM.putTypeMapping(var, setOTypes);
    			}
    			else 
    				throw new RefinementUnsupportedException("");
    		}
    	}
    	
    	return copyTM;
    }


	
	// Implement value equality
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof TM)) {
			return false;
		}

		TM other = (TM) o;
		return (other.key.equals(this.key)) && (other.typeMapping.equals(this.typeMapping));
	}

	// Always override hashcode when you override equals
	@Override
    public int hashCode() {
		int result = 17;

		result = 37 * result + (key == null ? 0 : key.hashCode());
		// the hash code of a map is defined to be the sum of the hash codes of each entry in the map's entrySet
		result = 37 * result + (typeMapping == null ? 0 : typeMapping.hashCode());

		return result;
	}
    
    public boolean hasEmptySet() {
    	for (Entry<Variable, Set<OType>> entry : typeMapping.entrySet()) {
    		Variable var = entry.getKey();
			Set<OType> setOTypes = entry.getValue();
			if (setOTypes.isEmpty()) {
				return true;
			}
			
    	}
    	return false;
    }
    
    public boolean isInitial() {
    	return this.key.equals(KEY_INITIAL);
    }
    
    public Set<Variable> getVariables(){
    	return this.typeMapping.keySet();
    }
    
    public void putVarBindingMap(IBinding binding, Variable variable){
    	this.varBindingMap.put(binding.getKey(), variable);
    }
    
    public Variable getVarBindingMap(IBinding binding){
    	//XXX. for a variable of type of a TypeVariable, key is different, so it cannot find the variable
    	Variable variable = this.varBindingMap.get(binding.getKey());
//		if (variable == null) {
//			for (Entry<String, Variable> entry : varBindingMap.entrySet()) {
//				if (entry.getKey().equals(binding)) {
//					variable = entry.getValue();
//					break;
//				}
//			}
//		}
    	
    	return variable; 
    }
    
    public Variable getVariableFromBindingKey(String bindingKey){
    	return this.bindingKeyToVariable.get(bindingKey);
    }
    
    public void addBindingKeyToVariable(String bindingKey, Variable variable){
    	this.bindingKeyToVariable.put(bindingKey,variable);
    }

    public Set<OType> initTypeMapping(boolean isMain, Variable variable, boolean isLent, boolean isUnique) {
    	Set<OType> typeSet = new HashSet<OType>();
    	OType fdOType = null;

    	ITypeBinding typeName = variable.resolveType();


    	// XXX. Eventually, read the Manifest Ownership from a config file, etc.
    	// MANIFEST OD: for variables of type String we always have shared<shared>
    	if(typeName.getQualifiedName().equals("java.lang.String") ||
    	   typeName.getQualifiedName().equals("java.lang.Integer") ||
    	   typeName.getQualifiedName().equals("java.lang.Double") ||
    	   typeName.getQualifiedName().equals("java.lang.Float") ||
    	   typeName.getQualifiedName().equals("java.lang.Boolean") ||
    	   typeName.getQualifiedName().equals("java.lang.Character") ||
    	   typeName.getQualifiedName().equals("java.lang.Long") ||
    	   typeName.getQualifiedName().equals("java.lang.Short") ||
    	   typeName.getQualifiedName().equals("java.lang.Number") ||
    	   typeName.getQualifiedName().equals("java.lang.Byte") ||
    	   typeName.getQualifiedName().equals("java.awt.Point") ||
    	   typeName.getQualifiedName().equals("java.awt.Rectangle") ||
    	   typeName.getQualifiedName().equals("java.awt.Color") ||
    	   typeName.getQualifiedName().equals("java.awt.Dimension") ||
    	   typeName.getQualifiedName().equals("java.awt.event.KeyEvent") ||
    	   typeName.getQualifiedName().equals("java.awt.event.MouseEvent")){
    		fdOType = new OType("shared", "shared");
    		typeSet.add(fdOType);
    	}
    	else{
    		if(typeName.isTypeVariable()){
    			fdOType = new OType("p", null);
    			typeSet.add(fdOType);
    		}
    		else if(typeName.isParameterizedType() || (typeName.getSuperclass()!=null && typeName.getSuperclass().isParameterizedType())){
    			ITypeBinding[] typeArguments = null;
    			if(typeName.isParameterizedType()){
    				typeArguments = typeName.getTypeArguments();
    			}
    			else{
    				ITypeBinding superclass = typeName.getSuperclass();
    				typeArguments = superclass.getTypeArguments();
    			}
    			if(typeArguments.length>0){
    				if(typeArguments[0].isTypeVariable()){
    					// OT is turned off
    					if(!InferOptions.getInstance().inferOT()){
    						fdOType = new OType("this.owned","p");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.PD","p");
    						typeSet.add(fdOType);
    						fdOType = new OType("owner","p");
    						typeSet.add(fdOType);
    						fdOType = new OType("p","p");
    						typeSet.add(fdOType);
    						if(!InferOptions.getInstance().isTurnOFFLent()){
    							if(isLent){
    								fdOType = new OType("lent","p");
    								typeSet.add(fdOType);
    							}
    						}
    						if(!InferOptions.getInstance().isTurnOFFUnique()){
    							if(isUnique){
    								fdOType = new OType("unique","p");
    								typeSet.add(fdOType);
    							}
    						}
    					}
    					// OT is turned on
    					else{
    						fdOType = new OType("this.owned","p");
    						typeSet.add(fdOType);
    						fdOType = new OType("owner","p");
    						typeSet.add(fdOType);
    						fdOType = new OType("p","p");
    						typeSet.add(fdOType);
    					}
    				}
    				else{
    					if(!isMain){
    						// OT is turned off
    						if(!InferOptions.getInstance().inferOT()){
    							fdOType = new OType("this.owned", "this.owned", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.owned", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.owned", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.owned", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "owner", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "owner", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "owner", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "p", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "p", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "p", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "shared", "shared");
    							typeSet.add(fdOType);

    							//Then add 'PD'. 
    							fdOType = new OType("this.owned", "this.PD", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.PD", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.PD", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.PD", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "owner", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "p", "this.PD");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("this.owned", "shared", "this.PD");
    							//	    		typeSet.add(fdOType);

    							fdOType = new OType("owner", "this.owned", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "this.owned", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "this.owned", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "this.owned", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("owner", "owner", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "owner", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "owner", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "p", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "p", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "p", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("owner", "this.PD", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "this.PD", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "this.PD", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "this.PD", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "owner", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "p", "this.PD");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("owner", "shared", "this.PD");
    							//	    		typeSet.add(fdOType);

    							fdOType = new OType("p", "this.owned", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "this.owned", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "this.owned", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "this.owned", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("p", "owner", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "owner", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "owner", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "p", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "p", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "p", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("p", "this.PD", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "this.PD", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "this.PD", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "this.PD", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "owner", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "p", "this.PD");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("p", "shared", "this.PD");
    							//	    		typeSet.add(fdOType);

    							fdOType = new OType("shared", "this.owned", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.owned", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.owned", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.owned", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("shared", "owner", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "owner", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "owner", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "p", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "p", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "p", "shared");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("shared", "shared", "this.owned");
    							//	    		typeSet.add(fdOType);
    							//	    		fdOType = new OType("shared", "shared", "owner");
    							//	    		typeSet.add(fdOType);
    							//	    		fdOType = new OType("shared", "shared", "p");
    							//	    		typeSet.add(fdOType);
    							fdOType = new OType("shared", "shared", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("shared", "this.PD", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.PD", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.PD", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.PD", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "owner", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "p", "this.PD");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("shared", "shared", "this.PD");
    							//	    		typeSet.add(fdOType);


    							fdOType = new OType("this.PD", "this.PD", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "this.PD", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "this.PD", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "this.PD", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "owner", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "owner", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "owner", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "owner", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "p", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "p", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "p", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "p", "shared");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("this.PD", "shared", "this.PD");
    							//	    		typeSet.add(fdOType);
    							//	    		fdOType = new OType("this.PD", "shared", "owner");
    							//	    		typeSet.add(fdOType);
    							//	    		fdOType = new OType("this.PD", "shared", "p");
    							//	    		typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "shared", "shared");
    							typeSet.add(fdOType);
    							if(isLent){
    								fdOType = new OType("lent", "this.owned", "this.owned");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "this.owned", "this.PD");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "this.owned", "owner");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "this.owned", "p");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "this.owned", "shared");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "this.PD", "this.PD");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "this.PD", "owner");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "this.PD", "p");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "this.PD", "shared");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "owner", "this.PD");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "owner", "owner");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "owner", "p");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "owner", "shared");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "p", "this.PD");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "p", "owner");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "p", "p");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "p", "shared");
    								typeSet.add(fdOType);
    								fdOType = new OType("lent", "shared", "shared");
    								typeSet.add(fdOType);
    							}
    							if(isUnique){
    								fdOType = new OType("unique", "this.owned", "this.owned");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "this.owned", "this.PD");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "this.owned", "owner");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "this.owned", "p");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "this.owned", "shared");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "this.PD", "this.PD");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "this.PD", "owner");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "this.PD", "p");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "this.PD", "shared");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "owner", "this.PD");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "owner", "owner");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "owner", "p");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "owner", "shared");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "p", "this.PD");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "p", "owner");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "p", "p");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "p", "shared");
    								typeSet.add(fdOType);
    								fdOType = new OType("unique", "shared", "shared");
    								typeSet.add(fdOType);
    							}
    						}
    						// OT is turned on
    						else{
    							fdOType = new OType("this.owned", "this.owned", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.owned", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.owned", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.owned", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "owner", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "owner", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "owner", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "p", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "p", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "shared", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("owner", "owner", "owner");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "owner", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "owner", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "p", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("owner", "p", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("p", "p", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "p", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("p", "shared", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("shared", "shared", "shared");
    							typeSet.add(fdOType);
    						}
    					}
    					else{
    						// OT is turned off
    						if(!InferOptions.getInstance().inferOT()){
    							fdOType = new OType("this.owned", "this.owned", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.owned", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "shared", "shared");
    							typeSet.add(fdOType);

    							fdOType = new OType("this.owned", "this.PD", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.PD", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.PD", "shared");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("this.owned", "shared", "this.PD");
    							//	    		typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "this.PD", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.PD", "this.owned");
    							typeSet.add(fdOType);

    							fdOType = new OType("shared", "this.owned", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.owned", "shared");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("shared", "shared", "this.owned");
    							//	    		typeSet.add(fdOType);

    							fdOType = new OType("shared", "shared", "shared");
    							typeSet.add(fdOType);


    							fdOType = new OType("shared", "this.PD", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("shared", "this.PD", "shared");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType("shared", "shared", "this.PD");
    							//	    		typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "this.PD", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "this.PD", "shared");
    							typeSet.add(fdOType);
    							//	    		fdOType = new OType(fieldTypeName, "this.PD", "shared", "this.PD");
    							//	    		typeSet.add(fdOType);
    							fdOType = new OType("this.PD", "shared", "shared");
    							typeSet.add(fdOType);
    							if(!InferOptions.getInstance().isTurnOFFLent()){
    								if(isLent){
    									fdOType = new OType("lent", "this.owned", "this.owned");
    									typeSet.add(fdOType);
    									fdOType = new OType("lent", "this.owned", "this.PD");
    									typeSet.add(fdOType);
    									fdOType = new OType("lent", "this.owned", "shared");
    									typeSet.add(fdOType);
    									fdOType = new OType("lent", "this.PD", "this.owned");
    									typeSet.add(fdOType);
    									fdOType = new OType("lent", "this.PD", "this.PD");
    									typeSet.add(fdOType);
    									fdOType = new OType("lent", "this.PD", "shared");
    									typeSet.add(fdOType);
    									fdOType = new OType("lent", "shared", "shared");
    									typeSet.add(fdOType);
    								}
    							}
    							if(!InferOptions.getInstance().isTurnOFFUnique()){
    								if(isUnique){
    									fdOType = new OType("unique", "this.owned", "this.owned");
    									typeSet.add(fdOType);
    									fdOType = new OType("unique", "this.owned", "this.PD");
    									typeSet.add(fdOType);
    									fdOType = new OType("unique", "this.owned", "shared");
    									typeSet.add(fdOType);
    									fdOType = new OType("unique", "this.PD", "this.owned");
    									typeSet.add(fdOType);
    									fdOType = new OType("unique", "this.PD", "this.PD");
    									typeSet.add(fdOType);
    									fdOType = new OType("unique", "this.PD", "shared");
    									typeSet.add(fdOType);
    									fdOType = new OType("unique", "shared", "shared");
    									typeSet.add(fdOType);
    								}
    							}
    						}
    						// OT is turned on
    						else{
    							fdOType = new OType("this.owned", "this.owned", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "this.owned", "shared");
    							typeSet.add(fdOType);
    							fdOType = new OType("this.owned", "shared", "shared");
    							typeSet.add(fdOType);
    						}
    					}
    				}
    			}
    		}
    			else{
    				if(!isMain){
    					// OT is turned off
    					if(!InferOptions.getInstance().inferOT()){
    						fdOType = new OType("this.owned", "this.owned");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "this.PD");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "owner");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "p");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "shared");
    						typeSet.add(fdOType);

    						fdOType = new OType("owner", "owner");
    						typeSet.add(fdOType);
    						fdOType = new OType("owner", "p");
    						typeSet.add(fdOType);
    						fdOType = new OType("p", "p");
    						typeSet.add(fdOType);

    						// XXX. New set of valid typings
    						fdOType = new OType("p", "owner");
    						typeSet.add(fdOType);
    						// end New set of valid typings


    						fdOType = new OType("owner", "shared");
    						typeSet.add(fdOType);
    						fdOType = new OType("p", "shared");
    						typeSet.add(fdOType);
    						fdOType = new OType("shared", "shared");
    						typeSet.add(fdOType);

    						//Commented out to avoid starting with a big initial set of typings that containing everything.
    						//Idea: try to generate typings on-demand
    						fdOType = new OType("this.PD", "this.PD");
    						typeSet.add(fdOType);
    						// Commented out because lead to generation of lent in alpha
    						fdOType = new OType("this.PD", "owner");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.PD", "p");
    						typeSet.add(fdOType);
    						fdOType = new OType("owner", "this.PD");
    						typeSet.add(fdOType);
    						fdOType = new OType("p", "this.PD");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.PD", "shared");
    						typeSet.add(fdOType);
    						// fdOType = new OType("shared", "this.PD");
    						// typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.any", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.owned", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.PD", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("owner", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("p", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("shared", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.any", "this.PD");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.any", "owner");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.any", "p");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.any", "shared");
    						//	    	typeSet.add(fdOType);
    						if(isLent){
    							fdOType = new OType("lent", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("lent", "this.PD");
    							typeSet.add(fdOType);
    							//						fdOType = new OType("lent", "owner");
    							//						typeSet.add(fdOType);
    							fdOType = new OType("lent", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("lent", "shared");
    							typeSet.add(fdOType);
    						}
    						if(isUnique){
    							fdOType = new OType("unique", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("unique", "this.PD");
    							typeSet.add(fdOType);
    							//						fdOType = new OType("unique", "owner");
    							//						typeSet.add(fdOType);
    							fdOType = new OType("unique", "p");
    							typeSet.add(fdOType);
    							fdOType = new OType("unique", "shared");
    							typeSet.add(fdOType);
    						}
    					}
    					// OT is turned on
    					else{
    						fdOType = new OType("this.owned", "this.owned");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "owner");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "p");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "shared");
    						typeSet.add(fdOType);

    						fdOType = new OType("owner", "owner");
    						typeSet.add(fdOType);
    						fdOType = new OType("owner", "p");
    						typeSet.add(fdOType);
    						fdOType = new OType("owner", "shared");
    						typeSet.add(fdOType);

    						fdOType = new OType("p", "p");
    						typeSet.add(fdOType);
    						fdOType = new OType("p", "shared");
    						typeSet.add(fdOType);
    						fdOType = new OType("shared", "shared");
    						typeSet.add(fdOType);
    					}
    				}
    				else{
    					// OT is turned off
    					if(!InferOptions.getInstance().inferOT()){
    						fdOType = new OType("this.owned", "this.owned");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "this.PD");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.PD", "this.owned");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "shared");
    						typeSet.add(fdOType);

    						fdOType = new OType("shared", "shared");
    						typeSet.add(fdOType);

    						//Commented out to avoid starting with a big initial set of typings that containing everything.
    						//Idea: try to generate typings on-demand
    						//	    			fdOType = new OType("this.PD", "this.owned");
    						//	    			typeSet.add(fdOType);
    						fdOType = new OType("this.PD", "this.PD");
    						typeSet.add(fdOType);
    						// fdOType = new OType("shared", "this.PD");
    						// typeSet.add(fdOType);
    						fdOType = new OType("this.PD", "shared");
    						typeSet.add(fdOType);
    						//	    	
    						//	    	fdOType = new OType("this.any", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.owned", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.PD", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("shared", "this.any");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.any", "this.PD");
    						//	    	typeSet.add(fdOType);
    						//	    	fdOType = new OType("this.any", "shared");
    						//	    	typeSet.add(fdOType);
    						if(isLent){
    							fdOType = new OType("lent", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("lent", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("lent", "shared");
    							typeSet.add(fdOType);
    						}
    						if(isUnique){
    							fdOType = new OType("unique", "this.owned");
    							typeSet.add(fdOType);
    							fdOType = new OType("unique", "this.PD");
    							typeSet.add(fdOType);
    							fdOType = new OType("unique", "shared");
    							typeSet.add(fdOType);
    						}
    					}
    					// OT is turned on
    					else{
    						fdOType = new OType("this.owned", "this.owned");
    						typeSet.add(fdOType);
    						fdOType = new OType("this.owned", "shared");
    						typeSet.add(fdOType);
    						fdOType = new OType("shared", "shared");
    						typeSet.add(fdOType);
    					}
    				}
    			}
    	}
    	return typeSet;
    }

    /**
     * A method to initialize the set of qualifiers for target variables of a parameterized type
     * based on different solution indexes
     * The set is not singleton: for solution index solIndex, the set contains <solIndex, X> and X is all the possible valid modifiers  
     * @param owningDomain: The owning domain of the object of a parameterized type, this is determined by refinement or heuristic objects
     * @param isMain: To indicate if enclosing class in the Main class
     * @param solAlpha: Solution index
     * @return
     */
	public Set<OType> initParametrizedTypeMapping(String owningDomain, boolean isMain) {
		Set<OType> typeMapping = new HashSet<OType>();
		OType oType = null;
		if(isMain){
			oType = new OType(owningDomain, "this.owned", "this.owned");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.owned", "this.PD");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.owned", "shared");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD", "this.owned");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD", "this.PD");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD", "shared");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "shared", "shared");
			typeMapping.add(oType);
		}
		else{
			oType = new OType(owningDomain, "this.owned", "this.owned");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.owned", "this.PD");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.owned", "owner");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.owned", "p");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.owned", "shared");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD", "this.PD");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD", "owner");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD", "p");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD", "shared");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "owner", "this.PD");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "owner", "owner");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "owner", "p");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "owner", "shared");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "p", "this.PD");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "p", "owner");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "p", "p");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "p", "shared");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "shared", "shared");
			typeMapping.add(oType);
		}
		return typeMapping;
	}
	
	public Set<OType> initRespectTypeMapping(String owningDomain, boolean isMain) {
		Set<OType> typeMapping = new HashSet<OType>();
		OType oType = null;
		if(isMain){
			oType = new OType(owningDomain, "this.owned");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "shared");
			typeMapping.add(oType);
		}
		else{
			oType = new OType(owningDomain, "this.owned");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "this.PD");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "owner");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "p");
			typeMapping.add(oType);
			oType = new OType(owningDomain, "shared");
			typeMapping.add(oType);
		}
		return typeMapping;
	}

	/**
	 * Initialize 'this' with the singleton set {<owner,p>} 
	 * @return
	 */
	public Set<OType> initThisVariable() {
		Set<OType> thisVarSet = new HashSet<OType>();
		OType ownerP = new OType("owner","p");
		thisVarSet.add(ownerP);
		ownerP = new OType("this.owned","p");
		thisVarSet.add(ownerP);
		return thisVarSet;
	}
}
