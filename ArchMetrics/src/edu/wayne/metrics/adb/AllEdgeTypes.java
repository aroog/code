package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.ograph.OPTEdge;

import oog.itf.IEdge;
import oog.itf.IObject;

// TODO: Rename: AllEdges
public class AllEdgeTypes extends EdgeMetricBase {
	private static String HEADER = "Blank";
	
	private int numEdges;
	private int numPtEdges;

	public AllEdgeTypes() {
	    super();
	    
	    this.shortName = "All";	 
    }

	@Override
    public String getHeader() {
	    return HEADER;
    }

	@Override
    public String getHeaderShort() {
	    return HEADER;
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

		public void writeTo(Writer writer) throws IOException {
			// DO NOTHING
			
//	    	ADBTriplet src = this.getSrcTriplet();
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(src.getInstanceA());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(src.getTypeA());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//
//	    	// writer.append(",\"" + srctriplet.getRawTypeA()+ "\"");
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(src.getDomainD());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//	    	// writer.append(",\"" + srctriplet.getRawDomainD() + "\"");
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(src.getInstanceB());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//	    	
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(src.getTypeB());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//	    	// writer.append(",\"" + srctriplet.getRawTypeB()+ "\"");
//	    
//
//	    	ADBTriplet dst = this.getDstTriplet();
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(dst.getInstanceA());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//	    	
//
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(dst.getTypeA());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//	    	
//	    	// writer.append(",\"" + dstTriplet.getRawTypeA()+ "\"");
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(dst.getDomainD());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//	    	// writer.append(",\"" + dstTriplet.getRawDomainD() + "\"");
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(dst.getInstanceB());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//
//	    	
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(dst.getTypeB());
//	    	writer.append(CSVConst.DOUBLE_QUOTES);
//	    	writer.append(CSVConst.COMMA);
//	    	// writer.append(",\"" + dstTriplet.getRawTypeB()+ "\"");
	    }
		
		@Override
		public void writeShortTo(Writer writer) throws IOException {
			// TODO: Implement me
	    }		

		@Override
        public DataPoint[] getDataPoints() {
	        return null;
        }
	}

	@Override
    public void compute(Set<IEdge> allEdges) {
		for (IEdge edge : allEdges) {
			numEdges++;
			
			if (edge instanceof OPTEdge ) {
				numPtEdges++;
			}
			
//			EdgeInfo edgeInfo = new EdgeInfoBase(edge);
//			edgeInfos.add(edgeInfo);
		}

    }

	@Override
    public DataPoint[] getDataPoints() {

		return new DataPoint[] { new DataPoint("NumEdges", Float.valueOf(numEdges)),
								 new DataPoint("NumPtEdges", Float.valueOf(numPtEdges)) };
    }
	
}
