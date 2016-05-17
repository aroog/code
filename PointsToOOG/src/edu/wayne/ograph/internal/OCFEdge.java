package edu.wayne.ograph.internal;

import edu.wayne.ograph.OGraphVisitor;

/**
 * This class represents a CF OEdge in the OGraph
 * 
 */
public class OCFEdge extends OEdge {
	
	private OCFEdgeKey key; 

	protected OCFEdge() {
		super();
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

	public OCFEdge(OCFEdgeKey key) {
		super();
	}


	public String getLabel() {
		return key.getLabel();
	}
//
//	public MethodInvocation getOLabel() {
//		return label;
//	}

	@Override
	public String toString() {
		return "<" + key.toString() + ">";
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		return false;
	}

	private edu.wayne.ograph.OCFEdge realEdge = null;
	
	@Override
    public edu.wayne.ograph.OEdge getReal() {
		if (realEdge == null )  {
			realEdge = new edu.wayne.ograph.OCFEdge(getOsrc().getReal(), getOdst().getReal(), getLabel());
			realEdge.setPath(getPath());
		}
	    return realEdge;
    }
}
