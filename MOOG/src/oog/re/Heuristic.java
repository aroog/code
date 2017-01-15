package oog.re;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Transient;

/**
 * Base class for the heuristics; used for persistence
 */
public class Heuristic implements oog.re.IHeuristic {

	@Attribute(name="srcObject")
	protected String srcObject;

	@Attribute(name="dstObject")
	protected String dstObject;
	
	@Attribute(name = "dstDomain")
	protected String dstDomain;

	@Attribute(name = "state")
	protected RefinementState state = RefinementState.Pending;

	// TODO: Make this immutable
	@Attribute(required = false, name = "refID")
	protected String refID;

	// TODO: Pull up into base class
	@Transient
	// XXX. Do we not want to persist this?!
	protected RankedTypings rankedTypings = new RankedTypings();
	
	/**
	 * Use this constructor when reviving objects from persistence
	 * @param srcObj
	 * @param dstObj
	 * @param dstDomain
	 */
	public Heuristic(@Attribute(name = "srcObject")String srcObj, 
					@Attribute(name = "dstObject")String dstObj, 
					@Attribute(name = "dstDomain") String dstDomain) {
		this.srcObject = srcObj;
		this.dstObject = dstObj;
		this.dstDomain = dstDomain;
	}
	

	@Override
	public RefinementState getState() {
		return state;
	}

	public void setState(RefinementState state) {
		this.state = state;
	}

	public String getRefID() {
    	return this.refID;
    }

	public void setRefID(String refID) {
    	this.refID = refID;
    }

	// TODO: return true if the heuristic found anything
	public void apply() {
	}
	

	@Override
    public String getSrcObject() {
	    return this.srcObject;
    }

	@Override
    public String getDstObject() {
	    return this.dstObject;
    }

	@Override
    public String getDomainName() {
	    return this.dstDomain;
    }

	@Override
    public String toDisplayName() {
	    return null;
    }

	@Override
    public RankedTypings getRankedTypings() {
	    return rankedTypings;
    }


	@Override
	// All Heuristics are implicit; not requested by the user
    public boolean isImplicit() {
	    return true;
    }
}
