package edu.wayne.metrics.mapping;

import org.simpleframework.xml.Attribute;

public class Entry {

	@Attribute(required = false)
	private String type;
	
	public Entry() {
	}


	public Entry(String type) {
	    super();
	    this.type = type;
    }
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	@Override
    public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Type: ");
		builder.append(getType());
		
		return builder.toString();
    }
}
