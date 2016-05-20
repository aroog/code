package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.core.dom.ITypeBinding;
import oog.itf.IEdge;
import oog.itf.IElement;
import ast.FieldAccess;
import ast.FieldDeclaration;
import ast.FieldWrite;
import ast.MethodDeclaration;
import ast.MethodInvocation;
import ast.Type;
import ast.TypeDeclaration;
import ast.VariableDeclaration;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.qual.Q_DFEP;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.metrics.utils.ObjectsUtils;
import edu.wayne.ograph.ODFEdge;
import edu.wayne.ograph.OObject;

// DONE: split up long method into smaller pieces: meth. return type, meth. receiver, meth. arguments.
// DONE: add domain
// DONE: add domain: to equals, hashcode(); populate it
// DONE: populate typeB;
// DONE: HIGH. populate typeB/Enclosing type for FR,FW,MI and Domains;
// TODO: check signatures of TypeA and TypeB. Not both the same. If TypeA is of type String, Qual visitors do not work.
// We need type for qualitative analysis
// DONE: error message: Use the current classname, not the old name.
// DONE: exclude primitive types; not part of OGraph.
// DONE: missing FieldWrite
// DONE. Rename: ReferenceVariable -> DFType?
public class DFEdgePrecision extends EdgeMetricBase {

	private static final String FACTOR = "F";

	// Header for the Excel
	private static String HEADER = "Expression,VariableType,Domain,TypeB,DFType,Type_NumSubClasses,Type_PossibleSubClasses,Type_NumConcreteClasses,Type_ConcreteClasses,PrecisionRatio,PrecisionFactor";

	private static String SHORT_HEADER = "ClusterSize,Key";

	public DFEdgePrecision() {
		super();
		this.shortName = "DFEP";
		this.generateShortOutput = true;
	}

	public String getHeader() {
		return HEADER;
	}

	@Override
	public String getHeaderShort() {
		return SHORT_HEADER;
	}

	// Enum for identifying the position of reference variables

	public enum RefVariable {
		FieldReadReceiver, FieldWriteReceiver, MethodParameter, MethodReceiver, MethodReturnType
	}

	public class DFEdgePrecisionInfo implements EdgeInfo {

		public RefVariable refVar;

		// public Expression (MI, FR, FW)
		public String expression;

		private Type typeA;

		private String typeB;

		private String domain;

		private Set<String> allSubClasses = new HashSet<String>();

		private Set<String> concreteClasses = new HashSet<String>();

		private float precisionFactor;

		private float precisionRatio;

		public DFEdgePrecisionInfo(Type typeA, String domain, String typeB, String expression, RefVariable refVar) {
			this.typeA = typeA;
			this.domain = domain;
			this.typeB = typeB;
			this.expression = expression;
			this.refVar = refVar;
		}

		// Getters and Setters
		public Set<String> getAllSubClasses() {
			return this.allSubClasses;
		}

		public Set<String> getConcreteClasses() {
			return this.concreteClasses;
		}

		public int getNumSubClasses() {
			return this.allSubClasses.size();
		}

		public int getNumConcreteClasses() {
			return this.concreteClasses.size();
		}

		public float getPrecisionRatio() {
			return precisionRatio;
		}

		public float getPrecisionFactor() {
			return precisionFactor;
		}

		public Type getTypeA() {
			return typeA;
		}

		public String getTypeB() {
			return typeB;
		}

		public String getExpression() {
			return expression;
		}

		public String getDomain() {
			return domain;
		}

		public RefVariable getRefVar() {
			return refVar;
		}

		public void setRefVar(RefVariable refVar) {
			this.refVar = refVar;
		}

		// Equals and Hash functions to avoid duplicates in HashSet
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DFEdgePrecisionInfo)) {
				return false;
			}
			DFEdgePrecisionInfo key = (DFEdgePrecisionInfo) obj;
			return this.typeA.equals(key.typeA) && this.typeB.equals(key.typeB)
			        && this.concreteClasses.equals(key.concreteClasses) && this.allSubClasses.equals(key.allSubClasses)
			        && this.domain.equals(key.domain) && this.expression.equals(key.expression)
			        && this.refVar.equals(key.refVar);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((typeA == null) ? 0 : typeA.hashCode());
			result = prime * result + ((typeB == null) ? 0 : typeB.hashCode());
			result = prime * result + ((concreteClasses == null) ? 0 : concreteClasses.hashCode());
			result = prime * result + ((allSubClasses == null) ? 0 : allSubClasses.hashCode());
			result = prime * result + ((domain == null) ? 0 : domain.hashCode());
			result = prime * result + ((expression == null) ? 0 : expression.hashCode());
			result = prime * result + ((refVar == null) ? 0 : refVar.hashCode());
			return result;
		}

		// Calculates the precision factors
		public void calculate() {
			int numConcreteClasses = getNumConcreteClasses();
			int numSubClasses = getNumSubClasses();
			// OGraph is not precise compared with type hierarchy
			if (numSubClasses < numConcreteClasses) {
				System.err.println("ArchMetrics: Dataflow Precision: negative value being adjusted! Please investigate.");
			}
			// Precision Ratio and Precision Factor
			precisionRatio = (float) numConcreteClasses / (float) numSubClasses;
			precisionFactor = 1.0f - precisionRatio;
		}

		@Override
		public DataPoint[] getDataPoints() {
			// Update calculated fields
			this.calculate();
			List<DataPoint> dataPoints = new ArrayList<DataPoint>();
			dataPoints.add(new DataPoint(FACTOR, getPrecisionFactor()));
			return dataPoints.toArray(new DataPoint[0]);
		}

		@Override
		public String toString() {
			StringBuffer builder = new StringBuffer();
			builder.append("<");
			builder.append(getTypeA());
			builder.append(",");
			builder.append(getTypeB());
			builder.append(">");
			return builder.toString();
		}

		// Write into Excel - It creates one if not already created
		public void writeTo(Writer writer) throws IOException {

			this.calculate();
			// Expression of MI FR FW
			writer.append(CSVOutputUtils.sanitize(this.getExpression()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getTypeA().toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getDomain().toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getTypeB().toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(getRefVar().name());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(this.getNumSubClasses()));
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getAllSubClasses().toString()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(this.getNumConcreteClasses()));
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVOutputUtils.sanitize(this.getConcreteClasses().toString()));
			writer.append(CSVConst.COMMA);
			writer.append(Float.toString(getPrecisionRatio()));
			writer.append(CSVConst.COMMA);
			writer.append(Float.toString(getPrecisionFactor()));
		}

		// Short output
		@Override
		public void writeShortTo(Writer writer) throws IOException {
			this.calculate();
			writer.append(Float.toString(getPrecisionFactor()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.getTypeA().toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
		}
	}

	// Compute the metric here
	public void compute(Set<IEdge> DataflowEdges) {
		// Set of edges
		edgeInfos = new HashSet<EdgeInfo>();
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();

		// Mapping of MI, FA, FW to their corresponding edges
		Map<MethodInvocation, Set<IElement>> methodInvokMap = instance.getMethodInvokMap();
		Map<FieldAccess, Set<IElement>> fieldAccessMap = instance.getFieldReadMap();
		Map<FieldWrite, Set<IElement>> fieldWriteMap = instance.getFieldWriteMap();

		computeDFEP_MethodInvocation(methodInvokMap);
		computeDFEP_FieldWrite(fieldWriteMap);
		computeDFEP_FieldRead(fieldAccessMap);
	}

	// Split each case into separate method

	/**
	 * Computes precision of Receivers, Arguments, Return types of method invocation
	 */

	private void computeDFEP_MethodInvocation(Map<MethodInvocation, Set<IElement>> methodInvokMap) {
		// Method Invocation
		for (Entry<MethodInvocation, Set<IElement>> entry : methodInvokMap.entrySet()) {
			MethodInvocation methInvk = entry.getKey();
			Set<IElement> elements = entry.getValue();
			MethodDeclaration methodDeclr = methInvk.methodDeclaration;
			Crystal crystal = Crystal.getInstance();
			if (methodDeclr != null) {
				List<VariableDeclaration> parameters = methodDeclr.parameters;
				Type retType = methodDeclr.returnType;
				Type recvType = methInvk.recvType;
				// Type retType = methInvk.retType;
				Type paramType = null;
				// XXX. XXX. Do we want actuals or formals on the args?
				// List<String> argAnnotations = methInvk.argumentAnnotations;
				// For every argument 'arg1,arg2,arg3' in e.m(arg1, arg2, arg3)
				if (!parameters.isEmpty()) {
					// int ii = -1;
					for (VariableDeclaration param : parameters) {
						// Always increment index! The order and number of params matters
						// ii++;
						paramType = param.varType;
						// Domain info of Argument Type
						ITypeBinding paramTypeBinding = crystal.getTypeBindingFromName(paramType.getFullyQualifiedName());
						if (paramTypeBinding != null && !paramTypeBinding.isPrimitive()) {
							String domain = "";
							// XXX. Formal vs. actual
							domain = Util.getDomainString(param.annotation);

							// There seems to be a misalignment of the argAnnotations
							/*
							 * if (argAnnotations != null) { domain = argAnnotations.get(ii); }
							 */

							// TypeB
							String typeB = "";
							TypeDeclaration enclosingType = ObjectsUtils.getEnclosingType(methInvk);
							if (enclosingType != null) {
								typeB = enclosingType.getFullyQualifiedName();
							}

							// DFEdgePrecisionInfo
							DFEdgePrecisionInfo edgeInfo = new DFEdgePrecisionInfo(paramType,
							        domain,
							        typeB,
							        methInvk.toString(),
							        RefVariable.MethodParameter);
							getConcreteClassesParam(methInvk, elements, edgeInfo.concreteClasses);
							Util.getSubClasses(paramType, edgeInfo.allSubClasses);
							if (edgeInfo.concreteClasses.size() > edgeInfo.allSubClasses.size()) {
								System.err.println("DFEdgePrecision: no subclasses for "
								        + paramType.getFullyQualifiedName());
								continue;
							}
							if (edgeInfo.concreteClasses.size() == 0) {
								System.err.println("DFEdgePrecision: no concrete classes for "
								        + paramType.getFullyQualifiedName());
								continue;
							}
							// Bug fixed here!
							edgeInfos.add(edgeInfo);
						}
					}
				}
				// For every receiver 'e' in e.m(arg1, arg2, arg3)
				if (recvType != null) {
					// Domain info of receiver type
					ITypeBinding recvTypeBinding = crystal.getTypeBindingFromName(recvType.getFullyQualifiedName());
					if (recvTypeBinding != null && !recvTypeBinding.isPrimitive()) {

						// Set the receiver actual domain
						String domain = "";
						if (methInvk.recvAnnotation != null) {
							domain = methInvk.recvAnnotation;
						}

						// TypeB
						String typeB = "";
						TypeDeclaration enclosingType = ObjectsUtils.getEnclosingType(methInvk);
						if (enclosingType != null) {
							typeB = enclosingType.getFullyQualifiedName();
						}

						// Precision
						DFEdgePrecisionInfo edgeInfo = new DFEdgePrecisionInfo(recvType,
						        domain,
						        typeB,
						        methInvk.toString(),
						        RefVariable.MethodReceiver);
						getConcreteClassesRecv(methInvk, elements, edgeInfo.concreteClasses);
						Util.getSubClasses(recvType, edgeInfo.allSubClasses);
						if (edgeInfo.concreteClasses.size() > edgeInfo.allSubClasses.size()) {
							System.err.println("DFEdgePrecision: no subclasses for " + recvType.getFullyQualifiedName());
							continue;
						}
						if (edgeInfo.concreteClasses.size() == 0) {
							System.err.println("DFEdgePrecision: no concrete classes for "
							        + recvType.getFullyQualifiedName());
							continue;
						}
						edgeInfos.add(edgeInfo);
					}
				}

				// For every return type 't' in e.m(arg1, arg2, arg3) {return t}
				if (retType != null) {
					String domain = "";
					// Domain info of Return type
					ITypeBinding retTypeBinding = crystal.getTypeBindingFromName(retType.getFullyQualifiedName());
					if (retTypeBinding != null && !retTypeBinding.isPrimitive() && methodDeclr.returnAnnotation != null) {
						domain = Util.getDomainString(methodDeclr.returnAnnotation);
					}

					// TypeB
					String typeB = "";
					TypeDeclaration enclosingType = ObjectsUtils.getEnclosingType(methInvk);
					if (enclosingType != null) {
						typeB = enclosingType.getFullyQualifiedName();
					}

					// Precision
					DFEdgePrecisionInfo edgeInfo = new DFEdgePrecisionInfo(retType,
					        domain,
					        typeB,
					        methInvk.toString(),
					        RefVariable.MethodReturnType);
					getConcreteClassesRet(methInvk, elements, edgeInfo.concreteClasses);
					Util.getSubClasses(retType, edgeInfo.allSubClasses);
					if (edgeInfo.concreteClasses.size() > edgeInfo.allSubClasses.size()) {
						System.err.println("DFEdgePrecision: no subclasses for " + retType.getFullyQualifiedName());
						continue;
					}
					if (edgeInfo.concreteClasses.size() == 0) {
						System.err.println("DFEdgePrecision: no concrete classes for "
						        + retType.getFullyQualifiedName());
						continue;
					}
					edgeInfos.add(edgeInfo);
				}
			}
		}
	}

	/**
	 * Computes precision of Receivers of field writes
	 */

	private void computeDFEP_FieldWrite(Map<FieldWrite, Set<IElement>> fieldWriteMap) {
		Crystal crystal = Crystal.getInstance();

		// Field Write
		for (Entry<FieldWrite, Set<IElement>> fieldWriteExp : fieldWriteMap.entrySet()) {
			FieldWrite fieldWriteKey = fieldWriteExp.getKey();
			FieldDeclaration fieldDeclr_FW = fieldWriteKey.fieldDeclaration;
			Type writeRecv = fieldWriteKey.recvType;
			if (writeRecv != null) {
				// Domain info for field write receiver
				ITypeBinding fieldWriteTypeBinding = crystal.getTypeBindingFromName(fieldDeclr_FW.fieldType.getFullyQualifiedName());

				String domain = "";
				if (fieldWriteTypeBinding != null && !fieldWriteTypeBinding.isPrimitive()) {
					domain = Util.getDomainString(fieldDeclr_FW.annotation);
				}

				// TypeB
				String typeB = "";
				TypeDeclaration enclosingType = ObjectsUtils.getEnclosingType(fieldWriteKey);
				if (enclosingType != null) {
					typeB = enclosingType.getFullyQualifiedName();
				}
				// Precision
				DFEdgePrecisionInfo edgeInfo = new DFEdgePrecisionInfo(writeRecv,
				        domain,
				        typeB,
				        fieldWriteKey.toString(),
				        RefVariable.FieldWriteReceiver);
				getConcreteClassesRecv(fieldWriteKey, fieldWriteExp.getValue(), edgeInfo.concreteClasses);
				Util.getSubClasses(writeRecv, edgeInfo.allSubClasses);
				if (edgeInfo.concreteClasses.size() > edgeInfo.allSubClasses.size()) {
					System.err.println("DFEdgePrecision: no subclasses for " + writeRecv.getFullyQualifiedName());
					continue;
				}
				if (edgeInfo.concreteClasses.size() == 0) {
					System.err.println("DFEdgePrecision: no concrete classes for " + writeRecv.getFullyQualifiedName());
					continue;
				}
				edgeInfos.add(edgeInfo);
			}

		}
	}

	/**
	 * Computes precision of Receivers of field reads
	 */

	private void computeDFEP_FieldRead(Map<FieldAccess, Set<IElement>> fieldAccessMap) {
		Crystal crystal = Crystal.getInstance();

		// For FieldAccess
		for (Entry<FieldAccess, Set<IElement>> fieldReadExp : fieldAccessMap.entrySet()) {
			FieldAccess fieldReadKey = fieldReadExp.getKey();
			// DONE. Getting null FieldDeclaration here.
			FieldDeclaration fieldDeclr_FR = fieldReadKey.fieldDeclaration;
			Type readRecv = fieldReadKey.recvType;
			// Domain info of Receiver
			ITypeBinding fieldReadTypeBinding = crystal.getTypeBindingFromName(fieldDeclr_FR.fieldType.getFullyQualifiedName());

			String domain = "";
			if (fieldReadTypeBinding != null && !fieldReadTypeBinding.isPrimitive()) {
				domain = Util.getDomainString(fieldDeclr_FR.annotation);
			}

			// TypeB
			String typeB = "";
			TypeDeclaration enclosingType = ObjectsUtils.getEnclosingType(fieldReadKey);
			if (enclosingType != null) {
				typeB = enclosingType.getFullyQualifiedName();
			}

			// For every receiver 'b' read in b.var
			if (readRecv != null && domain != null && typeB != null) {
				DFEdgePrecisionInfo edgeInfo = new DFEdgePrecisionInfo(readRecv,
				        domain,
				        typeB,
				        fieldReadKey.toString(),
				        RefVariable.FieldReadReceiver);
				getConcreteClassesRecv(fieldReadKey, fieldReadExp.getValue(), edgeInfo.concreteClasses);
				Util.getSubClasses(readRecv, edgeInfo.allSubClasses);
				if (edgeInfo.concreteClasses.size() > edgeInfo.allSubClasses.size()) {
					System.err.println("DFEdgePrecision: no subclasses for " + readRecv.getFullyQualifiedName());
					continue;
				}
				if (edgeInfo.concreteClasses.size() == 0) {
					System.err.println("DFEdgePrecision: no concrete classes for " + readRecv.getFullyQualifiedName());
					continue;
				}
				edgeInfos.add(edgeInfo);
			}
		}
	}

	/**
	 * Get classes in reachable domains from OGraph - MethodInvocation, FieldAccess, FieldWrite
	 */

	private static void getConcreteClassesParam(MethodInvocation methodInvokExpr, Set<IElement> elements,
	        Set<String> concreteClasses) {
		// DO NOT add the field type, which could be an interface
		MethodDeclaration methodDeclaration = methodInvokExpr.methodDeclaration;
		List<VariableDeclaration> parameters = methodDeclaration.parameters;
		for (VariableDeclaration param : parameters) {
			for (IElement element : elements) {
				Type paramType = param.varType;
				subtypeCompatibleFlowObj(concreteClasses, paramType, element);
			}
		}
	}

	private static void getConcreteClassesRecv(MethodInvocation methodInvokExpr, Set<IElement> elements,
	        Set<String> concreteClasses) {
		Type recvType = methodInvokExpr.recvType;
		if (recvType != null) {
			for (IElement element : elements) {
				subtypeCompatibleSrcDstObj(concreteClasses, recvType, element);
			}

		}
	}

	private static void getConcreteClassesRet(MethodInvocation methodInvokExpr, Set<IElement> elements,
	        Set<String> concreteClasses) {
		MethodDeclaration methodDeclr = methodInvokExpr.methodDeclaration;
		Type retType = methodDeclr.returnType;
		if (retType != null) {
			for (IElement element : elements) {
				subtypeCompatibleFlowObj(concreteClasses, retType, element);
			}
		}
	}

	private static void getConcreteClassesRecv(FieldAccess fieldReadExpr, Set<IElement> elements,
	        Set<String> concreteClasses) {
		Type recvType = fieldReadExpr.recvType;
		if (recvType != null) {
			for (IElement element : elements) {
				subtypeCompatibleSrcObj(concreteClasses, recvType, element);
			}
		}
	}

	private static void getConcreteClassesRecv(FieldWrite fieldWriteExpr, Set<IElement> elements,
	        Set<String> concreteClasses) {
		Type fieldWriterecvType = fieldWriteExpr.recvType;
		if (fieldWriterecvType != null) {
			for (IElement element : elements) {
				subtypeCompatibleDstObj(concreteClasses, fieldWriterecvType, element);
			}
		}
	}

	/**
	 * Subtype compatibility check
	 */

	private static void subtypeCompatibleFlowObj(Set<String> concreteClasses, Type varType, IElement element) {
		if (element instanceof ODFEdge) {
			ODFEdge edge = (ODFEdge) element;
			OObject runtimeObjectFlow = edge.getFlow();
			Type flowType = runtimeObjectFlow.getC();
			if (Util.isSubtypeCompatible(flowType, varType)) {
				concreteClasses.add(flowType.toString());
			}
		}
	}

	private static void subtypeCompatibleDstObj(Set<String> concreteClasses, Type varType, IElement element) {
		if (element instanceof ODFEdge) {
			ODFEdge edge = (ODFEdge) element;
			OObject runtimeObjectDst = edge.getOdst();
			Type dstType = runtimeObjectDst.getC();
			if (Util.isSubtypeCompatible(dstType, varType)) {
				concreteClasses.add(dstType.toString());
			}
		}
	}

	private static void subtypeCompatibleSrcObj(Set<String> concreteClasses, Type varType, IElement element) {
		if (element instanceof ODFEdge) {
			ODFEdge edge = (ODFEdge) element;
			OObject runtimeObjectSrc = edge.getOsrc();
			Type srcType = runtimeObjectSrc.getC();
			if (Util.isSubtypeCompatible(srcType, varType)) {
				concreteClasses.add(srcType.toString());
			}
		}
	}

	private static void subtypeCompatibleSrcDstObj(Set<String> concreteClasses, Type varType, IElement element) {
		if (element instanceof ODFEdge) {
			ODFEdge edge = (ODFEdge) element;
			OObject runtimeObjectSrc = edge.getOsrc();
			Type srcType = runtimeObjectSrc.getC();
			OObject runtimeObjectDst = edge.getOdst();
			Type dstType = runtimeObjectDst.getC();
			if (Util.isSubtypeCompatible(srcType, varType)) {
				concreteClasses.add(srcType.toString());
			}
			if (Util.isSubtypeCompatible(dstType, varType)) {
				concreteClasses.add(dstType.toString());
			}
		}
	}

	@Override
	public String[] getColumnHeaders() {
		return new String[] { FACTOR };
	}

	@Override
	public void visitOutliers(Writer writer, Set<EdgeInfo> outliers) throws IOException {
		Q_DFEP qVisit = new Q_DFEP(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}
}
