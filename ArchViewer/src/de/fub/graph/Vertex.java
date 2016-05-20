package de.fub.graph;

/**
 * Describes a vertex of a graph.
 * 
 * @version $Id: Vertex.java,v 1.4 2000/05/26 11:52:48 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class Vertex implements Cloneable, Constants, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8472211346949189030L;

	private int ident; // unique number, i.e. identifier

	private static int counter; // produce default numbers

	/**
	 * Useful variables for graph algorithms, calculated by DFS.
	 * 
	 * @see DFS
	 */
	private int dtime, ftime;

	private int color; // Color of vertex during traversal

	private Vertex pred; // Predecessor of vertex

	private int distance; // (Shortest) path to given root node (BFS)

	/**
	 * Create vertex with default identifier.
	 */
	public Vertex() {
		ident = Vertex.counter++;
	}

	/**
	 * @return ident, i.e. identifier of this node
	 */
	public final int getIdent() {
		return ident;
	}

	/**
	 * @return number of created vertices
	 */
	public static final int getCounter() {
		return Vertex.counter;
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
		switch (verbose) {
		case VERBOSE:
		case TALKATIVE:
			return "Vertex(" + ident + ", pred=" + ((pred == null) ? "null" : ("" + pred.ident)) + ", discovery="
			        + dtime + ", finishing=" + ftime + ", distance=" + distance + ")";

		case V_VERBOSE:
		case V_TALKATIVE:
			return "Vertex(" + ident + ")\npred=" + ((pred == null) ? "null" : ("" + pred.ident)) + "\ndiscovery="
			        + dtime + "\nfinishing=" + ftime + "\ndistance=" + distance;

		case PLAIN:
		case V_PLAIN:
		default:
			return "Vertex(" + ident + ")";
		}
	}

	/**
	 * Computed by DFS, BFS or other visitors
	 * 
	 * @see Visitor
	 */
	public int getDiscoveryTime() {
		return dtime;
	}

	/**
	 * Computed by DFS, BFS or other visitors
	 * 
	 * @see Visitor
	 */
	public int getFinishingTime() {
		return ftime;
	}

	/**
	 * Computed by DFS, BFS or other visitors
	 * 
	 * @see Visitor
	 * @deprecated use Graph.getPredecessors
	 */
	public Vertex getPredecessor() {
		return pred;
	}

	/**
	 * Computed by DFS, BFS or other visitors
	 * 
	 * @see Visitor
	 * @return distance from start vertex (No. edges in between)
	 * @see BFSVisitor
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * May be set only by classes of this package, Visitor in particular.
	 */
	public void setDistance(int d) {
		distance = d;
	}

	public void setDiscoveryTime(int d) {
		dtime = d;
	}

	public void setFinishingTime(int f) {
		ftime = f;
	}

	public void setColor(int c) {
		color = c;
	}

	public int getColor() {
		return color;
	}

	public void setPredecessor(Vertex p) {
		pred = p;
	}

	/**
	 * Access for visitors
	 */
	public void accept(Visitor v) {
		v.discoverVertex(this);
	}

	/**
	 * @return deep copy
	 */
	public Vertex copy() {
		try {
			return (Vertex) clone();
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
		if (!(o instanceof Vertex)) {
			return false;
		}

		Vertex v = (Vertex) o;
		return v.ident == ident;
	}

	// Always override hashcode when you override equals
	@Override
    public int hashCode() {
		int result = 17;

		result = 37 * result * ident;

		return result;
	}
	// private void readObject(java.io.ObjectInputStream in)
	// throws java.io.IOException, ClassNotFoundException
	// {
	// counter++; // Adjust counter, since a new object has been created
	// in.defaultReadObject();
	// }
}
