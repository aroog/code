package util;

import ast.BaseTraceability;
import oog.itf.IElement;

// TODO: Could revert changes here. Attempt at reifying the generics.
public class Pair<T1 extends IElement, T2 extends BaseTraceability> {
	// first member of the pair
	//@Element(required = true, name = "first")
	private T1 first;

	// second member of the pair
	//@Element(required = true, name = "second")
	private T2 second;

	public Pair( T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	public T1 getFirst() {
		return first;
	}

	public T2 getSecond() {
		return second;
	}
}
