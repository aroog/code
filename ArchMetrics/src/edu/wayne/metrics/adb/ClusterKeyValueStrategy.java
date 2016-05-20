package edu.wayne.metrics.adb;

import java.util.Set;


/**
 * Represent each metric as a strategy, i.e., an object
 *
 */
public interface ClusterKeyValueStrategy<K,E,V> extends MetricInfo {

	K getKey(E tt);
	
	V getValue(E tt);	

	ClusterInfo<K,V> getClusters();
	
	void compute(Set<E> allTriplets);
	
	boolean satisfiesMetric(E tt1, E tt2);
	

}
