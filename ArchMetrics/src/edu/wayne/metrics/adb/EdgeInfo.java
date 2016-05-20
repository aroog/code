package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;

// TODO: Hold on to underlying IEdge
public interface EdgeInfo {

	/**
	 * Get the relevant data for the EdgeInfo to be displayed by the statistics
	 * This handles the case where EdgeInfo has more than one relevant data point
	 */
	DataPoint[] getDataPoints();
	
	void writeTo(Writer writer)  throws IOException;

	void writeShortTo(Writer writer)  throws IOException;
}
