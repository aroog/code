package oog.heuristics;

import java.util.HashSet;

import org.simpleframework.xml.ElementList;

/**
 * Model to guide the heuristics.
 * 
 * NOTE: This was placed in MOOG package in order to use the persistence library.
 * 
 * XXX. Other things to add: input for "abstraction by types"
 * - "design intent types"
 * 
 *
 * XXX. Have multiple ways to refer to things:
 * - by package name;
 * - by reg. ex.;
 * - by individual list of types
 * 
 * Keep the model simple and easy to understand.
 * Or that's the job of the UI. Everything boiled down to fully qualified names?
 * But if a new type gets added to the package, it will not show up here.
 *  
 *  XXX. Add a list of excluded types that we don't want to generate heuristics for:
 *  - e.g., String
 *  
 */
public class HeuristicsModel {

	
	@ElementList(required = false)
	private HashSet<String> collectionTypes = new HashSet<String>();

	@ElementList(required = false)
	// TODO: Rename: ArchitecturalRelevant? IsApplicationDomain type? (overloaded term domain)
	private HashSet<String> applicationTypes = new HashSet<String>();

	protected HeuristicsModel() {
	}
	
	
	public HashSet<String> getCollectionTypes() {
    	return collectionTypes;
    }

	public boolean isCollectionType(String type) {
    	return collectionTypes.contains(type);
    }

	public boolean isApplicationType(String type) {
    	return applicationTypes.contains(type);
    }
	
	// TODO: make this package protected
	public static HeuristicsModel load(String path) {
		// XXX. HACK: hard-coded path
		//String path = "C:\\Temp\\heuristics.xml";
		return Persist.load(path);
	}
	
	public void save(HeuristicsModel model, String path ) {
		Persist.save(model, path);
	}
}
