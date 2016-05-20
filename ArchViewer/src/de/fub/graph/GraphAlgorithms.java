package de.fub.graph;

import java.util.*;

/**
 * Collection of graph algorithms such as transposeGraph.
 * 
 * @version $Id: GraphAlgorithms.java,v 1.3 1999/08/17 14:36:40 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public abstract class GraphAlgorithms implements Constants {
	/**
	 * Compute the inverse of a graph, i.e. with all edges reverted
	 */
	public static final Graph transposeGraph(Graph g) {
		if (!g.isDirected()) {
			throw new GraphError("Can only transpose directed graphs");
		}

		Graph graph = new Graph(true);

		for (Iterator i = g.getVertices().iterator(); i.hasNext();) {
			graph.addVertex((Vertex) i.next());
		}

		for (Iterator i = g.getEdges().iterator(); i.hasNext();) {
			Edge f = (Edge) i.next();

			graph.addEdge(g.getTarget(f), g.getSource(f), f);
		}

		return graph;
	}

	/**
	 * Test whether the SCC g is a loop.
	 * 
	 * @param super_g super graph containing the subset of vertices
	 * @param g subset of super_g (g must be SCC of super_g)
	 */
	public static final boolean isLoop(Graph super_g, Graph loop) {
		return GraphAlgorithms.findLoopEntry(super_g, loop) != null;
	}

	/**
	 * Test whether the SCC g is a loop.
	 * 
	 * @param super_g super graph containing the subset of vertices
	 * @param g subset of super_g (g must be SCC of super_g)
	 * @return root node of loop, i.e. the entry point, or null if this is no loop
	 */
	public static final Vertex findLoopEntry(Graph super_g, Graph loop) {
		switch (loop.getNoVertices()) {
		case 0:
			return null; // certainly not a loop

		case 1: // singleton must point to itself, i.e. be a self-loop
			Vertex v = loop.getVertexArray()[0];
			Vertex[] edges = loop.getVertexArray(v);

			for (int i = 0; i < edges.length; i++) {
				if (edges[i] == v) {
					return v;
				}
			}

			return null;
		}

		/*
		 * Make sure that the root node is the only entry point to the SCC, i.e. there is no way from a node v \in
		 * (super_g \ g) into a node in g except via the root node. So we have to find the root node first ...
		 */
		Vertex root = null;
		Vertex[] vertices = super_g.getVertexArray();

		for (int i = 0; i < vertices.length; i++) {
			Vertex v = vertices[i];

			if (loop.contains(v)) {
				continue;
			}

			Vertex[] edges = super_g.getVertexArray(v); // All nodes reachable from v
			for (int j = 0; j < edges.length; j++) {
				Vertex u = edges[j];

				if (loop.contains(u)) {
					if (root == null) {
						root = u;
					}
					else {
						return null; // Can't be a loop then, i.e. has more than one root node
					}
				}
			}
		}

		return root; // Has loop a root node at all !?
	}
}
