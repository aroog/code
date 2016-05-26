package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.crystal.tac.model.Variable;

import oog.re.IOperation;
import oog.re.RankedTypings;
import oog.re.RefinementState;

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
	protected Map<Variable,Set<OType>> vars = new HashMap<Variable, Set<OType>>();
	
	private boolean isImplicit = false;

	/**
	 * For storing metrics data; do not save
	 */
	protected String data = null;
	
	/**
     *  Refinement sets exactly 1 typing. 
     *  The TFs do NOT add more since they only kill invalid typings.
     */
    protected Map<TMSolutionType, OType> newTyping = new HashMap<TMSolutionType, OType>();
    
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

	public void putVariableMap(Variable var, Set<OType> qSet) {
    	this.vars.put(var, qSet);
    }

	
	public Set<OType> getVariableSet(Variable var) {
    	return this.vars.get(var);
    }
	
	public Set<Variable> getVars() {
    	return vars.keySet();
    }

	public OType getNewTyping(TMSolutionType solIndex) {
    	return newTyping.get(solIndex);
    }

	public void setNewTyping(TMSolutionType solIndex, OType newTyping) {
    	this.newTyping.put(solIndex, newTyping);
    }

	public String getOwner(TMSolutionType solIndex) {
    	return newTyping.get(solIndex).getOwner();
    }

	public String getAlpha(TMSolutionType solIndex) {
    	return newTyping.get(solIndex).getAlpha();
    }

	public void setData(String data) {
		this.data = data;
    }
	
	public String getData() {
		return this.data;
    }

	public boolean isImplicit() {
    	return isImplicit;
    }

	public void setInferred(boolean isInferred) {
    	this.isImplicit = isInferred;
    }
	
	

}
