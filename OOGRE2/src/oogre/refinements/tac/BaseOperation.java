package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import oog.re.IOperation;
import oog.re.RankedTypings;
import oog.re.RefinementState;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public abstract class BaseOperation implements IOperation {
	// TODO: Make this immutable
	// Avoid keeping this a null. The UI may display it. Can we auto-populate this with a counter?
	protected String refID = "";
		
	private RefinementState state;
	
	// LOW. Rename: to match getter/setter
	protected oog.re.IOperation facadeOperation;

	// XXX. rename: getOperationType
	public abstract RefinementType getRefinementType();

	// XXX. Why do we have BaseOperation maintain a list of TAC Variables?
	// In order to respect previous refinements...
	// XXX. If we move to a stateless model, where we change the code and re-run the analysis, and re-apply the refinements
	// these Src Variable objects may get recycled....
	protected Map<Variable,String> vars = new HashMap<Variable, String>();
	
	protected Map<Variable,Set<OType>> varsSets = new HashMap<Variable, Set<OType>>();
	
	// Auto-refinements
	private boolean isImplicit = false;

	/**
	 * For storing metrics data; do not save
	 */
	protected String data = null;
	
	/**
     *  Refinement sets exactly 1 typing. 
     *  The TFs do NOT add more since they only kill invalid typings.
     */
    // protected Map<TMSolutionType, OType> newTyping = new HashMap<TMSolutionType, OType>();
    
	public BaseOperation() {
	    super();
    }

	public String getRefID() {
    	return this.refID;
    }

	public void setRefID(String refID) {
    	this.refID = refID;
    }
	
	@Override
    public RefinementState getState() {
    	if (facadeOperation != null ) {
    		state = facadeOperation.getState();
    	}
    	return state;
    }

	public void setState(RefinementState state) {
    	if(facadeOperation != null ) {
    		facadeOperation.setState(state);
    	}
    	this.state = state;
    }

    public void markCompleted() {
    	if(facadeOperation != null ) {
    		facadeOperation.setState(RefinementState.Completed);
    	}
    }
    
    public void markUnsupported() {
    	if(facadeOperation != null ) {
    		facadeOperation.setState(RefinementState.Unsupported);
    	}
    }

	public oog.re.IOperation getFacadeOpr() {
    	return facadeOperation;
    }

	public void setFacadeOpr(oog.re.IOperation facadeOpr) {
    	this.facadeOperation = facadeOpr;
    }
	

	@Override
    public RankedTypings getRankedTypings() {
    	if(facadeOperation != null ) {
    		return facadeOperation.getRankedTypings();
    	}
	    return null;
    }

	public void putVariableMap(Variable var, String owner) {
    	this.vars.put(var, owner);
    }

	
	public String getOprOwner(Variable var) {
    	return this.vars.get(var);
    }
	
	public Set<Variable> getVars() {
    	return vars.keySet();
    }
	
	public void putVariableSetMap(Variable var, Set<OType> set) {
    	this.varsSets.put(var, set);
    }

	
	public Set<OType> getVarSet(Variable var) {
    	return this.varsSets.get(var);
    }

//	public OType getNewTyping(TMSolutionType solIndex) {
//    	return newTyping.get(solIndex);
//    }

	/**
	 * @deprecated TOEBI: Stop calling this.
	 */
	public void setNewTyping(TMSolutionType solIndex, OType newTyping) {
    	// this.newTyping.put(solIndex, newTyping);
    }

//	public String getOwner(TMSolutionType solIndex) {
//    	return newTyping.get(solIndex).getOwner();
//    }
//
//	public String getAlpha(TMSolutionType solIndex) {
//    	return newTyping.get(solIndex).getAlpha();
//    }

	public void setData(String data) {
		this.data = data;
    }
	
	public String getData() {
		return this.data;
    }

	public boolean isImplicit() {
    	return isImplicit;
    }

	public void setImplicit(boolean isImplicit) {
    	this.isImplicit = isImplicit;
    }
	
	public void setMoreInformation(TM tm, Set<Variable> auSet, String conflictInstruction, ArrayList<String> suggestedRefinements) {
		// XXX. Can facadeOperation be null?
		RankedTypings rankedTypings = facadeOperation.getRankedTypings();
		
		// Add the conflict expression
		ArrayList<String> confInstrList = new ArrayList<String>();
		confInstrList.add(conflictInstruction);
		rankedTypings.addMapEntry("The conflict Instruction", confInstrList);
		
		for(Variable var : auSet) {
			
			// TODO: Do instanceof Variable...
			// TACMethod, TACVariable (Field), TACNewExpr
			// Crystal Locals/params
			// enough to distinguish just 2 cases: Field or Local/Param
			// since local and param cannot have the same name;
			// but field and param can have the same name
			Set<OType> auTypings = tm.getReadOnlyTypeMapping(var);
			// TOEBI: Override AU.toString();
			StringBuilder varString = new StringBuilder();
			if(var instanceof TACVariable){
				IVariableBinding varBinding = ((TACVariable) var).getVarDecl();
				ITypeBinding declaringClass = varBinding.getDeclaringClass();
				varString.append("Cls: ");
				varString.append(declaringClass.getName());
				varString.append("; Fld: ");
				varString.append(var.getSourceString());
			}
			else if(var instanceof TACMethod){
				IMethodBinding methDecl = ((TACMethod) var).getMethDecl();
				ITypeBinding declaringClass = methDecl.getDeclaringClass();
				varString.append("Cls: ");
				varString.append(declaringClass.getName());
				varString.append("; Mth: ");
				varString.append(var.getSourceString());
			}
			else if(var instanceof SourceVariable){
				SourceVariable srcVar = (SourceVariable)var;
				IVariableBinding srcVarBinding = srcVar.getBinding();
				
				if(srcVarBinding.isParameter()){
					IMethodBinding declMethod = srcVarBinding.getDeclaringMethod();
					ITypeBinding declaringClass = declMethod.getDeclaringClass();
					varString.append("Cls: ");
					varString.append(declaringClass.getName());
					varString.append("; Mth: ");
					varString.append(declMethod.getName());
					varString.append("; Param: ");
				}
				else{
					IMethodBinding declMethod = srcVarBinding.getDeclaringMethod();
					ITypeBinding declaringClass = declMethod.getDeclaringClass();
					varString.append("Cls: ");
					varString.append(declaringClass.getName());
					varString.append("; Mth: ");
					varString.append(declMethod.getName());
					varString.append("; Var: ");
				}
				varString.append(var.getSourceString());
			}
			else{
				// TODO: Can we show more context here?
				varString.append("Temporary Variable: ");
				varString.append(var.getSourceString());
			}
			
			// TOEBI: Override OType.toString();
			ArrayList<String> typings = new ArrayList<String>();
			for(OType auTyping : auTypings ) {
				typings.add(auTyping.toString());
			}
			
			rankedTypings.addMapEntry(varString.toString(), typings);
		}
		
		// Add the suggested refinements
		rankedTypings.addMapEntry("The Prioritized suggested refinements", suggestedRefinements);
    }

	// Return the variables touched by a refinement
	public Collection<Variable> getAllVars() {
		return vars.keySet();
	}
}
