package oog.itf;

import java.util.Set;

import ast.Type;

// TODO: Add MISSING methods:
// - DONE. isPublic
// - DONE. hasChildren?
// - getQualifiedName to get the "C::d"? or does getD() already return "C::d"? We need the "C::d" for ArchMetrics.
//   We may not able to completely shield clients from the "C::d" business.
// - isTopLevel: return true for top-level domains??
// - isShared: return true for SHARED domain
public interface IDomain extends IElement {

	/**
	 * Return true for public domains
	 */
	public boolean isPublic();

	// TODO: Rename?! getObjects()?
	// TODO: Generalize to return a Collection?	
	public Set<IObject> getChildren();

	// TODO: Rename?! hasObjects?
	public boolean hasChildren();
	
	// NOTE: An ODomain does not have a unique parent
	public Set<IObject> getParents();

	// TODO: VLOW. Why didn't we call this something simpler? getId? underscore makes it harder to read
	public String getD_id();

	/**
	 * this is C from "C::d"
	 */
	public Type getC();
	
	/**
	 * this is d from "C::d"
	 */
	public String getD();

	public boolean isTopLevel();
}