package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.core.runtime.IPath;

// TODO: ADD: generateSummary (return true if the metric should be added to the summary)
// TODO: ADD: getMetricNameSummary (typically, use the ShortName)
public interface MetricInfo {

	void writeDataToSummary(Writer writer) throws IOException;

	void writeHeaderToSummary(Writer writer) throws IOException;
	/**
	 * The "major" metric name, e.g., PulledObjects
	 * We often use the same name as the class implementing the metric to avoid confusion.
	 */
	String getMetricName();

	
	/**
	 * If true, generate the concise output to be used as input to R.
	 * By default, it is false. This way, we can exclude metrics for which we are not using R.
	 */
	boolean generateShortOutput();
	
	String getFilePath(IPath location, String projectName, String postfix);
	
	String getFilePathShort(IPath location, String projectName, String postfix);
	
	/**
	 * 
	 * getHeader is used when listing all the EdgeInfos in the CSV file. 
	 * getColumnHeaders is used when generating statistics. 
	 * TODO: MED. The names could be confusing. Rename.
	 */
	String getHeader();
	
	OutputOptions getOutputOptions();
	
	/**
	 * The abbreviated metric name that will contain very concise output used as input to R. 
	 */
	String getMetricShortName();
	
	/**
	 * The short header
	 * @return
	 */
	String getHeaderShort();
	
	OutputOptions getOutputOptionsShort();
	
	void display(String tablePath) throws IOException;
	
	void displayShort(String tablePath) throws IOException;
	
	void display(IPath location, String projectName, String postfix) throws IOException;
	
}
