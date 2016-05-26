package oogre.refinements.tac;

import oog.re.IOperation;

public class TMKey {

	private TMSolutionType solIndex;
	private int generation;
	
	public TMKey(TMSolutionType solIndex, int gen) {
		super();
		this.solIndex = solIndex;
		this.generation = gen;
	}

	public TMSolutionType getSolIndex() {
		return solIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + generation;
		result = prime * result + ((solIndex == null) ? 0 : solIndex.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TMKey other = (TMKey) obj;
		if (generation != other.generation)
			return false;
		if (solIndex != other.solIndex)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TMKey [solIndex=" + solIndex + ", generation=" + generation +"]";
	}
}
