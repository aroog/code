package edu.wayne.ograph.internal;

/**
 *
 *  XXX. One optimization is to exclude the fieldName from the OPTEdge value equality.
 *  This way, one OPTEdge will have many traceability entries...but there will be one edge.
 *  Yes, the previous thinking was that "abstraction of edges" happens in the displaygraph.
 *  But fewer edges means faster lookup, graph comparison, etc.
 *  The example of: 96 new LetterTile... one object with 96 edges. Make the OGraph very needlessly...ornate...
 */
public class OPTEdge extends OEdge {

	private OPTEdgeKey key;

	protected OPTEdge() {
		super();
	}

	public OPTEdge(OPTEdgeKey key) {
		this.key = key;
	}

	public String getFieldName() {
		return key.getFieldName();
	}
	
	private edu.wayne.ograph.OPTEdge realEdge = null;
	
	@Override
    public edu.wayne.ograph.OEdge getReal() {
		if (realEdge == null )  {
			realEdge = new edu.wayne.ograph.OPTEdge(getOsrc().getReal(), getOdst().getReal(), getFieldName());
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

	@Override
    public String toString() {
		return this.getFieldName();
    }
}
