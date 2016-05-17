package edu.wayne.ograph.internal;

import edu.wayne.ograph.OGraphVisitor;

/**
 * This class represents a CR OEdge instance in the OGraph
 * 
 */
public class OCREdge extends OEdge {
	
	private OCREdgeKey key; 
	
	protected OCREdge() {
		super();
	}
	
	public OCREdge(OCREdgeKey key) {
		this.key = key;
	}


	public QualifiedClassName getCLabel() {
		return getOLabel().getQCN();
	}

	public OObject getOLabel() {
		return key.getOLabel();
	}

	@Override
	public String toString() {
		//return "<" + key.toString() + ">";
		return "creation::" + getOLabel().getInstanceDisplayName() + ":"
				+ getOLabel().getTypeDisplayName();
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		return false;
	}
	
	private edu.wayne.ograph.OCREdge realEdge = null;
	
	@Override
    public edu.wayne.ograph.OEdge getReal() {
		if (realEdge == null )  {
			realEdge = new edu.wayne.ograph.OCREdge(getOsrc().getReal(), getOdst().getReal(), getOLabel().getReal());
			realEdge.setPath(getPath());
		}
	    return realEdge;
    }

	@Override
    public OEdgeKey getKey() {
	    return key;
    }

	@Override
    public OObject getOsrc() {
	    return key.getOsrc();
    }

	@Override
    public OObject getOdst() {
	    return key.getOdst();
    }	
}
