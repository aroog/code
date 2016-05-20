package edu.wayne.metrics.datamodel;

import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.aliasjava.parser.DomainParams;
import edu.wayne.metrics.adb.ADBTripletSimple;

public class ObjectMetricItem {

	// Default constructor, required by persistence framework
	public ObjectMetricItem() {
	}
	
	public ObjectMetricItem(String key, String name, String type, String annotation, String enclosingType, String astNode,
	        boolean isStatic, String nodeType, boolean isArray, boolean isEnum, boolean isParametrizedType, boolean isDomain, boolean isDomainParam, boolean isObjectPublicDomain) {
		
		
		this.setKey(key);
		this.setName(name);
		this.setType(type);
		this.setAnnotation(annotation);
		this.setEnclosingType(enclosingType);
		this.setAstNode(astNode);
		this.setStatic(isStatic);
		this.setNodeType(nodeType);
		this.setArray(isArray);
		this.setEnum(isEnum);
		this.setParametrizedType(isParametrizedType);
		this.setDomain(isDomain);
		this.setDomainParams(isDomainParam);
		this.setObjectPublicDomain(isObjectPublicDomain);
	}

	// 0. a unique key as returned by resolveBinding.getKey()
	private String key;
	
	// 1. Fully qualified name
	private String name;

	//TODO: LOW. maybe use ITypeBinding instead.
	// 2. Declared Type
	private String declaredType;

	// 3. Annotation
	private String annotation;

	//TODO: LOW. maybe use ITypeBinding instead.
	// 4. Enclosing type
	private String enclosingType;

	// 5. ASTNode
	private String astNode;

	public String getAstNode() {
    	return astNode;
    }

	// 6. IsStatic
	private boolean isStatic;

	// 7. nodeType ( Field, LocalVariable, MethodParams, ReturnType)
	private String nodeType;

	// 8.
	private boolean isArray;

	// 9.
	private boolean isEnum;

	// 10.
	private boolean isParametrizedType;
	
	private boolean isDomain;
	
	private boolean isDomainParams;
	
	// f1.DOM
	private boolean isObjectPublicDomain;
	
	private void setKey(String key) {
	    this.key = key;    
    }

	// TODO: LOW. Maybe hold on to the actual ASTNode object.
	// private ASTNode node;
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append(key.trim().replaceAll(",", ";"));
		buffer.append(",");
		
		buffer.append(getName().trim().replaceAll(",", ";"));
		buffer.append(",");

		buffer.append(declaredType.trim().replaceAll(",", ";") );
		buffer.append(",");

		buffer.append(getAnnotation().trim().replaceAll(",", ";"));
		buffer.append(",");

		buffer.append(enclosingType.trim().replaceAll(",", ";"));
		buffer.append(",");

		buffer.append(isStatic);
		buffer.append(",");

		buffer.append(getNodeType().trim().replaceAll(",", ";"));
		buffer.append(",");

		int x =astNode.indexOf('\n');
		if (x <= 0)
			x = astNode.length();
		buffer.append(((String) astNode.subSequence(0, x)).replaceAll(",", ";"));
		buffer.append(",");

		buffer.append(isArray);
		buffer.append(",");

		buffer.append(isEnum);
		buffer.append(",");

		buffer.append(isParametrizedType);
		buffer.append(",");
		
		buffer.append(isDomain);
		buffer.append(",");
		
		buffer.append(isDomainParams);
		buffer.append(",");
		
		buffer.append(isObjectPublicDomain);
		

		// buffer.append(",");
		//
		// buffer.append(key);

		return buffer.toString();
	}

	  public DomainParams extractDomain() {
		  AnnotationInfo annotInfo = AnnotationInfo.parseAnnotation(annotation); 
		  DomainParams annot = annotInfo.getAnnotation();
		  return annot;
//			;
//		    String tempStr = this.getAnnotation().substring(9);
//
//		    int leftquote = tempStr.indexOf("\""); // -1 if not exists
//		    int leftAngle = tempStr.indexOf("<");
//		    int leftSQ = tempStr.indexOf("[");
//
//		    int leftEnd = leftquote;
//
//		    if (leftAngle > 0 && leftAngle < leftEnd) {
//		    	leftEnd = leftAngle;
//		    }
//
//		    if (leftSQ > 0 && leftSQ < leftEnd) {
//		    	leftEnd = leftSQ;
//		    }	   
//		    return null;
	    }

	  public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.declaredType = type;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public void setEnclosingType(String enclosingType) {
		this.enclosingType = enclosingType;
	}

	public void setAstNode(String astNode) {
		this.astNode = astNode;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}

	public void setEnum(boolean isEnum) {
		this.isEnum = isEnum;
	}

	public void setParametrizedType(boolean isParametrizedType) {
		this.isParametrizedType = isParametrizedType;
	}

	public String getName() {
	    return name;
    }

    public String getAnnotation() {
	    return annotation;
    }

    public String getNodeType() {
	    return nodeType;
    }

    public void setDomain(boolean isDomain) {
	    this.isDomain = isDomain;
    }

    public void setObjectPublicDomain(boolean isObjectPublicDomain) {
    	this.isObjectPublicDomain = isObjectPublicDomain;
    }

	public boolean isDomain() {
	    return isDomain;
    }

    public void setDomainParams(boolean isDomainParam) {
	    this.isDomainParams = isDomainParam;
    }

    public boolean isDomainParams() {
	    return isDomainParams;
    }

	public String getDeclaredType() {
    	return declaredType;
    }

	public void setDeclaredType(String declaredType) {
    	this.declaredType = declaredType;
    }

	public String getKey() {
    	return key;
    }

	public String getEnclosingType() {
    	return enclosingType;
    }

	public boolean isStatic() {
    	return isStatic;
    }

	public boolean isArray() {
    	return isArray;
    }

	public boolean isEnum() {
    	return isEnum;
    }

	public boolean isParametrizedType() {
    	return isParametrizedType;
    }

	public boolean isObjectPublicDomain() {
	    return isObjectPublicDomain;
    }

	public ADBTripletSimple getTriplet() {
		ADBTripletSimple triplet = new ADBTripletSimple();
		triplet.setTypeA(getDeclaredType());
		triplet.setDomainD(extractDomain().getDomain());
		triplet.setTypeB(getEnclosingType());
		// TODO: Is isObjectPublicDomain the right one?
		triplet.setDomainPublic(isObjectPublicDomain());
		triplet.setDomainFormal(isDomainParams());
		return triplet;
    }

    
  
}
