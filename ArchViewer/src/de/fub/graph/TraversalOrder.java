package de.fub.graph;

/**
 * Class takes a visitor "piggy-backed" and applies it to all nodes and edges. The traversal order implemented by
 * children of this class may be Depth-first or Breadth-first search, e.g.. This relates to the "Strategy" design
 * pattern.
 * 
 * @version $Id: TraversalOrder.java,v 1.3 2000/05/26 11:52:48 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see DFS
 * @see BFS
 * @see Sequential
 */
public abstract class TraversalOrder implements Constants {
	protected Visitor visitor;

	protected Graph graph;

	protected boolean stopped;

	protected boolean traverse_complete_graph;

	/**
	 * Depth first search algorithm.
	 * 
	 * @param v visitor object to apply when discovering and finishing a vertex or edge
	 * @param traverse_all traverse the complete graph or stop after finishing all nodes reachable from the start
	 */
	public TraversalOrder(Visitor v, boolean traverse_all) {
		visitor = v;
		traverse_complete_graph = traverse_all;
	}

	public TraversalOrder(Visitor v) {
		this(v, true);
	}

	/**
	 * Start traversal.
	 * 
	 * @param g graph to traverse
	 * @param start vertex to start at (or null, starts at some vertex then)
	 * @param all_vertices visit all vertices or just those reache from start
	 */
	public abstract void start(Graph g, Vertex start);

	/**
	 * May be called to stop traversal, e.g. if a searched node has been found.
	 */
	public void stop() {
		stopped = true;
	}

	/**
	 * Depth first search algorithm starting at some vertex.
	 * 
	 * @param g graph to traverse
	 */
	public void start(Graph g) {
		start(g, null);
	}
}
