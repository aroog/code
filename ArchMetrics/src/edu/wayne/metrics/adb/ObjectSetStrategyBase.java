package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IObject;
import edu.wayne.metrics.utils.CSVConst;

public abstract class ObjectSetStrategyBase extends DataPointInfoBase implements ObjectSetStrategy {
	private HashMap<String, DataStatistics> statsMap = new HashMap<String, DataStatistics>();
	protected Set<IObject> allObjects = null;
	
	// TODO: HIGH. XXX. Should this be created here? Or in the compute method?
	protected Set<ObjectInfo> objectInfos = new HashSet<ObjectInfo>();
	
	@Override
    public abstract String getHeader();

	@Override
	public void display(String tablePath) throws IOException {
		
		Writer writer = new CustomWriter(tablePath);
		writer.append(getHeader());
		
		// First, write all the ObjectInfos
		Set<ObjectInfo> set = getObjectInfos();
		if(set!=null){
			for (ObjectInfo objectInfo : set) {
				writer.append(CSVConst.NEWLINE);
				objectInfo.writeTo(writer);
			}
			
			// Separate out statistics
			writer.append(CSVConst.NEWLINE);
			
			writeStatistics(writer);
		}
		
		// Then write all the metric-level datapoints
		writer.append(CSVConst.NEWLINE);
		writer.append(CSVConst.NEWLINE);
		
		writeDataPoints(writer);
		
		writeOutliers(writer);
		writer.flush();
		writer.close();
    }


	@Override
	public void displayShort(String tablePath) throws IOException {
		
		Writer writer = new CustomWriter(tablePath);
		writer.append(getHeaderShort());
		
		// First, write all the ObjectInfos
		Set<ObjectInfo> set = getObjectInfos();
		if(set!=null){
			for (ObjectInfo objectInfo : set) {
				writer.append(CSVConst.NEWLINE);
				objectInfo.writeShortTo(writer);
			}
			
			// Separate out statistics
			writer.append(CSVConst.NEWLINE);
		}
		
		writer.flush();
		writer.close();
    }

	public Set<ObjectInfo> getObjectInfos() {
    	return objectInfos;
    }
	

	
	private void writeStatistics(Writer writer) throws IOException {
		writeStatistics( writer, false, false);
	}
	
	public void writeStatistics( Writer writer, boolean writeSummary, boolean writeHeader) throws IOException {
		String[] colHeaders = getColumnHeaders();
		Set<ObjectInfo> set = getObjectInfos();
		if (colHeaders != null) {
			for (int ii = 0; ii < colHeaders.length; ii++) {
				String colHeader = colHeaders[ii];
				DataStatistics stats =  new DataStatisticsImpl();
				for (ObjectInfo objectInfo : set) {
					
					DataPoint[] dataPoints = objectInfo.getDataPoints();
					if(dataPoints.length != colHeader.length() ) {
						System.err.println("getColumnHeaders() inconsistent with getDataPoints ");
						// Return prematurely to avoid array bounds exception
						return;
					}
					
					DataPoint dataPoint = dataPoints[ii];
					if (colHeader.compareTo(dataPoint.name)== 0) {
						stats.addData(dataPoint.data);
					}
					else {
						System.err.println("getColumnHeaders() inconsistent with getDataPoints ");
					}
				}
				if(writeSummary){
					if(writeHeader){
						// TODO: Consolidate logic; maybe create a getSummaryName()?
						// Use the short name if available, if not, the long name
						String metricName = getMetricShortName();
						if (metricName == null || metricName.length() == 0) {
							metricName = getMetricName();
						}					
						stats.writeHeaders(metricName, colHeader, writer);
					}else{
						stats.writeData(writer);
					}
				}else{
					stats.writeTo(getMetricName(), colHeader, writer);
				}
				statsMap.put(colHeader, stats);
			}
		}
	}


	
	@Override
    public String[] getColumnHeaders() {
	    return new String[0];
    }
	
	@Override
	public void writeDataToSummary(Writer writer) throws IOException {
		writeDataPointData(writer);
		writeStatistics(writer, true, false);

	}

	@Override
	public void writeHeaderToSummary(Writer writer) throws IOException {
		writeDataPointsToSummary(writer);
		writeStatistics(writer,  true, true);
	}
	
	// TODO: HIGH. Split out computation of outliers from outputting of data
	private void writeOutliers(Writer writer) throws IOException {
		Set<ObjectInfo> set = getObjectInfos();
		
		Set<ObjectInfo> outliers = new HashSet<ObjectInfo>();		
		
		String[] colHeaders = getColumnHeaders();
		if(colHeaders !=null){
			for(int i = 0; i< colHeaders.length; i++){
				String header = colHeaders[i];
				writer.append(CSVConst.NEWLINE);
				writer.append(getMetricName());
				writer.append("_");
				writer.append(header);
				writer.append("_Outliers");
				writer.append(CSVConst.NEWLINE);
				DataStatistics stats = statsMap.get(header);
				if(stats!=null){
					double threshold = stats.getThreshold();
					for(ObjectInfo objectInfo:set){
						DataPoint[] dataPoints = objectInfo.getDataPoints();
						DataPoint dataPoint = dataPoints[i];
						if (header.compareTo(dataPoint.name)== 0) {
							if(dataPoint.data > threshold){
								objectInfo.writeTo(writer);
								writer.append(CSVConst.NEWLINE);
								
								// Add to to set of outliers
								outliers.add(objectInfo);								
							}
						}
						else {
							System.err.println("getColumnHeaders() inconsistent with getDataPoints ");
						}
					}
				}
			}
			
			if (ArchMetricsOptions.runQualVisitors){
				visitOutliers(writer, outliers);
			}
		}
	}

	// TODO: Convert this to abstract method
	public void visitOutliers(Writer writer, Set<ObjectInfo> outliers)  throws IOException {
    }
	
}
