package de.fub.graph;

import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
 * Describes a generic graph G = (V, E), i.e. a set of vertices and edges.
 * 
 * @version $Id: Graph.java,v 1.7 2001/11/12 14:49:28 dahm Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class Graph implements Cloneable, Constants, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7401505664863571971L;

	private boolean directed = true;

	/*
	 * Map Vertex to VertexWrapper and Edge to EdgeWrapper, respectively. Wrapper objects carry the necessary
	 * information about associations between vertices. VertexWrappers contain the edges coming from and leading to a
	 * vertex (if the graph is undirected both vertices "have" this edge, otherwise the source vertex only "has" it).
	 * EdgeWrappers contain references to the source and target vertices.
	 */
	private Map<Vertex, VertexWrapper> vmap = new HashMap<Vertex, VertexWrapper>();

	private Map<Edge, EdgeWrapper> emap = new HashMap<Edge, EdgeWrapper>();

	/**
	 * Create empty directed graph.
	 */
	public Graph() {
	}

	/**
	 * Create directed graph initialized with a set of subgraphs.
	 */
	public Graph(Graph[] graphs) {
		for (int i = 0; i < graphs.length; i++) {
			addGraph(graphs[i]);
		}
	}

	/**
	 * Create empty graph.
	 * 
	 * @param d graph is directed?
	 */
	public Graph(boolean d) {
		directed = d;
	}

	/**
	 * Read graph from file using Serializable.
	 * 
	 * @param name file name to read from
	 */
	public Graph(String name) throws IOException, ClassNotFoundException {
		this(new FileInputStream(name));
	}

	/**
	 * Read graph from input stream using Serializable.
	 * 
	 * @param stream stream to read from
	 */
	public Graph(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream oi = new ObjectInputStream(new GZIPInputStream(stream));
		Graph g = (Graph) oi.readObject();

		directed = g.directed;
		vmap = g.vmap;
		emap = g.emap;
	}

	/*
	 * Add sub graph to this graph.
	 */
	public void addGraph(Graph g) {
		Vertex[] vertices = g.getVertexArray();
		for (int j = 0; j < vertices.length; j++) {
			addVertex(vertices[j]);
		}

		Edge[] edges = g.getEdgeArray();
		for (int j = 0; j < edges.length; j++) {
			addEdge(g.getSource(edges[j]), g.getTarget(edges[j]), edges[j]);
		}
	}

	/**
	 * Add a vertex to the graph.
	 * 
	 * @param v vertex to add
	 */
	public void addVertex(Vertex v) {
		if (v == null) {
			throw new GraphError("Vertex must not be null");
		}

		vmap.put(v, new VertexWrapper(v));
	}

	/**
	 * Add a (default) edge between two vertices to the graph.
	 * 
	 * @param u source vertex.
	 * @param v target vertex.
	 */
	public void addEdge(Vertex u, Vertex v) {
		addEdge(u, v, new Edge());
	}

	/**
	 * Add an edge between two vertices to the graph.
	 * 
	 * @param u source vertex.
	 * @param v target vertex.
	 * @param e edge between
	 */
	public void addEdge(Vertex u, Vertex v, Edge e) {
		VertexWrapper uw = getWrapper(u);
		VertexWrapper vw = getWrapper(v);

		if (e == null) {
			throw new GraphError("Edge must not be null");
		}

		emap.put(e, new EdgeWrapper(e, u, v));
		uw.addEdge(e);

		if (!directed) {
			vw.addEdge(e);
		}

		vw.addPredecessorEdge(e);
	}

	/**
	 * Remove given vertex and the edges that connect it to other vertices.
	 * 
	 * @param v abandoned vertex.
	 */
	public void removeVertex(Vertex v) {
		VertexWrapper vw = getWrapper(v);

		Edge[] edges = vw.getEdges(); // Remove all adjacent edges

		if (!directed) { // Inform other side of the edge that it has to be removed
			for (int i = 0; i < edges.length; i++) {
				EdgeWrapper ew = getWrapper(edges[i]);
				Vertex u = (ew.src == v) ? ew.target : ew.src;
				VertexWrapper uw = getWrapper(u);
				uw.removeEdge(edges[i]);
			}
		}
		else {
			for (int i = 0; i < edges.length; i++) {
				VertexWrapper uw = getWrapper(getWrapper(edges[i]).target);
				uw.removePredecessorEdge(edges[i]);
			}
		}

		vmap.remove(v); // Remove vertex from the hash map

		// Finally remove the edges from the hash map
		for (int i = 0; i < edges.length; i++) {
			emap.remove(edges[i]);
		}
	}

	/**
	 * Remove given edge from the graph.
	 * 
	 * @param e abandoned edge.
	 */
	public void removeEdge(Edge e) {
		EdgeWrapper ew = getWrapper(e);
		emap.remove(e); // Remove edge from the hash map

		// Update source vertex
		VertexWrapper vw = getWrapper(ew.src);
		vw.removeEdge(e);

		if (!directed) { // Inform other node, too
			vw = getWrapper(ew.target);
			vw.removeEdge(e);
		}
	}

	/**
	 * Get source vertex of edge. Order between source and target doesn't really matter if this graph is undirected.
	 * 
	 * @param e edge.
	 */
	public final Vertex getSource(Edge e) {
		return getWrapper(e).src;
	}

	/**
	 * Get target vertex of edge. Order between source and target doesn't really matter if this graph is undirected.
	 * 
	 * @param e edge.
	 */
	public final Vertex getTarget(Edge e) {
		return getWrapper(e).target;
	}

	/**
	 * @return number of vertices.
	 */
	public final int getNoVertices() {
		return vmap.size();
	}

	/**
	 * @return number of edges.
	 */
	public final int getNoEdges() {
		return emap.size();
	}

	/**
	 * @return all vertices of the graph.
	 */
	public Collection<Vertex> getVertices() {
		return vmap.keySet();
	}

	/**
	 * @return all edges of the graph.
	 */
	public Collection<Edge> getEdges() {
		return emap.keySet();
	}

	/**
	 * @return all vertices of the graph.
	 */
	public final Vertex[] getVertexArray() {
		return getVertices().toArray(new Vertex[getNoVertices()]);
	}

	/**
	 * @return all edges of the graph.
	 */
	public final Edge[] getEdgeArray() {
		return getEdges().toArray(new Edge[getNoEdges()]);
	}

	/**
	 * @return all edges between source and target
	 */
	public Edge[] getEdgeArray(Vertex source, Vertex target) {
		Collection<Edge> c = getEdges(source, target);
		return c.toArray(new Edge[c.size()]);
	}

	/**
	 * @return all edges between source and target
	 */
	public Collection<Edge> getEdges(Vertex source, Vertex target) {
		Edge[] edges = getEdgeArray(source);
		List<Edge> list = new ArrayList<Edge>();

		for (int i = 0; i < edges.length; i++) {
			if (getTarget(edges[i]) == target) {
				list.add(edges[i]);
			}
		}

		return list;
	}

	/**
	 * @return all vertices adjacent to v.
	 */
	public Vertex[] getVertexArray(Vertex v) {
		Edge[] edges = getEdgeArray(v);
		int size = edges.length;
		Vertex[] vertices = new Vertex[size];

		/*
		 * Take the other side of the edge, no matter if directed or not
		 */
		for (int i = 0; i < size; i++) {
			EdgeWrapper ew = getWrapper(edges[i]);
			vertices[i] = (ew.src == v) ? ew.target : ew.src;
		}

		return vertices;
	}

	/**
	 * @return number of vertices adjacent to v.
	 */
	public int getNoVertices(Vertex v) {
		VertexWrapper vw = getWrapper(v);
		return vw.edges.size();
	}

	/**
	 * @return number of edges adjacent to v.
	 */
	public final int getNoEdges(Vertex v) {
		return getNoVertices(v);
	}

	/**
	 * @return all edges adjacent to v.
	 */
	public Edge[] getEdgeArray(Vertex v) {
		VertexWrapper vw = getWrapper(v);
		return vw.getEdges();
	}

	/**
	 * @return all vertices adjacent to v.
	 */
	public Collection<Vertex> getVertices(Vertex v) {
		return Arrays.asList(getVertexArray(v));
	}

	/**
	 * @return all edges adjacent to v.
	 */
	public Collection<Edge> getEdges(Vertex v) {
		return Arrays.asList(getEdgeArray(v));
	}

	/**
	 * Get all vertices from which v may be reached directly, in an undirected graph, this is identical to
	 * getVertexArray(v)
	 * 
	 * @return all predecessor vertices of v
	 */
	public Vertex[] getPredecessorArray(Vertex v) {
		if (!directed) {
			return getVertexArray(v);
		}

		Collection<Vertex> c = getPredecessors(v);
		return c.toArray(new Vertex[c.size()]);
	}

	/**
	 * Get all vertices from which v may be reached directly, in an undirected graph, this is identical to
	 * getVertices(v)
	 * 
	 * @return all predecessor vertices of v
	 */
	public Collection<Vertex> getPredecessors(Vertex v) {
		if (!directed) {
			return getVertices(v);
		}

		List<Vertex> list = new ArrayList<Vertex>();
		Collection<Edge> edges = getWrapper(v).pred_edges;

		for (Iterator<Edge> i = edges.iterator(); i.hasNext();) {
			list.add(getWrapper(i.next()).src);
		}

		return list;
	}

	public Edge[] getPredecessorEdgeArray(Vertex v) {
		if (!directed) {
			return getEdgeArray(v);
		}

		Collection<Edge> edges = getWrapper(v).pred_edges;

		return edges.toArray(new Edge[edges.size()]);
	}

	/**
	 * Get all edges pointing to v, in an undirected graph, this is identical to getEdges(v)
	 * 
	 * @return all predecessor vertices of v
	 */
	public Collection<Edge> getPredecessorEdges(Vertex v) {
		if (!directed) {
			return getEdges(v);
		}

		return (Collection<Edge>) ((ArrayList<Edge>) getWrapper(v).pred_edges).clone();
	}

	/**
	 * Is v in this graph?
	 */
	public final boolean contains(Vertex v) {
		return vmap.containsKey(v);
	}

	/**
	 * Is e in this graph?
	 */
	public final boolean contains(Edge e) {
		return emap.containsKey(e);
	}

	/**
	 * Check, if v is a vertex of graph g.
	 */
	public static final boolean contains(Graph g, Vertex v) {
		return g.contains(v);
	}

	/**
	 * Check, if e is an edge of graph g.
	 */
	public static final boolean contains(Graph g, Edge e) {
		return g.contains(e);
	}

	/**
	 * Check, if g is a subgraph of super_g, i.e. if all vertices and edges of g are contained in super_g, too.
	 * 
	 * @param super_g the super graph
	 * @param g the subgraph
	 */
	public static final boolean contains(Graph super_g, Graph g) {
		for (Iterator<Vertex> i = g.getVertices().iterator(); i.hasNext();) {
			Vertex v = i.next();
			if (!Graph.contains(super_g, v)) {
				return false;
			}
		}

		for (Iterator<Edge> i = g.getEdges().iterator(); i.hasNext();) {
			Edge e = i.next();
			if (!Graph.contains(super_g, e)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check, if g is a subgraph of this graph. Tests for object equivalence (==).
	 */
	public final boolean contains(Graph g) {
		return Graph.contains(this, g);
	}

	/**
	 * Access for visitor patterns
	 */
	public void accept(Visitor v) {
		v.discoverGraph(this);
	}

	/**
	 * Is this graph directed?
	 */
	public final boolean isDirected() {
		return directed;
	}

	/**
	 * @return wrapper object corresponding to vertex or throw an exception.
	 */
	private final VertexWrapper getWrapper(Vertex v) {
		VertexWrapper vw = vmap.get(v);

		if (vw == null) {
			throw new GraphError("Vertex not contained in this graph: " + v);
		}

		return vw;
	}

	/**
	 * @return wrapper object corresponding to edge or throw an exception.
	 */
	private final EdgeWrapper getWrapper(Edge e) {
		EdgeWrapper ew = emap.get(e);

		if (ew == null) {
			throw new GraphError("Edge not contained in this graph: " + e);
		}

		return ew;
	}

	@Override
    public String toString() {
		return toString(Constants.PLAIN);
	}

	/**
	 * @return graph contents traversed with DFS
	 */
	public String toString(int verbose) {
		if (verbose == Constants.PLAIN) {
			return Graph.printArray(getVertexArray());
		}
		else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(bos);

			new DFS(new PrintVisitor(out, Constants.VERBOSE)).start(this);
			out.close();

			return bos.toString();
		}
	}

	/**
	 * All vertices and edges are copied deeply, i.e. copy() will be called on them recursively.
	 * 
	 * @return deep copy of this graph.
	 */
	public Graph copy() {
		Graph g = new Graph(directed);

		for (Iterator<Vertex> i = getVertices().iterator(); i.hasNext();) {
			Vertex v = i.next().copy();
			g.addVertex(v);
		}

		for (Iterator<Edge> i = getEdges().iterator(); i.hasNext();) {
			Edge e = i.next();
			EdgeWrapper ew = getWrapper(e);

			g.addEdge(ew.src, ew.target, e.copy());
		}

		return g;
	}

	public Graph copy(Vertex start) {
		final Graph g = new Graph(directed);

		new DFS(new DefaultVisitor() {
			@Override
            public void discoverVertex(Vertex v) {
				g.addVertex(v);
			}

			@Override
            public void finishEdge(Edge e) {
				EdgeWrapper ew = getWrapper(e);

				g.addEdge(ew.src, ew.target, e);
			}
		}, false).start(this, start);

		return g;
	}

	/**
	 * @return shallow copy of this graph, i.e. all vertices and edges keep their object identity
	 */
	@Override
    public Object clone() {
		Graph g = new Graph(directed);

		for (Iterator<Vertex> i = getVertices().iterator(); i.hasNext();) {
			Vertex v = i.next();
			g.addVertex(v);
		}

		for (Iterator<Edge> i = getEdges().iterator(); i.hasNext();) {
			Edge e = i.next();
			EdgeWrapper ew = getWrapper(e);

			g.addEdge(ew.src, ew.target, e);
		}

		return g;
	}

	/*
	 * Wrap association information into class, s.a.
	 */
	private static final class VertexWrapper implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2821171396809608272L;

		Vertex vertex;

		List<Edge> edges = new ArrayList<Edge>(3);

		List<Edge> pred_edges = new ArrayList<Edge>(3);

		final void addEdge(Edge e) {
			edges.add(e);
		}

		final void addPredecessorEdge(Edge e) {
			pred_edges.add(e);
		}

		final void removeEdge(Edge e) {
			edges.remove(e);
		}

		final void removePredecessorEdge(Edge e) {
			pred_edges.remove(e);
		}

		final Edge[] getEdges() {
			return edges.toArray(new Edge[edges.size()]);
		}

		VertexWrapper(Vertex v) {
			vertex = v;
		}
	}

	/*
	 * Wrap association information into class, s.a.
	 */
	private static final class EdgeWrapper implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5507305478213062738L;

		Edge edge;

		Vertex src, target;

		EdgeWrapper(Edge e, Vertex u, Vertex v) {
			edge = e;
			src = u;
			target = v;
		}
	}

	/**
	 * Clear all vertex attributes like color, distance, ...
	 */
	public void clearVertices() {
		for (Iterator<Vertex> i = getVertices().iterator(); i.hasNext();) {
			Vertex v = i.next();
			v.setColor(-1);
			v.setDistance(-1);
			v.setDiscoveryTime(-1);
			v.setFinishingTime(-1);
			v.setPredecessor(null);
		}
	}

	/**
	 * Dump graph to file using Serializable.
	 * 
	 * @param name file name to dump to
	 */
	public void dump(String name) throws IOException {
		dump(new FileOutputStream(name));
	}

	/**
	 * Dump graph to output stream using Serializable.
	 * 
	 * @param stream stream to dump to
	 */
	public void dump(OutputStream stream) throws IOException {
		ObjectOutputStream oo = new ObjectOutputStream(new GZIPOutputStream(stream));
		oo.writeObject(this);
		oo.close();
	}

	/**
	 * Print the given array using toString() where appropiate.
	 */
	private static final String printArray(Object[] obj) {
		StringBuffer buf = new StringBuffer("{");

		for (int i = 0; i < obj.length; i++) {
			if (obj[i] != null) {
				buf.append(obj[i].toString());
			}
			else {
				buf.append("null");
			}

			if (i < obj.length - 1) {
				buf.append(", ");
			}
		}
		buf.append('}');

		return buf.toString();
	}
}
