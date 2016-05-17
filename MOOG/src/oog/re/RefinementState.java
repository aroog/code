package oog.re;

/**
 *	XXX. MoreInfoNeeded: still gotta check TM.moreInfoNeeded being true
 */
public enum RefinementState {
	/**
	 * This refinement admits at least one valid solution
	 */
	Completed,
	
	/**
	 * The user asked for this refinement; it is still not processed;
	 */
	Pending,
	
	/**
	 * This refinement does not admit at least one valid solution
	 */
	Unsupported,
	
	/**
	 * User changed his mind about making this refinement; still in queue; do not process
	 */
	Deleted,
	
	/**
	 * This refinement generates at least one TM where TM.moreInfoNeeded = true.
	 * XXX. Or is it when all the TMs have that flag set? i.e., no TM works without more info?
	 */
	MoreInfoNeeded 
}
