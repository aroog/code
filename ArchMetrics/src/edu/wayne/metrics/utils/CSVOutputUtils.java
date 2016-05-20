package edu.wayne.metrics.utils;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;

import edu.wayne.metrics.adb.CustomWriter;

public class CSVOutputUtils {
	public static void writeTableHeaderToFile(String fileName, String header) throws IOException{
		FileWriter writer = new CustomWriter(fileName);
		writer.append(header);
		writer.flush();
		writer.close();
	}
	
	
	//a row is a string where each item is separated by comma.
	public static void appendRowToFile(String fileName, String projectName, String row) throws IOException{
		FileWriter writer = new CustomWriter(fileName, true); //open to add
		writer.append(projectName+",");
		writer.append(row);
		writer.append('\n');
		writer.flush();
		writer.close();
	}
	
	
	/*
	 * DONE. Also need to escape newline character, e.g., when dealing with anonymous classes.
	 * 
	 * NOTE: Use Apache Commons Lang which has a class to escape a variety of chars, 
	 * according to need, StringEscapeUtils.
	 *  StringEscapeUtils.escapeCsv
	 *  StringEscapeUtils.unescapeCsv
	 */
	public static String sanitize(String data){
		return StringEscapeUtils.escapeCsv(data);
	}
}
