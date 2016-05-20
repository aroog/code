package edu.wayne.metrics.adb;

/**
 * Pair class: implements equals and hashCode() since being used as a key in a hashtable
 * TODO: HIGH. Will this do the right value equality for Strings???
 */
public class Pair<F, S> {
	public Pair() {
	}

	public Pair(F f, S s) {
		first = f;
		second = s;
	}

	public F first;

	public S second;

	@Override
    public String toString() {
		return "(" + first + "," + second + ")";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Pair)) {
			return false;
		}

		Pair<F, S> key = (Pair<F,S>) o;
		return this.first.equals(key.first) && this.second.equals(key.second);
	}

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (first == null ? 0 : first.hashCode());
		result = 37 * result + (second == null ? 0 : second.hashCode());
		return result;
	}	
}
