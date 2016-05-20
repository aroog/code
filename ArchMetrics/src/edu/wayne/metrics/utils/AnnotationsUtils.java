package edu.wayne.metrics.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import edu.wayne.metrics.datamodel.ClassMetricItem;
import edu.wayne.metrics.datamodel.ObjectMetricItem;

public class AnnotationsUtils {
	

	public static Hashtable<String, ClassMetricItem> classInfo = new Hashtable<String, ClassMetricItem>();


	
	static String regex = "\"(.)+";
	static Pattern p = Pattern.compile(regex);
	
	public static int extractNoPublicDomains(String domains) {
		if (domains == null) return 0;
		return extractNoItems(domains) - extractNoPrivateDomains(domains); 
    }

	public static int extractNoPrivateDomains(String domains) {
		if (domains == null) return 0;
        if (domains.contains("\"owned\"")) //we can have at most one private domain
        	return 1;
        return 0;
    }
	
	public static int extractNoItems(String items){
		if (items == null) return 0;
		List<String> itemList = extractItems(items);
		return itemList.size();
	}
	
	/**
	 * splits a string of type {("A","B<C,D>","C")} into ["A", "B<C,D>", "C"]
	 * */
	public static List<String> extractItems(String items){
		List<String> itemList = new ArrayList<String>();
		if (items == null) return itemList;
		items = items.replace(" ", ""); //remove all spaces		
		if (items.split("\"(.)*\"").length == 1) return itemList;
		String[] arrayItems = items.split("\",");
		
		for (int i = 0; i < arrayItems.length; i++) {
			//there is exactly one " in each arrayItem[i], and we need the string from the right of it
			String[] rightStr = arrayItems[i].split("\"");
			if (rightStr.length>0)
				itemList.add(rightStr[1]);
        }
		return itemList;
	}

}
