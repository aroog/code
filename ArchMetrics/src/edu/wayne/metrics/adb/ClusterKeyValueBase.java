package edu.wayne.metrics.adb;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.wayne.metrics.utils.CSVConst;

public abstract class ClusterKeyValueBase<K,E,V> extends ClusterMetricInfoBase<K, V> implements ClusterKeyValueStrategy<K,E,V>  {

	private Set<Collection<V>> outliers = new HashSet<Collection<V>>();

    public abstract String getHeader();
    
	@Override
    public void compute(Set<E> allTriplets) {
    	this.mmap = ClusterInfoFactory.create();
    	if (allTriplets != null) {
    		for (E tt1 : allTriplets) {
    			K key1 = getKey(tt1);
    			// Display trivial clusters too
				mmap.put(key1, null);

    			for (E tt2 : allTriplets) {
    				if (tt1 == tt2) 
    					continue; // Do not compare to self
    				
    				if (satisfiesMetric(tt1, tt2)) {
    					mmap.put(key1, getValue(tt1));
    					mmap.put(key1, getValue(tt2));
    				}
    			}
    		}
    	}
		stats = getClusterStatistics();
		stats.setClusters(getClusters());
    }

	@Override
	public abstract K getKey(E tt);
	
	@Override
	public abstract V getValue(E tt);
	

	@Override
	public abstract boolean satisfiesMetric(E tt1, E tt2);
	
	// Display clusters (buckets)
	// TODO: Factor out Writer opening/closing
	// TODO: Factor out tablePath, etc.
	@Override
	public void display(String tablePath) throws IOException {
		
		FileWriter writer = new CustomWriter(tablePath);
		writer.append(getHeader());
		
		writeClusters(writer);

		// Separate out statistics
		writer.append(CSVConst.NEWLINE);
		
		// Write the stats to the writer, after the clusters
		writeStatistics(writer);
		
		if (ArchMetricsOptions.runQualVisitors){
			visitOutliers(writer, outliers);
		}
		
		// Close
		writer.flush();
		writer.close();
    }
	
	@Override
	public void displayShort(String tablePath) throws IOException {
		
		FileWriter writer = new CustomWriter(tablePath);
		writer.append(getHeaderShort());
		
		writeClustersShort(writer);

		// Close
		writer.flush();
		writer.close();
    }	

	private void writeClusters(FileWriter writer) throws IOException {
		ClusterInfo<K, V> allClusters = getClusters();
		
	    Map<K, Collection<V>> asMap = allClusters.asMap();
		for (K key : asMap.keySet()) {
			Collection<V> collection = asMap.get(key);
		
			writer.append(CSVConst.NEWLINE);
			writer.append(CSVConst.NEWLINE);
			
			if(isOutlier(collection)){
				writer.append('X');
				outliers.add(collection);
			}
			writer.append(CSVConst.COMMA);
			writer.append(Integer.toString(collection.size()));
		
			writer.append(CSVConst.COMMA);
			
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(toStringKey(key));
			writer.append(CSVConst.DOUBLE_QUOTES);
			for (V val : collection) {
				writer.append(CSVConst.NEWLINE);
				// Skip three column
				writer.append(",,,");
				writer.append(toStringValue(val));
			}
		}
    }
	
	private void writeClustersShort(FileWriter writer) throws IOException {
		ClusterInfo<K, V> allClusters = getClusters();
	    Map<K, Collection<V>> asMap = allClusters.asMap();
		for (K key : asMap.keySet()) {
			Collection<V> collection = asMap.get(key);
			writer.append(CSVConst.NEWLINE);
			writer.append(Integer.toString(collection.size()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(key.toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
		}
    }	
}
