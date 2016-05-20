package de.fub.graph;

/**
 * Describes an edge with a weight.
 * 
 * @version $Id: WeightedEdge.java,v 1.2 1999/05/18 09:33:39 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class WeightedEdge extends Edge {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4066829415936947853L;

	private int weight;

	/**
	 * Create edge with a weight.
	 */
	public WeightedEdge(int w) {
		weight = w;
	}

	/**
	 * @param verbose toggle output format
	 * @return String representation.
	 */
	@Override
    public String toString(int verbose) {
		switch (verbose) {
		case TALKATIVE:
		case V_TALKATIVE:
			return "WeightedEdge(" + getIdent() + "\nweight=" + weight + ")";
		default:
			return "" + weight;
		}
	}

	public void addToWeight(int w) {
		weight += w;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}
}
