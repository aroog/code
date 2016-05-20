package de.fub.graph;

import java.io.OutputStream;

/**
 * Visualizes graphs into the <A HREF="http://www.fmi.uni-passau.de/Graphlet/">GML</A> format.
 * 
 * @version $Id: GMLVisualizer.java,v 1.2 1999/05/18 09:33:29 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class GMLVisualizer extends GraphVisualizer {
	private Graph graph;

	/**
	 * @param out stream to print output to
	 * @param verbose toggle verbosity of output
	 */
	public GMLVisualizer(OutputStream out, int verbose) {
		super(out, verbose);
	}

	@Override
    public void discoverGraph(Graph g) {
		String d = g.isDirected() ? "1" : "0";

		graph = g;

		println(0, "graph [");
		println(1, "Creator\t \"GMLVisualizer\"");
		println(1, "id\t " + Vertex.getCounter()); // Make sure this is a unique number
		println(1, "directed\t " + d + "\n");
	}

	@Override
    public void finishGraph(Graph g) {
		println(0, "]\n");
		out.close();
	}

	@Override
    public void discoverVertex(Vertex v) {
		println(1, "node [");
		println(2, "id\t " + v.getIdent());
		println(2, "label");

		out.println("\"" + v.toString(verbose) + "\"");

		println(2, "graphics [");
		println(3, "type\t\t \"rectangle\"");
		println(3, "fill\t\t \"white\"");
		println(3, "outline\t\t \"black\"");
		println(3, "background\t \"blue\"");
		println(3, "foreground\t \"green\"");
		println(2, "]\n");

		println(2, "LabelGraphics [");
		println(3, "type\t \"text\"");
		println(3, "fill\t \"black\"");
		println(3, "justify\t \"left\"");
		println(2, "]\n");

		println(1, "]\n");
	}

	@Override
    public void finishVertex(Vertex v) {
	}

	@Override
    public void discoverEdge(Edge e) {
		int src = graph.getSource(e).getIdent();
		int target = graph.getTarget(e).getIdent();

		println(1, "edge [");
		println(2, "source\t " + src);
		println(2, "target\t " + target);

		println(2, "label");
		out.println("\"" + e.toString(verbose) + "\"");

		println(2, "graphics [");

		println(3, "fill\t \"black\"");
		// println(3, "fill\t\t \"white\"");

		println(3, "arrow\t \"last\"");
		println(2, "]\n");

		println(2, "LabelGraphics [");
		println(3, "type\t \"text\"");
		println(3, "fill\t \"black\"");
		println(2, "]\n");

		println(1, "]\n");
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
