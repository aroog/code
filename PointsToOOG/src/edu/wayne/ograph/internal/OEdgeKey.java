package edu.wayne.ograph.internal;


public abstract class OEdgeKey {

	protected OObject osrc;

	protected OObject odst;
	
	// Add default constructor for serialization
	protected OEdgeKey() {
		super();
	}

	public OEdgeKey(OObject osrc,
			OObject odst) {
		super();
		this.osrc = osrc;
		this.odst = odst;
	}

	public OObject getOsrc() {
		return osrc;
	}

	public OObject getOdst() {
		return odst;
	}
}