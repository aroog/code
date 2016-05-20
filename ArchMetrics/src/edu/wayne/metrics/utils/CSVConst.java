package edu.wayne.metrics.utils;

public class CSVConst {
	public static final char NEWLINE = '\n';
	public static final char DOUBLE_QUOTES = '\"';
	public static final char COMMA = ',';
	
	// TODO: Add "__" underscore to the list of constants and use the constant.
	
	// TODO: LOW. Remove the underscore from the constant?
	// And have the writer.append add the underscore explicitly?
	// Constants for generating metric names
	// DONE. Drop the "Size" suffix. Max, Min, etc. is enough.
	public static final String _N = "_N";
	public static final String MAX = "_Max";
	public static final String MEAN = "_Mean";
	public static final String MEDIAN = "_Median";
	public static final String MIN = "_Min";
	public static final String STD_DEV = "_StdDev";
	public static final String THRESHOLD = "_Threshold";
	
	// Constants for generating file names
	public static final String R_PREFIX = "R__";
	public static final String R_Qual = "Q__";
	public static final String ALL_METRICS = "All_Metrics";
}
