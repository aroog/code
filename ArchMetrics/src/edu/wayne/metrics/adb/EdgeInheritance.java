package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;
import ast.BaseTraceability;
import ast.FieldDeclaration;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.ograph.OPTEdge;


// TOMAR: TODO: HIGH. Look at NumPTEdges vs. #PTE in OOGMetricsReport.
// TODO: HIGH. Careful: this is just PTEdgeInheritance.
// TODO: HIGH. Can we also define a DFEdgeInheritance?
// - No. since this metric refers to declarations.

// DONE. Compute % inherited edges: |InheritedEdges| / |allEdges|
// TOMAR: TODO: HIGH. Revisit this metric.
// - Why not using fields aux. judgement
public class EdgeInheritance extends EdgeMetricBase {
	// TODO: Record how far down is the field inherited;
	// Could be related to the depth of the inheritance tree.
	private static String HEADER = "DeclaringType,FieldName,ConcreteType";

	private int numPtEdges = 0;
	
	public EdgeInheritance() {
	    super();
	    
	    // this.shortName = "PTIE";
	    this.shortName = "InhE";
    }

	@Override
    public String getHeader() {
	    return HEADER;
    }

	@Override
    public String getHeaderShort() {
	    return HEADER;
    }

	private class EdgeInheritanceInfo implements EdgeInfo {

		private String fieldName;
		private IEdge edge;
		private String declaringType;
		private String concreteType;

		@Override
        public void writeTo(Writer writer) throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.declaringType);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.fieldName);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.concreteType);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
//			writer.append("\"");
//			writer.append(this.edge.toString());
//			writer.append("\"");
        }

		@Override
		public void writeShortTo(Writer writer) throws IOException {
			// TODO: Implement me
		}

		@Override
		public DataPoint[] getDataPoints() {
			return new DataPoint[0];
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof EdgeInheritanceInfo)) {
				return false;
			}

			EdgeInheritanceInfo key = (EdgeInheritanceInfo) o;
			return this.fieldName.equals(key.fieldName) && this.declaringType.equals(key.declaringType) && this.concreteType.equals(key.concreteType);
		}

		// Always override hashcode when you override equals
		@Override
		public int hashCode() {
			int result = 17;

			result = 37 * result + (fieldName == null ? 0 : fieldName.hashCode());
			result = 37 * result + (declaringType == null ? 0 : declaringType.hashCode());
			result = 37 * result + (concreteType == null ? 0 : concreteType.hashCode());
			return result;
		}		
	}

	@Override
    public void compute(Set<IEdge> allEdges) {
		edgeInfos = new HashSet<EdgeInfo>();
		
		for (IEdge edge : allEdges) {
			
			// NOTE: we just use points-to edges here...
			if (!(edge instanceof OPTEdge) ) {
				continue;
			}
			
			this.numPtEdges++;
			
			IObject osrc = edge.getOsrc();
			Type Asrc = osrc.getC();
			
			OPTEdge optEdge = (OPTEdge)edge;
			String fieldName = optEdge.getFieldName();
			
			// By creating EdgeInfo based on the traceability, we can create many such objects.
			// And obtain an out-of-range percentage!
			// So we use value equality.
			for(BaseTraceability trace: edge.getTraceability() ) {
				FieldDeclaration fieldDecl = ReverseTraceabilityMap.getFieldDeclaration(trace);
				
				if (fieldDecl != null) {
					TypeDeclaration enclosingTypeDecl = fieldDecl.enclosingType;

					if (enclosingTypeDecl != null) {
						Type enclosingType = enclosingTypeDecl.type;
						// DONE. Check that the field names are the same.
						// DONE. Check inheritance. 
						if (fieldName != null && fieldName.equals(fieldDecl.fieldName) && !enclosingType.equals(Asrc)
						        && Util.isSubtypeCompatible(Asrc, enclosingType)) {

							EdgeInheritanceInfo info = new EdgeInheritanceInfo();
							info.fieldName = fieldDecl.fieldName;
							info.declaringType = fieldDecl.enclosingType.type.toString();
							info.concreteType = Asrc.toString();
							info.edge = edge;

							edgeInfos.add(info);
						}
					}
				}
			}
		}
	}

	@Override
    public DataPoint[] getDataPoints() {
		int numAllEdges = this.numPtEdges;
		int numInheritedEdges = this.edgeInfos.size();
		// TODO: HIGH. XXX. Check that the percentage is within range.
		float pctInheritedEdges = (float)numInheritedEdges/(float)numAllEdges * 100;
		
		if (pctInheritedEdges > 100.0f) {
			int debug = 0; debug++;
			System.err.println("EdgeInheritance: percentage out of range");
		}
		
		return new DataPoint[] { 
				// DONE. Rename: "NumPTEdges" -> NAE (NumAllEdges)
				new DataPoint("NAE", numAllEdges),
				// DONE. Rename: "NumInheritedEdges" -> N
				new DataPoint("N", numInheritedEdges),
				// DONE. Rename: "%InheritedEdges" -> P				
				new DataPoint("P", pctInheritedEdges) };
    }

}
