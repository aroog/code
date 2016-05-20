package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;
import ast.AstNode;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import ast.EdgeDFTraceability;
import ast.EdgePTTraceability;
import ast.Expression;
import ast.ExpressionType;
import ast.FieldAccess;
import ast.FieldWrite;
import ast.MethodInvocation;
import edu.wayne.metrics.utils.CSVConst;

/**
 * EXPERIMENTAL: Measure the strength of the communication between objects.
 * Counts the number of expressions along the multi-edges between two objects.

 * The expressions between objects:
 * For DF edges
	 * FieldRead
	 * FieldWrites
	 * MethodInvocation
 * For PT edges:
 *   * Field Declarations
 *   
 * Compute some weighted average of the different types of expressions?
 * 
 * TODO: Do a version that does not take into account directionality of communication;
 * come up with different versions
 * 
 * TODO: Do variants that focus on one type of edge?
 * TODO: Do variants that focus on one type of expression?
 * TODO: Include self-edges: some of them are quite heavy, DF-wise
 * TODO: Edges between objects seem to be more about "coupling of objects".
 * TODO: Self-edges seem to be more about "cohesion of objects".
 * 
 * WACI. Lift relations to the types of the objects?
 * 
 * TODO: HIGH. The output for this metric is very verbose/bulky (3.5MB for PX). Make it more concise!
 */
public class WeightOfEdgeBetweenTwoObjects extends ObjectSetStrategyBase {
	private static String HEADER = "SrcObject,DstObject,Communication,HowMany";
	private static String SHORT_HEADER = "ClusterSize,Key";

	private Set<IEdge> allEdges = null;
	
	public WeightOfEdgeBetweenTwoObjects(Set<IEdge> allEdges) {
		super();
		this.allEdges = allEdges;
		
		// this.shortName = "WEBO";
		this.shortName = "EBO";
	}
	
	@Override
    public String getHeader() {
	    return  HEADER;
    }
	
	@Override
    public String getHeaderShort() {
	    return SHORT_HEADER;
    }		

	// TODO: LOW. Reference underlying IObject?
	private class WeightOfEdgeInfo implements ObjectInfo {

		Set<String> commSet = new HashSet<String>();

		public String src;

		public String dst;
		
		@Override
        public void writeTo(Writer writer)  throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.src);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.dst);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);			
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(commSet.toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(commSet.size()));
			writer.append(CSVConst.DOUBLE_QUOTES);
        }
		
		@Override
        public void writeShortTo(Writer writer)  throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(commSet.size()));
			writer.append(CSVConst.DOUBLE_QUOTES);			
			writer.append(CSVConst.COMMA);
			// TODO: HIGH. Come up with a shorter key than this (converting the whole set to a String)!
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(commSet.toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
        }		

		@Override
        public DataPoint[] getDataPoints() {
			// NOTE: Careful, the names must match whatever getColumnHeaders() returns			
			return new DataPoint[]{ new DataPoint("W", (float)commSet.size()) };
        }
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof WeightOfEdgeInfo)) {
				return false;
			}

			WeightOfEdgeInfo key = (WeightOfEdgeInfo) o;
			return this.src.equals(key.src) && this.dst.equals(key.dst) && this.commSet.equals(key.commSet);
		}

		// Always override hashcode when you override equals
		@Override
		public int hashCode() {
			int result = 17;

			result = 37 * result + (src == null ? 0 : src.hashCode());
			result = 37 * result + (dst == null ? 0 : dst.hashCode());
			result = 37 * result + (commSet == null ? 0 : commSet.hashCode());
			return result;
		}		
	}
	
	@Override
	public void compute(Set<IObject> allObjects) {
		
		this.objectInfos = new HashSet<ObjectInfo>(); 


		for (IObject src : allObjects) {
			for (IObject dst : allObjects) {
				if ( src == dst) {
					continue;
				}
				
				String srcKey = ADBTriplet.getTripletFrom(src).toShortString();
				String dstKey = ADBTriplet.getTripletFrom(dst).toShortString();

				// NOTE: Use a set to filter out duplicates
				Set<String> commSet = new HashSet<String>();
				Set<IEdge> edgesBetween = Util.getEdgesBetween(allEdges, src, dst, false);
				for(IEdge edgeBetween : edgesBetween ) {
					Set<BaseTraceability> traceability = edgeBetween.getTraceability();
					for(BaseTraceability trace: traceability ) {
						
						// TODO: Pick the different strategy for the communication
						String comm = getCommunication1(trace);
						// String comm = getCommunication2(trace);
						if ( comm != null && comm.length() > 0 ) {
							commSet.add(comm);
						}
					}
				}
				
				// TODO: HIGH: Include only objects that have any communication. Why not show everything?
				// Since we are measuring the "connectivity" of the graph here, 
				// We are measuring the "strength" of the communication when it exists
				// TODO: HIGH. Should we not add the "trivial" clusters of size 0?
				if ( commSet.size() > 0 ) {
					WeightOfEdgeInfo objectInfo = new WeightOfEdgeInfo();
					objectInfo.src = srcKey;
					objectInfo.dst = dstKey;
					objectInfo.commSet = commSet;
					objectInfos.add(objectInfo);
				}
			}
		}
	}

	// Strategy 1 for computing communication
	// Avg. = 1.9375
	private String getCommunication1(BaseTraceability trace) {
	    // TODO: HIGH. XXX. Do something more precise here!
	    // Parse the content of the traceability object!!!
	    // Not sure if BaseTraceability.toString() will always give us what we need!
		// Right now, .toString() simply returns the complexExpression
	    return trace.toString(); 
	}
	
	// TODO: HIGH. XXX. do something with points-to edges.
	// Strategy 2 for co1puting communication
	// Avg. = 2.0
	private String getCommunication2(BaseTraceability trace) {
	    StringBuilder builder = new StringBuilder();
	    
	    if ( trace instanceof EdgePTTraceability ) {
	    	EdgePTTraceability ptTrace = (EdgePTTraceability)trace;
	    	
	    	// trace.expression is null here
	    	
	    }
	    	
	    if (trace instanceof EdgeDFTraceability ) {
	    	EdgeDFTraceability dfTrace = (EdgeDFTraceability)trace;
	    }

	    // Handle DF edges.
	    AstNode exprNode = trace.getExpression();
		if (exprNode instanceof Expression) {
	    	Expression expression = (Expression) exprNode;
	    	
	    	ExpressionType expressionType = expression.getExpressionType();
	    	// Add the expression type
	    	builder.append(expressionType.toString());
	    	builder.append(":");
	    	
			switch (expressionType) {
	    	case NewExpression:
	    		ClassInstanceCreation objectCreation = (ClassInstanceCreation) expression;
	    		builder.append( objectCreation.complexExpression );
	    		break;
	    	case FieldRead:
	    		FieldAccess fieldRead = (FieldAccess) expression;
	    		builder.append( fieldRead.complexExpression );
	    		break;
	    	case FieldWrite:
	    		FieldWrite fieldWrite = (FieldWrite) expression;
	    		builder.append( fieldWrite.complexExpression );
	    		break;
	    	case MethodInvocation:
	    		MethodInvocation methInvk = (MethodInvocation) expression;
	    		builder.append( methInvk.complexExpression );
	    		break;
	    	case Unknown:
	    		break;
	    	default:
	    		break;
	    	}
	    }
	    
	    return builder.toString();
    }
	
	@Override
	public String[] getColumnHeaders() {
		// DONE. Rename: "EdgeWeight" -> just "W"
		return new String[] { "W" };
	}
}
