package edu.wayne.ograph.internal;

import java.util.Stack;

import org.simpleframework.xml.Transient;

import oog.itf.IEdge;
import oog.itf.IObject;
import util.TraceabilityEntry;
import util.TraceabilityList;
import edu.wayne.ograph.OGraphVisitor;


public abstract class OEdge extends OElement implements IEdge {

	public abstract OEdgeKey getKey();

	// Do not save this
	@Transient
	protected boolean highlighted = false;
	
	@Override
	public boolean accept(OGraphVisitor visitor) {
		return false;
	}

	// XXX: Not using O argument??
	// XXX: Is this the right order?
	public void addToASTNodePath(Stack<TraceabilityEntry> stack, TraceabilityEntry path2, OObject O) {
		TraceabilityList expressionStack = new TraceabilityList();
		// Add to the current list
		for(TraceabilityEntry entry: stack ) {
			expressionStack.add(entry);
		}
		// Add this one last
		expressionStack.add(path2);
		addLink(expressionStack);
	}
	
	@Override
    public abstract IObject getOsrc();

	@Override
    public abstract IObject getOdst();
	
	public abstract edu.wayne.ograph.OEdge getReal();

	public boolean isHighlighted() {
    	return highlighted;
    }

	public void setHighlighted(boolean highlighted) {
    	this.highlighted = highlighted;
    }
}
