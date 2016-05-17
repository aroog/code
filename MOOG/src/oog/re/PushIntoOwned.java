package oog.re;

import org.simpleframework.xml.Attribute;

import oog.itf.IObject;

public class PushIntoOwned extends Refinement implements IPushIntoOwned {

	public PushIntoOwned(@Attribute(name = "srcObject")String srcObj, 
			@Attribute(name = "dstObject")String dstObj, 
			@Attribute(name = "dstDomain") String dstDomain) {
		super(srcObj, dstObj, dstDomain);
	}
	
	public PushIntoOwned(IObject src, IObject dst, String domainName) {
		super(src, dst, domainName);
	}
}
