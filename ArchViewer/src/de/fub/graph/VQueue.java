package de.fub.graph;

/**
 * Implements a simple first-in, first-out queue of vertices of fixed size. Can be extended, e.g., to implement a
 * priority queue, so that the traversal order of the BFS algorithm may be influenced.
 * 
 * @version $Id: VQueue.java,v 1.2 1999/05/18 09:33:36 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class VQueue {
	protected Vertex[] vertices;

	protected int left, right;

	public VQueue(int s) {
		vertices = new Vertex[s];
	}

	public void enqueue(Vertex v) {
		vertices[right++] = v;
	}

	public Vertex dequeue() {
		return vertices[left++];
	}

	public Vertex head() {
		return vertices[left];
	}

	public boolean empty() {
		return right <= left;
	}
}
