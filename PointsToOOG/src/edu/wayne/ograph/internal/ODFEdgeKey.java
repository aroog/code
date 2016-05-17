package edu.wayne.ograph.internal;

import edu.wayne.ograph.EdgeFlag;

/**
 * This class represents the key of an ODFEdge
 * 
 */
public class ODFEdgeKey extends OEdgeKey {

	// DONE: Is ITypeBinding the right type for this field?
	// Might be more flexible to either:
	// a) Use a String; (just a plain label)
	// b) Use an OObject (as discussed at some point); (SELECTED)
	private OObject Olabel;

	private EdgeFlag flag;
	
	protected ODFEdgeKey() {
		super();
	}
	
	public ODFEdgeKey(OObject osrc,
			OObject odst,
			OObject olabel,
			EdgeFlag direction) {
		super(osrc, odst);

		this.Olabel = olabel;
		this.flag = direction;
	}

	public EdgeFlag getFlag() {
		return flag;
	}

	public QualifiedClassName getCLabel() {
		return Olabel.getQCN();
	}

	public OObject getOLabel() {
		return Olabel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((odst == null) ? 0 : odst.hashCode());
		result = prime * result + ((osrc == null) ? 0 : osrc.hashCode());
		result = prime * result + ((Olabel == null) ? 0 : Olabel.hashCode());
		result = prime * result + ((flag == null) ? 0 : flag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ODFEdgeKey))
			return false;

		ODFEdgeKey other = (ODFEdgeKey) obj;
		// For comparing OObjects, just compare references; no need for value equality		
		return (osrc == other.osrc) && (odst == other.odst) && (Olabel == other.Olabel) && (flag == other.flag);
	}

	@Override
	public String toString() {
		return "<" + osrc.getTypeName() + ", " + odst.getTypeName() + ", " + flag + ", " + Olabel.getTypeName() + ">";
	}
}
