package oog.re;

import oog.itf.IObject;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Transient;

/**
 * Store both the object pointer and the string names to identify the object across multiple oGraphs. Right now, object
 * pointers are needed to communicate between ArchDoc and OOGRE. It is assumed that both ArchDoc and OOGRE are operating
 * on the same OGraph. Otherwise, this does not make any sense. 
 * 
 * If we want to save the refinement in order to re-apply them later, we need to save string names, not objects,
 * because we will be looking for objects in a different graph instance (for the same system).
 * 
 * 
 * XXX. Used to persist the refinement:
 * 
 * What do we need to store in srcObject to be able to lookup the object?
 * We need <A,D,B> info. But that does not uniquely identify things.
 * 
 * XXX. Extract out the non-fully qualified type name. Should we store both?
 * For display, could just show the short name.
 * 
 * NOTE: We store the OObjectKey in srcObject and dstObject. 
 * OObjectKey is of the form: C<D1,...,Dn>
 * I wanted to add, in addition OObjectKey, the srcType and dstType.
 * But those can be extracted from the OObjectKey, by getting first index of '<'
 *  
 *  
 */
public abstract class Refinement implements oog.re.IRefinement {
	
	@Transient
    private IObject srcIObject;

	@Transient
    private IObject dstIObject;
	
	@Attribute(name="srcObject")
	protected String srcObject;

	@Attribute(name="dstObject")
	protected String dstObject;
	
	@Attribute(name = "dstDomain")
	protected String dstDomain;

	@Attribute(name = "state")
	protected RefinementState state = RefinementState.Pending;
	
	// TODO: Make refID immutable
	@Attribute(required = false, name = "refID")
	protected String refID = "";
	
	@Transient
	// XXX. Do we not want to persist this?!
	protected RankedTypings rankedTypings = new RankedTypings();

	@Attribute(required = false, name = "isImplicit")
	protected boolean isImplicit = false;

	/**
	 * Use this constructor when exchanging live objects
	 * @param src
	 * @param dst
	 * @param domainName
	 */
	public Refinement(IObject src, IObject dst, String domainName) {
		
		this.refID = IDDictionary.generateID();
		
		this.srcIObject = src;

		// Update the string
		//this.srcObject = src.getTypeDisplayName();
		this.srcObject = src.getObjectKey();
		
		this.dstIObject  = dst;
		
		// Update the string		
		//this.dstObject = dst.getTypeDisplayName();
		this.dstObject = dst.getObjectKey();
		
		this.dstDomain = domainName;
	}

	/**
	 * Use this constructor when reviving objects from persistence
	 * @param srcObj
	 * @param dstObj
	 * @param dstDomain
	 */
	public Refinement(@Attribute(name = "srcObject")String srcObj, 
					@Attribute(name = "dstObject")String dstObj, 
					@Attribute(name = "dstDomain") String dstDomain) {
		this.srcObject = srcObj;
		this.dstObject = dstObj;
		this.dstDomain = dstDomain;
	}
	
	public void refine(){
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
    public RefinementState getState() {
    	return state;
    }

	@Override
    public void setState(RefinementState state) {
    	this.state = state;
    }

	public String getRefID() {
    	return this.refID;
    }

	public void setRefID(String refID) {
    	this.refID = refID;
    }
	

	@Override
    public String getDomainName() {
	    return this.dstDomain;
    }

	/**
	 * Resolve the string object id to a live object reference in the current graph
	 * @return
	 */
	public IObject getSrcIObject() {
	    return srcIObject;
    }

	
	public void setSrcIObject(IObject srcIObject) {
    	this.srcIObject = srcIObject;
    }

	/**
	 * Resolve the string object id to a live object reference  in the current graph
	 * @return
	 */
	public IObject getDstIObject() {
	    return dstIObject;
    }
	
	public void setDstIObject(IObject dstIObject) {
    	this.dstIObject = dstIObject;
    }

	@Override
	public String toDisplayName() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append(": {src= ");
		builder.append(getSrcObject());
		builder.append("; dst= ");
		builder.append(getDstObject());
		builder.append("; domain= ");
		builder.append(getDomainName());
		builder.append("}. Status= ");
		builder.append(getState());
		return builder.toString();
	}

	@Override
    public RankedTypings getRankedTypings() {
	    return rankedTypings;
    }

	@Override
    public boolean isImplicit() {
	    return isImplicit;
    }

	// Make this as part of construction of this object
	public void setImplicit(boolean isImplicit) {
    	this.isImplicit = isImplicit;
    }
	
	@Transient
	/*
	 * Getter to display readable string in UI, just the typename.
	 * Since the srcObject contains an OObjectKey C<D1...Dn>
	 */
	public String getDisplaySrcObject() {
		return getTypeFromKey(srcObject);
	}
	
	@Transient
	/*
	 * Getter to display readable string in UI, just the typename
	 * Since the dstObject contains an OObjectKey C<D1...Dn>	 * 
	 */
	public String getDisplayDstObject() {
		return getTypeFromKey(dstObject);
	}

	/**
	 * Given an OObjectKey C<D1,..,Dn>, return the C.
	 */
	private static String getTypeFromKey(String oObjectKey) {
	    int indexOf = oObjectKey.indexOf('<');
		if(indexOf != -1){
			return oObjectKey.substring(0, indexOf);
		}
		return oObjectKey;
    }
	
}
