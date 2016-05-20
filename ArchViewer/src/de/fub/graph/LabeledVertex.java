package de.fub.graph;

/**
 * Describes a labeled vertex of a graph.
 * 
 * @version $Id: LabeledVertex.java,v 1.2 1999/05/18 09:33:33 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class LabeledVertex extends Vertex {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4789406645760959164L;

	private String label;

	/**
	 * Create vertex with label l
	 */
	public LabeledVertex(String l) {
		label = l;
	}

	/**
	 * @param verbose toggle output format
	 * @return String representation.
	 */
	@Override
    public String toString(int verbose) {
		return label;
	}

	public final String getLabel() {
		return label;
	}
}
