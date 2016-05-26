package oogre.refinements.tac;

import oogre.utils.Utils;

/**
 * This object is immutable since it is used as a key in a HashMap (TM)
 * 
 * Take out names since ITypeBindings do not have names.
 * Also, we don't have context-sensitivity to set different typings for variables of the same type but different names
 * Included only for debugging; not used in value equality.
 * 
 * XXX. Add examples for AU
 */
public class AnnotatableUnit {
	
	private String name;
	
	private AnnotateUnitEnum kind;
	
	private String type;
	
	/*
	 * Declaring class of a method or a field
	 */
	private String enclosingType;
	
	private String enclosingMethod;

	private int index;
	
	public AnnotatableUnit(String name, AnnotateUnitEnum kind, String type, String enclosingMethod, String enclosingType){
		this.name = name;
		this.kind = kind;
		this.type = type;
		this.enclosingMethod = enclosingMethod;
		this.enclosingType = enclosingType;
	}
	
	public String getName(){
		return this.name;
	}
	
	public AnnotateUnitEnum getKind(){
		return this.kind;
	}
	public String getType(){
		return this.type;
	}
	
	// TODO: Rename: isSubType
	public boolean isTypeEqual(String otherType){
		//return this.type.equals(otherType);
		return Utils.isSubtypeCompatible(this.type, otherType) ||
				Utils.isSubtypeCompatible(otherType, this.type);
	}
	
	
	public String getEnclosingMethod(){
		return this.enclosingMethod;
	}
	public String getEnclosingType(){
		return this.enclosingType;
	}
	
	// TODO: Rename: isSubType
	public boolean isEnclosingTypeEqual(String otherType) {
		//return this.enclosingType.equals(otherType);
		return Utils.isSubtypeCompatible(this.enclosingType, otherType) ||
				Utils.isSubtypeCompatible(otherType, this.enclosingType);		
	}
	
	@Override
	// DO NOT use index for equality
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final AnnotatableUnit other = (AnnotatableUnit) obj;
		if (this.name.equals(other.name) && this.kind.equals(other.kind)
		        && this.enclosingMethod.equals(other.enclosingMethod) && this.enclosingType.equals(other.enclosingType)
		// Take out types from value equality; they could be subtypes		        
		       /* && this.type.equals(other.type)*/)
	    	return true;
	    else
	    	return false;
	}

	@Override
	// DO NOT use index for equality	
	public int hashCode() {
	    int hash = 3;
	    hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
	    hash = 53 * hash + (this.kind != null ? this.kind.hashCode() : 0);
	    // Take out types from value equality; they could be subtypes
	    /*hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);*/
	    hash = 53 * hash + (this.enclosingMethod != null ? this.enclosingMethod.hashCode() : 0);
	    hash = 53 * hash + (this.enclosingType != null ? this.enclosingType.hashCode() : 0);
	    return hash;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.name);
		builder.append(":");
		builder.append(this.type);
		if(this.enclosingMethod != null){
			builder.append(" in the method ");
			builder.append(this.enclosingMethod);
		}
		builder.append(" in the class ");
		builder.append(this.enclosingType);
		return builder.toString();
	}

	// Used only for AnnotateUnitEnum.p
	public int getIndex() {
    	return index;
    }

	// Used only for AnnotateUnitEnum.p
	public void setParamIndex(int ii) {
		this.index = ii;
    }
}
