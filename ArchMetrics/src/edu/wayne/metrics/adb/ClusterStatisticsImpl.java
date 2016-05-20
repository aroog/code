package edu.wayne.metrics.adb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// TODO: HIGH. XXX. Generate datapoints automatically to be able to plot.
// TODO: HIGH. XXX. For the cluster statistics, are already excluding the trivial clusters?
public class ClusterStatisticsImpl<K,V> extends DataStatisticsImpl implements ClusterStatistics<K,V>  {

	private ClusterInfo<K,V> clusters = null;

	@Override
	public ClusterInfo<K,V> getClusters() {
		return this.clusters;
	}

	@Override
    public boolean isAboveThreshold(Collection<V> cluster, int threshold) {
	    return compare(cluster.size(), threshold, LogicOperator.GREATER_THAN);
    }

	@Override
    public boolean isBelowThreshold(Collection<V> cluster, int threshold) {
	    return compare(cluster.size(), threshold, LogicOperator.LESS_THAN_OR_EQUAL);
    }

	
	@Override
	// NOTE: For AboveThreshold, return strictly greater; for below, return less than or equal;
	public Set<ClusterInfo<K,V>> getClustersAboveThreshold(int threshold) {
// TOAND: TODO: Why hard-code a threshold? Avoid magic numbers.
//		if (threshold < 2) {// should I return an empty set or null
//			return new HashSet<ClusterInfo<K,V>>();
//		} else {
			// return getClusters(threshold, LogicOperator.GREATER_THAN_OR_EQUAL);
			return getClusters(threshold, LogicOperator.GREATER_THAN);
//		}
	}

	@Override
	public Set<ClusterInfo<K,V>> getClustersBelowThreshold(int threshold) {
		return getClusters(threshold, LogicOperator.LESS_THAN_OR_EQUAL);
	}

	@Override
	public Set<ClusterInfo<K,V>> getLargestClusters() {
		return getClusters((int) super.getMax(), LogicOperator.EQUAL);
	}

	@Override
	public Set<ClusterInfo<K,V>> getSmallestClusters() {
		return getClusters((int) super.getMin(), LogicOperator.EQUAL);
	}

	@Override
	public void setClusters(ClusterInfo<K,V> clusters) {
		this.clusters = clusters;
		
		super.clear();
		
		for (K key : clusters.keySet()) {
			super.addData(clusters.get(key).size());
		}
	}

	/**
	 * Gets the clusters of a specific size
	 * 
	 * @param size
	 */
	// TODO: find if there is a way to get clusters without having to iterate
	// through the entire ClusterInfo over and over.
	private Set<ClusterInfo<K,V>> getClusters(int size, LogicOperator operator) {
		Set<ClusterInfo<K,V>> retSet = new HashSet<ClusterInfo<K,V>>();
		for (K key : clusters.keySet()) {
			Collection<V> cluster = clusters.get(key);
			if (compare(cluster.size(), size, operator)) {
				ClusterInfo<K,V> multimap = ClusterInfoFactory.create();
				multimap.putAll(key, cluster);
				retSet.add(multimap);
			}
		}

		return retSet;
	}

	private boolean compare(int size, int compareSize, LogicOperator operator) {
		switch (operator) {
		case EQUAL:
			return size == compareSize;
		case GREATER_THAN:
			return size > compareSize;
		case LESS_THAN:
			return size < compareSize;
		case GREATER_THAN_OR_EQUAL:
			return size >= compareSize;
		case LESS_THAN_OR_EQUAL:
			return size <= compareSize;
		}
		return false;
	}

	private enum LogicOperator {
		EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL;
	}
}
