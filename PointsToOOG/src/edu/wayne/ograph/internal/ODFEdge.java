package edu.wayne.ograph.internal;

import edu.wayne.ograph.EdgeFlag;
import edu.wayne.ograph.OGraphVisitor;

/**
 * This class represents a DF OEdge in the OGraph
 * 
 * 
 */
public class ODFEdge extends OEdge {

	private ODFEdgeKey key;
	
	protected ODFEdge() {
		super();
	}
	
	public ODFEdge(ODFEdgeKey key) {
		super();
		
		this.key = key;
	}

	public EdgeFlag getFlag() {
		return key.getFlag();
	}

	public QualifiedClassName getCLabel() {
		return getOLabel().getQCN();
	}

	// XXX. Rename: getFlow() to match SecOOG
	public OObject getOLabel() {
		return key.getOLabel();
	}

	@Override
	public String toString() {
		//  return "<" + key.toString() + ">";
		return getFlag().toString() + "::" + getOLabel().getInstanceDisplayName() + ":"
		        + getOLabel().getTypeDisplayName();
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		return false;
	}
	
	private edu.wayne.ograph.ODFEdge realEdge = null;
	
	@Override
    public edu.wayne.ograph.OEdge getReal() {
		if (realEdge == null )  {
			realEdge = new edu.wayne.ograph.ODFEdge(getOsrc().getReal(), getOdst().getReal(), getOLabel().getReal(), getFlag());
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
