package oogre.refinements.tac;

import java.util.Set;


/**
 * XXX. It's better if this object is immutable; 
 * - NO. This object has to be immutable!
 *  
 *  Maybe jType needs to be stored outside of this.
 *  Since it is the same type across all elements of the set.
 *  Can it be different, due to subtyping? But that would be a different AU.
 *  
 */
public class OType {
	
	private String owner;
	private String alpha;
	
	private String inner;
	
	// XXX. Call one constructor in terms of other
	public OType(String owner, String alpha){
		this.owner = owner;
		this.alpha = alpha;
		this.inner = null; 	// XXX. Should probably initialize to "" to avoid NPEs or extra null checks
	}
	
	public OType(String owner, String alpha, String inner){
		this.owner = owner;
		this.alpha = alpha;
		this.inner = inner;
	}
	
	public String print(){
		String retVal = "";
		retVal = "<"+this.owner+","+this.alpha+">";
		return retVal;
	}

// CUT: make immutable
//	public void setjType(String jType) {
//		this.jType = jType;
//	}

	public String getOwner() {
		return owner;
	}

// CUT: make immutable
//	public void setOwner(String owner) {
//		this.owner = owner;
//	}

	public String getAlpha() {
		return alpha;
	}
	
	public String getInner() {
		return inner;
	}

	
// CUT: make immutable	
//	public void setAlpha(String alpha) {
//		this.alpha = alpha;
//	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final OType other = (OType) obj;
		// Take out types from value equality; they could be subtypes		        
	    if(/*this.jType.equals(other.jType) &&*/ this.owner.equals(other.owner) && (this.alpha == other.alpha || this.alpha.equals(other.alpha)) &&
	    		( this.inner == other.inner || this.inner.equals(other.inner)) ) 
	    	return true;
	    else
	    	return false;
	}

	@Override
	public int hashCode() {
	    int hash = 3;
		// Take out types from value equality; they could be subtypes		        
	    /*hash = 53 * hash + (this.jType != null ? this.jType.hashCode() : 0);*/
	    hash = 53 * hash + (this.owner != null ? this.owner.hashCode() : 0);
	    hash = 53 * hash + (this.alpha != null ? this.alpha.hashCode() : 0);
	    hash = 53 * hash + (this.inner != null ? this.inner.hashCode() : 0);
	    return hash;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<");
		builder.append(this.owner);
		builder.append(",");
		builder.append(this.alpha);
		builder.append(">");
		return builder.toString();
	}

	/**
	 * 	XXX. Strip out "n." from "n.d". Return just "d" 
	 * @param domain
	 * @return
	 */
	public static String strip(String domain) {
		String domainName = domain;
		int indexOf = domain.indexOf(".");
		if (indexOf != -1) {
			domainName = domain.substring(indexOf + 1);
		}
		return domainName;
	}
	
	public static String strip2(String domain) {
		String domainName = domain;
		if(domainName !=null && !domainName.contains("this.")){
			int indexOf = domain.indexOf(".");
			if (indexOf != -1) {
				domainName = domain.substring(indexOf);
			}
		}	
		return domainName;
	}
	
	public OwnerAlpha getOwnerAlpha() {
		return new OwnerAlpha(strip2(owner), strip2(alpha));
	}

}
