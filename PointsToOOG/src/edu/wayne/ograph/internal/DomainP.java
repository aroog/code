package edu.wayne.ograph.internal;

import junit.framework.Assert;
import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.aliasjava.Constants;

/**
 * Class representing a domain parameter, a domain declaration C::d, or ::SHARED
 * 
 * @author radu
 * 
 */
public class DomainP {
	protected QualifiedClassName C;
	private String d;
	private boolean isDeclaration;

	// public DomainP(String d) {
	// this.d = d;
	// }

	public DomainP(QualifiedClassName C, String d) {
		Assert.assertNotNull("null d in C::d", d);
		Assert.assertFalse("empty d in C::d", d.equals(""));
		this.C = C;
		this.d = d;
		isDeclaration = false;
	}

	public void setAsDeclaration() {
		isDeclaration = true;
	}

	public DomainP(QualifiedClassName C, String d, boolean isDecl) {
		this(C, d);
		isDeclaration = isDecl;
	}

	/**
	 * use for IDs only
	 * */
	public String getShortName() {
		return d;
	}

	public QualifiedClassName getTypeBinding() {
		return C;
	}

	@Override
	public String toString() {
		return C + "::" + d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((C == null) ? 0 : C.hashCode());
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainP other = (DomainP) obj;
		if (C == null) {
			if (other.C != null)
				return false;
		} else if (!C.equals(other.C))
			return false;
		if (d == null) {
			if (other.d != null)
				return false;
		} else if (!d.equals(other.d))
			return false;
		return true;
	}

	public boolean isOwner() {
		return d.equals(Constants.OWNER);
	}

	public boolean isDeclaration() {
		return isDeclaration;
	}

	public boolean isUnique() {
		return d.equals(Constants.UNIQUE);
	}
	
	public boolean isLent() {
		return d.equals(Constants.LENT);
	}

	public boolean isPrecise() {		
		return !isUnique() && !isLent();
	}
	
	public boolean isPublic(){
		// XXX. Inefficient use of AnnotationInfo.parseAnnotation
		// Can we at least cache the result in a field?!
		AnnotationInfo annot = AnnotationInfo.parseAnnotation(d);
		return annot.getAnnotation().isObjectPublicDomain();
		//XXX.HACK: 
		//return d.matches("^[a-zA-Z_$][a-zA-Z_$0-9]*$"+"\\."+"^[a-zA-Z_$][a-zA-Z_$0-9]*$");
	}

}
