package edu.wayne.metrics.adb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oog.itf.IObject;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;

// TODO: Genericize this class too. To be consistent with ClusterKeyValueBase.
public abstract class ClusterMetricBase extends ClusterMetricInfoBase<String,TripletPairAlt> implements ClusterMetricStrategy<String,IObject,TripletPairAlt> {

	private static final String HEADER = "IsOutlier,ClusterSize,Key,TypeA1,DomainD1,TypeB1,TypeA2,DomainD2,TypeB2";

	private static final String SHORT_HEADER = "ClusterSize,Key";

	private Set<Collection<TripletPairAlt>> outliers = new HashSet<Collection<TripletPairAlt>>();

	@Override
	public String getHeader() {
		return HEADER;
	}
	
	@Override
    public String getHeaderShort() {
	    return SHORT_HEADER;
    }	

	@Override
	public void compute(Set<IObject> allObjects) {
		this.mmap = ClusterInfoFactory.create();
		
		doCompute(allObjects);

		stats = getClusterStatistics();
		stats.setClusters(getClusters());
	}

	// TODO: Expose this in the interface?
	// Subclasses should override this to do the work
	protected void doCompute(Set<IObject> allObjects) {
		if (allObjects != null) {
			for (IObject o1 : allObjects) {
				String key1 = getKey(o1);
				mmap.put(key1, null);

				for (IObject o2 : allObjects) {
					if (o1 == o2)
						continue; // Do not compare to self

					if (satisfiesMetric(o1, o2)) {
						// TODO: HIGH. Use TripletPair
						TripletPairAlt pair = new TripletPairAlt(o1, o2);
						mmap.put(key1, pair);
					}
				}
			}
		}
	}

	public abstract String getKey(IObject o);

	@Override
	// TODO: HIGH. Should this method return a set of TripletPairs?? Instead of boolean?
	public abstract boolean satisfiesMetric(IObject o1, IObject o2);

	// Display clusters (buckets)
	// TODO: Factor out Writer opening/closing
	// TODO: Factor out tablePath, etc.
	@Override
	public void display(String tablePath) throws IOException {

		FileWriter writer = new CustomWriter(tablePath);
		writer.append(getHeader());

		ClusterInfo<String, TripletPairAlt> subSet = getClusters();

		writeClusters(subSet, writer);

		// Separate out statistics
		writer.append(CSVConst.NEWLINE);

		// Write the stats to the writer, after the clusters
		writeStatistics(writer);

		// Close
		writer.flush();
		writer.close();
	}

	private void writeClusters(ClusterInfo<String, TripletPairAlt> allClusters, Writer writer) throws IOException {

		Map<String, Collection<TripletPairAlt>> asMap = allClusters.asMap();
		for (String key : asMap.keySet()) {
			/*
			 * Get the subset of all the clusters based on the key, then create a multimap with the key and clusters
			 * then check if the multimap cluster is a outlier is this necessary or should we change how the
			 * ClusterStatistics gets clusters above threshold?
			 */
			Collection<TripletPairAlt> collection = asMap.get(key);

			writer.append(CSVConst.NEWLINE);
			writer.append(CSVConst.NEWLINE);
			if (isOutlier(collection)) {
				writer.append('X');
				
				outliers.add(collection);
			}
			writer.append(CSVConst.COMMA);

			writer.append(Integer.toString(collection.size()));
			writer.append(CSVConst.COMMA);
			// writer.append(CSVConst.DOUBLE_QUOTES);
			// Sanitize the data
			// NOTE: When sanitizing the data, DO NOT add more double quotes; can lead to problems.
			writer.append(CSVOutputUtils.sanitize(key));
			// writer.append(CSVConst.DOUBLE_QUOTES);

			if (collection != null && !collection.isEmpty()) {
				for (TripletPairAlt pair : collection) {
					// NOTE: could encounter null elements add to the collection
					// Exclude those.
					// TODO: HIGH. XXX. Make sure that nulls do not mess up the statistics.
					if (pair != null) {
						writer.append(CSVConst.NEWLINE);
						writer.append(",,,");
						pair.writeTo(writer);
					}
				}
			}
		}

		if (ArchMetricsOptions.runQualVisitors) {
			visitOutliers(writer, outliers);
		}
	}
	
	@Override
	public void displayShort(String tablePath) throws IOException {

		FileWriter writer = new CustomWriter(tablePath);
		writer.append(getHeaderShort());

		ClusterInfo<String, TripletPairAlt> subSet = getClusters();
		writeClustersShort(subSet, writer);

		// Close
		writer.flush();
		writer.close();
	}
	
	private void writeClustersShort(ClusterInfo<String, TripletPairAlt> allClusters, Writer writer) throws IOException {

		Map<String, Collection<TripletPairAlt>> asMap = allClusters.asMap();
		for (String key : asMap.keySet()) {
			writer.append(CSVConst.NEWLINE);

			Collection<TripletPairAlt> collection = asMap.get(key);
			writer.append(Integer.toString(collection.size()));
			writer.append(CSVConst.COMMA);
			// writer.append(CSVConst.DOUBLE_QUOTES);
			// Sanitize the data
			// NOTE: When sanitizing the data, DO NOT add more double quotes; can lead to problems.
			writer.append(CSVOutputUtils.sanitize(key));
			//writer.append(CSVConst.DOUBLE_QUOTES);
		}
	}	
	
}
