package edu.wayne.metrics.mapping;

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.simpleframework.xml.ElementList;


/**
 *
 *  XXX. Silly to store the package and the fully qualified type name
 *  - Why do we need both?
 *  
 *  NOTE: This is the "template".
 *  
 *  XXX. Refine the model to track the standard framework layring in Gamma et al.:
 *  - core fwk package
 *  - default/standard package
 *  - kit package
 *
 *  
 *  Store packages
 *  
 *  
 *  Store types
 *  
 *  Take union of packages (all the types in the package) + the individual types (in case they are misplaced)
 *  
 *  DONE. Store in LinkedHashSet to preserve the insertion order...
 *  
 *  TODO: LOW. Rename Defaulting -> Template
 *  
 *  TODO: LOW. Check this weird inheritance model:
 *  
 *  TODO: HIGH. Gotta store reg. expressions, so can handle package and subpackages.
 *  - without having to list them individually
 *  
 */
public class DefaultingModel extends Model {

	@ElementList(required = false)
	private HashSet<String> frameworkPackages = new LinkedHashSet<String>();

	public void addFrameworkPackage(String fwkPackage) {
		this.frameworkPackages.add(fwkPackage);
    }

	public HashSet<String> getFrameworkPackages() {
    	return frameworkPackages;
    }

	@ElementList(required = false)
	private HashSet<String> applicationPackages = new LinkedHashSet<String>();

	public void addApplicationPackage(String fwkPackage) {
		this.applicationPackages.add(fwkPackage);
    }

	public HashSet<String> getApplicationPackages() {
    	return applicationPackages;
	}
}
