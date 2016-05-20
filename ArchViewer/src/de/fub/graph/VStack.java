package de.fub.graph;

/**
 * Implements a simple first-in, last-out stack of vertices of fixed size.
 * 
 * @version $Id: VStack.java,v 1.2 1999/05/18 09:33:37 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
final class VStack {
	private Vertex[] vertices;

	private int top;

	VStack(int s) {
		vertices = new Vertex[s];
	}

	final void push(Vertex v) {
		vertices[top++] = v;
	}

	final Vertex pop() {
		return vertices[--top];
	}

	final Vertex top() {
		return vertices[top - 1];
	}

	final boolean empty() {
		return top <= 0;
	}
}
