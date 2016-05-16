package edu.wayne.ograph;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.ElementList;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Set of name/value pairs
 * 
 * TODO: Add value equality
 * TODO: Add Simple.XML annotations too.
 */
public class PropertyList {

	@ElementList(required = true)
	@JsonProperty
	private Map<String,String> properties = new HashMap<String,String>();
	
	public void addProperty(String name, String value) {
		properties.put(name, value);
	}
	
	public String getProperty(String name) {
		return properties.get(name);
	}
	
	public String removeProperty(String name) {
		return properties.remove(name);
	}
	
	public void clear() {
		properties.clear();
	}

	@Override
    public String toString() {
	    return properties.toString();
    }
	
}
