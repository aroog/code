package edu.wayne.ograph.internal;

import edu.wayne.ograph.internal.OEdgeKey;
import edu.wayne.ograph.internal.OObject;
import edu.wayne.ograph.internal.OPTEdgeKey;

/**
 *
 *  XXX. One optimization is to exclude the fieldName from the OPTEdge value equality.
 *  This way, one OPTEdge will have many traceability entries...but there will be one edge.
 *  Yes, the previous thinking was that "abstraction of edges" happens in the displaygraph.
 *  But fewer edges means faster lookup, graph comparison, etc.
 *  The example of: 96 new LetterTile... one object with 96 edges. Make the OGraph very needlessly...ornate...
 */
public class OPTEdgeKey extends OEdgeKey {

	private String fieldName;

	protected OPTEdgeKey() {
		super();
	}

	public OPTEdgeKey(OObject osrc,
			OObject odst,
			String fieldName) {
		super(osrc, odst);
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((odst == null) ? 0 : odst.hashCode());
		result = prime * result + ((osrc == null) ? 0 : osrc.hashCode());
		// result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof OPTEdgeKey))
			return false;
		
		OPTEdgeKey other = (OPTEdgeKey) obj;
		// For comparing OObjects, just compare references; no need for value equality		
		return (osrc == other.osrc) && (odst == other.odst)/* && (fieldName == other.fieldName)*/;
	}
	
}
