package de.fub.graph;

/**
 * Describes a labeled edge between two vertices of a graph.
 * 
 * @version $Id: LabeledEdge.java,v 1.2 1999/05/18 09:33:32 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class LabeledEdge extends Edge {
	/**
	 * 
	 */
	private static final long serialVersionUID = 510535079040174097L;

	private String label;

	/**
	 * Create edge with label.
	 */
	public LabeledEdge(String l) {
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

	public final void setLabel(String l) {
		label = l;
	}
}
