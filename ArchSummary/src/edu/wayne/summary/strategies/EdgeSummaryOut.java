package edu.wayne.summary.strategies;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;
import secoog.EdgeType;
import ast.Type;
/**
 * Sub-Strategy to count only outgoing edges for MIC and MIRC 
 * @author Andrew
 *
 */
public class EdgeSummaryOut extends EdgeSummaryAll{

	@Override
	protected Set<IEdge> getSummaryEdges(String javaClass,
			Set<EdgeType> edgeTypes) {
		Set<IEdge> edges = new HashSet<IEdge>();
		for (IEdge edge : this.getEdges(edgeTypes)) {

			Type src = edge.getOsrc().getC();
			if(Utils.isSubtypeCompatible(src, javaClass)){
				addEdge(edges, edge);
			}
		}
		return edges;
	}

	@Override
	protected Set<IObject> getClassesFromEdges(Set<IEdge> edges, String javaClass) {
		Set<IObject> objects = new HashSet<IObject>();
		for(IEdge edge:edges){
			if(Utils.isSubtypeCompatible(edge.getOsrc().getC(), javaClass)){
				objects.add(edge.getOdst());
			}
		}
		return objects;
	}
	
	

}
