package edu.wayne.summary.strategies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oog.itf.IEdge;
import secoog.EdgeType;
import edu.wayne.ograph.OCFEdge;
import edu.wayne.ograph.OCREdge;
import edu.wayne.ograph.ODFEdge;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OPTEdge;

public class OGraphSingleton {
	// Use lazy instantiation
	private static OGraphSingleton sOGraph = null;

	protected Map<EdgeType, Set<IEdge>> edgeMap = new HashMap<EdgeType, Set<IEdge>>();

	private OGraph oGraph;
	
	// Private constructor to enforce singleton
	private OGraphSingleton() {
	}
	
	public static OGraphSingleton getInstance() {
		if (sOGraph == null) {
			sOGraph = new OGraphSingleton();
		}

		return sOGraph;
	}
	
    public OGraph getGraph() {
	    return oGraph;
    }
    
	public void setGraph(OGraph oGraph) {
		this.oGraph = oGraph;
		
		// Read the types of edges available in the graph
		if (oGraph != null) {
			for (IEdge edge : oGraph.getEdges()) {
				if (edge instanceof ODFEdge) {
					addToEdgeMap(EdgeType.DataFlow, edge);
				}
				if (edge instanceof OPTEdge) {
					addToEdgeMap(EdgeType.PointsTo, edge);
				}
				if (edge instanceof OCFEdge) {
					addToEdgeMap(EdgeType.ControlFlow, edge);
				}
				if (edge instanceof OCREdge) {
					addToEdgeMap(EdgeType.Creation, edge);
				}
			}
	    }
    }

	private void addToEdgeMap(EdgeType type, IEdge edge) {
		if (edgeMap.containsKey(type)) {
			Set<IEdge> set = edgeMap.get(type);
			set.add(edge);
		}
		else {
			Set<IEdge> set = new HashSet<IEdge>();
			set.add(edge);
			edgeMap.put(type, set);
		}
	}
	
	public Set<IEdge> getEdges(Set<EdgeType> types){
		Set<IEdge> edges = new HashSet<IEdge>();
		if (oGraph != null) {
			if (types == null) {
				return oGraph.getEdges();
			}
			else {
				for (EdgeType type : types) {
					Set<IEdge> c = edgeMap.get(type);
					if (c != null) {
						edges.addAll(c);
					}
				}
			}
		}
		return edges;
	}

	public void reset() {
		edgeMap.clear();
		oGraph = null;
	}
}
