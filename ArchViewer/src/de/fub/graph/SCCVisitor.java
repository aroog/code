package de.fub.graph;

import java.util.Vector;

/**
 * Visit graph computing its "Strongly Connected Components" (SCC).
 * 
 * @version $Id: SCCVisitor.java,v 1.2 1999/05/18 09:33:34 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see DFS
 */
public class SCCVisitor implements Visitor {
	private int time; // Global time

	private VStack stack; // Stack to remember members of a SCC

	private Vector<Graph> sccs; // Forest of SCCs

	private Graph graph;

	private Graph[] graphs;

	private int size;

	public SCCVisitor() {
	}

	public void discoverGraph(Graph g) {
		graph = g;
		size = g.getNoVertices();
		Vertex[] vertices = g.getVertexArray();

		for (int i = 0; i < size; i++) {
			vertices[i].setDiscoveryTime(0);
			/*
			 * Should rather be setMyMinTime(), "abused" to remember local minimum
			 */
			vertices[i].setFinishingTime(size * 2); // Invalidate
		}

		time = 0; // Reset timer
		stack = new VStack(size);
		sccs = new Vector<Graph>();
	}

	public void discoverVertex(Vertex v) {
		int min = time++; // Clock tick
		v.setDiscoveryTime(min); // Remember time of discovery
		v.setFinishingTime(min); // Remember local minimum
		stack.push(v); // Push current node on stack
	}

	public void discoverEdge(Edge e) {
	}

	public void finishEdge(Edge e) {
		Vertex src = graph.getSource(e);
		Vertex target = graph.getTarget(e);
		int m = target.getFinishingTime();

		/*
		 * After recursion, test if the (white) vertex just visited has now a smaller minium than the local one, i.e. if
		 * target.min < src.min && !target.isBlack().
		 */
		if ((m < src.getFinishingTime()) && (target.getDiscoveryTime() < size + 1)) {
			src.setFinishingTime(m);
		}
	}

	public void finishVertex(Vertex v) {
		/*
		 * If the local minimum hasn't changed, i.e. if no vertices with smaller minimums have been reached, this must
		 * be the root vertex of a SCC.
		 */
		if (v.getFinishingTime() == v.getDiscoveryTime()) { // Root of SCC?
			Graph scc = new Graph(); // Gather nodes in subgraph
			Vertex p;

			do { // Pop all nodes belonging to this SCC subgraph
				p = stack.pop();
				scc.addVertex(p); // May have no edges, so put it first in there
				p.setDiscoveryTime(size * 2); // Invalidate, i.e. mark vertex as black
			}
			while (p != v);

			Vertex[] vertices = scc.getVertexArray();
			for (int j = 0; j < vertices.length; j++) {
				p = vertices[j];

				Edge[] edges = graph.getEdgeArray(p);
				for (int i = 0; i < edges.length; i++) { // Build up subgraph
					Edge e = edges[i];
					Vertex src = graph.getSource(e);
					Vertex target = graph.getTarget(e);

					// Add only edges that are within the subgraph
					if (scc.contains(src) && scc.contains(target)) {
						scc.addEdge(src, target, e);
					}
				}
			}

			sccs.addElement(scc); // Add subgraph vectorto forest of subgraphs
		}
	}

	public void finishGraph(Graph g) {
		graphs = new Graph[sccs.size()];
		sccs.copyInto(graphs);
	}

	/**
	 * @return all strongly connected components found during traversal
	 */
	public final Graph[] getSCCs() {
		return graphs;
	}

	/**
	 * Visit graph with DFS and find all its strongly connected components which may be obtained with getSCCs().
	 * 
	 * @param g graph to traverse
	 * @param start where to start traversal
	 */
	public void visit(Graph g, Vertex start) {
		new DFS(this).start(g, start);
	}
}
