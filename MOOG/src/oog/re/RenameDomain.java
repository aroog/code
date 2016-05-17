package oog.re;

import oog.itf.IObject;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Transient;

// TODO: maybe hold on to the IDomain object
public class RenameDomain implements oog.re.IOtherRefinement {
	
	@Transient
    private IObject srcIObject;

	@Attribute(name="srcObject")
	protected String srcObject;

	@Attribute(name = "oldDomainName")
	protected String oldDomainName;

	
	@Attribute(name = "newDomainName")
	protected String newDomainName;

	/**
	 * Use this constructor when loading from file
	 * @param srcObject
	 * @param dstDomain
	 */
	public RenameDomain(@Attribute(name = "srcObject") String srcObject,
								@Attribute(name = "oldDomainName") String oldDomainName,	
								@Attribute(name = "newDomainName") String newDomainName) {
	    super();
	    this.srcObject = srcObject;
	    this.oldDomainName = oldDomainName;
	    this.newDomainName = newDomainName;
    }

	/**
	 * Use this constructor when exchanging live objects
	 * @param srcIObject
	 * @param dst
	 * @param dstDomain
	 */
	public RenameDomain(IObject srcIObject, String dstDomain) {
		super();
		this.srcIObject = srcIObject;
		this.srcObject = srcIObject.getTypeDisplayName();
		this.newDomainName = dstDomain;
	}

	public IObject getSrcIObject() {
    	return srcIObject;
    }

	@Override
    public String getSrcObject() {
    	return srcObject;
    }

    public String getNewDomainName() {
    	return newDomainName;
    }

	public void setNewDomainName(String newDomainName) {
		this.newDomainName = newDomainName;
	    
    }

	@Override
	// XXX. Refused bequest
    public String getDstDomain() {
	    return newDomainName;
    }

}
