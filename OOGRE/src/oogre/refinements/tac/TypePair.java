package oogre.refinements.tac;



public class TypePair {
	private String type;
	private OwnershipFormalParameter parameter;
	
	public TypePair(String type, OwnershipFormalParameter parameter){
		this.type = type;
		this.parameter = parameter;
	}
	
	public String print(){
		return "("+this.type+", "+this.parameter.name()+")";
	}
	
	public String getType(){
		return this.type;
	}
	
	public OwnershipFormalParameter getParameter(){
		return this.parameter;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final TypePair other = (TypePair) obj;
	    if(this.type.equals(other.type) && this.parameter.equals(other.parameter))
	    	return true;
	    else
	    	return false;
	}

	@Override
	public int hashCode() {
	    int hash = 5;
	    hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
	    hash = 89 * hash + (this.parameter != null ? this.parameter.hashCode() : 0);
	    return hash;	    
	}
}
