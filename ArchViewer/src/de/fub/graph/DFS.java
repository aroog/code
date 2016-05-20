package de.fub.graph;

/**
 * Visit graph with depth first search and mark discovery and finishing time in all vertices.
 * 
 * @version $Id: DFS.java,v 1.4 2001/11/12 14:49:27 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DFS extends TraversalOrder {
	/**
	 * Depth first search algorithm.
	 * 
	 * @param v visitor object to apply when discovering and finishing a vertex or edge
	 */
	public DFS(Visitor v) {
		super(v);
	}

	/**
	 * Depth first search algorithm.
	 * 
	 * @param v visitor object to apply when discovering and finishing a vertex or edge
	 * @param traverse_all traverse the complete graph or stop after finishing all nodes reachable from the start
	 */
	public DFS(Visitor v, boolean traverse_all) {
		super(v, traverse_all);
	}

	/**
	 * Run depth first search (DFS) algorithm.
	 * 
	 * @param g graph to traverse
	 * @param start vertex to start at (or null, starts at some vertex then)
	 */
	@Override
    public void start(Graph g, Vertex start) {
		int size = g.getNoVertices();
		Vertex[] vertices = g.getVertexArray();

		graph = g;

		visitor.discoverGraph(g);

		/*
		 * Initially mark all vertices as WHITE
		 */
		for (int i = 0; i < size; i++) {
			vertices[i].setColor(Constants.WHITE);
		}

		if ((start != null) && g.contains(start)) {
			DFS_Visit(start);
		}

		if (traverse_complete_graph) {
			for (int i = 0; (i < size) && !stopped; i++) {
				Vertex v = vertices[i];

				if (v.getColor() == Constants.WHITE) {
					DFS_Visit(v);
				}
			}
		}

		visitor.finishGraph(g);
	}

	private final void DFS_Visit(Vertex u) {
		u.setColor(Constants.GRAY); // Mark as visited
		visitor.discoverVertex(u);

		Vertex[] vertices = graph.getVertexArray(u); // Get adjacent vertices
		Edge[] edges = graph.getEdgeArray(u); // and edges
		int size = vertices.length;
		// System.out.println(java.util.Arrays.asList(edges));

		for (int i = 0; (i < size) && !stopped; i++) { // Explore all edges
			visitor.discoverEdge(edges[i]);

			Vertex v = vertices[i];

			if (v.getColor() == Constants.WHITE) {
				DFS_Visit(v);
			}

			visitor.finishEdge(edges[i]);
		}

		u.setColor(Constants.BLACK); // Mark as finished
		visitor.finishVertex(u);
	}
}
