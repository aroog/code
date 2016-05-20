package de.fub.graph;

import java.io.PrintWriter;

/**
 * Visit graph with depth first search and simply prints the current vertices and edges as they are discovered. May be
 * used with DFS, BFS, Sequential or any other visitor. Uses DFS by default.
 * 
 * @version $Id: PrintVisitor.java,v 1.2 1999/05/18 09:33:34 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class PrintVisitor implements Visitor {
	private PrintWriter out;

	private int verbose;

	/**
	 * Simply print vertex and edge contents, when visiting them.
	 * 
	 * @param o stream to print to
	 * @param v toggle verbosity of output
	 * @see Constants
	 */
	public PrintVisitor(PrintWriter o, int v) {
		out = o;
		verbose = v;
	}

	public void discoverGraph(Graph g) {
	}

	public void finishGraph(Graph g) {
	}

	public void finishVertex(Vertex v) {
	}

	public void finishEdge(Edge e) {
	}

	public void discoverVertex(Vertex v) {
		out.println(v.toString(verbose));
	}

	public void discoverEdge(Edge e) {
		out.println(e.toString(verbose));
	}

	public void visit(Graph g, Vertex start) {
		new DFS(this).start(g, start);
	}
}
