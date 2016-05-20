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
import ast.MethodDeclaration;
import ast.MethodInvocation;
import ast.Type;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.qual.Q_1MInE_RetType;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.ograph.ODFEdge;

public class HowManyEdgesToMethodInvok_RetType extends EdgeMetricBase {

	private static String HEADER = "MethodInvocation,HowMany,Domain,Edges";

	private static String SHORT_HEADER = "ClusterSize,Key";

	private static final String HOW_MANY = "N";

	public HowManyEdgesToMethodInvok_RetType() {
		super();

		this.generateShortOutput = true;
		this.shortName = "1MInE_RetType";
	}

	public String getHeader() {
		return HEADER;
	}

	@Override
	public String getHeaderShort() {
		return SHORT_HEADER;
	}

	public class HowManyEdges_MIRetType implements EdgeInfo {
		private MethodInvocation methodInvok;

		private Set<IElement> elems = new HashSet<IElement>();

		// Edge set for 1MInE_RetType
		private Set<String> retEdgeSet = new HashSet<String>();

		private String domains;

		public MethodInvocation getMethodInvocation() {
			return methodInvok;
		}

		public String getDomain() {
			return domains;
		}

		public Set<IElement> getElems() {
			return elems;
		}

		@Override
		public void writeTo(Writer writer) throws IOException {

			writer.append(CSVOutputUtils.sanitize(this.methodInvok.toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(Integer.valueOf(this.retEdgeSet.size()).toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getDomain().toString()));
			writer.append(CSVConst.COMMA);
			int count = 0;
			for (String edge : this.retEdgeSet) {
				writer.append(CSVOutputUtils.sanitize(edge));
				count++;
				if (count < this.retEdgeSet.size()) {
					writer.append(CSVConst.NEWLINE);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
				}
			}

		}

		@Override
		public void writeShortTo(Writer writer) throws IOException {
			writer.append(Integer.valueOf(this.retEdgeSet.size()).toString());
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.methodInvok.toString()));
		}

		@Override
		public DataPoint[] getDataPoints() {
			List<DataPoint> dataPoints = new ArrayList<DataPoint>();
			dataPoints.add(new DataPoint(HOW_MANY, this.retEdgeSet.size()));
			return dataPoints.toArray(new DataPoint[0]);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof HowManyEdges_MIRetType)) {
				return false;
			}

			HowManyEdges_MIRetType key = (HowManyEdges_MIRetType) o;
			return this.methodInvok.equals(key.methodInvok) && (this.retEdgeSet.equals(key.retEdgeSet))
			        && (this.elems.equals(key.elems)) && (this.domains.equals(key.domains));
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((methodInvok == null) ? 0 : methodInvok.hashCode());
			result = prime * result + ((retEdgeSet == null) ? 0 : retEdgeSet.hashCode());
			result = prime * result + ((elems == null) ? 0 : elems.hashCode());
			result = prime * result + ((domains == null) ? 0 : domains.hashCode());
			return result;
		}
	}

	public void compute(Set<IEdge> allEdges) {
		edgeInfos = new HashSet<EdgeInfo>();
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
		Map<MethodInvocation, Set<IElement>> methodInvkMap = instance.getMethodInvokMap();
		Util util = Util.getInstance();
		String domain = "";

		if (methodInvkMap != null) {
			for (Entry<MethodInvocation, Set<IElement>> entry : methodInvkMap.entrySet()) {
				MethodInvocation methodInvok = entry.getKey();

				MethodDeclaration methodDeclr = methodInvok.methodDeclaration;

				Set<IElement> elems = entry.getValue();
				Set<String> retEdgeSet = new HashSet<String>();

				MethodInvocation metInv_RetType = null;

				Crystal crystal = Crystal.getInstance();
				Type methodreturnType = null;

				// TODO. DONE. I have to change this to Method Declaration's Return Type!!!
				//if (methodInvok != null) {
					Type retType = methodInvok.retType;

					Type recvType = methodInvok.recvType;

					// I have to check if MethodDeclr is NOT NULL. NO Domain for primitive time.
					if (methodDeclr != null) {
						methodreturnType = methodDeclr.returnType;
						ITypeBinding retTypeBinding = crystal.getTypeBindingFromName(methodreturnType.getFullyQualifiedName());
						if (retTypeBinding != null && !retTypeBinding.isPrimitive()
						        && methodDeclr.returnAnnotation != null) {
							// Domain info for Ret Type
							domain = Util.getDomainString(methodDeclr.returnAnnotation);
						}
					}

					for (IElement element : elems) {
						if (element instanceof ODFEdge) {
							ODFEdge edge = (ODFEdge) element;

							Type flowObjType = edge.getFlow().getC();
							String flag = edge.getFlag().name();
							String flowType = flowObjType.getFullyQualifiedName();

							if (flag == "Import" && retType != null && recvType != null) {
								// Type of return are not the same as flow oobjects.

								if (retType != flowObjType) {
									// If return types are containers.

									if (Util.isContainerType(retType.getFullyQualifiedName())) {
										if (Util.isContainerType(flowObjType.getFullyQualifiedName())) {

											ITypeBinding typeBindingC = util.getElementOfContainer(flowType);
											ITypeBinding typeBindingB = util.getElementOfContainer(retType.getFullyQualifiedName());
											if (Util.isSubtypeCompatible(typeBindingC, typeBindingB)
											        || Util.isSubtypeCompatible(typeBindingB, typeBindingC)) {
												// Edges associated with Ret
												retEdgeSet.add(edge.toString());
												metInv_RetType = methodInvok;
											}
										}
										else {
											ITypeBinding typeBindingC = crystal.getTypeBindingFromName(flowType);
											ITypeBinding typeBindingB = util.getElementOfContainer(retType.getFullyQualifiedName());
											if (Util.isSubtypeCompatible(typeBindingC, typeBindingB)
											        || Util.isSubtypeCompatible(typeBindingB, typeBindingC)) {
												// Edges associated with Ret
												retEdgeSet.add(edge.toString());
												metInv_RetType = methodInvok;
											}
										}

									}

									// // If flow objects are containers.
									else if (Util.isContainerType(flowType)) {
										ITypeBinding typeBindingC = util.getElementOfContainer(flowObjType.getFullyQualifiedName());
										ITypeBinding typeBindingB = crystal.getTypeBindingFromName(retType.getFullyQualifiedName());
										if (Util.isSubtypeCompatible(typeBindingC, typeBindingB)
										        || Util.isSubtypeCompatible(typeBindingB, typeBindingC)) {
											// Edges associated with Ret
											retEdgeSet.add(edge.toString());
											metInv_RetType = methodInvok;
										}
									}
									// If return types are not containers
									else if (methodDeclr != null && methodDeclr.returnType != flowObjType) {
										methodreturnType = methodDeclr.returnType;
										if (Util.isSubtypeCompatible(flowObjType, methodreturnType)
										        || Util.isSubtypeCompatible(methodreturnType, flowObjType)) {
											// Edges associated with Ret
											retEdgeSet.add(edge.toString());
											metInv_RetType = methodInvok;

										}

									}
								}
							}
						}
					}
			//	}

				if (metInv_RetType != null) {
					HowManyEdges_MIRetType edgeInfo = new HowManyEdges_MIRetType();
					edgeInfo.methodInvok = methodInvok;
					edgeInfo.domains = domain;
					edgeInfo.retEdgeSet = retEdgeSet;
					edgeInfo.elems = elems;
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
		
		Q_1MInE_RetType qVisit = new Q_1MInE_RetType(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();

	}

}
