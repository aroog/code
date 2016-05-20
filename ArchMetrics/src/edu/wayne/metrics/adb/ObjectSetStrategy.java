package edu.wayne.metrics.adb;

import java.util.Set;

import oog.itf.IObject;

/*
 * TODO: HIGH. Need to compute stats at the level of the metric object:
 * E.g., %PulledObjects = |Set<PulledEdgeInfo>| / |Set<OObject>|
 * Then, across all systems, compute Min., Max., Avg. of %PulledObjects (the last step would be done manually)
 */
public interface ObjectSetStrategy extends MetricInfo{
	
	
	/**
	 * Compute a Set<ObjectInfo> objects, and hold on to them.
	 * Can optionally hold on to the Set<IObject>. 
	 * Both the Set<IObject> and the Set<ObjectInfo> may be useful, e.g., to compute %PulledObjects
	 */
	void compute(Set<IObject> allObjects);
	
	/**
	 * Stored from the previous call to compute()
	 */
	Set<ObjectInfo> getObjectInfos();
	

	
	/**
	 * Get the metric-level data
	 */
	DataPoint[] getDataPoints();
	
	/**
	 * Used to add headers for statistics computed on the ObjectInfo objects returned by getObjectInfos. 
	 * The array returned must be consistent with what ObjectInfo.getDataPoints() is returning!
	 * The order must be the same!
	 */
	String[] getColumnHeaders();	
}

