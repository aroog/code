package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import edu.wayne.metrics.utils.CSVConst;

import oog.itf.IEdge;
import oog.itf.IObject;

public class EdgeTriplets extends EdgeMetricBase {

	public EdgeTriplets() {
	    super();
    }

	@Override
    public String getHeader() {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public String getHeaderShort() {
	    // TODO Auto-generated method stub
	    return null;
    }


	/*
	 * Information about an OEdge represented in terms of <A,D,B> triplets instead of RuntimeObjects.
	 * An OEdge in an OGraph is represented as: OEdge E = <O_src, O_dst, ...>
	 */
	private class EdgeInfoBase implements EdgeInfo {

		// TODO: Add additional information
		// FieldName
		// EdgeType: "Points-To", "DataFlow".
		// DeclaredType (of field), which could be a supertype of the concrete class.
		
		private ADBTriplet srcTriplet = null;
		private ADBTriplet dstTriplet = null;
		
		private IEdge edge = null;
		
		public EdgeInfoBase(IEdge edge) {
			this.edge = edge;
		}

		/**
	     * @return the srcTriplet
	     */
	    public ADBTriplet getSrcTriplet() {
	    	// Lazily populate dstTripet	    	
	    	if (srcTriplet == null ) {
	    		IObject fromObject = edge.getOsrc();
	    		this.srcTriplet = ADBTriplet.getTripletFrom(fromObject);
	    	}
	    	return srcTriplet;
	    }


		public ADBTriplet getDstTriplet() {
			// Lazily populate dstTripet
			if (dstTriplet == null) {
	    		IObject toObject = edge.getOdst();
				this.dstTriplet = ADBTriplet.getTripletFrom(toObject);
			}
			return dstTriplet;
		}

		// TODO: How is this being used
		public void writeTo(Writer writer) throws IOException {
	    	ADBTriplet src = this.getSrcTriplet();
	    	
	    	writer.append(CSVConst.DOUBLE_QUOTES);
	    	writer.append(src.toObjectString());
	    	writer.append(CSVConst.DOUBLE_QUOTES);
	    	writer.append(CSVConst.COMMA);

	    	ADBTriplet dst = this.getDstTriplet();
	    	
	    	writer.append(CSVConst.DOUBLE_QUOTES);
	    	writer.append(dst.toObjectString());
	    	writer.append(CSVConst.DOUBLE_QUOTES);
	    	writer.append(CSVConst.COMMA);
	    }
		
		@Override
		public void writeShortTo(Writer writer) throws IOException {
			// TODO: Implement me
	    }		

		@Override
        public DataPoint[] getDataPoints() {
	        return new DataPoint[0];
        }
	}

	@Override
    public void compute(Set<IEdge> allEdges) {
		edgeInfos = new HashSet<EdgeInfo>();

		for (IEdge edge : allEdges) {
			EdgeInfo edgeInfo = new EdgeInfoBase(edge);
			edgeInfos.add(edgeInfo);
		}

    }

	@Override
    public String[] getColumnHeaders() {
	    // TODO Auto-generated method stub
	    return null;
    }
	

	@Override
    public DataPoint[] getDataPoints() {
		Set<EdgeInfo> edgeInfos = getEdgeInfos();
		
		int numEdges = edgeInfos.size();

		return new DataPoint[] { new DataPoint("NumEdges", Float.valueOf(numEdges)) };
    }
	
}
