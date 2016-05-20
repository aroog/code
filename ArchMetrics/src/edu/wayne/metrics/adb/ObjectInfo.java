package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;

//TODO: Hold on to underlying IObject
public interface ObjectInfo {
	
	void writeTo(Writer writer) throws IOException;
	
	void writeShortTo(Writer writer)  throws IOException;

	DataPoint[] getDataPoints();

}
