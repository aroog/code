package oog.re;

/** 
 * Marker interface for the other refinements that do not involve using transfer functions.
 * 
 * These refinements could be pre- or post-processed.
 * 
 * XXX. Do not add methods here; refused bequest
 * But the method names are called very similarly to ones on IOperation.
 *
 */
public interface IOtherRefinement {

	public String getDstDomain();

	public String getSrcObject();

}
