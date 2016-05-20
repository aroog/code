package de.fub.graph;

import java.io.OutputStream;

/**
 * Visualizes graphs into the <A HREF="http://www.cs.uni-sb.de/RW/users/sander/html/gsvcg1.html">VCG</A> format.
 * 
 * @version $Id: VCGVisualizer.java,v 1.2 1999/05/18 09:33:36 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class VCGVisualizer extends GraphVisualizer {
	private Graph graph;

	/**
	 * @param out stream to print output to
	 * @param verbose toggle verbosity of output
	 */
	public VCGVisualizer(OutputStream out, int verbose) {
		super(out, verbose);
	}

	@Override
    public void discoverGraph(Graph g) {
		String d = g.isDirected() ? "1" : "0";

		graph = g;

		println(0, "/* Created by VCGVisualizer */\n");
		println(0, "graph: {");
		println(1, "display_edge_labels: yes");
		println(1, "port_sharing: no");
		println(1, "manhattan_edges: yes");
		println(1, "straight_phase: yes");
		println(1, "priority_phase: yes");
		println(1, "node.color: white");
		println(1, "node.bordercolor: black");
		println(1, "node.textcolor: black");
		println(1, "node.shape: box");
		println(1, "node.textmode: left_justify");
		println(1, "edge.textcolor: black");
		println(1, "edge.color: black\n");
	}

	@Override
    public void finishGraph(Graph g) {
		println(0, "}");
		out.close();
	}

	@Override
    public void discoverVertex(Vertex v) {
		String label = GraphVisualizer.convertString(v.toString(verbose));

		println(1, "node: {");
		println(2, "title: \"" + v.getIdent() + "\"");
		println(2, "label: \"" + label + "\"");
		println(1, "}");
	}

	@Override
    public void finishVertex(Vertex v) {
	}

	@Override
    public void discoverEdge(Edge e) {
		int src = graph.getSource(e).getIdent();
		int target = graph.getTarget(e).getIdent();
		String label = GraphVisualizer.convertString(e.toString(verbose));

		println(1, "edge: {");
		println(2, "sourcename: \"" + src + "\"");
		println(2, "targetname: \"" + target + "\"");
		if (!label.equals("")) {
			println(2, "label: \"" + label + "\"");
		}
		println(1, "}");
	}

	@Override
    public void finishEdge(Edge e) {
	}

	/**
	 * Visualize graph g with this Visualizer.
	 * 
	 * @param g graph to visualize
	 * @param start node to start at
	 */
	@Override
    public void visualize(Graph g, Vertex start) {
		new DFS(this).start(g, start);
	}
}
