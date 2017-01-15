package oogre.analysis;

import java.util.HashSet;
import java.util.Set;

import oogre.refinements.tac.TM;
import edu.cmu.cs.crystal.tac.model.TACInstruction;

public class ConflictException extends RuntimeException {

	// XXX. Do you need to store this here too? It's on the TFs already?
	// NO. TFs has no-conflict expr (skipExprs). But we still don't need this.
	// AllExprs = conflictExprs + no-conflictExprs + ones we haven't analyzed yet
	//Set<TACInstruction> conflictExprs = new HashSet<TACInstruction>();
	
	TACInstruction instr;
	TM tm;
	
	public ConflictException(TACInstruction instr, TM tm) {
		this.instr = instr;
		//conflictExprs.add(instr);
		this.tm = tm;
	}

	public TACInstruction getInstr() {
		return instr;
	}

	// Make immutable
	// public void setInstr(TACInstruction instr) {
	// this.instr = instr;
	// }

	public TM getTm() {
		return tm;
	}
	// Make immutable
	// public void setTm(TM tm) {
	// this.tm = tm;
	// }
	
	

}
