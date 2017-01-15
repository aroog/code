package oogre.annotations;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import oogre.refinements.tac.MoreInformationNeededException;
import oogre.refinements.tac.OType;
import oogre.refinements.tac.RefinementUnsupportedException;
import oogre.refinements.tac.TM;
import edu.cmu.cs.crystal.tac.model.Variable;

public class MoreInfoNeededFromUser {
	
	public MoreInfoNeededFromUser(){
	}

	public void promptUser(final TM copyTM){
		Variable au = null;

		Set<Variable> units = new HashSet<Variable>();
		// Use the sorted map
		for (Entry<Variable, Set<OType>> tmEntry : copyTM.entrySet()) {
			// XXX. Which AU to pick? Any?! The first one? We may be picking an unimportant AU!
			// How do we identify the *important* AUs?
			// Maybe pick an AU that's either src or dst of refinement
			// OR a type that occurs inside type of src or dst of refinement
			au = tmEntry.getKey();
			Set<OType> setOTypes = tmEntry.getValue();
			if (setOTypes.size() == 1 ) {
				continue;
			}
			// We should not get here...
			if ( setOTypes.size() == 0 ) {
				throw new RefinementUnsupportedException("");
			}
			
			// XXX. Can we set a flag on AU to see if it's new expression
			if (setOTypes.size() > 1) {
				units.add(au);
			}
		}
		
		// Throw the exception
		throwException(units, null);
	}

	private OType throwException(Set<Variable> units, Set<OType> attempts) {
		throw new MoreInformationNeededException("Message", units, null,null);
	}
	
	private OType askUser(Variable au, Set<OType> asSet, Set<OType> attempts) {
		Set<Variable> units = new HashSet<Variable>();
		units.add(au);
		throw new MoreInformationNeededException("Message", units, null,null);
	}

	public void debugTM(TM newTM) {
		System.out.println("DEBUGGING TM");

		for (Entry<Variable, Set<OType>> tmEntry : newTM.entrySet()) {
			Variable au = tmEntry.getKey();
			Set<OType> setOTypes = tmEntry.getValue();

			if (setOTypes.size() > 1) {
				System.out.println(au);
				System.out.println(" | ");
				System.out.println(setOTypes.size());
				System.out.println(" | ");
				System.out.println(setOTypes.toString());
			}
		}
	}
}
