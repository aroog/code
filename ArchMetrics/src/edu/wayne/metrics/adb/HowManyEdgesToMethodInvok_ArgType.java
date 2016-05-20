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
import edu.wayne.metrics.qual.Q_1MInE_ArgType;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.ograph.ODFEdge;

public class HowManyEdgesToMethodInvok_ArgType extends EdgeMetricBase {

	private static String HEADER = "MethodInvocation,HowMany,Domain,Edges";

	private static String SHORT_HEADER = "ClusterSize,Key";

	private static final String HOW_MANY = "N";	

	public HowManyEdgesToMethodInvok_ArgType() {
		super();

		this.generateShortOutput = true;
		this.shortName = "1MInE_ArgType";
	}

	public String getHeader() {
		return HEADER;
	}

	@Override
	public String getHeaderShort() {
		return SHORT_HEADER;
	}

	public class HowManyEdges_MIArgType implements EdgeInfo {
		private MethodInvocation methodInvok;

		private Set<IElement> elems = new HashSet<IElement>();

		// Edge set for 1MInE_RetType
		private Set<String> argEdgeSet = new HashSet<String>();

		// TODO: HIGH. Populate me.
		private List<String> domains = new ArrayList<String>();

		public MethodInvocation getMethodInvocation() {
			return methodInvok;
		}

		public List<String> getDomain() {
			return domains;
		}

		public Set<IElement> getElems() {
			return elems;
		}

		@Override
		public void writeTo(Writer writer) throws IOException {

			writer.append(CSVOutputUtils.sanitize(this.methodInvok.toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(Integer.valueOf(this.argEdgeSet.size()).toString()));
			writer.append(CSVConst.COMMA);			
			int count1 = 0;
			for (String arg : this.domains) {
				if (arg != null) {
					writer.append(CSVOutputUtils.sanitize(arg));
					count1++;
					if (count1 < this.domains.size()) {
						writer.append(CSVConst.NEWLINE);
						writer.append(CSVConst.COMMA);
						writer.append(CSVConst.COMMA);											
					}
				}
			}
			writer.append(CSVConst.COMMA);
			int count = 0;
			for (String edge : this.argEdgeSet) {
				writer.append(CSVOutputUtils.sanitize(edge));
				count++;
				if (count < this.argEdgeSet.size()) {
					writer.append(CSVConst.NEWLINE);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);										
				}
			}

		}

		@Override
		public void writeShortTo(Writer writer) throws IOException {
			writer.append(Integer.valueOf(this.argEdgeSet.size()).toString());
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.methodInvok.toString()));
		}

		@Override
		public DataPoint[] getDataPoints() {
			List<DataPoint> dataPoints = new ArrayList<DataPoint>();
			dataPoints.add(new DataPoint(HOW_MANY, this.argEdgeSet.size()));
			return dataPoints.toArray(new DataPoint[0]);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof HowManyEdges_MIArgType)) {
				return false;
			}

			HowManyEdges_MIArgType key = (HowManyEdges_MIArgType) o;
			return this.methodInvok.equals(key.methodInvok) && (this.argEdgeSet.equals(key.argEdgeSet))
			        && (this.elems.equals(key.elems)) && (this.domains.equals(key.domains));
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((methodInvok == null) ? 0 : methodInvok.hashCode());
			result = prime * result + ((argEdgeSet == null) ? 0 : argEdgeSet.hashCode());
			result = prime * result + ((elems == null) ? 0 : elems.hashCode());
			result = prime * result + ((domains == null) ? 0 : domains.hashCode());
			return result;
		}
	}

	public void compute(Set<IEdge> allEdges) {
		edgeInfos = new HashSet<EdgeInfo>();
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
		Map<MethodInvocation, Set<IElement>> methodInvkMap = instance.getMethodInvokMap();
		Crystal crystal = Crystal.getInstance();
		List<String> argDomain = new ArrayList<String>();

		if (methodInvkMap != null) {
			for (Entry<MethodInvocation, Set<IElement>> entry : methodInvkMap.entrySet()) {
				MethodInvocation methodInvok = entry.getKey();

				Set<IElement> elems = entry.getValue();
				Set<String> argEdgeSet = new HashSet<String>();

				MethodInvocation metInv_RetType = null;

				// TOSUM: Must I go to the method declaration and get the arguments? I think not.
				List<Type> argumentTypes = methodInvok.argumentTypes;				

				Type recvType = methodInvok.recvType;			
				

				// Get domain Info  Arg Types
				for (Type arg : argumentTypes) {
					ITypeBinding argTypeBinding = crystal.getTypeBindingFromName(arg.getFullyQualifiedName());
					if (argTypeBinding != null && !argTypeBinding.isPrimitive()
					        && methodInvok.argumentAnnotations != null) {
						argDomain = methodInvok.argumentAnnotations;
					}

				}


				for (IElement element : elems) {
					if (element instanceof ODFEdge) {
						ODFEdge edge = (ODFEdge) element;

						// Get the flow object
						Type flowObjType = edge.getFlow().getC();
						String flag = edge.getFlag().toString();

						// Export edges and must have arguments.
						if (flag.equals("Export") && recvType != null && !argumentTypes.isEmpty()) {

							// For every argument in the list, check the subcompatiblity with flow object
							for (Type paramType : argumentTypes) {

								// Add a condition that the type of flow objects are not the same as arguments
								// If not this measure is not useful, I will always have an edge.
								if (paramType != flowObjType) {

									if (Util.isSubtypeCompatible(flowObjType, paramType)
									        || Util.isSubtypeCompatible(paramType, flowObjType)) {
										argEdgeSet.add(edge.toString());
										metInv_RetType = methodInvok;
									}
								}
							}

						}

					}
				}

				if (metInv_RetType != null) {
					HowManyEdges_MIArgType edgeInfo = new HowManyEdges_MIArgType();
					edgeInfo.methodInvok = metInv_RetType;
					edgeInfo.domains = argDomain;
					edgeInfo.argEdgeSet = argEdgeSet;
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
		Q_1MInE_ArgType qVisit = new Q_1MInE_ArgType(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();

	}

}
