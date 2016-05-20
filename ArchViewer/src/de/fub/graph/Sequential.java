package de.fub.graph;

/**
 * Visit graph in sequential order, i.e. in the order returned by Graph.getVertices().
 * 
 * @version $Id: Sequential.java,v 1.3 2000/05/26 11:52:48 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public final class Sequential extends TraversalOrder {
	/**
	 * Depth first search algorithm.
	 * 
	 * @param v visitor object to apply when discovering and finishing a vertex or edge
	 */
	public Sequential(Visitor v) {
		super(v);
	}

	/**
	 * Visit graph in sequential order
	 * 
	 * @param g graph to traverse
	 * @param start vertex to start at (or null, starts at first vertex then)
	 */
	@Override
    public void start(Graph g, Vertex start) {
		int size = g.getNoVertices();
		Vertex[] vertices = g.getVertexArray();
		int index = 0;

		visitor.discoverGraph(g);

		/*
		 * Initially mark all vertices as WHITE
		 */
		for (int i = 0; i < size; i++) {
			vertices[i].setColor(Constants.WHITE);
			if (vertices[i] == start) {
				index = i;
			}
		}

		for (int i = 0; (i < size) && !stopped; i++) {
			Vertex v = vertices[index++ % size];

			visitor.discoverVertex(v);
			v.setColor(Constants.GRAY);

			Edge[] edges = g.getEdgeArray(v);
			for (int j = 0; j < edges.length; j++) {
				visitor.discoverEdge(edges[j]);
				visitor.finishEdge(edges[j]);
			}

			visitor.finishVertex(v);
			v.setColor(Constants.BLACK);
		}

		visitor.finishGraph(g);
	}
}
