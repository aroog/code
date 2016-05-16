package edu.wayne.ograph;

import java.util.Hashtable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


// TODO: Add Simple.XML annotations too.
public class OGraphState {

	/**
	 * Map: OObjectKey (String) -> PropertyList
	 */
	@JsonProperty
	private Map<String, PropertyList> map = new Hashtable<String, PropertyList>();
	
	public OGraphState() {
	    super();
    }

	public PropertyList getProperties(String objectKey) {
		return map.get(objectKey);
	}
	
	public void setProperties(String objectKey, PropertyList val) {
		map.put(objectKey, val);
	}
	
	
}
