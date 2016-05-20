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
import ast.MethodInvocation;
import ast.Type;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.qual.Q_1MInE_RecType;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.ograph.ODFEdge;

public class HowManyEdgesToMethodInvok_RecType extends EdgeMetricBase {

	private static String HEADER = "MethodInvocation,HowMany,Domain,Edges";

	private static String SHORT_HEADER = "ClusterSize,Key";

	private static final String HOW_MANY = "N";

	public HowManyEdgesToMethodInvok_RecType() {
		super();
		this.generateShortOutput = true;
		this.shortName = "1MInE_RecType";
	}

	public String getHeader() {
		return HEADER;
	}

	@Override
	public String getHeaderShort() {
		return SHORT_HEADER;
	}

	public class HowManyEdges_MIRecType implements EdgeInfo {
		private MethodInvocation methodInvok;

		private Set<IElement> elems = new HashSet<IElement>();

		// Edge set for 1MInE_RecType
		private Set<String> recEdgeSet = new HashSet<String>();

		private String domains = new String();

		public MethodInvocation getMethodInvocation() {
			return methodInvok;
		}

		public Set<IElement> getElems() {
			return elems;
		}

		public String getDomain() {
			return domains;
		}

		@Override
		public void writeTo(Writer writer) throws IOException {

			writer.append(CSVOutputUtils.sanitize(this.methodInvok.toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(Integer.valueOf(this.recEdgeSet.size()).toString()));
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
			writer.append(CSVOutputUtils.sanitize(this.methodInvok.toString()));
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
			if (!(o instanceof HowManyEdges_MIRecType)) {
				return false;
			}

			HowManyEdges_MIRecType key = (HowManyEdges_MIRecType) o;
			return this.methodInvok.equals(key.methodInvok) && (this.recEdgeSet.equals(key.recEdgeSet))
			        && (this.elems.equals(key.elems)) && (this.domains.equals(key.domains));
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((methodInvok == null) ? 0 : methodInvok.hashCode());
			result = prime * result + ((recEdgeSet == null) ? 0 : recEdgeSet.hashCode());
			result = prime * result + ((elems == null) ? 0 : elems.hashCode());
			result = prime * result + ((domains == null) ? 0 : domains.hashCode());
			return result;
		}
	}

	public void compute(Set<IEdge> allEdges) {

		edgeInfos = new HashSet<EdgeInfo>();
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
		Map<MethodInvocation, Set<IElement>> methodInvkMap = instance.getMethodInvokMap();

		// Get Util instance to access methods.
		Util util = Util.getInstance();

		if (methodInvkMap != null) {
			for (Entry<MethodInvocation, Set<IElement>> entry : methodInvkMap.entrySet()) {
				MethodInvocation methodInvok = entry.getKey();
				// Edges for MIs and Edges associated with Rec
				Set<IElement> elems = entry.getValue();
				Set<String> recEdgeSet = new HashSet<String>();
				MethodInvocation metInv_RecType = null;
				String domain = "";
				Crystal crystal = Crystal.getInstance();

				// Receiver
				Type recvType = methodInvok.recvType;

				// Get the ITypeBinding. No domain info for Primitive types.
				ITypeBinding recvTypeBinding = crystal.getTypeBindingFromName(recvType.getFullyQualifiedName());
				if (recvTypeBinding != null && !recvTypeBinding.isPrimitive() && methodInvok.recvAnnotation != null) {
					// Domain info for recv of Method Invok
					domain = methodInvok.recvAnnotation;
				}

				for (IElement element : elems) {
					if (element instanceof ODFEdge) {
						ODFEdge edge = (ODFEdge) element;

						// Destination of the edge
						Type dstObjType = edge.getOdst().getC();
						String dstType = dstObjType.getFullyQualifiedName();
						// 1MInE_RecType - Export edges only
						String flag = edge.getFlag().toString();

						if (flag.equals("Export")) {

							// If recv types are containers.
							if (Util.isContainerType(recvType.getFullyQualifiedName())) {
								ITypeBinding typeBindingC = crystal.getTypeBindingFromName(dstType);
								ITypeBinding typeBindingB = util.getElementOfContainer(recvType.getFullyQualifiedName());

								// Type of receiver are not the same as destination oobejcts
								if (recvType != dstObjType) {
									if (Util.isSubtypeCompatible(typeBindingC, typeBindingB)) {
										// Edges associated with Recv
										recEdgeSet.add(edge.toString());
										metInv_RecType = methodInvok;
									}
								}
							}
							// If rec types are not containers
							// Type of receiver are not the same as destination oobjects

							else if (recvType != dstObjType) {
								if (Util.isSubtypeCompatible(dstObjType, recvType)
								        || Util.isSubtypeCompatible(recvType, dstObjType)) {
									// Edges associated with Rec
									recEdgeSet.add(edge.toString());
									metInv_RecType = methodInvok;

								}
							}

						}
					}
				}

				if (metInv_RecType != null) {
					HowManyEdges_MIRecType edgeInfo = new HowManyEdges_MIRecType();
					edgeInfo.methodInvok = metInv_RecType;
					edgeInfo.recEdgeSet = recEdgeSet;
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
		
		Q_1MInE_RecType qVisit = new Q_1MInE_RecType(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();

	}

}
