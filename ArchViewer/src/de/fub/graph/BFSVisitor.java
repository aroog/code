package de.fub.graph;

/**
 * Visit graph with BFS and compute predecessors during traversal as well as the "distance" to the source node (number
 * of edges in between).
 * 
 * @version $Id: BFSVisitor.java,v 1.3 1999/08/17 14:36:36 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see BFS
 */
public class BFSVisitor extends DefaultVisitor implements Constants {
	private VStack stack; // Stack to remember predecessors

	public BFSVisitor() {
	}

	@Override
    public void discoverGraph(Graph g) {
		int size = g.getNoVertices();
		Vertex[] vertices = g.getVertexArray();

		for (int i = 0; i < size; i++) {
			vertices[i].setDistance(Constants.INFINITE);
			vertices[i].setPredecessor(null);
		}

		stack = new VStack(size);
	}

	@Override
    public void discoverVertex(Vertex v) {
		if (stack.empty()) { // At start vertex, v == start
			v.setPredecessor(null);
			v.setDistance(0);
		}
		else {
			Vertex pred = stack.top();
			v.setPredecessor(pred);
			v.setDistance(pred.getDistance() + 1);
		}

		stack.push(v);
	}

	@Override
    public void finishVertex(Vertex v) {
		stack.pop();
	}

	@Override
    public void visit(Graph g, Vertex start) {
		new BFS(this).start(g, start);
	}
}
