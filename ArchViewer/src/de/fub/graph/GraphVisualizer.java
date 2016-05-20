package de.fub.graph;

import java.io.*;

/**
 * Abstract super class for drivers that visualize graphs in different graph formats such as <A
 * HREF="http://www.fmi.uni-passau.de/Graphlet/">GML</A> or <A
 * HREF="http://www.informatik.uni-bremen.de/~inform/forschung/daVinci/daVinci.html"> daVinci</A> or <A
 * HREF="http://www.cs.uni-sb.de/RW/users/sander/html/gsvcg1.html"> VCG</A>. By default graphs are dumped in the native
 * Java format, i.e. by using the Serialization feature.
 * 
 * @version $Id: GraphVisualizer.java,v 1.5 2001/11/12 14:49:28 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see DaVinciVisualizer
 * @see GMLVisualizer
 * @see VCGVisualizer
 */
public class GraphVisualizer implements Visitor {
	protected OutputStream orig_out;

	protected PrintWriter out;

	protected int verbose;

	/**
	 * @param out stream to print output to
	 * @param verbose toggle verbosity
	 */
	public GraphVisualizer(OutputStream out, int verbose) {
		orig_out = out;
		this.out = new PrintWriter(out, true);
		this.verbose = verbose;
	}

	/**
	 * Print string using ident tab as prefix, used for nested calls
	 * 
	 * @param indent how many tabs
	 * @param s string to print
	 */
	protected final void print(int indent, String s) {
		for (int i = 0; i < indent; i++) {
			out.print('\t');
		}

		out.print(s);
	}

	/**
	 * Print string using ident tab as prefix, used for nested calls
	 * 
	 * @param indent how many tabs
	 * @param s string to print
	 */
	protected final void println(int indent, String s) {
		print(indent, s);
		out.print("\r\n");
	}

	/**
	 * Escape all occurences of newline chars '\n' and quotes \"
	 */
	protected static final String convertString(String label) {
		char[] ch = label.toCharArray();
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < ch.length; i++) {
			switch (ch[i]) {
			case '\n':
				buf.append("\\n");
				break;
			case '\"':
				buf.append("\\\"");
				break;
			case '\\':
				buf.append("\\\\");
				break;
			default:
				buf.append(ch[i]);
				break;
			}
		}

		return buf.toString();
	}

	/**
	 * Visualize graph g with this Visualizer.
	 * 
	 * @param g graph to visualize
	 * @param start node to start at
	 */
	public void visualize(Graph g, Vertex start) {
		try {
			g.dump(orig_out);
		}
		catch (IOException e) {
			System.err.println(e);
		}

		out.close();
	}

	public void discoverGraph(Graph g) {
	}

	public void finishGraph(Graph g) {
	}

	public void discoverVertex(Vertex v) {
	}

	public void finishVertex(Vertex v) {
	}

	public void discoverEdge(Edge e) {
	}

	public void finishEdge(Edge e) {
	}

	public void visit(Graph g, Vertex start) {
		visualize(g, start);
	}
}
