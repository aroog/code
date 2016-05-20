package de.fub.graph;

/**
 * Interface for visitor patterns.
 * 
 * @version $Id: Visitor.java,v 1.2 1999/05/18 09:33:38 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public interface Visitor {
	public void discoverGraph(Graph g); // may be also used as "visitGraph"

	public void finishGraph(Graph g);

	public void discoverVertex(Vertex v); // same as above

	public void finishVertex(Vertex v);

	public void discoverEdge(Edge e); // same as above

	public void finishEdge(Edge e);

	public void visit(Graph g, Vertex v); // Start visiting graph g beginning at start
}
