package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.Set;

import edu.cmu.cs.crystal.tac.model.Variable;

public class MoreInformationNeededException extends
		RefinementUnsupportedException {

	private static final long serialVersionUID = 2795963059096348671L;

	private Set<Variable> auSet;
	
	private String conflictInstruction;
	
	private ArrayList<String> suggestedRefinements = new ArrayList<String>();
	
	public MoreInformationNeededException(String message, Set<Variable> units, String conflictInstruction, ArrayList<String> suggestedRefinements) {
		super(message);
		
		this.auSet = units;
		this.conflictInstruction = conflictInstruction;
		this.suggestedRefinements = suggestedRefinements;
	}
	
	public static String createMessage(Variable au) {
		StringBuilder builder = new StringBuilder();
		builder.append("The analysis needs more information about ");
		builder.append(au.getSourceString());
		builder.append(" of type ");
		builder.append(au.resolveType().getQualifiedName());
		// XXX. Fix me
		// builder.append(" in the class ");
		// builder.append(au.getEnclosingType());

		return builder.toString();
	}
	
	// XXX. No longer being used
	public static MoreInformationNeededException create(Set<Variable> auSet) {
		String createMessage = ""; // createMessage(au);
		
		// XXX. TOEBI: Mismatch: is it one AnnotatableUnit or a  set?
		return new MoreInformationNeededException(createMessage, auSet, null, null);
	}	

	public Set<Variable> getAuSet() {
    	return auSet;
    }
	
	public String getConflictInstruction(){
		return conflictInstruction;
	}
	
	public ArrayList<String> getSuggestedRefinements(){
		return this.suggestedRefinements;
	}
}

