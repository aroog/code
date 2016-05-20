package edu.wayne.metrics.adb;

import java.util.Set;

import oog.itf.IObject;


public interface ClusterObjectStrategy extends MetricInfo {
	
	String getKey(IObject tt);

	void compute(Set<IObject> allObjects);
	
	ClusterInfo<String, IObject> getClusters();
	
	boolean satisfiesMetric(IObject tt1, IObject tt2);


}
