package de.fub.graph;

/**
 * @version $Id: GraphError.java,v 1.2 1999/05/18 09:33:31 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class GraphError extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1890872781285846404L;

	public GraphError(String s) {
		super(s);
	}
}
