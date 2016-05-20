package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;

/**
 * Interface to define simple measures
 */
public interface DataStatistics {

	/**
	 * Clear all the data. Call between invocations.
	 */
	void clear();
	
	/**
	 * Add data
	 */
	void addData(float value);
	
	
	/**
	 * Return largest size 
	 * @return
	 */
	double getMax();
	
	/**
	 * Return the average/mean size 
	 * @return
	 */
	double getMean();
	
	/**
	 * Return the median size
	 * @return
	 */
	double getMedian();
	
	/**
	 * Return the std. deviation 
	 * @return
	 */
	double getStdDev();
	
	/**
	 * Return the smallest size 
	 * @return
	 */
	double getMin();

	/**
	 * 	Fix the prefix for each metric being saved to file:
	 *  Use majorName__metricName__statName
	 *	- Example: Instead of Edge_Precision_MinSize or Precision_Factor_MinSize
     *	- Use Edge_Precision__Precision_Factor_MinSize 
		- since we will be combining all these metrics into one file, for one system.
		
	 * @param majorName: the major metric name, e.g., EdgePrecision
	 * @param minorName: the minor metric name, e.g., PrecisionFactor
	 * @param writer
	 * @throws IOException
	 */
	void writeTo(String majorName, String minorName, Writer writer)  throws IOException;
	
	// TODO: HIGH. instead of having a majorname, minorname, ask for the short metric name
	// So instead of "Edge_PrecisionFactor" or, we can write: "EPF".
	// XXX. Cannot use the majorname to be "Edge". If the filename is the same, the same file will be overwritten.
	void writeHeaders(String majorName, String minorName, Writer writer) throws IOException;
	
	void writeData(Writer writer) throws IOException;

	/**
	 * Return the size of the data set
	 * @return
	 */
	long getN();
	
	double getThreshold();
}
