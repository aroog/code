package edu.wayne.summary.strategies;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;
import secoog.EdgeType;
import ast.Type;
/**
 * Sub-Strategy to count only incoming edges for MIC and MIRC 
 * @author Andrew
 *
 */
public class EdgeSummaryIn extends EdgeSummaryAll{


	/**
	 * Get count of all the incoming edges of the specified java class including self edges
	 */
	@Override
	protected Set<IEdge> getSummaryEdges(String javaClass,
			Set<EdgeType> edgeTypes) {
		Set<IEdge> edges = new HashSet<IEdge>();
		for (IEdge edge : this.getEdges(edgeTypes)) {
			
			Type dst = edge.getOdst().getC();
			
			if(Utils.isSubtypeCompatible(dst, javaClass)){
				addEdge(edges, edge);
			}
		}
		return edges;
	}

	@Override
	protected Set<IObject> getClassesFromEdges(Set<IEdge> edges,String javaClass) {
		Set<IObject> objects = new HashSet<IObject>();
		for(IEdge edge:edges){
			if(Utils.isSubtypeCompatible(edge.getOdst().getC(), javaClass)){
				objects.add(edge.getOsrc());
			}
			
		}
		return objects;
	}

}
