package edu.wayne.metrics.datamodel;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
/**
 * Singleton object that stores the metric values read from the src.code.xml file
 *
 */
public class CodeStructureInfo {
	private static final CodeStructureInfo instance = new CodeStructureInfo();
	private HashMap<String, MetricValue> metricValueMap = new HashMap<String, MetricValue>();
	
	
	private CodeStructureInfo(){
	}
	

	/**
	 * Add the MetricValue to the object hashmap 
	 * @param metricId the metric id, i.e. DOI, NOC, DIT, etc
	 * @param value the statistical data for the metric i.e max, stdDev, avg, total
	 */
	public void putMetric(String metricId, MetricValue value){
		metricValueMap.put(metricId, value);
	}
	public MetricValue get(String metricId){
		return metricValueMap.get(metricId);
	}
	
	/**
	 * Writes the Headers to the writer, used to write the headers to the summary file
	 * 
	 * @param writer 
	 * @throws IOException 
	 */
	public void writeHeaders(Writer writer) throws IOException{
		for(String metricId: metricValueMap.keySet()){
			MetricValue metricValue = metricValueMap.get(metricId);
			metricValue.writeHeaders(writer, metricId);
		}
	}
	
	/**
	 * Writes the metric values to the writer, used to write the metric values to the summary file
	 * 
	 * @param writer 
	 * @throws IOException 
	 */
	public void writeMetrics(Writer writer) throws IOException{
		for(String metricId: metricValueMap.keySet()){
			MetricValue metricValue = metricValueMap.get(metricId);
			metricValue.writeMetrics(writer);
		}
	
	}
	
	/**
	 * Get the singleton instance of this object
	 * @return
	 */
	public static CodeStructureInfo getInstance() {
		return instance;
	}
}
