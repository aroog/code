package edu.wayne.metrics.datamodel;

import java.io.IOException;
import java.io.Writer;

import edu.wayne.metrics.utils.CSVConst;

public class MetricValue {
	private String per;
	private int total = -1;
	private double avg;
	public String getPer() {
		return per;
	}

	public int getTotal() {
		return total;
	}

	public double getAvg() {
		return avg;
	}

	public double getStdDev() {
		return stdDev;
	}

	public double getMax() {
		return max;
	}

	private double stdDev;
	private double max;
	
	public MetricValue(String per, double avg, double stdDev, double max){
		this.per = per;
		this.avg = avg;
		this.stdDev = stdDev;
		this.max = max;
	}
	
	public void setTotal(int total){
		this.total = total;
	}

	
	public void writeHeaders(Writer writer, String metricId) throws IOException{
		StringBuilder builder = new StringBuilder();
		builder.append( metricId);
		builder.append("_");
		String prefix = builder.toString();
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append("AVG" );
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append("STDDEV" );
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append("MAX" );
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		if(total != -1){
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(prefix);
			writer.append("TOTAL" );
			writer.append(CSVConst.DOUBLE_QUOTES);
		}
		
		
	}
	public void writeMetrics(Writer writer) throws IOException{
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getAvg()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getStdDev()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMax()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		if(total != -1){
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(getTotal()));
			writer.append(CSVConst.DOUBLE_QUOTES);
		}
	}
	@Override
	public String toString() {
		return "MetricValue [per=" + per + ", total=" + total + ", avg=" + avg
				+ ", stdDev=" + stdDev + ", max=" + max + "]";
	}
	
	
}
