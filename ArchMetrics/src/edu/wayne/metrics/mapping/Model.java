package edu.wayne.metrics.mapping;

import java.util.ArrayList;
import java.util.HashSet;

import org.simpleframework.xml.ElementList;


// TOSUM: TODO: Add all the things that must be stored in the map file here:
// - list of types
// - etc.
// XXX. Store in LinkedHashSet to preserve the insertion order...
public class Model {
	
	// Elements of the XML file
	@ElementList(required = false)
	private ArrayList<Entry> mappings = new ArrayList<Entry>();
	
	@ElementList(required = false)
	private HashSet<String> containerTypes = new HashSet<String>();
	
	@ElementList(required = false)
	private HashSet<String> javaLibTypes = new HashSet<String>();
	
	// XXX. Rename: DataType -> DataClass
	@ElementList(required = false)
	private HashSet<String> dataTypes = new HashSet<String>();
	
	@ElementList(required = false)
	private HashSet<String> exceptionTypes = new HashSet<String>();
	
	@ElementList(required = false, name="appFrameworkTypes")
	private HashSet<PackageType> frameworkPackageTypes = new HashSet<PackageType>();
	
	@ElementList(required = false, name = "appDefaultTypes")
	private HashSet<PackageType> applicationPackageTypes = new HashSet<PackageType>();
	

	// Getters
	public HashSet<String> getContainerTypes() {
    	return containerTypes;
    }

	public ArrayList<Entry> getMappings() {
		return this.mappings;
	}
	
	public HashSet<String> getJavaLibTypes() {
		return javaLibTypes;
	}
	
	public HashSet<String> getDataTypes() {
		return dataTypes;
	}

	public HashSet<String> getExceptionTypes() {
		return exceptionTypes;
	}

	public HashSet<PackageType> getApplicationPackageTypes() {
		return applicationPackageTypes;
	}
	
	public HashSet<PackageType> getFrameworkPackageTypes() {
		return frameworkPackageTypes;
	}
	

	
	// Add methods
	public void addContainerType(String containerType) {
    	this.containerTypes.add(containerType);
    }

	public void addMapping(Entry mapping) {
		this.mappings.add(mapping);
	}
	
	public void addJavaLibType(String javaLibType) {
		this.javaLibTypes.add(javaLibType);
	}

	public void addDataType(String dataType) {
		this.dataTypes.add(dataType);
	}
	
	public void addExceptionType(String exceptionType) {
		this.exceptionTypes.add(exceptionType);
	}
	
	// XXX. Change signature: PackageType -> String
	public void addFrameworkPackageType(PackageType packType) {
		this.frameworkPackageTypes.add(packType);
	}
	
	// XXX. Change signature: PackageType -> String
	public void addApplicationPackageType(PackageType packageType) {
		this.applicationPackageTypes.add(packageType);
	}
	
	public boolean hasMapping(String qualifiedName) {
	    return false;
    }
	
	public void finish() {
	}


}
