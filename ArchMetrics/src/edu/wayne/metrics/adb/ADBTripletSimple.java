package edu.wayne.metrics.adb;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

// TODO: Figure out if we still need/use this class.
// TODO: Convert this into simple wrapper; instead of duplicating stuff in ObjectMetricItem
// This is not an OOG-level triplet A,D,B; this is an annotation-level triplet  
// TODO: Rename: ADBTripletSimple -> AnnotationTriplet
public class ADBTripletSimple {

	String typeA;
	String domainD;
	String typeB;
	
	// true if the domainD is public; false if it is private. 
	boolean isDomainPublic;

	// true if the domainD is formal; false if it is locally declared. 
	boolean isDomainFormal;

	public ADBTripletSimple() {
	}
	
	public ADBTripletSimple(String typeA, String domain, String typeB) {
		this.typeA = typeA;
		this.domainD = domain;
		this.typeB = typeB;
	}

	public String getTypeA() {
		return this.typeA;
	}
	
	public void setTypeA(String typeA) {
    	this.typeA = typeA;
    }

	public String getDomainD() {
    	return this.domainD;
    }
	
	public void setDomainD(String domainD) {
    	this.domainD = domainD;
    }

	public boolean isDomainPublic() {
    	return isDomainPublic;
    }
	
	public void setDomainPublic(boolean isDomainPublic) {
    	this.isDomainPublic = isDomainPublic;
    }

	public String getTypeB() {
		return this.typeB;
	}	

	public void setTypeB(String typeB) {
    	this.typeB = typeB;
    }

	
	public boolean isDomainFormal() {
    	return isDomainFormal;
    }

	public void setDomainFormal(boolean isDomainFormal) {
    	this.isDomainFormal = isDomainFormal;
    }
	
	@Override
    public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof ADBTripletSimple)) {
			return false;
		}
		ADBTripletSimple other = (ADBTripletSimple) o;
		return (typeA == other.typeA && domainD == other.domainD && typeB == other.typeB);
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((typeA == null) ? 0 : typeA.hashCode());
	    result = prime * result + ((domainD == null) ? 0 : domainD.hashCode());
	    result = prime * result + ((typeB == null) ? 0 : typeB.hashCode());
	    return result;
    }
	
	@Override
	public String toString() {
		StringBuffer builder = new StringBuffer();
		builder.append("<");
		builder.append(getTypeA());
		builder.append(",");
		builder.append(getDomainD());
		builder.append(",");
		builder.append(getTypeB());
		builder.append(">");
		return builder.toString();
	}
	
	// TODO: HIGH. XXX. The column headings are incorrect: TypeA can be an interface when dealing with annotations;
	public static void save(Set<ADBTripletSimple> triplets, String fileName) throws IOException {
		FileWriter writer = new CustomWriter(fileName);
		writer.append("TypeA,DomainD,DomainD_IsPublic,DomainD_IsFormal,TypeB");
		writer.append('\n');

		for (ADBTripletSimple triplet : triplets) {
			writer.append("\"");
			writer.append(triplet.getTypeA());
			writer.append("\"");			
			writer.append(",");
			writer.append("\"");
			writer.append(triplet.getDomainD());
			writer.append("\"");			
			writer.append(",");
			writer.append(Boolean.valueOf(triplet.isDomainPublic()).toString());
			writer.append(",");			
			writer.append(Boolean.valueOf(triplet.isDomainFormal()).toString());
			writer.append(",");
			writer.append("\"");			
			writer.append(triplet.getTypeB());
			writer.append("\"");
			writer.append('\n');
		}
		writer.flush();
		writer.close();
    }	
}
