package oog.re;

import oog.itf.IObject;

import org.simpleframework.xml.Attribute;

public class SplitUp extends Refinement implements ISplitUp {

	// Use to reconstruct an AU
	@Attribute(name = "name")
	private String name;
	
	@Attribute(name = "kind")
	private String kind;
	
	@Attribute(name = "type")
	private String type;
	
	@Attribute(name = "enclosingMethod")
	private String enclosingMethod;
	
	@Attribute(name = "enclosingType")
	private String enclosingType;
	
	public SplitUp(@Attribute(name = "srcObject")String srcObj, 
			@Attribute(name = "dstObject")String dstObj, 
			@Attribute(name = "dstDomain") String dstDomain) {
		super(srcObj, dstObj, dstDomain);
	}

	public SplitUp(IObject src, IObject dst, String domainName) {
		super(src, dst, domainName);
	}
	
	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public String getKind() {
    	return kind;
    }

	public void setKind(String kind) {
    	this.kind = kind;
    }

	public String getType() {
    	return type;
    }

	public void setType(String type) {
    	this.type = type;
    }

	public String getEnclosingType() {
    	return enclosingType;
    }

	public void setEnclosingType(String enclosingType) {
    	this.enclosingType = enclosingType;
    }

	public String getEnclosingMethod() {
		return enclosingMethod;
	}

	public void setEnclosingMethod(String enclosingMethod) {
		this.enclosingMethod = enclosingMethod;
	}	
	
}
