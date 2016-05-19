package edu.wayne.summary.strategies;

import java.util.Set;

import oog.itf.IEdge;


public abstract class EdgeSummaryBase extends EdgeSummary {

	@Override
	protected boolean includeSelfEdges() {
		return true;
	}

	public boolean isSelfEdge(IEdge edge){
		return edge.getOdst().equals(edge.getOsrc());
		
	}

	protected void addEdge(Set<IEdge> edges, IEdge edge) {
		if(includeSelfEdges()){
			edges.add(edge);
		}else{
			if(!isSelfEdge(edge)){
				edges.add(edge);
			}
		}
	}
}
