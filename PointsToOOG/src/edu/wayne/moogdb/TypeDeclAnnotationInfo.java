package edu.wayne.moogdb;

import java.util.Hashtable;
import java.util.List;

public class TypeDeclAnnotationInfo {

	private static Hashtable<String, List<String>> hash = new Hashtable<String, List<String>>();

	public TypeDeclAnnotationInfo() {
	}

	public static void reset() {
		TypeDeclAnnotationInfo.hash.clear();
	}

	public static List<String> get(String fullyQualifiedName) {
	    return hash.get(fullyQualifiedName);
    }

	public static void put(String fullyQualifiedName, List<String> listP) {
		hash.put(fullyQualifiedName, listP);
    }
}
