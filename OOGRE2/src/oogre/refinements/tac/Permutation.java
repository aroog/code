package oogre.refinements.tac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.tac.model.Variable;

public class Permutation {
	
	private Map<Variable,Set<OType>> methodPermutation;

	public Permutation(Map<Variable,Set<OType>> methodPermutation) {
		super();
		this.methodPermutation = methodPermutation;
	}


	public Map<Variable, Set<OType>> getVariableSets() {
		// TODO Auto-generated method stub
		return this.methodPermutation;
	}


}
