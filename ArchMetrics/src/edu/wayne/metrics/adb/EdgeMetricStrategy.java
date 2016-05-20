package edu.wayne.metrics.adb;

import java.util.Set;

import oog.itf.IEdge;

/*
 * TODO: HIGH. Need to compute stats at the level of the metric object:
 * E.g., %LiftedEdges= |Set<LiftedEdgeInfo>| / |Set<Edge>|
 * Then, across all systems, compute Min., Max., Avg. of %LiftedEdges  (the last step would be done manually) 
 */

// TODO: Rename: EdgeSetStrategy
// TODO: Extract super interface for method getDataPoints...so all the metrics can be saved to a file.
public interface EdgeMetricStrategy extends MetricInfo {

	/**
	 * Compute a Set<EdgeInfo> objects, and hold on to them.
	 * Can optionally hold on to the Set<IEdge>. 
	 * Both the Set<IEdge> and the Set<EdgeInfo> may be useful, e.g., to compute %LiftedEdges
	 */
	void compute(Set<IEdge> allEdges);
	

	
	/**
	 * Used to add headers for statistics computed on the EdgeInfo objects returned by getEdgeInfos. 
	 * The array returned must be consistent with what EdgeInfo.getDataPoints() is returning!
	 * The order must be the same!
	 */
	String[] getColumnHeaders();

	/**
	 * Stored from the previous call to compute()
	 */
	abstract Set<EdgeInfo> getEdgeInfos();
	
	/**
	 * Get the metric-level data
	 */
	DataPoint[] getDataPoints();
}
