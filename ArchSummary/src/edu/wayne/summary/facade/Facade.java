package edu.wayne.summary.facade;

import java.util.Set;

import edu.wayne.ograph.OGraph;

// TODO: HIGH. Decision, decisions:
// in signature, expose:
// - IJavaElements? 
// - Or ITypeBindings? 
// - Or Strings? 
// 
//- where to do this computation: 
//-- load the pre-computed OGraph; 
//-- reach into Eclipse AST to get hold of JavaElements? ITypeBindings/
//-- build intermediate data structures?
//-- Or do things on the fly (for now), might not be the most efficient.

// DONE. Create singleton.
// DONE. Create unit tests for testing high-level design.
@Deprecated
public interface Facade {

	// CUT: [for now]
	// public String getAnnotationAssociatedWithIJavaElement(IJavaElement element);


	/**
	 * Return the MIMs(C)
	 */
	Set<String> getMostImportantMethodsOfClass(String javaClass);
	
	/**
	 * 
	 * Return the MIRCs(C)
	 * @param javaClass
	 * @param relationshipType: currently ignored...
	 */
	Set<String> getMostImportantClassesRelatedToClass(String javaClass, RelationshipType relationshipType);
	
	/**
	 * Return the CBIs
	 * @param itf
	 */
	Set<String> getClassesBehindInterface(String itf);

	/**
	 * Return the MICs
	 */
	Set<String> getMostImportantClasses();

	// Get access to the OOG; must be set before calling other methods!
	OGraph getRuntimeModel();
	void setRuntimeModel(OGraph runtimeModel);
}


