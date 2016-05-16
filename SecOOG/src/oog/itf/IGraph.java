package oog.itf;

import java.util.Set;

import edu.wayne.ograph.OGraphVisitor;

// TODO: Maybe expose:
// - getDShared()?
// - getDummy? (getRoot is returning the Dummy); should be renamed.
// - getMainObject()
public interface IGraph {

	// TODO: Generalize to return a Collection?
	public Set<? extends IEdge> getEdges();

	// NOTE: This is really the dummy very-top-level object, which contains shared, lent, unique
	public IObject getRoot();
	
	public boolean accept(OGraphVisitor visitor);

}