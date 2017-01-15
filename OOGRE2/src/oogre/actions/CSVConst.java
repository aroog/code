package oogre.actions;

import oogre.refinements.tac.AnnotateUnitEnum;

import org.apache.commons.lang3.StringEscapeUtils;

// TODO: Rename: CSVUtils, since not just constants!
public class CSVConst {
	public static final char NEWLINE = '\n';
	public static final char DOUBLE_QUOTES = '\"';
	public static final char COMMA = ',';
	
	// TOEBI: Really? One letter variable names?
	public static final String FIELD = AnnotateUnitEnum.f.toString().toUpperCase();
	public static final String METHOD_PARAM = AnnotateUnitEnum.p.toString().toUpperCase();
	public static final String METHOD_RETURN = AnnotateUnitEnum.r.toString().toUpperCase();
	public static final String VARIABLE = AnnotateUnitEnum.v.toString().toUpperCase();
	
	public static String sanitize(String data){
		// DONE. Strip out newline characters; otherwise, they will simply get escaped by getting enclosed in quotes.
		String noNewLine = data.replaceAll("\r\n|\r|\n", " ");
		// DONE. Also trim whitespaces to the right.
		String trim = noNewLine.trim();
		return StringEscapeUtils.escapeCsv(trim);
	}
	
	public static String getOwner(String ownerAlpha) {
		int idx1 = ownerAlpha.indexOf('<');
		if (idx1 != -1) {
			return ownerAlpha.substring(0, idx1);
		}

		return ownerAlpha;
	}

	public static String getAlpha(String ownerAlpha) {
		int idx1 = ownerAlpha.indexOf('<');
		int idx2 = ownerAlpha.indexOf('>');
		if (idx1 != -1 && idx2 != -1) {
			return ownerAlpha.substring(idx1 + 1, idx2);
		}

		return "";
	}

	// Testing
	public static void main(String[] args) {
		
		String owner1 = getOwner("owned");
		System.out.println(owner1);
		
		String owner2 = getOwner("owned<p>");
		System.out.println(owner2);
		
		String owner3 = getOwner("");
		System.out.println(owner3);
		
		String alpha1 = getAlpha("owner");
		System.out.println(alpha1);
		
		String alpha2 = getAlpha("owner<p>");
		System.out.println(alpha2);
		
		String alpha3 = getAlpha("");
		System.out.println(alpha3);
	}
	
}
