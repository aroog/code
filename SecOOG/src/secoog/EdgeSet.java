package secoog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import oog.itf.IEdge;
import secoog.itf.IEdgeSet;

/**
 * TODO: use me
 * */
public class EdgeSet extends ElemSet implements IEdgeSet {

	private Set<IEdge> cachedEdges;

	public EdgeSet(Set<IEdge> edges, String name, Property[] props) {
		super(name, props);
		cachedEdges = new HashSet<IEdge>();
		cachedEdges.addAll(edges);
	}

	@Override
	public Iterator<IEdge> edges() {
		return cachedEdges.iterator();
	}

}
