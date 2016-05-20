package edu.wayne.metrics.mapping;

import org.simpleframework.xml.Attribute;

// HACK: Rename PackageType -> PackageEntry
// XXX. Get rid of this package type business. Use either: package name, or fully qualified type. No need for both!
public class PackageType {

	// Attributes needed for Framework and ApplicationDefault Types
	
	@Attribute(required = false)
	private String packageName;
	
	@Attribute(required = false)
	private String typeName;
	
	//Default constructor
	public PackageType() {
	}

	// Constructor
	public PackageType(String packageName, String typeName) {
	    super();
	    this.packageName = packageName;
	    this.typeName = typeName;
	   
    }
	
	// Getters for the attributes
	public String getTypeName() {
		return typeName;
	}
	
	public String getPackageName() {
		return packageName;
	}


	// Setters for the attributes
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public void setType(String type) {
		this.typeName = type;
	}

	@Override
    public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Package: ");
		builder.append(getPackageName());
		builder.append("Type: ");
		builder.append(getTypeName());
		
		return builder.toString();
    }
	
	// Implemented Equals and Hashcode
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof PackageType)) {
			return false;
		}

		PackageType key = (PackageType) o;
		return this.packageName.equals(key.packageName) && this.typeName.equals(key.typeName);
	}
	
	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (packageName == null ? 0 : packageName.hashCode());
		result = 37 * result + (typeName == null ? 0 : typeName.hashCode());
		
		return result;
	}
	
}
