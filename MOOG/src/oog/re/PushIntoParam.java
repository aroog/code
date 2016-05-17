package oog.re;

import org.simpleframework.xml.Attribute;

import oog.itf.IObject;

public class PushIntoParam extends Refinement implements IPushIntoParam {

	public PushIntoParam(@Attribute(name = "srcObject")String srcObj, 
			@Attribute(name = "dstObject")String dstObj, 
			@Attribute(name = "dstDomain") String dstDomain) {
		super(srcObj, dstObj, dstDomain);
	}

	public PushIntoParam(IObject src, IObject dst, String domainName) {
		super(src, dst, domainName);
	}
}
