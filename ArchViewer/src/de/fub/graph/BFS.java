package de.fub.graph;

/**
 * Visit graph with breadth first search and compute the predecessor during traversal for all vertices. The algorithm
 * could be modified so that it performs Dijkstra's shortest-path algorithm, for instance, if you extend the VQueue
 * class to a Priority Queue.
 * 
 * @version $Id: BFS.java,v 1.3 2000/05/26 11:52:46 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class BFS extends TraversalOrder {
	private VQueue q;

	/**
	 * Breadth first search algorithm.
	 * 
	 * @param v visitor object to apply when discovering and finishing a vertex
	 */
	public BFS(Visitor v) {
		this(v, null);
	}

	/**
	 * Breadth first search algorithm.
	 * 
	 * @param v visitor object to apply when discovering and finishing a vertex
	 * @param q queue to use during traversal
	 */
	public BFS(Visitor v, VQueue q) {
		super(v);
		this.q = q;
	}

	/**
	 * Run breadth first search (BFS) algorithm.
	 * 
	 * @param g graph to traverse
	 * @param start vertex to start at (or null, starts at some vertex then)
	 */
	@Override
    public void start(Graph g, Vertex start) {
		int size = g.getNoVertices();
		Vertex[] vertices = g.getVertexArray();

		visitor.discoverGraph(g);

		/*
		 * Initially mark all vertices as WHITE
		 */
		for (int i = 0; i < size; i++) {
			vertices[i].setColor(Constants.WHITE);
			vertices[i].setPredecessor(null);
		}

		if ((start == null) || !g.contains(start)) {
			start = vertices[0];
		}

		if (q == null) {
			q = new VQueue(size);
		}

		visitor.discoverVertex(start); // Initialization

		start.setColor(Constants.GRAY);
		q.enqueue(start);

		while (!q.empty() && !stopped) {
			Vertex u = q.head();

			vertices = g.getVertexArray(u); // Get adjacent vertices and edges
			Edge[] edges = g.getEdgeArray(u);
			size = vertices.length; // reuse var

			for (int i = 0; (i < size) && !stopped; i++) { // Explore all edges, same length
				visitor.discoverEdge(edges[i]);

				Vertex v = vertices[i];
				if (v.getColor() == Constants.WHITE) { // Unvisited vertex?
					visitor.discoverVertex(v);

					v.setColor(Constants.GRAY);
					q.enqueue(v);
				}

				visitor.finishEdge(edges[i]);
			}

			visitor.finishVertex(u);
			q.dequeue();
			u.setColor(Constants.BLACK);
		}

		visitor.finishGraph(g);
	}
}
