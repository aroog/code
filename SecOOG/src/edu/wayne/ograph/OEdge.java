package edu.wayne.ograph;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import oog.itf.IEdge;

// DONE. Add traceability to edge.
// DONE. Define a base class? OElement which has the traceability stuff.
// DONE. Make OEdge extend from OElement
// DONE: Make this abstract to enforce correct equals discipline.
public abstract class OEdge extends OElement implements IEdge {

	@Element(required=true, name="osrc")	
	protected OObject osrc;
	
	@Element(required=true, name="odst")
	protected OObject odst;
	
	// Do not save this
	@Transient
	protected boolean highlighted = false;

	// Add default constructor for serialization
	protected OEdge() {
		super();
	}

	public OEdge(@Element(required = true, name = "osrc") OObject osrc,
	        @Element(required = true, name = "odst") OObject odst) {
		super();
		this.osrc = osrc;
		this.odst = odst;
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		// TODO: OK to discard return value of accept?
		boolean visitChildren = visitor.visit(this);
		return super.accept(visitor);
	}
	
	public OObject getOsrc() {
		return osrc;
	}

	public OObject getOdst() {
		return odst;
	}

	@Override
	public String toString() {
		return osrc + "->" + odst;
	}

	public boolean isHighlighted() {
    	return highlighted;
    }

	public void setHighlighted(boolean highlighted) {
    	this.highlighted = highlighted;
    }

}
