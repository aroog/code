package oog.itf;

// XXX. Expose getLabel() method!
// XXX. Expose OEdgeKey? Can be computed from getOsrc(), getDst();
public interface IEdge extends IElement {

	public IObject getOsrc();

	public IObject getOdst();
	
	public boolean isHighlighted();

	public void setHighlighted(boolean highlighted);

}
