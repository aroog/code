package oog.itf;

import java.util.Set;

import ast.Type;

// TODO: Add:
// - DONE. getInstanceDisplayName, e.g,. to return "o: C", since O_id may be not reader friendly;
// - DONE. getTypeDisplayName 
// - DONE. hasChildren
//- isTopLevel: return true for top-level objects??

// TODO: Why:
// - OObject has hasParent(); but not exposed in itf?
// - I don't think hasParent() is useful; you still need to check the result of getParent() and if it is not null
// XXX. Gotta expose in IObject: getObjectKey()
public interface IObject extends IElement,IObjectHierarchy {

	// XXX. Rename: getDomains()
	// TODO: Generalize to return a Collection?
	public Set<IDomain> getChildren();

	// XXX. Rename: hasDomains()
	public boolean hasChildren();
	
	public IDomain getParent();

	// TODO: Why do we have hasParent? but not hasChidren?
	public boolean hasParent();

	/**
	 * Return the fully qualified type
	 */
	public Type getC();

	// TODO: VLOW. Why didn't we call this something simpler? getId? underscore makes it harder to read
	public String getO_id();
	
	/**
	 * Return the "o" part of the object's label of the form "o: C"
	 */
	public String getInstanceDisplayName();

	/**
	 * Return the "C" part of the object's label of the form "o: C"
	 * Just return a non-qualified type name, since the fully qualified name is available from getC()
	 */
	public String getTypeDisplayName();

	/**
	 * Return true if this object is in a top-level domain
	 */
	public boolean isTopLevel();

	/**
	 * Return true if this object is the main object (the mainclass is an input to the static analysis)
	 */
	public boolean isMainObject();

	
	/**
	 * Return true if this object is created by resolving unique
	 */
	public boolean isUnique();
	
	/**
	 * Return true if this object is created by resolving lent
	 */
	public boolean isLent();

	/**
	 * Return the object key used to uniquely identify the object: C<D1...Dn>
	 */
	public String getObjectKey();
	
	// CUT: [use the other version]
	//public boolean isSubtypeCompatible(IObject secret);
	
}