package edu.wayne.metrics.adb;

import java.util.Set;

/**
 * Represent each metric as a strategy, i.e., an object
 *
 */
public interface ClusterMetricStrategy<K,E,V> extends MetricInfo {
	
	K getKey(E tt);

	ClusterInfo<K, V> getClusters();
	
	void compute(Set<E> allTriplets);
	
	// TODO: HIGH. This is no longer being called by compute()
	boolean satisfiesMetric(E tt1, E tt2);
}
