package edu.wayne.ograph.internal;


/**
 * This class represents the key of OCREdge instance in OGraph
 * 
 */
public class OCREdgeKey extends OEdgeKey {

	// DONE: Is ITypeBinding the right type for this field?
	// Might be more flexible to either:
	// a) Use a String; (just a plain label)
	// b) Use an OObject (as discussed at some point); (SELECTED)
	private OObject Olabel;

	protected OCREdgeKey() {
		super();
	}
	
	public OCREdgeKey(OObject osrc,
			OObject odst,
			OObject olabel) {
		super(osrc, odst);

		this.Olabel = olabel;
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
		result = prime * result + ((osrc == null) ? 0 : osrc.hashCode());
		result = prime * result + ((odst == null) ? 0 : odst.hashCode());
		result = prime * result + ((Olabel == null) ? 0 : Olabel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if(!(obj instanceof OCREdgeKey)) 
			return false;
		
		OCREdgeKey other = (OCREdgeKey) obj;
		// For comparing OObjects, just compare references; no need for value equality		
		return (osrc == other.osrc) && (odst == other.odst) && (Olabel == other.Olabel);
	}

	@Override
	public String toString() {
		return "<" + osrc.getTypeName() + ", " + odst.getTypeName() + ", " + Olabel.getTypeName() + ">";
	}

}
