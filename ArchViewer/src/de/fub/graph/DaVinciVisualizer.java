package de.fub.graph;

import java.io.OutputStream;

/**
 * Visualizes directed graphs into the <A
 * HREF="http://www.informatik.uni-bremen.de/~inform/forschung/daVinci/daVinci.html"> DaVinci</A> format. It can only
 * be used for directed graphs and does not support labeled edges very well, unfortunately. They have to be simulated
 * via dummy vertices. Traverses graph with the Sequential visitor, since it uses invariants of that algorithm.
 * 
 * @version $Id: DaVinciVisualizer.java,v 1.4 2000/05/26 11:52:47 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DaVinciVisualizer extends GraphVisualizer implements Constants {
	private Graph graph;

	private int no_edges, edges;

	private int no_vertices, vertices;

	/**
	 * @param out stream to print output to
	 * @param verbose toggle verbosity of output
	 */
	public DaVinciVisualizer(OutputStream out, int verbose) {
		super(out, verbose);
	}

	@Override
    public void discoverGraph(Graph g) {
		graph = g;

		no_vertices = g.getNoVertices();
		vertices = 0;
		out.println("[");
	}

	@Override
    public void finishGraph(Graph g) {
		out.println("]");
		out.close();
	}

	@Override
    public void discoverVertex(Vertex v) {
		String label = GraphVisualizer.convertString(v.toString(verbose));
		// System.out.println("vertex:" + v.getIdent() + ":" + v);

		println(1, "l(\"" + v.getIdent() + "\",");
		println(1, "n(\"anything\", [");
		println(2, "a(\"OBJECT\", \"" + label + "\"),");
		println(2, "a(\"FONTSTYLE\", \"bold\"),");
		println(2, "a(\"FONTFAMILY\", \"courier\")],");
		println(1, "["); // array of successor nodes

		no_edges = graph.getNoEdges(v);
		edges = 0;
	}

	@Override
    public void finishVertex(Vertex v) {
		String str = (vertices < no_vertices - 1) ? "]))," : "]))";
		println(1, str);
		vertices++;
	}

	private static int label_counter = 0;

	private final String getEdgeAttribute() {
		if (graph.isDirected()) {
			return "";
		}
		else {
			return "a(\"_DIR\", \"none\"";
		}
	}

	@Override
    public void discoverEdge(Edge e) {
		int u = graph.getSource(e).getIdent();
		int v = graph.getTarget(e).getIdent();
		String label = GraphVisualizer.convertString(e.toString(verbose));

		if (label.equals("")) {
			print(2, "l(\"" + u + "->" + v + "\", e(\"\", [" + getEdgeAttribute() + "], r(\"" + v + "\")))");
		}
		else { // Simulate labeled edge with intermediate node
			String dummy = "label" + DaVinciVisualizer.label_counter++; // Dummy name for intermediate node

			print(2, "l(\"" + u + "->" + dummy + "\", e(\"\", [a(\"_DIR\", \"none\")], " + "l(\"" + dummy
			        + "\", n(\"anything\",[a(\"OBJECT\", \"" + label + "\"), a(\"_GO\", \"text\")], [l(\"" + dummy
			        + "->" + v + "\", e(\"\",[" + getEdgeAttribute() + "], r(\"" + v + "\")))]))))");
			// System.out.println(graph.getTarget(e).getIdent() + ":" + graph.getTarget(e));
		}
	}

	@Override
    public void finishEdge(Edge e) {
		if (edges < no_edges - 1) {
			out.println(", ");
		}
		else {
			out.print('\n');
		}

		edges++;
	}

	/**
	 * Visualize graph g with this Visualizer.
	 * 
	 * @param g graph to visualize
	 * @param start node to start at
	 */
	@Override
    public void visualize(Graph g, Vertex start) {
		new Sequential(this).start(g);
	}
}
