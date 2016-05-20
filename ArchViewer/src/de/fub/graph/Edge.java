package de.fub.graph;

/**
 * Describes an edge between two vertices of a graph, i.e. a relation between two arbitrary vertices.
 * 
 * @version $Id: Edge.java,v 1.3 1999/08/17 14:36:38 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class Edge implements Cloneable, Constants, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 303741669816528628L;

	private int ident; // unique number, i.e. identifier

	private static int counter; // produce default numbers

	/**
	 * Create edge.
	 */
	public Edge() {
		ident = Edge.counter++;
	}

	/**
	 * @return ident, i.e. identifier of this node
	 */
	public final int getIdent() {
		return ident;
	}

	/**
	 * @return number of created edges
	 */
	public static final int getCounter() {
		return Edge.counter;
	}

	/**
	 * @return String representation.
	 */
	@Override
    public String toString() {
		return toString(Constants.PLAIN);
	}

	/**
	 * @param verbose toggle output format
	 * @return String representation.
	 */
	public String toString(int verbose) {
		if ((verbose == Constants.TALKATIVE) || (verbose == Constants.V_TALKATIVE)) {
			return "Edge(" + ident + ")";
		}

		return "";
	}

	/**
	 * Access for visitor patterns
	 */
	public void accept(Visitor v) {
		v.discoverEdge(this);
	}

	/**
	 * @return deep copy
	 */
	public Edge copy() {
		try {
			return (Edge) clone();
		}
		catch (CloneNotSupportedException e) {
		}

		return null;
	}

	/**
	 * Check for equality.
	 */
	@Override
    public boolean equals(Object o) {
		if (!(o instanceof Edge)) {
			return false;
		}

		Edge e = (Edge) o;
		return e.ident == ident;
	}
	
	@Override
    public int hashCode() {
		int result = 17;

		result = 37 * result * ident;

		return result;
	}
	
}
