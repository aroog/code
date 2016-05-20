package edu.wayne.metrics.adb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;

public abstract class ObjectMetricBase<K, V> extends ClusterMetricInfoBase<K, V> implements ObjectMetricStrategy<K,V> {

	private Set<Collection<V>> outliers = new HashSet<Collection<V>>();

	@Override
    public abstract String getHeader();
	
	@Override
	public void display(String tablePath) throws IOException {
		
		FileWriter writer = new CustomWriter(tablePath);
		writer.append(getHeader());
		ClusterInfo<K, V> subSet = getClusters();
		stats = getClusterStatistics();
		stats.setClusters(subSet);
		writeCluster(writer, subSet);
		
		// Separate out statistics
		writer.append(CSVConst.NEWLINE);

		// Write the stats to the writer, after the clusters
		writeStatistics(writer);		

		writer.append(CSVConst.NEWLINE);
		
		if (ArchMetricsOptions.runQualVisitors){
			visitOutliers(writer, outliers);
		}
		
		writer.flush();
		writer.close();
    }

	private void writeCluster(Writer writer, ClusterInfo<K, V> subSet) throws IOException {
		Map<K, Collection<V>> asMap = subSet.asMap();
		
		for (K key : asMap.keySet()) {
			writer.append(CSVConst.NEWLINE);
			writer.append(CSVConst.NEWLINE);
			Collection<V> collection = asMap.get(key);
			
			
			if (isOutlier(collection)) {
				writer.append('X');
				outliers.add(collection);
			}
			writer.append(CSVConst.COMMA);	
			
			// writer.append(CSVConst.DOUBLE_QUOTES);
			// Sanitize the data
			// NOTE: When sanitizing the data, DO NOT add more double quotes; can lead to problems.
			// Use callback to customize the output, instead of key.toString()
			writer.append(CSVOutputUtils.sanitize( toStringKey(key)));
			// writer.append(CSVConst.DOUBLE_QUOTES);
			
			writer.append(CSVConst.COMMA);
			
			for (V val : collection) {
				writer.append(CSVConst.NEWLINE);
				writer.append(CSVConst.COMMA);
				writer.append(CSVConst.COMMA);
				writer.append(CSVConst.DOUBLE_QUOTES);
				
				// Use callback to customize the output, instead of val.toString()
				writer.append( toStringValue(val) );
				writer.append(CSVConst.DOUBLE_QUOTES);
			}
		}
	}

	@Override
	public void displayShort(String tablePath) throws IOException {
		
		FileWriter writer = new CustomWriter(tablePath);
		writer.append(getHeaderShort());

		ClusterInfo<K, V> subSet = getClusters();
		writeClusterShort(writer, subSet);
		
		writer.flush();
		writer.close();
    }

	private void writeClusterShort(Writer writer, ClusterInfo<K, V> subSet) throws IOException {
		Map<K, Collection<V>> asMap = subSet.asMap();
		
		for (K key : asMap.keySet()) {
			writer.append(CSVConst.NEWLINE);

			Collection<V> collection = asMap.get(key);
			
			writer.append(Integer.toString(collection.size()));
			writer.append(CSVConst.COMMA);
			
			// writer.append(CSVConst.DOUBLE_QUOTES);
			// Sanitize the data
			// NOTE: When sanitizing the data, DO NOT add more double quotes; can lead to problems.
			writer.append(CSVOutputUtils.sanitize(key.toString()));
			// writer.append(CSVConst.DOUBLE_QUOTES);
		}
	}

	@Override
    public DataPoint[] getDataPoints() {
	    return new DataPoint[0];
    }
}
