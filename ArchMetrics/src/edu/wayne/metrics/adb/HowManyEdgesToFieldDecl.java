package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IElement;
import ast.FieldDeclaration;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.metrics.qual.Q_1FnE;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.ograph.OPTEdge;

/**
 * How many edges are traced back to the same field declaration
 * 
 * TODO: Measure virtual fields separately from the non-virtual ones?
 * TODO: Exclude fields of some common base types, like Object, Serializable, etc.
 * (A.k.a. the "trivial types" from the OOG)
 * (But those cases end up being really difficult for comprehension!)
 * Base classes that have fields of a type that's very general! That should be an anti-pattern!
 * 
 * TODO: HIGH. Investigate some very large numbers
 * NOTE: maybe large number of 1FnE indicates a large number of imprecise edges in OOG (false positive)
 * which are inherently there (since the OOG is sound).
 * Threat to validity: using an abstracted graph with false positives to measure comprehensibility
 * 
 * If the numbers are too high or higher than they should be, they could indicate some design smell: the lack of user-defined types, loosely-typed containers
 * (see discussion from QoSA paper)
 * 
 */
public class HowManyEdgesToFieldDecl extends EdgeMetricBase {
	private static String HEADER = "FieldDeclaration,HowMany,Edges";
	private static String SHORT_HEADER = "ClusterSize,Key";
	
	// DONE. Rename: "HowMany" -> "N"
	private static final String HOW_MANY = "N";

	public HowManyEdgesToFieldDecl() {
	    super();
	    
	    this.generateShortOutput = true;
	    
	    // this.shortName = "ETFD";		    
	    // this.shortName = "HME";
	    // DONE. Rename: 1DnE -> 1FnE: D is defined to already mean Domain, so use F for Field 
	    this.shortName = "1FnE";
    }

	public String getHeader() {
		return HEADER;
	}
/*	
	// TODO: HIGH - We need a method that returns the field associated with an EGDE
	public String getFieldType() {
    	return fieldType;
    }*/
	
	
	@Override
    public String getHeaderShort() {
	    return SHORT_HEADER;
    }

	// Made the class public from private to access the Field and set of edges
	public class HowManyEdgesInfo implements EdgeInfo {
		// TODO: HIGH. Double-check that we are hashing FieldDeclaration objects and not ending up with multiple ones.
		// How does FieldDeclaration deal with inherited fields???
		private FieldDeclaration fieldDeclaration;
		// DONE. Store the set of edges: the howMany field is just the set size()
		private Set<IElement> elems = new HashSet<IElement>();
		
		// TODO: HIGH. Get rid of storing IEdges as both String and IElements
		private Set<String> edgeSet = new HashSet<String>();
		private Type field;
		
		public Type getField() {
			return field;
		}
		public FieldDeclaration getFieldDeclaration() {
			return fieldDeclaration;
		}
	
		//TOSUM: Return the set of IElement 
		public Set<IElement> getElems() {
			return elems;
		}
		

		@Override
        public void writeTo(Writer writer) throws IOException {
			// TODO: Need to escape FieldDeclaration?
	    	writer.append(CSVConst.DOUBLE_QUOTES);
	    	writer.append(this.fieldDeclaration.toString());
	    	writer.append(CSVConst.DOUBLE_QUOTES);
	    	writer.append(CSVConst.COMMA);
	    	
	    	writer.append(CSVConst.DOUBLE_QUOTES);
	    	writer.append(Integer.valueOf(this.edgeSet.size()).toString());
	    	writer.append(CSVConst.DOUBLE_QUOTES);
	    	writer.append(CSVConst.COMMA);	    	
	    	
	    	int ii = 0;
	    	for(String edge : this.edgeSet ) {
	    		writer.append(CSVConst.DOUBLE_QUOTES);
	    		writer.append(edge);
	    		writer.append(CSVConst.DOUBLE_QUOTES);
	    		ii++;
	    		
	    		if ( ii < this.edgeSet.size() ) {
		    		writer.append(CSVConst.NEWLINE);
		        	writer.append(CSVConst.COMMA);	
		        	writer.append(CSVConst.COMMA);		    		
	    		}
	    	}
        }
		
		@Override
        public void writeShortTo(Writer writer) throws IOException {
			// Removed Double quotes
			writer.append(Integer.valueOf(this.edgeSet.size()).toString());
			writer.append(CSVConst.COMMA);
			
	    	writer.append(CSVConst.DOUBLE_QUOTES);
			// TODO: Need to escape FieldDeclaration?
	    	writer.append(this.fieldDeclaration.toString());
	    	writer.append(CSVConst.DOUBLE_QUOTES);
        }		

		@Override
        public DataPoint[] getDataPoints() {
			// NOTE: Careful, the names must match whatever getColumnHeaders() returns
			List<DataPoint> dataPoints = new ArrayList<DataPoint>();
			dataPoints.add(new DataPoint(HOW_MANY, this.edgeSet.size()));
			return dataPoints.toArray(new DataPoint[0]);
		}
		

		@Override
		// TODO: HIGH. Fix implementation of equals, hashCode: take into  account all fields	
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof HowManyEdgesInfo)) {
				return false;
			}

			HowManyEdgesInfo key = (HowManyEdgesInfo) o;
			// HACK: FieldDeclaration does NOT implement equals
			return this.fieldDeclaration.equals(key.fieldDeclaration) && (this.edgeSet.equals(key.edgeSet));
		}		

		// TODO: HIGH. Fix implementation of equals, hashCode: take into  account all fields	
	    public int hashCode() {
		    final int prime = 31;
		    int result = 1;
			// HACK: FieldDeclaration does NOT implement hashCode
		    result = prime * result + ((fieldDeclaration == null) ? 0 : fieldDeclaration.hashCode());
		    result = prime * result + ((edgeSet == null) ? 0 : edgeSet.hashCode());
		    return result;
	    }
		
				
	}
	

	public void compute(Set<IEdge> allEdges) {
		edgeInfos = new HashSet<EdgeInfo>();

		// TODO: HIGH. Investigate some more ReverseTraceabilityMap: could be returning too many things; need safety checks below based on subtyping.
		// It could be that Traceability data contains incorrect data
		// Check how populating Traceabiity map from: TraceabilityObject -> IElement
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
		Map<FieldDeclaration, Set<IElement>> fieldDeclMap = instance.getFieldDeclMap();
		
		for (Entry<FieldDeclaration, Set<IElement>> entry : fieldDeclMap.entrySet()) {
			FieldDeclaration fieldDecl = entry.getKey();
			
			// Exclude ones greater than 1?
			Set<IElement> elems = entry.getValue();
			
			Set<String> edgeSet = new HashSet<String>();
			
			// NOTE: elems could also contain objects!
			// Filter out edges: count only points-to edges!
			for (IElement element : elems) {
				if (element instanceof OPTEdge) {
					OPTEdge edge = (OPTEdge) element;

					Type srcObjType = edge.getOsrc().getC();
					Type dstObjType = edge.getOdst().getC();

					// TODO: Check for nulls
					TypeDeclaration enclosingType = fieldDecl.enclosingType;
					Type fieldType = fieldDecl.fieldType;

					// Sanity check using subtyping
					if (enclosingType != null && fieldType != null) {
						Type declaringType = enclosingType.type;
						if (Util.isSubtypeCompatible(srcObjType, declaringType)
						        && Util.isSubtypeCompatible(dstObjType, fieldType)) {
							edgeSet.add(Util.toString(edge));
						}
					}
				}
			}
			
			HowManyEdgesInfo edgeInfo = new HowManyEdgesInfo();
			edgeInfo.fieldDeclaration = fieldDecl;
			edgeInfo.edgeSet = edgeSet;
			// Returning the elems of IElement for each edge
			edgeInfo.elems = elems;
			edgeInfos.add(edgeInfo);
		}
	}
	


	@Override
    public String[] getColumnHeaders() {
	    return new String[]{HOW_MANY};
    }
	
	@Override
	public void visitOutliers(Writer writer, Set<EdgeInfo> outliers) throws IOException {
		Q_1FnE qVisit = new Q_1FnE(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}
}
