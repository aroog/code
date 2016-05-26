package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * DONE. We probably need a pair <owner,alpha>. Then have OType use that. 
 *
 * XXX. Should probably hash things out, since this will be called a lot. To minimize calls to equals...
 * - at least, intern the strings...
 * 
 * XXX. Can we implement "dynamic ranking"?
 * - e.g., take into account the class  of the AU (e.g., java.util...)
 * - take into account the type of the expression being annotated (object creation expression, etc.)
 * 
 * XXX. This hard-codes the name of the public domain to be PD.
 * - How will we rank PD2, etc.?
 * - Need more general solution
 * 
 * XXX. This is doing linear search! Can we use something more efficient?!
 */
public class RankingStrategy {
	
	private static RankingStrategy instance = null;
	
	private RankingStrategy() {
	}
	
	private ArrayList<OwnerAlpha> rank1 = new ArrayList<OwnerAlpha>();

	private ArrayList<String> rankInner = new ArrayList<String>();	
	
	// DONE. Assume "n.d" -> "d" (n == this)
	
	// Overall rank:
	//  "PD", "owned", "owner", "p", "any", "shared"
	// Justification:
	// - we will use heuristics to infer owned;
	// - refinements apply after that;
	// - so prefer PD to owned (would have been found), and owner (leads to flatter trees)

	// OK to leave things that may not occur.
	// Collections.min requires that All elements in the collection must be <i>mutually comparable</i> by the comparator
	private void initStrategy() {
		rank1.add(new OwnerAlpha("this.owned", "this.owned")); //
		rank1.add(new OwnerAlpha("this.owned", ".PD")); //
		rank1.add(new OwnerAlpha("this.owned", "this.PD")); //
		rank1.add(new OwnerAlpha("this.owned", ".any")); //
		rank1.add(new OwnerAlpha("this.owned", "this.any")); //
		rank1.add(new OwnerAlpha("this.owned", "owner")); //
		rank1.add(new OwnerAlpha("this.owned", "p")); //
		rank1.add(new OwnerAlpha("this.owned", "shared")); //
		
		rank1.add(new OwnerAlpha(".PD", "this.owned")); // Not valid
		rank1.add(new OwnerAlpha(".PD", ".PD")); //
		rank1.add(new OwnerAlpha(".PD", "this.PD")); //
		rank1.add(new OwnerAlpha(".PD", ".any")); //
		rank1.add(new OwnerAlpha(".PD", "this.any")); //
		rank1.add(new OwnerAlpha(".PD", "owner")); //
		rank1.add(new OwnerAlpha(".PD", "p")); //
		rank1.add(new OwnerAlpha(".PD", "shared")); //

		rank1.add(new OwnerAlpha("this.PD", "this.owned")); // Not valid
		rank1.add(new OwnerAlpha("this.PD", ".PD")); //
		rank1.add(new OwnerAlpha("this.PD", "this.PD")); //
		rank1.add(new OwnerAlpha("this.PD", ".any")); //
		rank1.add(new OwnerAlpha("this.PD", "this.any")); //
		rank1.add(new OwnerAlpha("this.PD", "owner")); //
		rank1.add(new OwnerAlpha("this.PD", "p")); //
		rank1.add(new OwnerAlpha("this.PD", "shared")); //
		
		rank1.add(new OwnerAlpha(".any", "this.owned")); //
		rank1.add(new OwnerAlpha(".any", ".PD")); //
		rank1.add(new OwnerAlpha(".any", "this.PD")); //
		rank1.add(new OwnerAlpha(".any", ".any")); //
		rank1.add(new OwnerAlpha(".any", "this.any")); //
		rank1.add(new OwnerAlpha(".any", "owner")); //
		rank1.add(new OwnerAlpha(".any", "p")); //
		rank1.add(new OwnerAlpha(".any", "shared")); //
		
		rank1.add(new OwnerAlpha("this.any", "this.owned")); //
		rank1.add(new OwnerAlpha("this.any", ".PD")); //
		rank1.add(new OwnerAlpha("this.any", "this.PD")); //
		rank1.add(new OwnerAlpha("this.any", ".any")); //
		rank1.add(new OwnerAlpha("this.any", "this.any")); //
		rank1.add(new OwnerAlpha("this.any", "owner")); //
		rank1.add(new OwnerAlpha("this.any", "p")); //
		rank1.add(new OwnerAlpha("this.any", "shared")); //

		rank1.add(new OwnerAlpha("owner", "this.owned")); //
		rank1.add(new OwnerAlpha("owner", ".PD")); //
		rank1.add(new OwnerAlpha("owner", "this.PD")); //
		rank1.add(new OwnerAlpha("owner", ".any")); //
		rank1.add(new OwnerAlpha("owner", "this.any")); //
		rank1.add(new OwnerAlpha("owner", "owner")); //
		rank1.add(new OwnerAlpha("owner", "p")); //
		rank1.add(new OwnerAlpha("owner", "shared")); //
		
		rank1.add(new OwnerAlpha("p", "this.owned")); //
		rank1.add(new OwnerAlpha("p", ".PD")); //
		rank1.add(new OwnerAlpha("p", "this.PD")); //
		rank1.add(new OwnerAlpha("p", ".any")); //
		rank1.add(new OwnerAlpha("p", "this.any")); //
		rank1.add(new OwnerAlpha("p", "owner")); //
		rank1.add(new OwnerAlpha("p", "p")); //
		rank1.add(new OwnerAlpha("p", "shared")); //

		
		rank1.add(new OwnerAlpha("shared", "shared")); // If owner is shared, rank that higher...
		rank1.add(new OwnerAlpha("shared", "this.owned")); //
		rank1.add(new OwnerAlpha("shared", ".PD")); //
		rank1.add(new OwnerAlpha("shared", "this.PD")); //
		rank1.add(new OwnerAlpha("shared", ".any")); //
		rank1.add(new OwnerAlpha("shared", "this.any")); //
		rank1.add(new OwnerAlpha("shared", "owner")); //
		rank1.add(new OwnerAlpha("shared", "p")); //

		// Inner ranking (same as above)
		// Must include "" and null to account for no inner
		rankInner.add(null);
		rankInner.add("");
		rankInner.add("this.owned"); //
		rankInner.add(".PD"); //
		rankInner.add("this.PD"); //
		rankInner.add(".any"); //
		rankInner.add("this.any"); //
		rankInner.add("owner"); //
		rankInner.add("p"); //
		rankInner.add("shared"); //
	}

	public static RankingStrategy getInstance() {
		if (instance == null) {
			instance = new RankingStrategy();
			instance.initStrategy();
		}
		return instance;
	}
	
	/**
	 * Given a set of OTypes, pick the preferred one, based on the OwnerAlpha
	 * 
	 * XXX. Need to add expression type as well, as second argument, to rule out "any" as owner for new C<owner,alpha>()
	 *  
	 * @param types
	 * @return
	 */
	public OType pickFromSet(Set<OType> types) {
		// XXX. getMin can return null!
		OType selectedType = getMin(types, null);
		return selectedType;
	}

	/**
	 * Alternate version that remember the bad attempts
	 * @param types
	 * @param badAttempts
	 * @return
	 */
	public OType pickFromSet(Set<OType> types, final Set<OType> badAttempts) {
		// XXX. getMin can return null!
		OType selectedType = getMin(types, badAttempts);
		return selectedType;
	}

	
	/**
	 * Pick the highest ranked
	 * @param types
	 * @return
	 */
	private OType getMin(Set<OType> types, final Set<OType> badAttempts) {
	     // Collections.min throws NoSuchElementException if the collection is empty.
		if(types.size() == 0 ) {
			return null;
		}
		
		if (types.size() == 1 ) {
			return getFirst(types);
		}
		
	    OType min = Collections.min(types, new Comparator<OType>() {
			@Override
            public int compare(OType o1, OType o2) {
	            int diff = compareBetween(o1, o2);

	            // Candidate for deletion (no longer use badAttempts)
	            if(badAttempts != null ) {
	            	OwnerAlpha ownerAlpha2 = o2.getOwnerAlpha();
	            	OwnerAlpha ownerAlpha1 = o1.getOwnerAlpha();
	            	int rankO1 = rank1.indexOf(ownerAlpha1);
	            	int rankO2 = rank1.indexOf(ownerAlpha2);

	            	// XXX. This code will not run; badAttempts is always null
	            	// If this was a bad attempt, give it a very low rank
	            	if (badAttempts != null && badAttempts.contains(o1) ) {
	            		rankO1 = Integer.MAX_VALUE;
	            	}
	            	// If this was a bad attempt, give it a very low rank
	            	if (badAttempts != null && badAttempts.contains(o2) ) {
	            		rankO2 = Integer.MAX_VALUE;
	            	}
	            	diff = rankO1 - rankO2;
	            }

				return diff;
            }
		});
		return min;
    }

	/*
	 * Default strategy pick the first one!
	 */
	private OType getFirst(Set<OType> types) {
		OType type = null;
	
		if (types != null ) {
			Iterator<OType> iterator = types.iterator();
			while(iterator.hasNext()){
				type = iterator.next();
				break;
			}
		}
	    return type;
    }

	private boolean equals(String inner1, String inner2) {
		if (inner1 == null && inner2 != null) {
			return false;
		}
		else
			return inner1.equals(inner2);
	}
	
	// For TreeSet, the ordering maintained by a set (whether or not an explicit comparator is provided)
	// must be consistent with equals if it is to correctly implement the Set interface.
	// The ordering imposed by a comparator c on a set of elements S is said to be consistent with equals if
	// and only if c.compare(e1, e2)==0 has the same boolean value as e1.equals(e2) for every e1 and e2 in S.
	public int compareBetween(OType o1, OType o2) {
		// Consistent with equal
		// if (o1.equals(o2)) {
		// return 0;
		// }

		OwnerAlpha ownerAlpha1 = o1.getOwnerAlpha();
		OwnerAlpha ownerAlpha2 = o2.getOwnerAlpha();

		int rankO1 = rank1.indexOf(ownerAlpha1);
		int rankO2 = rank1.indexOf(ownerAlpha2);
		
		int compare = rankO1 - rankO2;
		if (compare == 0) { // Equal without inner, then compare the inner
			int rankO1i = rankInner.indexOf(o1.getInner());
			int rankO2i = rankInner.indexOf(o2.getInner());
			compare = rankO1i - rankO2i;
		}
		// Could add assert to check that compare consistent with equals
		
		
		return compare;
    }
}
