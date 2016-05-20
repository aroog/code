package edu.wayne.metrics.adb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.wayne.metrics.utils.CSVConst;

public abstract class EdgeMetricBase extends DataPointInfoBase implements EdgeMetricStrategy {
	
	private HashMap<String, DataStatistics> statsMap = new HashMap<String, DataStatistics>();
	
	@Override
    public abstract String getHeader();
	
	protected Set<EdgeInfo> edgeInfos = null;
	
	@Override
	public void display(String oogEdgeTripletsTablePath) throws IOException {
		Set<EdgeInfo> set = getEdgeInfos();
		Writer writer = new CustomWriter(oogEdgeTripletsTablePath);
		writer.append(getHeader());
		
		if (set != null) {
			// First, write all the EdgeInfos
			for (EdgeInfo edgeInfo : set) {
				writer.append(CSVConst.NEWLINE);				
				edgeInfo.writeTo(writer);
			}
			
			// Separate out statistics
			writer.append(CSVConst.NEWLINE);
			
			// Then, write all the stats at the end
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


	private void writeStatistics(Writer writer) throws IOException {
		writeStatistics(writer, false,false);
	}

	private void writeStatistics(Writer writer, boolean writeToSummary, boolean writeHeaders) throws IOException {
		Set<EdgeInfo> set = getEdgeInfos();
		String[] colHeaders = getColumnHeaders();
		if (colHeaders != null) {
			for (int ii = 0; ii < colHeaders.length; ii++) {
				DataStatistics stats = new DataStatisticsImpl();
				String colHeader = colHeaders[ii];
				for (EdgeInfo edgeInfo : set) {

					DataPoint[] dataPoints = edgeInfo.getDataPoints();
					DataPoint dataPoint = dataPoints[ii];
					if (colHeader.compareTo(dataPoint.name)== 0) {
						stats.addData(dataPoint.data);
					}
					else {
						System.err.println("getColumnHeaders() inconsistent with getDataPoints ");
					}
				}
				if(writeToSummary){
					if(writeHeaders){
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
    public DataPoint[] getDataPoints() {
	    return new DataPoint[0];
    }

	@Override
	public Set<EdgeInfo> getEdgeInfos() {
		if(edgeInfos== null){
			return new HashSet<EdgeInfo>();
		}else{
			return edgeInfos;
		}
	}
	
	@Override
	public void writeHeaderToSummary(Writer writer) throws IOException {
		writeDataPointsToSummary(writer);
		writeStatistics(writer, true, true);
	}		
	
	@Override
	public void writeDataToSummary(Writer writer) throws IOException {
		writeDataPointData(writer);
		writeStatistics(writer, true, false);
	}
	
	// TODO: HIGH. Split out computation of outliers from outputting of data
	private void writeOutliers(Writer writer) throws IOException {
		Set<EdgeInfo> set = getEdgeInfos();
		
		Set<EdgeInfo> outliers = new HashSet<EdgeInfo>();
		
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
					for(EdgeInfo edgeInfo:set){
						DataPoint[] dataPoints = edgeInfo.getDataPoints();
						DataPoint dataPoint = dataPoints[i];
						if (header.compareTo(dataPoint.name)== 0) {
							if(dataPoint.data > threshold){
								edgeInfo.writeTo(writer);
								writer.append(CSVConst.NEWLINE);
								// Add to to set of outliers
								outliers.add(edgeInfo);
							}
						}
						else {
							System.err.println("getColumnHeaders() inconsistent with getDataPoints ");
						}
					}
				
				}
			}
		}
		
		if (ArchMetricsOptions.runQualVisitors){
			visitOutliers(writer, outliers);
		}
		
	}
	
	// TODO: Convert this to abstract method
	// Returns Set of EdgeInfo 
	public void visitOutliers(Writer writer, Set<EdgeInfo> outliers)  throws IOException {
    }

	@Override
	public void displayShort(String oogEdgeTripletsTablePath) throws IOException {
		Set<EdgeInfo> set = getEdgeInfos();
		Writer writer = new CustomWriter(oogEdgeTripletsTablePath);
		writer.append(getHeaderShort());
		
		if (set != null) {
			// First, write all the EdgeInfos
			for (EdgeInfo edgeInfo : set) {
				writer.append(CSVConst.NEWLINE);				
				edgeInfo.writeShortTo(writer);
			}
		}
		
		writer.flush();
		writer.close();
	}	
	
	@Override
    public String[] getColumnHeaders() {
	    return null;
    }	
}
