package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import edu.wayne.metrics.utils.CSVConst;
public class DataStatisticsImpl implements DataStatistics {
	private DescriptiveStatistics stats = new DescriptiveStatistics();
	
	@Override
	public double getMax() {
		return stats.getMax();
	}

	@Override
	public double getMean() {
		return stats.getMean();
	}

	@Override
	public double getMedian() {
		return stats.getPercentile(50);
	}

	@Override
	public double getStdDev() {
		return stats.getStandardDeviation();
	}

	@Override
	public double getMin() {
		return stats.getMin();
	}
	
	@Override
	public long getN() {
		return stats.getN();
	}
	
	/**
	 * TOAND: Use this prefix when saving the statistics:
		getName() + _ + "MinSize" ...
		so we can get
		"Which_A_In_B_MinSize"
		This will make it easier later on to combine all the metrics into one file.
	 */
	@Override
	public void writeTo(String metricName, String metricStat, Writer writer) throws IOException {
		
		writer.append(CSVConst.NEWLINE);		
		writer.append(CSVConst.NEWLINE);
		
		StringBuilder builder = new StringBuilder();
		builder.append( metricName);
		builder.append("_");
		builder.append(metricStat);
		String prefix = builder.toString();
		
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst._N);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Long.toString(getN()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(CSVConst.NEWLINE);
		
		
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.MAX);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMax()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(CSVConst.NEWLINE);
		
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.MIN);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMin()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(CSVConst.NEWLINE);
		
		
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.STD_DEV);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getStdDev()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(CSVConst.NEWLINE);
		
		
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.MEDIAN);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMedian()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(CSVConst.NEWLINE);
		
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.MEAN);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMean()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(CSVConst.NEWLINE);
		
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.THRESHOLD);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getThreshold()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(CSVConst.NEWLINE);

		


	}

	/**
	 * Add a datapoint.
	 */
	@Override
	public void addData(float value) {
		stats.addValue(value);
	}
	
	/**
	 * Clear the data to date, if reusing the stats object for different metrics.
	 */
	public void clear() {
		stats.clear();
	}

	@Override
	public void writeHeaders(String majorName, String minorName, Writer writer)
			throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append( majorName);
		builder.append("_");
		builder.append(minorName);
		String prefix = builder.toString();
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst._N );
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.MAX);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.MIN);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.STD_DEV);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.MEDIAN);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.MEAN);
		writer.append(CSVConst.DOUBLE_QUOTES);

		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(prefix);
		writer.append(CSVConst.THRESHOLD);
		writer.append(CSVConst.DOUBLE_QUOTES);



		
	}

	@Override
	public void writeData(Writer writer)
			throws IOException {
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Long.toString(getN()));
		writer.append(CSVConst.DOUBLE_QUOTES);

		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMax()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMin()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getStdDev()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMedian()));	
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getMean()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(Double.toString(getThreshold()));
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		
	}

	@Override
	public double getThreshold() {
		return stats.getPercentile(75.0);
	}

}
