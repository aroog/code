package edu.wayne.summary.strategies;

import java.util.Collection;
import java.util.Set;


public interface Info<V> extends Comparable<Info<V>> {
	/**
	 * The key value, either a Class or a Method name
	 * @return
	 */
	String getKey();
	/**
	 * @return the ranking number for this object
	 */
	double getNumber();
	
	Set<V> getValues();
	boolean add(V item);
	boolean addAll(Collection<? extends V> c);
	InfoType getType();
	
	/**
	 * The mark for current node: Visited/Unchanged/Impacted/Not visited
	 */
	MarkStatus getMark();
	void setMark(MarkStatus mark);
}
