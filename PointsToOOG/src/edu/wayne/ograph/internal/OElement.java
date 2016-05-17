package edu.wayne.ograph.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import oog.itf.IElement;

import edu.wayne.ograph.OGraphVisitor;

import util.TraceabilityEntry;
import util.TraceabilityList;
import util.TraceabilityListSet;
import ast.BaseTraceability;

public abstract class OElement implements IElement {
	
	protected TraceabilityListSet links = new TraceabilityListSet();

	public void addLink(Stack<TraceabilityEntry> stack) {
		TraceabilityList tList = new TraceabilityList();
		for(TraceabilityEntry entry : stack) {
			tList.add(entry);
		}
		links.add(tList);
	}

	// TODO: rename: --> add
	public void addLink(TraceabilityList node) {
		links.add(node);
	}

	// TODO: rename: --> addAll
	public void unionLink(TraceabilityListSet traceabilityLinks) {
		links.addAll(traceabilityLinks);
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		return false;
	}

	/**
	 * returns an empty set, subclasses that have information should save it.
	 * */

	@Override
	public TraceabilityListSet getPath() {
		return links;
	}

	/**
	 * Empty setter. Do NOT use. Used by serialization only.
	 */
	public final void setPath(TraceabilityListSet path) {
	}

	/**
	 * returns an empty list for the general case
	 * */
	@Override
	public Set<BaseTraceability> getTraceability() {
		if (links != null) {
			return links.getSetOfLasts();
		}
		return new HashSet<BaseTraceability>();
	}
	
}
