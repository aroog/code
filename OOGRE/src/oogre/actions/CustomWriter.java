package oogre.actions;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Custom Writer: flag cases where blank text is being written to CSV. If writing a blank/null string to CSV, replace it
 * with a non-blank string, e.g., "XXX", AND output a meaningful warning to Console Window.
 * 
 * TODO: Check empty strings
 * TODO: Check empty escaped/quoted string; it may not be legal for R either.
 * 
 * XXX. Check for nulls everywhere. if the file is locked, constructor will throw exception.
 * 
 * TODO: Add simple overload: write name, value 
 */
public class CustomWriter  {
	private  FileWriter writer;
	
	protected static final String U_SEP = "_";
	protected static final String BAD_SEP = "<|>";
	
	/**
	 *
	 * The toString() of a Refinement may contain characters that cannot be used in a filename, such as < or > for generics.
	 * NOTE: [] are allowed. 
	 * May we encounter ? for wildcards?
	 * So replace with underscores.
	 */
	private static String replaceAll(String id) {
	    String replaceAll = id.replaceAll(BAD_SEP, U_SEP);
	    // DONE. Double-check to remove all spaces; including this in the DOT_SEP regex does not seem to do it!
		return  replaceAll.replaceAll("\\s+", U_SEP);
    }	

	public CustomWriter(String fileName) {
		try {
			writer = new FileWriter(replaceAll(fileName));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// XXX. Add escaping...
	public void write(String str) {
		try {
	        writer.write(str);
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
	}

	public void append(int val) {
		try {
	        writer.append(Integer.toString(val));
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
	}
	
	public void append(String data) {
		try {
			// DONE. Do not always escape. Let the caller do it.
	        // writer.append(escape(data));
			writer.append(data);
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
    }
	
	public void appendln(String data) {
		try {
			// DONE. Do not always escape. Let the caller do it.
	        // writer.append(escape(data));
			writer.append(data);
			writer.append(CSVConst.NEWLINE);
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
    }	

//	// CUT: Very simplistic impl. Gotta escape properly...
//	private String escape(String string) {
//		return string.replaceAll("\n", "_");
//	    
//    }

	// NOTE: No escaping here!
	public void append(char c)  {
	    try {
	        writer.append(c);
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
    }

	public void close() {
		try {
	        writer.close();
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
    }



}
