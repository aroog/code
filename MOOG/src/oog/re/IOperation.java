package oog.re;

public interface IOperation {
	
	String getRefID();
	
	String getSrcObject();
	
	String getDstObject();

	String getDomainName();

	void setState(RefinementState state);

	RefinementState getState();

	// Display name of a refinement?
	String toDisplayName();
	
	// Used to:
	// - let the user pick between preferred typings
	// - show the user why a refinement was unsupported (no valid typings)
	// - track if the user changed things
	RankedTypings getRankedTypings();
	
	boolean isImplicit();
}
