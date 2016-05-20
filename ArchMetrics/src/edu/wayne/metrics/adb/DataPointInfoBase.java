package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;

import edu.wayne.metrics.utils.CSVConst;

public abstract class DataPointInfoBase extends MetricInfoBase {
	protected DataPoint[] dataPoints = new DataPoint[0];
	
	

    public DataPoint[] getDataPoints() {
	    return this.dataPoints;
    }



	protected void writeDataPoints(Writer writer) throws IOException {
		DataPoint[] dataPoints = getDataPoints();
		for (int ii = 0; ii < dataPoints.length; ii++) {
			DataPoint dataPoint = dataPoints[ii];
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(dataPoint.name);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(Float.toString(dataPoint.data));
			writer.append(CSVConst.NEWLINE);
		}
	}



	protected void writeDataPointsToSummary(Writer writer) throws IOException {
		for(DataPoint dataPoint: getDataPoints()){
			StringBuffer buffer = new StringBuffer();
			
			// TODO: Consolidate logic; maybe create a getSummaryName()?
			// Use the short name if available, if not, the long name
			String metricName = getMetricShortName();
			if (metricName == null || metricName.length() == 0) {
				metricName = getMetricName();
			}			
			buffer.append(metricName);
			buffer.append("_");
			buffer.append(dataPoint.name);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(buffer.toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
		}
	}



	protected void writeDataPointData(Writer writer) throws IOException {
		for(DataPoint dataPoint: getDataPoints()){
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Float.toString(dataPoint.data));
			writer.append(CSVConst.DOUBLE_QUOTES);
	
		}
	}
	









}
