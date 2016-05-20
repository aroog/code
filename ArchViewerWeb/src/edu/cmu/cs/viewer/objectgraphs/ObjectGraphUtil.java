package edu.cmu.cs.viewer.objectgraphs;


public class ObjectGraphUtil {
	
	public static boolean DEBUG = false;

	/**
	 * Characters that are not allowed in DOT URLs, IDs, etc.
	 */
	private static final char[] stripChars = { ':', '<', '>', ' ', '[', ']', '.', ',' };

	private static final VisualReportOptions options = VisualReportOptions.getInstance();

	public static String stripChars(String str) {

		String ret = str;
		if (ret != null) {
			for (int i = 0; i < stripChars.length; i++) {
				if (ret.indexOf(stripChars[i]) > -1) {
					ret = ret.replace(stripChars[i], '_');
				}
			}
		}
		return ret;
	}
}
