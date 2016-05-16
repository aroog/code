package edu.wayne.ograph;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class OPTEdge extends OEdge {
	
	@Attribute(required=true, name="fieldName")
	private String fieldName;
	
	protected OPTEdge() {
		super();
	}

	public OPTEdge(@Element(required = true, name = "osrc") OObject osrc,
	        @Element(required = true, name = "odst") OObject odst,
	        @Attribute(required = true, name = "fieldName") String fieldName) {
		super(osrc, odst);
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}
	
	@Override
	public String toString() {
		return super.toString()+"[label=" + fieldName + "]";
	}
}
