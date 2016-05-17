package edu.wayne.ograph.internal;

import java.util.Map.Entry;

import edu.wayne.ograph.OObject;

public class ObjectDomainPair implements Entry<OObject, DomainP> {

	private OObject o;
	private DomainP d;
	
	ObjectDomainPair(OObject o, DomainP d){
		this.o = o;
		this.d = d;
	}

	@Override
	public OObject getKey() {
		// TODO Auto-generated method stub
		return o;
	}

	@Override
	public DomainP getValue() {
		return d;
	}

	/**
	 * Immutable object DO NOT USE
	 * */
	@Deprecated
	@Override
	public DomainP setValue(DomainP value) {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		result = prime * result + ((o == null) ? 0 : o.hashCode());
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
		ObjectDomainPair other = (ObjectDomainPair) obj;
		if (d == null) {
			if (other.d != null)
				return false;
		} else if (!d.equals(other.d))
			return false;
		if (o == null) {
			if (other.o != null)
				return false;
		} else if (!o.equals(other.o))
			return false;
		return true;
	}

}
