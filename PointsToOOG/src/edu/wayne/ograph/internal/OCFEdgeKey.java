package edu.wayne.ograph.internal;


/**
 * This class represents the key of an OCFEdge in OGraph
 * 
 */
public class OCFEdgeKey extends OEdgeKey {

	private String label;

	protected OCFEdgeKey() {
		super();
	}
	
	public OCFEdgeKey(OObject osrc,
			OObject odst,
			String label) {
		super(osrc, odst);

		this.label = label;
	}

	// XXX. vs. getControl() in SecOOG. Consolidate.
	public String getLabel() {
		return label;
	}
//
//	public MethodInvocation getOLabel() {
//		return label;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((osrc == null) ? 0 : osrc.hashCode());
		result = prime * result + ((odst == null) ? 0 : odst.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!(obj instanceof OCFEdgeKey))
			return false;
		
		OCFEdgeKey other = (OCFEdgeKey) obj;
		// For comparing OObjects, just compare references; no need for value equality
		return (osrc == other.osrc) && (odst == other.odst) && (label == other.label);
	}

	@Override
	public String toString() {
		//return "<" + osrc.getTypeName() + ", " + odst.getTypeName() + ", " + label + ">";
		
		return "control::" + getLabel();
	}

}
