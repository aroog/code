package edu.wayne.metrics.adb;

import java.util.HashSet;
import java.util.Set;

/**
 * Class used to implement a value with sub-values;
 * The sub-values are NOT included in the value equality.
 * TODO: HIGH. Will this do the right value equality for Strings???
 * 
 */
public class PrimaryValue<F, S> {
	public F first; // The primary value
	public Set<S> second = new HashSet<S>(); // The set of secondary values
	
	public PrimaryValue() {
	}

	public PrimaryValue(F f) {
		first = f;
	}
	
	public void addSecondaryValue(S s) {
		second.add(s);
	}


	@Override
    public String toString() {
		return "(" + first + "," + second.toString() + ")";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof PrimaryValue)) {
			return false;
		}

		PrimaryValue<F,S> key = (PrimaryValue<F,S>) o;
		// Important: use ONLY the first value;
		return this.first.equals(key.first)/* && this.second.equals(key.second)*/;
	}

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (first == null ? 0 : first.hashCode());
		// Important: use ONLY the first value
		/*result = 37 * result + (second == null ? 0 : second.hashCode())*/;
		return result;
	}
	
}
