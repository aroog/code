package oog.re;

public class IDDictionary {
	private static long NUMBERID = 0;

	public static String generateID() {
		StringBuffer buff = new StringBuffer();
		buff.append(NUMBERID);
		NUMBERID++;
		return buff.toString();
	}

}