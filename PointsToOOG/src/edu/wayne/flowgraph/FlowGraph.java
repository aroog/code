package edu.wayne.flowgraph;

import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.OObject;
import edu.wayne.ograph.internal.OObjectKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ast.Type;

/**
 * 
 * 
 * */
public class FlowGraph {
	private static FlowGraph instance;
	private Set<FlowGraphEdge> flowEdges;
	private Set<FlowGraphNode> nodes;
	private Map<OObjectKey, Set<FlowGraphEdge>> cacheUniqueSource;
	private Map<OObjectKey, Set<FlowGraphEdge>> cacheLentDest;

	private FlowGraph() {
		flowEdges = new HashSet<FlowGraphEdge>();
		cacheUniqueSource  = new HashMap<OObjectKey, Set<FlowGraphEdge>>();
		cacheLentDest  = new HashMap<OObjectKey, Set<FlowGraphEdge>>();		
	}
	
	private FlowGraph(Set<FlowGraphEdge> flowEdges) {
		this();
		this.flowEdges = flowEdges;
	}
	
	public FlowGraph transitiveClosure() {
		Set<FlowGraphEdge> flowEdges = new HashSet<FlowGraphEdge>();
		TransitiveClosure<FlowGraphEdge, FlowGraphNode> transitiveClosure = new TransitiveClosure<FlowGraphEdge, FlowGraphNode>(
				getVertices(), getEdges());
		for (FlowGraphNode v1 : getVertices())
			for (FlowGraphNode v2 : getVertices())
				if (transitiveClosure.hasPath(v1, v2) ) {
					flowEdges.add(new FlowGraphEdge(v1, v2, FlowAnnot.getEmpty()));
				}
		return new FlowGraph(flowEdges);
	}

	/**
	 * implements Computation of summarized flow graph FG*. from Liu'10 - dissertation
	 * PRACTICAL STATIC ANALYSIS FRAMEWORK 
	 * FOR INFERENCE OF 
	 * SECURITY-RELATED PROGRAM PROPERTIES
	 * */
	public FlowGraph summarize() {
		Set<FlowGraphEdge> result = new HashSet<FlowGraphEdge>();
		result.addAll(flowEdges);
		Stack<FlowGraphEdge> WL = new Stack<FlowGraphEdge>();
		for (FlowGraphEdge e : flowEdges) {
			if (e.isCall())
				WL.push(e);
		}
		while (!WL.isEmpty()) {
			FlowGraphEdge e1 = WL.pop();
			//HACK: This is inefficient - create a copy of the set of edges to avoid concurrent modification exception
			Set<FlowGraphEdge> result2 = new HashSet<FlowGraphEdge>();
			result2.addAll(result);
			if (e1.isCall()){
				for (FlowGraphEdge e2 : result2) {
					if (e2.src.equals(e1.dst)) {
						FlowGraphEdge e3 = concat(e1, e2);
						if (e3!=null && !result.contains(e3)) {
							result.add(e3);
							WL.push(e3);
						}
					}
				}
			}
			else if (e1.hasEmptyAnnot() || e1.isStar()){
				for (FlowGraphEdge e2 : result2) {
					if (e2.dst.equals(e1.src)) {
						FlowGraphEdge e3 = concat(e2, e1);
						if (e3!=null && !result.contains(e3)) {
							result.add(e3);
							WL.push(e3);
						}
					}
				}
			}
		}
		return new FlowGraph(result);
	}
	
	private FlowGraphEdge concat(FlowGraphEdge e1, FlowGraphEdge e2) {
		if (e1.isCall() && e1.dst.equals(e2.src) && e2.hasEmptyAnnot()) return new FlowGraphEdge(e1.src,e2.dst,e1.annot);
		if (e1.isCall() && e1.dst.equals(e2.src) && e2.isReturn() && e1.annot.i==e2.annot.i) return new FlowGraphEdge(e1.src,e2.dst,FlowAnnot.getEmpty());
		if (e1.isCall() && e1.dst.equals(e2.src) && e2.isStar() ) 
			return new FlowGraphEdge(e1.src,e2.dst,FlowAnnot.getSTAR());
		//do something about fields? && !(e2.dst.getX() instanceof FieldVariable)
		return null;
	}

	// XXX. We should find a way of minimizing these copies. Why not return a Set?
	public List<FlowGraphEdge> getEdges() {
		List<FlowGraphEdge> l = new ArrayList<FlowGraphEdge>();
		l.addAll(flowEdges);
		return Collections.unmodifiableList(l);
	}

	public List<FlowGraphNode> getVertices() {
		if (nodes == null)
			nodes = new HashSet<FlowGraphNode>();
		for(FlowGraphEdge edge:flowEdges)
		{
			nodes.add(edge.src);
			nodes.add(edge.dst);
		}
		List<FlowGraphNode> l = new ArrayList<FlowGraphNode>();
		l.addAll(nodes);
		return Collections.unmodifiableList(l);
	}
	/**
	 * adapted from http://code.google.com/p/jbpt/
	 * see TransitiveClosure class
	 * invariant: once the transitive closure is computed the graph does not change
	 * */
	class TransitiveClosure<E extends FlowGraphEdge,V extends FlowGraphNode> {

		private List<V> verticesAsList;
		private List<E> edgesAsList;
		private boolean[][] matrix;
		
		
		public TransitiveClosure(List<V> verticesAsList, List<E> edgesAsList ) {
			this.verticesAsList = verticesAsList;
			this.edgesAsList = edgesAsList;
			this.matrix = new boolean[this.verticesAsList.size()][this.verticesAsList.size()];
			calculateMatrix();
		}

		private void calculateMatrix() {

			/*
			 * Init matrix with edges
			 */
			for (E e: this.edgesAsList) {
				int source = this.verticesAsList.indexOf(e.src);
				int target = this.verticesAsList.indexOf(e.dst);
				matrix[source][target] = true;
			}
			
			/*
			 * Compute the transitive closure
			 */
			for (int i = 0; i < matrix.length; i++) 
				for (int j = 0; j < matrix.length; j++) 
					if (matrix[j][i])
						for (int k = 0; k < matrix.length; k++)
							matrix[j][k] = matrix[j][k] | matrix[i][k];
		}
		
		/**
		 * Check if there exists a directed path between two vertices
		 * @param v1 Vertex
		 * @param v2 Vertex
		 * @return <code>true</code> if there is a directed path from v1 to v2, <code>false</code> otherwise 
		 */
		public boolean hasPath(V v1, V v2) {
			int i = this.verticesAsList.indexOf(v1);
			int j = this.verticesAsList.indexOf(v2);
			return matrix[i][j];
		}
		
		/**
		 * Check if vertex is part of a loop
		 * @param v Vertex
		 * @return <code>true</code> if vertex is part of a loop, <code>false</code> otherwise
		 */
		public boolean isInLoop(V v) {
			int index = this.verticesAsList.indexOf(v);
			return matrix[index][index];
		}
		
		@Override
		public String toString() {
			if (matrix == null)
				calculateMatrix();
			
			String result = "";
			
			result += "==================================================\n";
			result += " Transitive Closure\n";
			result += "--------------------------------------------------\n";
			for (int i=0; i<verticesAsList.size(); i++)
				result += String.format("%d : %s\n", i, verticesAsList.get(i).toString());
			result += "--------------------------------------------------\n";
			result += "    ";
			for (int i=0; i<verticesAsList.size(); i++) result += String.format("%-4d", i);
			result += "    \n";
			for (int i=0; i<verticesAsList.size(); i++) {
				result += String.format("%-4d", i);
				for (int j=0; j<verticesAsList.size(); j++) {
					result += String.format("%-4s",(matrix[i][j] ? "+" : "-"));
				}
				result += String.format("%-4d", i);
				result += "\n";
			}
			result += "    ";
			for (int i=0; i<verticesAsList.size(); i++) result += String.format("%-4d", i);
			result += "    \n";
			result += "==================================================";
			
			return result;
		}

	}
	public static FlowGraph getInstance() {
		if (instance==null){
			instance = new FlowGraph();
		}			
		return instance;
	}

	public void clear() {
		if (nodes!=null){
			nodes.clear();
			nodes = null;
		}
		flowEdges.clear();
		cacheUniqueSource.clear();
		cacheLentDest.clear();
		flowEdges = null;
		instance = null;
	}

	public FlowGraph clone(){
		FlowGraph clone = new FlowGraph();
		for (FlowGraphEdge edge : flowEdges) {			
			clone.flowEdges.add(new FlowGraphEdge(edge.src,edge.dst,edge.annot));
		}
		return clone;
	}

	public String print(){
//		TransitiveClosure<FlowGraphEdge,FlowGraphNode> transitiveClosure = new TransitiveClosure<FlowGraphEdge, FlowGraphNode>(getVertices(), getEdges());
//		return transitiveClosure.toString();
		StringBuffer sb = new StringBuffer();
		for (FlowGraphEdge edge : flowEdges) {
			sb.append(edge);
			sb.append("\n");
		}
		return sb.toString();
	}

	public void addInfoFlow(FlowGraphEdge edge) {
		FlowGraphNode v1 = edge.src;
		FlowGraphNode v2 = edge.dst;
		if (v1.getB().isUnique() && cacheUniqueSource.containsKey(v1.getO()))
			cacheUniqueSource.get(v1.getO()).add(edge);
		if (v2.getB().isLent() && cacheLentDest.containsKey(v2.getO()))
			cacheLentDest.get(v2.getO()).add(edge);
		flowEdges.add(edge);
	}

	public Set<FlowGraphEdge> findEdgesWithUniqueSource(OObject o) {
    if (cacheUniqueSource.containsKey(o.getKey()))
    	return cacheUniqueSource.get(o.getKey());
		Set<FlowGraphEdge> result = new HashSet<FlowGraphEdge>();
		for (FlowGraphEdge edge:flowEdges){
			FlowGraphNode srcNode = edge.src;
			FlowGraphNode dstNode = edge.dst;
			DomainP srcNodeB = srcNode.getB();
			if (srcNode.getO().equals(o) 
					&& srcNodeB.isUnique()
					&& !dstNode.getB().isLent()
					)
				result.add(edge);
		}
		cacheUniqueSource.put(o.getKey(), result);
		return result;
	}

	// XXX. This is very slow. Many edges. Sparse graph.
	public Set<FlowGraphEdge> findEdgesWithLentDest(OObject o) {
		if (cacheLentDest.containsKey(o.getKey()))
			return cacheLentDest.get(o.getKey());
		Set<FlowGraphEdge> result = new HashSet<FlowGraphEdge>();
		for (FlowGraphEdge edge : flowEdges) {
			if (edge.dst.getO().equals(o) && edge.dst.getB().isLent()
					&& !edge.src.getB().isLent())
				result.add(edge);
		}
		cacheLentDest.put(o.getKey(), result);
		return result;
	}

	public void copy(FlowGraph fg) {
		this.flowEdges.addAll(fg.flowEdges);
		if (nodes != null && fg.nodes != null)
			this.nodes.addAll(fg.nodes);
	}

	// XXX. How to check if a graph is sub-graph
	// This is very slow. By short-circuiting the lent/unique, sped things up quite a bit.
	// We need to speed things up for analyzing lent/unique.
	public boolean isSubGraph(FlowGraph fg) {
		return this.flowEdges.containsAll(fg.flowEdges);
	}

	public Set<FlowGraphEdge> findEdgesWithUniqueSrcDest(OObject o, ast.Type C) {
		Set<FlowGraphEdge> flowEdgesSubset = flowEdges;
		if (cacheUniqueSource.containsKey(o.getKey())) {
			flowEdgesSubset = cacheUniqueSource.get(o.getKey());
		}
		Set<FlowGraphEdge> result = new HashSet<FlowGraphEdge>();
		for (FlowGraphEdge edge:flowEdgesSubset){
			FlowGraphNode srcNode = edge.src;
			FlowGraphNode dstNode = edge.dst;
			DomainP dstNodeB = dstNode.getB();
			ast.Type tDst = Type.createFrom(dstNode.getX().resolveType());
			if (dstNode.getO().equals(o) 
					&& tDst.isSubtypeCompatible(C)
					&& dstNodeB.isUnique()
					&& srcNode.getB().isUnique()
					)
				result.add(edge);
		}
		return result;
	}
	
	public Set<FlowGraphEdge> propagate(FlowGraphNode s){
		Set<FlowGraphEdge> result = new HashSet<FlowGraphEdge>();
		Stack<FlowGraphEdge> WL = new Stack<FlowGraphEdge>();
		List<FlowGraphEdge> fEdges =  getEdges();
		for (FlowGraphEdge edge : fEdges) {
			if (edge.src.equals(s)){
				if (s.getB().isLent() || s.getB().isUnique() || s.getB().isPublic() 
						|| edge.dst.getB().isLent() || edge.dst.getB().isUnique() || edge.dst.getB().isPublic() )
				{
				result.add(edge);
				WL.push(edge);
				}
			}
		}
		while (!WL.isEmpty()){
			FlowGraphEdge e1  = WL.pop();
			for (FlowGraphEdge e2 : fEdges) {
				if (e1.dst.equals(e2.src)){
					FlowGraphEdge e3 = concatP(e1,e2);
					if (e3!=null && !result.contains(e3)){
						result.add(e3);
						WL.push(e3);
					}
				}
			}
		}
		return result;
	}
	
	private FlowGraphEdge concatP(FlowGraphEdge e1, FlowGraphEdge e2){
		if (e1.isCall() && e2.isCall()) return new FlowGraphEdge(e1.src, e2.dst, FlowAnnot.getCall());
		if (e1.isCall() && (e2.hasEmptyAnnot()||(e2.isReturn()||e2.isStar()))) return new FlowGraphEdge(e1.src, e2.dst, FlowAnnot.getCall());
		if (!e1.isCall() && e2.isCall()) return new FlowGraphEdge(e1.src, e2.dst, FlowAnnot.getCall());
		if (!e1.isCall() && (e2.hasEmptyAnnot()||(e2.isReturn()||e2.isStar()))) return new FlowGraphEdge(e1.src, e2.dst, FlowAnnot.getnCall());
		return null;
	}

	public FlowGraph propagateAll() {
		Set<FlowGraphEdge> allEdges = new HashSet<FlowGraphEdge>();
		//allEdges.addAll(flowEdges);
		allEdges.addAll(summarize().getEdges());
		for (FlowGraphNode s: getVertices()) {
//			if (s.getB().isLent() || s.getB().isUnique() || s.getB().isPublic())
				allEdges.addAll(propagate(s));
		}
		return new FlowGraph(allEdges);
	}
}
