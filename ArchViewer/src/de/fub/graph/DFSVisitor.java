package de.fub.graph;

/**
 * Visit graph with DFS and compute predecessors during traversal as well as discovery and finishing time of all
 * vertices. Instances of this class (and of course its descendants) may be passed to a DFS object.
 * 
 * @version $Id: DFSVisitor.java,v 1.3 1999/08/17 14:36:37 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see DFS
 */
public class DFSVisitor extends DefaultVisitor {
	private int time; // Global time

	private VStack stack; // Stack to remember predecessors

	public DFSVisitor() {
	}

	@Override
    public void discoverGraph(Graph g) {
		int size = g.getNoVertices();
		Vertex[] vertices = g.getVertexArray();

		for (int i = 0; i < size; i++) {
			vertices[i].setPredecessor(null);
		}

		time = 0; // Reset timer
		stack = new VStack(size);
	}

	@Override
    public void discoverVertex(Vertex v) {
		if (stack.empty()) {
			v.setPredecessor(null);
		}
		else {
			v.setPredecessor(stack.top());
		}

		stack.push(v);
		v.setDiscoveryTime(time++);
	}

	@Override
    public void finishVertex(Vertex v) {
		v.setFinishingTime(time++);
		stack.pop();
	}
}
