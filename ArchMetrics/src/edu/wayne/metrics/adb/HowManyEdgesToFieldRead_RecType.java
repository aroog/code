package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ITypeBinding;

import oog.itf.IEdge;
import oog.itf.IElement;
import ast.FieldAccess;
import ast.FieldDeclaration;
import ast.Type;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.qual.Q_1FRnE_RecType;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.ograph.ODFEdge;

public class HowManyEdgesToFieldRead_RecType extends EdgeMetricBase {
	private static String HEADER = "FieldRead,HowMany,Domain,Edges";

	private static String SHORT_HEADER = "ClusterSize,Key";

	private static final String HOW_MANY = "N";

	public HowManyEdgesToFieldRead_RecType() {
		super();

		this.generateShortOutput = true;
		this.shortName = "1FRnE_RecType";
	}

	public String getHeader() {
		return HEADER;
	}

	@Override
	public String getHeaderShort() {
		return SHORT_HEADER;
	}

	public class HowManyEdges_FRRecType implements EdgeInfo {
		private FieldAccess fieldRead;

		private String domains;

		private Set<IElement> elems = new HashSet<IElement>();

		private Set<String> recEdgeSet = new HashSet<String>();

		public FieldAccess getFieldRead() {
			return fieldRead;
		}

		public Set<IElement> getElems() {
			return elems;
		}

		public String getDomain() {
			return domains;
		}

		@Override
		public void writeTo(Writer writer) throws IOException {

			writer.append(CSVOutputUtils.sanitize(this.fieldRead.toString()));
			writer.append(CSVConst.COMMA);
			writer.append(Integer.valueOf(this.recEdgeSet.size()).toString());
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getDomain().toString()));
			writer.append(CSVConst.COMMA);
			int count = 0;
			for (String edge : this.recEdgeSet) {
				writer.append(CSVOutputUtils.sanitize(edge));
				count++;
				if (count < this.recEdgeSet.size()) {
					writer.append(CSVConst.NEWLINE);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
				}
			}
		}

		@Override
		public void writeShortTo(Writer writer) throws IOException {
			writer.append(Integer.valueOf(this.recEdgeSet.size()).toString());
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.fieldRead.toString()));
		}

		@Override
		public DataPoint[] getDataPoints() {
			List<DataPoint> dataPoints = new ArrayList<DataPoint>();
			dataPoints.add(new DataPoint(HOW_MANY, this.recEdgeSet.size()));
			return dataPoints.toArray(new DataPoint[0]);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof HowManyEdges_FRRecType)) {
				return false;
			}

			HowManyEdges_FRRecType key = (HowManyEdges_FRRecType) o;
			return this.fieldRead.equals(key.fieldRead) && (this.recEdgeSet.equals(key.recEdgeSet))
			        && (this.elems.equals(key.elems)) && (this.domains.equals(key.domains));
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fieldRead == null) ? 0 : fieldRead.hashCode());
			result = prime * result + ((recEdgeSet == null) ? 0 : recEdgeSet.hashCode());
			result = prime * result + ((elems == null) ? 0 : elems.hashCode());
			result = prime * result + ((domains == null) ? 0 : domains.hashCode());
			return result;
		}
	}

	public void compute(Set<IEdge> allEdges) {
		edgeInfos = new HashSet<EdgeInfo>();
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
		Map<FieldAccess, Set<IElement>> fieldReadMap = instance.getFieldReadMap();
		String domain = "";
		Crystal crystal = Crystal.getInstance();

		if (fieldReadMap != null) {
			for (Entry<FieldAccess, Set<IElement>> entry : fieldReadMap.entrySet()) {
				FieldAccess fieldRead = entry.getKey();
				// Edges for FRs and Edges associated with Rec
				Set<IElement> elems = entry.getValue();
				Set<String> edgeSet = new HashSet<String>();

				Type recvType = fieldRead.recvType;

				// Field declaration corresponding to the field read
				// Why not use the annotations of the receiver? Well, We dont have it!
				FieldDeclaration fieldDeclr = fieldRead.fieldDeclaration;

				// Get the ITypeBinding. No domain info for Primitive types.				
				ITypeBinding fieldReadTypeBinding = crystal.getTypeBindingFromName(fieldDeclr.fieldType.getFullyQualifiedName());
				if (fieldReadTypeBinding != null && !fieldReadTypeBinding.isPrimitive()) {
  				    // The domain info of associated field declaration
					domain = Util.getDomainString(fieldDeclr.annotation);
				}

				// Only the edges that match the given condition
				FieldAccess read_RecType = null;

				for (IElement element : elems) {
					if (element instanceof ODFEdge) {
						ODFEdge edge = (ODFEdge) element;

						// Source object
						Type srcObjType = edge.getOsrc().getC();
						String flag = edge.getFlag().toString();

						// Import edges only
						if (flag.equals("Import")) {
							// Add a condition that the type of receiver oobject are not the same as source oobject
							// If not this measure is not useful, I will always have an edge.
							if (recvType != srcObjType) {
								if (Util.isSubtypeCompatible(srcObjType, recvType)
								        || Util.isSubtypeCompatible(recvType, srcObjType)) {
									edgeSet.add(edge.toString());
									read_RecType = fieldRead;
								}
							}
						}

					}
				}
				if (read_RecType != null) {
					HowManyEdges_FRRecType edgeInfo = new HowManyEdges_FRRecType();
					edgeInfo.fieldRead = read_RecType;
					edgeInfo.recEdgeSet = edgeSet;
					edgeInfo.elems = elems;
					edgeInfo.domains = domain;
					edgeInfos.add(edgeInfo);

				}
			}

		}

	}

	@Override
	public String[] getColumnHeaders() {
		return new String[] { HOW_MANY };
	}

	@Override
	public void visitOutliers(Writer writer, Set<EdgeInfo> outliers) throws IOException {
		Q_1FRnE_RecType qVisit = new Q_1FRnE_RecType(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}

}
