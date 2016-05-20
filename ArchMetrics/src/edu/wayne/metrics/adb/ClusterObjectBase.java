package edu.wayne.metrics.adb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import oog.itf.IObject;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;

/**
 * @deprecated This base class is not being used!
 */
@Deprecated
public abstract class ClusterObjectBase extends ClusterMetricInfoBase<String, IObject> implements ClusterObjectStrategy {
	private static final String TRIPLET_TABLE_HEADER_OGRAPH = "ObjectA,TypeA,TypeA_raw,DomainD,DomainD_raw,ObjectB,TypeB,TypeB_raw";
	
	@Override
	public String getHeader() {
		return TRIPLET_TABLE_HEADER_OGRAPH;
	}

	@Override
    public void compute(Set<IObject> allObjects) {
		this.mmap = ClusterInfoFactory.create();
		
    	if (allObjects != null) {
    		for (IObject tt1 : allObjects) {
    			String key1 = getKey(tt1);
    			
    			for (IObject tt2 : allObjects) {
    				if (tt1 == tt2) 
    					continue; // Do not compare to self
    				
    				if (satisfiesMetric(tt1, tt2)) {
    					mmap.put(key1, tt1);
    					mmap.put(key1, tt2);
    				}
    			}
    		}
    	}
		stats = getClusterStatistics();
		stats.setClusters(getClusters());
    }

	@Override
	public abstract String getKey(IObject tt);

	@Override
	public abstract boolean satisfiesMetric(IObject tt1, IObject tt2);
	
	// Display clusters (buckets)
	// TODO: Factor out Writer opening/closing
	// TODO: Factor out tablePath, etc.
	public void display(String tablePath) throws IOException {
		FileWriter writer = new CustomWriter(tablePath);
		writer.append(getHeader());
		ClusterInfo<String, IObject> subSet = getClusters();
		writeClusters(subSet, writer);
		
		// Separate out statistics
		writer.append(CSVConst.NEWLINE);
		
		// Write the stats to the writer, after the clusters
		writeStatistics(writer);		
		
		writer.flush();
		writer.close();
    }

	private void writeClusters(ClusterInfo<String, IObject> subSet, Writer writer) throws IOException {
		Map<String, Collection<IObject>> asMap = subSet.asMap();
		for (String key : asMap.keySet()) {
			Collection<IObject> collection = asMap.get(key);
			
			writer.append(CSVConst.NEWLINE);
			writer.append(CSVConst.NEWLINE);
			
			if (isOutlier(collection)) {
				writer.append('X');
			}
			writer.append(CSVConst.COMMA);			
			
			// writer.append(CSVConst.DOUBLE_QUOTES);
			// Sanitize the data
			// NOTE: When sanitizing the data, DO NOT add more double quotes; can lead to problems.
			writer.append(CSVOutputUtils.sanitize(key));
			// writer.append(CSVConst.DOUBLE_QUOTES);

			for (IObject triplet : collection) {
				writer.append(CSVConst.NEWLINE);
				write(triplet, writer);
			}
		}
	}

	private void write(IObject triplet, Writer writer) throws IOException {
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
        writer.append(triplet.getC().toString());
        writer.append(CSVConst.DOUBLE_QUOTES);

        writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
        writer.append(triplet.getParent().getD_id());
		writer.append(CSVConst.DOUBLE_QUOTES);
    }
	
}
