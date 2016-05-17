package edu.wayne.ograph.internal;

import edu.wayne.ograph.internal.OObject;

public class DomainMapKey {
	private OObject O;
	/**
	 * C::d
	 * */
	private DomainP dd;

	public DomainMapKey(OObject o, DomainP dd) {
		super();
		O = o;
		this.dd = dd;
	}

	public DomainP getDomDecl() {
		return dd;
	}

	public OObject getO() {
		return O;
	}

	@Override
	public String toString() {
		return "(" + O + "," + dd + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((O == null) ? 0 : O.hashCode());
		result = prime * result + ((dd == null) ? 0 : dd.hashCode());
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
		DomainMapKey other = (DomainMapKey) obj;
		if (O == null) {
			if (other.O != null)
				return false;
		} else if (!O.equals(other.O))
			return false;
		if (dd == null) {
			if (other.dd != null)
				return false;
		} else if (!dd.equals(other.dd))
			return false;
		return true;
	}

}
