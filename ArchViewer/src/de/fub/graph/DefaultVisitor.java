package de.fub.graph;

/**
 * Empty Visitor, i.e. all visit methods do nothing.
 * 
 * @version $Id: DefaultVisitor.java,v 1.2 2000/05/26 11:52:47 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DefaultVisitor implements Visitor {
	protected DFS dfs;

	public DefaultVisitor() {
	}

	public void discoverGraph(Graph g) {
	}

	public void finishGraph(Graph g) {
	}

	public void discoverVertex(Vertex v) {
	}

	public void finishVertex(Vertex v) {
	}

	public void discoverEdge(Edge e) {
	}

	public void finishEdge(Edge e) {
	}

	public void visit(Graph g, Vertex start) {
		dfs = new DFS(this);
		dfs.start(g, start);
	}
}
