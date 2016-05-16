package edu.wayne.ograph;

import org.simpleframework.xml.Attribute;

/**
 * Data structure to contain the name of the Eclipse Resource (file) and the line number. This is used to build a marker
 * and is preferable to holding on to ASTNode objects.
 * 
 * XXX. NOTE: This is the simpler, old version?
 * XXX. Should not have to recompute line number.
 * XXX. PointsToOOG has the resource and the line number. Why not have PointsTo populate this data?
 * 
 */
public class ResourceLineKey {

	@Attribute
	private String resource = null;

	@Attribute
	private int line = 0;

	public ResourceLineKey() {
		super();
	}

	public ResourceLineKey(String resource, int line) {
		super();
		this.resource = resource;
		this.line = line;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(resource.toString());
		buffer.append("; Line: ");
		buffer.append(line);
		return buffer.toString();
	}

	public String getResource() {
		return resource;
	}

	public int getLine() {
		return line;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setLine(int line) {
		this.line = line;
	}

}
