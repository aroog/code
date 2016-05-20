package edu.wayne.metrics.adb;

import java.util.Set;

import oog.itf.IObject;

/*
 * TODO: HIGH. Need to compute stats at the level of the metric object:
 * E.g., %PulledObjects = |Set<PulledEdgeInfo>| / |Set<OObject>|
 * Then, across all systems, compute Min., Max., Avg. of %PulledObjects (the last step would be done manually)
 */
// TODO: Rename: ObjectMetricStrategy-> ObjectClusterStrategy
public interface ObjectMetricStrategy<K,V>  extends MetricInfo  {
	
	void compute(Set<IObject> allObjects);
	
	// TODO: HIGH. This method does not need to be here; it is introduced in ClusterMetricInfoBase
	/**
	 * Stored from the previous call to compute()
	 */
	ClusterInfo<K, V> getClusters();
	
	/**
	 * Get the metric-level data
	 */
	DataPoint[] getDataPoints();	
}
