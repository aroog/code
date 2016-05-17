package oog.re;

import org.simpleframework.xml.Attribute;

import oog.itf.IObject;

public class PushIntoPD extends Refinement implements IPushIntoPD {

	public PushIntoPD(@Attribute(name = "srcObject")String srcObj, 
			@Attribute(name = "dstObject")String dstObj, 
			@Attribute(name = "dstDomain") String dstDomain) {
		super(srcObj, dstObj, dstDomain);
	}
	
	public PushIntoPD(IObject src, IObject dst, String domainName) {
		super(src, dst, domainName);
	}

}
