package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class ClusterMetricInfoBase<K, V> extends MetricInfoBase {
	private static final String MINOR_NAME = "Cluster";
	protected ClusterInfo<K, V> mmap = null;
	protected ClusterStatistics<K, V> stats;

	public ClusterInfo<K, V> getClusters() {
		return mmap;
	}
	
	public String toStringKey(K key) {
		return key.toString();
	}
	
	public String toStringValue(V val) {
		return val.toString();
	}
	
	public ClusterStatistics<K, V> getClusterStatistics() {
		return new ClusterStatisticsImpl<K, V>();
	}

	// TODO: Keep for now. Consider deleting.
	public boolean isOutlier(ClusterInfo<K, V> cluster) {
		if (stats != null) {
			Set<ClusterInfo<K, V>> clustersAboveThreshold = stats.getClustersAboveThreshold((int) stats.getThreshold());
			return clustersAboveThreshold.contains(cluster);
		}
		return false;
	}
	
	public boolean isOutlier(Collection<V> values) {
		if (stats != null) {
			return stats.isAboveThreshold(values, (int) stats.getThreshold());
		}
		return false;
	}

	// TODO: Convert this to abstract method
	public void visitOutliers(Writer writer, Set<Collection<V>> outliers)  throws IOException {
    }
	
	public void writeStatistics(Writer writer, boolean writeToSummary,
			boolean writeHeaders) throws IOException {
		if (writeToSummary) {
			if (writeHeaders) {
				// TODO: Consolidate logic; maybe create a getSummaryName()?
				// Use the short name if available, if not, the long name
				String metricName = getMetricShortName();
				if (metricName == null || metricName.length() == 0) {
					metricName = getMetricName();
				}
				stats.writeHeaders(metricName, MINOR_NAME, writer);
			} else {
				stats.writeData(writer);
			}
		} else {
			stats.writeTo(getMetricName(), MINOR_NAME, writer);
		}
	}

	@Override
	public void writeDataToSummary(Writer writer) throws IOException {
		writeStatistics(writer, true, false);
	}

	@Override
	public void writeHeaderToSummary(Writer writer) throws IOException {
		writeStatistics(writer, true, true);
	}

	protected void writeStatistics(Writer writer) throws IOException {
		writeStatistics(writer, false, false);
	}
}
