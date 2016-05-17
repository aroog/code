package oog.re;

import oog.itf.IObject;

import org.simpleframework.xml.Attribute;

public class DeleteDomain implements IOtherRefinement {

	@Attribute(name="srcObject")
	protected IObject srcObject;

	@Attribute(name = "dstDomain")
	protected String dstDomain;
	
	public DeleteDomain(@Attribute(name = "srcObject") IObject srcObject, 
								@Attribute(name = "dstDomain") String dstDomain){
	    super();
	    this.srcObject = srcObject;
	    this.dstDomain = dstDomain;
	}
	
	public IObject getSrcIObject() {
    	return srcObject;
    }
	
	@Override
    public String getSrcObject() {
    	return srcObject.getTypeDisplayName();
    }

	@Override
    public String getDstDomain() {
    	return dstDomain;
    }

}
