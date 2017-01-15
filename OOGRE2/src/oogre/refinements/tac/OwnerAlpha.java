package oogre.refinements.tac;


/*
 * DONE. Implement equals, hashCode
 * 
 */
public final class OwnerAlpha {
	
	private  String owner;
	
	private String alpha;
	
	
	public OwnerAlpha(String owner, String alpha) {
	    super();
	    this.owner = owner;
	    this.alpha = alpha;
    }

	public String getOwner() {
    	return owner;
    }

	public String getAlpha() {
    	return alpha;
    }

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
	    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
	    return result;
    }

	// XXX. Tighten this up since this well get called a lot!!!
	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    OwnerAlpha other = (OwnerAlpha) obj;
	    if (alpha == null) {
		    if (other.alpha != null)
			    return false;
	    }
	    else if (!alpha.equals(other.alpha))
		    return false;
	    if (owner == null) {
		    if (other.owner != null)
			    return false;
	    }
	    else if (!owner.equals(other.owner))
		    return false;
	    return true;
    }
	
}
