package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import oog.itf.IElement;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Transient;

import edu.cmu.cs.viewer.ui.ITreeElement;

public abstract class DisplayElement implements ITreeElement {

	@Transient
	protected IElement element;
	
	protected String label = "";

	@Attribute
	protected boolean visible = true; // Default is to show
	
	@Attribute
	protected boolean highlighted = false; // Default is not highlighted

	public DisplayElement() {
		super();
	}
	public abstract Object[] getChildren();

	public abstract Object getParent();

	public abstract boolean hasChildren();

	@Attribute(required=false)
	public String getLabel() {
		return this.label;
	}

	@Attribute(required=false)
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Transient
	public boolean isSelectable() {
		return true;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public IElement getElement() {
    	return element;
    }
	
	public void setElement(IElement element) {
    	this.element = element;
    }
	
	public boolean isHighlighted() {
    	return highlighted;
    }
	public void setHighlighted(boolean highlighted) {
    	this.highlighted = highlighted;
    }
	
}
