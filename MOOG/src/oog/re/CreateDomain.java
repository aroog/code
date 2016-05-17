package oog.re;

import oog.itf.IObject;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Transient;

// TODO: maybe hold on to the IDomain object
public class CreateDomain implements oog.re.IOtherRefinement {
	
	@Transient
    private IObject srcIObject;

	@Attribute(name="srcObject")
	protected String srcObject;

	@Attribute(name = "dstDomain")
	protected String dstDomain;

	/**
	 * Use this constructor when loading from file
	 * @param srcObject
	 * @param dstDomain
	 */
	public CreateDomain(@Attribute(name = "srcObject") String srcObject, 
								@Attribute(name = "dstDomain") String dstDomain) {
	    super();
	    this.srcObject = srcObject;
	    this.dstDomain = dstDomain;
    }

	/**
	 * Use this constructor when exchanging live objects
	 * @param srcIObject
	 * @param dst
	 * @param dstDomain
	 */
	public CreateDomain(IObject srcIObject, String dstDomain) {
		super();
		this.srcIObject = srcIObject;
		this.srcObject = srcIObject.getTypeDisplayName();
		this.dstDomain = dstDomain;
	}

	public IObject getSrcIObject() {
    	return srcIObject;
    }

	@Override
    public String getSrcObject() {
    	return srcObject;
    }

	@Override
    public String getDstDomain() {
    	return dstDomain;
    }

	public void setDstDomain(String dstDomain) {
		this.dstDomain = dstDomain;
	    
    }

}
