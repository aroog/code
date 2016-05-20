package de.fub.graph;

/**
 * Constants useful for graph algorithms.
 * 
 * @version $Id: Constants.java,v 1.3 1999/08/17 14:36:37 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public interface Constants {
	public static final int WHITE = 0;

	public static final int GRAY = 1;

	public static final int BLACK = 2;

	public static final int INFINITE = Integer.MAX_VALUE;

	/**
	 * Modes for toString(int verbose), three seem sufficient, but you can use your own
	 */
	public static final int PLAIN = 0; // Simple output

	public static final int VERBOSE = 1; // Verbose output

	public static final int TALKATIVE = 2; // Even more verbose output

	public static final int V_PLAIN = 3; // Simple output for graph layout

	public static final int V_VERBOSE = 4; // Verbose output for graph layout

	public static final int V_TALKATIVE = 5; // Even more verbose output for graph layout
}
