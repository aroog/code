package edu.wayne.ograph.internal;

import java.util.List;

import edu.wayne.ograph.internal.ODomain;

public class OObjectKey {

	// Maybe add for debugging purposes only
	// private String id;

	/**
	 * Use String instead of Type, because the equals() method need to be very
	 * well defined. Should not rely on Type.equals().
	 */
	private String typeC; // C

	// Verify that the List does not suffer from rep. exposure
	private List<ODomain> domainsList; // \ob{D}

	public OObjectKey(String C, List<ODomain> domainsList) {
		// super(type.getQualifiedName(), true); //create a temporary OObject
		this.typeC = C;
		this.domainsList = domainsList;
	}

	public String getTypeName() {
		return typeC;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typeC == null) ? 0 : typeC.hashCode());
		result = prime * result + ((domainsList == null) ? 0 : domainsList.hashCode());
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
		OObjectKey other = (OObjectKey) obj;
		if (typeC == null) {
			if (other.typeC != null)
				return false;
			// TODO: maybe use ITypeBinding.isEqualTo() instead
		} else if (other.typeC != null) {
			if (!typeC.equals(other.typeC))
				return false;
		} else
			return false;
		if (domainsList == null) {
			if (other.domainsList != null)
				return false;
		} else if (!domainsList.equals(other.domainsList))
			return false;
		return true;
	}

	public ODomain getOwnerDomain() {
		return domainsList.get(0);
	}

	// TODO: LOW. Fix rep. exposure
	public List<ODomain> getDomains() {
		return domainsList;
	}

	public boolean hasParent() {
		return domainsList.size() > 0;
	}

	public boolean hasChildren() {
		return domainsList != null && domainsList.size() > 0;
	}

	@Override
	/**
	 * Construct a persistent key: C<C1::d1,C2::d2, ...>
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(typeC);
		builder.append("<");
		if(domainsList != null ){
			int ii = 0;
			for(ODomain dom : domainsList) {
				// Returns the "C::d"
				if (ii > 0 ) {
					builder.append(",");
				}
				// XXX. Need a better way to tell if an ODomain is shared
				if(dom.getD().equals("SHARED")) {
					// Do not qualify SHARED with "::null"
					builder.append("SHARED");
				}
				else {
					DomainP domDecl = dom.getDomainDecl();
					builder.append(domDecl.toString());
				}
				ii++;
			}
		}
		builder.append(">");
		return builder.toString();
	}
	
	
}
