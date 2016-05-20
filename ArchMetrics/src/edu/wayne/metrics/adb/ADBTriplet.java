package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IObject;

/**
 * Information about an OObject extracted into an <A,D,B> triplet
 * NOTE: One IObject can generate many ADBTriplets.
 * So DO NOT generate ADBTriplets from IObjects, then traverse the ADBTriplets.
 * Always go back to and traverse IObjects. Generate an ADBTriplet as just an adapter to an IObject
 */
public class ADBTriplet {

	private IObject objectA;
	
	public ADBTriplet() {
	}
	
	public ADBTriplet(IObject childObject) {
		if ( childObject == null ) 
			throw new IllegalArgumentException("Null childObject not allowed");
		
		objectA = childObject;
	}

	// TODO: Make sure this returns the generic type
	private static String getType(IObject object) {
		String fullType = "";
		if (object != null) {
			fullType = object.getC().getFullyQualifiedName();
		}
		return fullType;
	}
	
	/**
	 *  Very inefficient implementation to get the short (unqualified type name)
	 *  TODO: HIGH. Ideally, Type must also store the short name; it is not obvious to compute from the fully qualified typename,
	 *  e.g. for java.util.ArrayList<java.lang.Object>
	 */
	private static String getShortType(IObject object) {
		String fullName = "";
		if (object != null) {
			fullName = getType(object);
			
			String shortName = org.eclipse.jdt.core.Signature.getSimpleName(fullName);
			// TODO: Check what we got?
			fullName = shortName;
		}
		return fullName;
	}
	
	private static String getInstanceName(IObject object) {
		String instanceName = "";
		if (object != null) {
			instanceName = object.getInstanceDisplayName();
		}
		return instanceName;
	}
	
	public String getTypeA() {
		return getType(this.objectA);
	}
	
	public String getShortTypeA() {
		return getShortType(this.objectA);
	}
	
	public Set<String> getTypeBs() {
		Set<String> typeB = new HashSet<String>();
		
		for(IObject parentObject : getParentObjects() ) {
			typeB.add ( getType(parentObject) );
		}
		
		return typeB;
	}

	public String getRawTypeA() {
		return Util.getRawTypeName(getType(this.objectA));
	}

	// TODO: HIGH. XXX. Some of the callers may expect an unqualified domain
	public String getDomainStringD() {
		IDomain domainD = getDomainD();
		String domain = "";
    	if (domainD != null ) {
    		domain = domainD.getD(); // TOMAR: TODO: HIGH. Make sure that D qualified?
    	}
    	return domain;
    }
	
	// NOTE: IDomain.getName() returns null. 
	// Return simple name, without qualifier of declaring class!
	public String getRawDomainD() { 
		IDomain domainD = getDomainD();
		String domainName = "";
    	if (domainD != null ) {
    		// NOTE: getD() returns just "d", not "C::d"
    		domainName = domainD.getD();
    	}
    	
    	return domainName;
    }
	
	public String getDid() { 
		IDomain domainD = getDomainD();
		String domainId = "";
    	if (domainD != null ) {
    		domainId = domainD.getD_id();
    	}
    	return domainId;
    }
	
	public Set<String> getRawTypeBs() {
		Set<String> rawTypeB = new HashSet<String>();
		
		for(IObject childObject : getObjectBs() ) {
			rawTypeB.add(Util.getRawTypeName(getType(childObject)));
		}
		return rawTypeB;
	}

	public String getInstanceA() {
		return getInstanceName(this.objectA);
	}
	
	@Override
    public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof ADBTriplet)) {
			return false;
		}
		ADBTriplet other = (ADBTriplet) o;
		// Two ADBTriplets are equal if they correspond to the same underlying IObject they adapt
		return (objectA == other.objectA);
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((objectA == null) ? 0 : objectA.hashCode());
	    return result;
    }
	
	// TODO: Have this be the toLongString. 
	// TODO: No need to show the Instance names. Not part of the <A,D,B> formal definition
	// TODO: Use full types, rather than raw types.
	@Override
	public String toString() {
		StringBuffer builder = new StringBuffer();
		builder.append("<");
		// Make less wordy
		builder.append(getRawTypeA());
		builder.append(",");
		builder.append(getRawDomainD());
		builder.append(",");
		// TODO: HIGH. This does not work anymore: saving a set to a string.
		builder.append(getRawTypeBs());
		builder.append(">");
		return builder.toString();
	}
	
	
	// DONE. No need to show the Instance names. Not part of the <A,D,B> formal definition.
	// DONE. Show full types, rather than raw types.
	public String toLongString() {
		StringBuffer builder = new StringBuffer();
		builder.append("<");
		builder.append(getTypeA());
		builder.append(",");
		// NOTE: We purposely return the Raw domain. The fully qualified domain name is a bit hard to read.
		builder.append(getRawDomainD());
		builder.append(",");
		// TODO: HIGH. This does not work anymore: saving a set to a string.
		builder.append(getTypeBs());
		builder.append(">");
		return builder.toString();
	}	
	
	// TODO: Maybe compute non qualified class names?
	public String toShortString() {
		StringBuffer builder = new StringBuffer();
		builder.append("<");
		builder.append(getRawTypeA());
		builder.append(",");
		builder.append(getRawDomainD());
		builder.append(",");
		// TODO: This does not work anymore...
		builder.append(getRawTypeBs());
		builder.append(">");
		return builder.toString();
	}
	
	
	// TODO: Have this be the toLongString. 
	// TODO: No need to show the Instance names. Not part of the <A,D,B> formal definition
	// TODO: Use full types, rather than raw types.
	public String toObjectString() {
		StringBuffer builder = new StringBuffer();
		// TODO: Why use <> for O_id?
		builder.append("<");
		builder.append(objectA.getO_id());
		builder.append(">");
		return builder.toString();
	}
		
	public void writeTo(Writer writer) throws IOException {
		// TODO: Why not use using toString()?
		writer.append(",\"" + getTypeA() + "\"");
        writer.append(",\"" + getRawDomainD() + "\"");
		// TODO: This does not work anymore...
        writer.append(",\"" + getTypeBs() +  "\"");
    }

	public IObject getObject() {
		return this.objectA;
	}
	
	public Set<? extends IObject> getParentObjects() {
		return this.objectA.getParentObjects();
	}
	
	public static ADBTriplet getTripletFrom(IObject objectA) {
		// TODO: We may need another class that has the following:
		// A,D,B,Domain type.
		// TripletGraphMetricItem tgmid = new TripleGraphMetricItem();
		// For now lets just use Triplet itself.

		if (objectA != null ) {
			return new ADBTriplet(objectA);
		}
		else {
			System.err.println("ADBTriplet: Encountered null IObject");
		}
        return null;
    }

	public IObject getObjectA() {
	    return objectA;
    }

	public Set<? extends IObject> getObjectBs() {
	    return objectA.getParentObjects();
    }

	public IDomain getDomainD() {
	    return objectA.getParent();
    }	
}
