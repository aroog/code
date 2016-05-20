package edu.wayne.metrics.adb;

import java.util.Collection;
import java.util.Set;

/**
 * Interface to define simple measures on "Object metrics" that involve clusters.
 * This is generic in the type of the key and of the value of the ClusterInfo,
 * since we encounter different types of clusters.
 *  
 * DONE. Add more advanced statistics, like std. deviation, mean/average, median, variance, etc.
 *
 * TODO: Rename:
 * -> ClusterStatistics or ClusterMetrics or ClusterMeasures
 */
public interface ClusterStatistics<K,V> extends DataStatistics {

	ClusterInfo<K,V> getClusters();
	
	void setClusters(ClusterInfo<K,V> clusters);

	/**
	 * Return the largest clusters.
	 * NOTE: This is a set in case there are ties in the largest clusters.
	 */
	Set<ClusterInfo<K,V>> getLargestClusters();
	
	/**
	 * Return the smallest clusters.
	 * NOTE: This is a set in case there are ties in the largest clusters.
	 */
	Set<ClusterInfo<K,V>> getSmallestClusters();	
	
	/**
	 * Return set of clusters the size of which is strictly above threshold value
	 * 
	 */
	Set<ClusterInfo<K,V>> getClustersAboveThreshold(int threshold);
	
	/**
	 * Return true if the cluster is strictly above the threshold
	 * @param cluster
	 * @param threshold
	 * @return
	 */
	boolean isAboveThreshold(Collection<V> cluster, int threshold);

	/**
	 * Return set of clusters the size of which is at or below threshold value
	 */
	Set<ClusterInfo<K,V>> getClustersBelowThreshold(int threshold);
	
	/**
	 * Return true if the cluster is below the threshold (less than or equal to)
	 * @param cluster
	 * @param threshold
	 * @return
	 */
	boolean isBelowThreshold(Collection<V> cluster, int threshold);	
}
