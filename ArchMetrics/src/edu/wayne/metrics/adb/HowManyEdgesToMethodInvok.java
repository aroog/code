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
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.ograph.ODFEdge;

public class HowManyEdgesToMethodInvok extends EdgeMetricBase {
	private static String HEADER = "MethodInvocation,HowMany,ReceiverTypeDomain,ReturnTypeDomain,ArgTypeDomain,Edges";

	private static String SHORT_HEADER = "ClusterSize,Key";

	private static final String HOW_MANY = "N";

	public HowManyEdgesToMethodInvok() {
		super();

		this.generateShortOutput = true;
		this.shortName = "1MInE";
	}

	public String getHeader() {
		return HEADER;
	}

	@Override
	public String getHeaderShort() {
		return SHORT_HEADER;
	}

	public class HowManyEdges implements EdgeInfo {

		private String domainRecv;

		private String domainRet;

		private List<String> domainArg = new ArrayList<String>();

		private MethodInvocation methodInvok;

		private Set<IElement> elems = new HashSet<IElement>();

		private Set<String> edgeSet = new HashSet<String>();

		public MethodInvocation getMethodInvocation() {
			return methodInvok;
		}

		public Set<IElement> getElems() {
			return elems;
		}

		public String getDomainRecv() {
			return domainRecv;
		}

		public String getDomainRet() {
			return domainRet;
		}

		public List<String> getDomainArg() {
			return domainArg;
		}

		@Override
		public void writeTo(Writer writer) throws IOException {

			writer.append(CSVOutputUtils.sanitize(this.methodInvok.toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(Integer.valueOf(this.edgeSet.size()).toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getDomainRecv().toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getDomainRet().toString()));
			writer.append(CSVConst.COMMA);
			int count1 = 0;
			for (String arg : this.domainArg) {
				if (arg != null) {
					writer.append(CSVOutputUtils.sanitize(arg));
					count1++;
					if (count1 < this.domainArg.size()) {
						writer.append(CSVConst.NEWLINE);
						writer.append(CSVConst.COMMA);
						writer.append(CSVConst.COMMA);
						writer.append(CSVConst.COMMA);
						writer.append(CSVConst.COMMA);
					}
				}
			}
			writer.append(CSVConst.COMMA);
			int count = 0;
			for (String edge : this.edgeSet) {
				writer.append(CSVOutputUtils.sanitize(edge));
				count++;
				if (count < this.edgeSet.size()) {
					writer.append(CSVConst.NEWLINE);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
					writer.append(CSVConst.COMMA);
				}
			}
		}

		@Override
		public void writeShortTo(Writer writer) throws IOException {
			writer.append(Integer.valueOf(this.edgeSet.size()).toString());
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.methodInvok.toString()));
		}

		@Override
		public DataPoint[] getDataPoints() {
			List<DataPoint> dataPoints = new ArrayList<DataPoint>();
			dataPoints.add(new DataPoint(HOW_MANY, this.edgeSet.size()));
			return dataPoints.toArray(new DataPoint[0]);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof HowManyEdges)) {
				return false;
			}

			HowManyEdges key = (HowManyEdges) o;
			return this.methodInvok.equals(key.methodInvok) && (this.edgeSet.equals(key.edgeSet))
			        && (this.domainRecv.equals(key.domainRecv)) && (this.domainRet.equals(key.domainRet))
			        && (this.elems.equals(key.elems))&& (this.domainArg.equals(key.domainArg));
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((methodInvok == null) ? 0 : methodInvok.hashCode());
			result = prime * result + ((edgeSet == null) ? 0 : edgeSet.hashCode());
			result = prime * result + ((elems == null) ? 0 : elems.hashCode());
			result = prime * result + ((domainRecv == null) ? 0 : domainRecv.hashCode());
			result = prime * result + ((domainRet == null) ? 0 : domainRet.hashCode());
			result = prime * result + ((domainArg == null) ? 0 : domainArg.hashCode());
			return result;
		}
	}

	public void compute(Set<IEdge> allEdges) {
		edgeInfos = new HashSet<EdgeInfo>();

		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
		Map<MethodInvocation, Set<IElement>> methodInvkMap = instance.getMethodInvokMap();
		String recvDomain = "";
		String retDomain = "";
		List<String> argDomain = new ArrayList<String>();

		if (methodInvkMap != null) {
			for (Entry<MethodInvocation, Set<IElement>> entry : methodInvkMap.entrySet()) {
				MethodInvocation methodInvok = entry.getKey();
				Set<IElement> elems = entry.getValue();
				Set<String> edgeSet = new HashSet<String>();
				List<Type> argumentTypes = methodInvok.argumentTypes;

				Type recvType = methodInvok.recvType;
				Crystal crystal = Crystal.getInstance();

				// Get the return type from the method declaration
				MethodDeclaration methodDeclaration = methodInvok.methodDeclaration;

				// Get domain Info Receiver / Return/ Arg Types
				ITypeBinding recvTypeBinding = crystal.getTypeBindingFromName(recvType.getFullyQualifiedName());
				if (recvTypeBinding != null && !recvTypeBinding.isPrimitive() && methodInvok.recvAnnotation != null) {
					recvDomain = methodInvok.recvAnnotation;
				}
				if (methodDeclaration != null) {
					Type retType = methodDeclaration.returnType;
					ITypeBinding retTypeBinding = crystal.getTypeBindingFromName(retType.getFullyQualifiedName());
					if (retTypeBinding != null && !retTypeBinding.isPrimitive()
					        && methodDeclaration.returnAnnotation != null) {
						retDomain = Util.getDomainString(methodDeclaration.returnAnnotation);
					}
				}

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
						edgeSet.add(edge.toString());
					}
				}
				HowManyEdges edgeInfo = new HowManyEdges();
				edgeInfo.methodInvok = methodInvok;
				edgeInfo.edgeSet = edgeSet;
				edgeInfo.elems = elems;
				edgeInfo.domainRecv = recvDomain;
				edgeInfo.domainRet = retDomain;
				edgeInfo.domainArg = argDomain;
				edgeInfos.add(edgeInfo);
			}

		}

	}

	@Override
	public String[] getColumnHeaders() {
		return new String[] { HOW_MANY };
	}

	@Override
	public void visitOutliers(Writer writer, Set<EdgeInfo> outliers) throws IOException {

	}

}
