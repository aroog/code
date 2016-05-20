package edu.cmu.cs.viewer.objectgraphs;

import org.simpleframework.xml.Attribute;

/**
 * Data structure to contain the name of the Eclipse Resource (file) and the line number. This is used to build a marker
 * and is preferable to holding on to ASTNode objects.
 */
public class ResourceLineKey {

	@Attribute
	private String resource = null;

	@Attribute
	private int line = 0;

	@Attribute(required=false)
	private String objectName;
	
	@Attribute(required=false)
	private String declaredType;
	
	@Attribute(required=false)
	private String declaringType;

	/**
	 * NOTE: This contains the fully qualified type version of the declaredType field.
	 * IJavaProject.findType requires a fully qualified type name.
	 * XXX: Perhaps, we could get rid of the declaredType field. And use string manipulation to show the short version from the fullyQualifiedTypeName.
	 */
	@Attribute(required=false)
	private String fullyQualifiedTypeName;
		

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

	public String getObjectName() {
    	return objectName;
    }

	public void setObjectName(String objectName) {
    	this.objectName = objectName;
    }

	public String getDeclaredType() {
    	return declaredType;
    }

	public void setDeclaredType(String declaredType) {
    	this.declaredType = declaredType;
    }

	public String getDeclaringType() {
    	return declaringType;
    }

	public void setDeclaringType(String declaringType) {
    	this.declaringType = declaringType;
    }
	
	public String getFullyQualifiedTypeName() {
    	return fullyQualifiedTypeName;
    }

	public void setFullyQualifiedTypeName(String fullyQualifiedTypeName) {
    	this.fullyQualifiedTypeName = fullyQualifiedTypeName;
    }
	
}

