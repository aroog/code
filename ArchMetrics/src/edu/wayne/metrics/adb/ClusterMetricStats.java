package edu.wayne.metrics.adb;



/**
 * TODO: Create lightweight wrapper:
 * for Multimap<String, ADBTriplet>
 * that exposes the key
 * 
 * Get set of clusters
 * 
 * Each cluster knows its size
 * 
 * Filter by clusters over a certain threshold
 * 
 * @deprecated Not in use
 */
@Deprecated
public interface ClusterMetricStats {
	
	ClusterInfo<String, ADBTriplet> getClusters();
	
	void setClusters(ClusterInfo<String, ADBTriplet> clusters);
	
	double getMax();
	
	double getMin();
	
	double getAverage();
}
