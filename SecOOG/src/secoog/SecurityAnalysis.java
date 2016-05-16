package secoog;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.Signature;

import ast.AstNode;
import ast.BaseTraceability;
import ast.BodyDeclaration;
import ast.FieldDeclaration;
import ast.MethodDeclaration;
import ast.MethodInvocation;
import ast.MiniAstUtils;
import ast.TypeDeclaration;
import oog.itf.IEdge;
import oog.itf.IObject;

// DONE: Reduce/eliminate static fields/methods
// XXX. What's with all these deprecated methods?
public class SecurityAnalysis {

	private static SecurityAnalysis s_Analysis = null;
		
	private SecurityAnalysis() {
		secGraph = SecGraph.getInstance();
		secMap = SecMap.getInstance();		
	}
	
	public static SecurityAnalysis getInstance() {
		if (s_Analysis == null ) {
			s_Analysis = new SecurityAnalysis();
		}
		return s_Analysis;
	}
	
	//DONE: how is the secGraph and secMap gets initialized?
	//- Could just set the OGraph;
	//- You will traverse the OGraph, create the SecGraph, and the individual wrapper objects
	//- In the process of creating the SecGraph, create the SecMap
	//- Some of these public fields need to be made private; add public getter only; no setter
	private SecGraph secGraph;
	
	// Not used
	private SecMap secMap;
	
	public SecGraph getSecGraph(){
		return secGraph;
	}
	
	public boolean checkReachability(SecObject o1, SecObject o2, EdgeType eType){
		return secGraph.getReachableObjects(o1, eType).contains(o2);
	}
	
	/**
	 * general check for information disclosure.
	 * TODO: XXX. revise 
	 * */
	@Deprecated
	public boolean checkInfDisclosure(SecObject secret, SecObject sink) {
		if (secret.trustLevel.equals(TrustLevelType.Full) 
				&& secret.isConfidential.equals(IsConfidential.True)
				&& sink.trustLevel.equals(TrustLevelType.Low)){
			for (SecObject parent : secret.getAncestors()){	
				for (DataFlowEdge edge: secGraph.getTransitiveCommunication(parent, sink, secret))
				{
						if (reachable(secret, edge))
							return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param secret
	 * @param edge
	 * @return true if the secret may be reachable from the edge
	 */
	@Deprecated
	private boolean reachable(SecObject secret, DataFlowEdge dfEdge) {
			return dfEdge.getFlow().equals(secret) 
				|| dfEdge.getFlowType().isSubtypeCompatible(secret.getObjectType())
				|| checkReachability(dfEdge.getFlow(), secret, EdgeType.PointsTo);
	}

	@Deprecated
	public boolean hasConfidentialFlow(DataFlowEdge edge) {
		// true if the flow itself is a secret
		SecObject flowObject = edge.getFlow();
		if ( flowObject.isConfidential.equals(IsConfidential.True)) 
			return true;
		// true if a child of the flow is a secret 
		for (SecObject child : flowObject.getDescendants())
			if (child.isConfidential.equals(IsConfidential.True))
				return true;
		return false;
	}
	
	//if the argument of SqlQuery comes from an untrusted source
	@Deprecated
	private boolean checkForSQLInjection(SecObject dataSource){

		List<String> queryMethods = new ArrayList<String>();
		queryMethods.add("query");
		queryMethods.add("rawQuery");
		//TODO add more methods here
		for (SecEdge edge : secGraph.getInEdges(dataSource, EdgeType.DataFlow)){
			DataFlowEdge dfEdge = (DataFlowEdge)edge;
			for(BaseTraceability link : dfEdge.getOEdge().getTraceability()){
				AstNode expression = link.getExpression();
				if (expression instanceof MethodInvocation){
					MethodInvocation methInvk = (MethodInvocation)expression;
					if (queryMethods.contains(methInvk.methodDeclaration.methodName)
						&& unTrustedFlow(dfEdge))
					return true;
				}
			}
		}
		return false;
	}

	@Deprecated
	private boolean unTrustedFlow(DataFlowEdge edge) {
		//check if parent of the flow is untrusted
		for (SecObject parent : edge.getFlow().getAncestors())
			if (parent.trustLevel.equals(TrustLevelType.Low))
				return true;
		//check if transitive source dataflow is untrusted.
		for (IObject parent : transitiveConnected(edge))
			  if (secGraph.hasPropertyValue(parent, TrustLevelType.Low))
				return true;
		//check if arg is reachable from untrusted component
		for (IObject rObject : secGraph.getReachableObjects(edge.getFlow(), EdgeType.PointsTo))
			if (secGraph.hasPropertyValue(rObject,TrustLevelType.Low))
				return true;
		return false;
	}

	@Deprecated
	private Set<? extends IObject> transitiveConnected(DataFlowEdge edge) {
		return secGraph.getReachableObjects(edge.getFlow(), EdgeType.DataFlow);
	}

	public void displayWarnings(Set<? extends IEdge> flowEdges) {
		for (IEdge iEdge : flowEdges) {
			displayWarnings(iEdge);
		}
	}

	public void displayWarnings(IEdge iEdge) {
		System.err.println("Suspicious edge : "+getTraceabilityInfo(iEdge) );		
	}

	public void displayObjectInfo(IObject o){
		System.out.println(displayObject(o));
		StringBuffer sb = new StringBuffer();
		for (BaseTraceability traceItem : o.getTraceability()) {
			sb.append("\t");
			sb.append(displayTraceItem(traceItem));
			sb.append("\n");
		}
		System.out.println(sb.toString());
	}
	
	public void displayEdgeInfo(IEdge iEdge){
		System.out.println(getTraceabilityInfo(iEdge) );	
	}
	
	private String getTraceabilityInfo(IEdge iEdge) {
		StringBuffer sb = new StringBuffer();		
		sb.append(displayObject(iEdge.getOsrc()));
		sb.append(" -> ");
		sb.append(displayObject(iEdge.getOdst()));
		sb.append(" [");
		sb.append(displayEdgeLabel(iEdge));
		sb.append("]");
		sb.append("\n");
		for (BaseTraceability traceItem : iEdge.getTraceability()) {
			sb.append("\t");
			sb.append(displayTraceItem(traceItem));
			sb.append("\n");
		}		
		return sb.toString();
	}

	private String displayTraceItem(BaseTraceability traceItem) {
		StringBuffer sb = new StringBuffer();
		
		AstNode expr = traceItem.getExpression();
		if (expr != null) {
			BodyDeclaration enclDecl = expr.enclosingDeclaration;
			
			// Inline this?
			TypeDeclaration enclTypeDeclaration = MiniAstUtils.getEnclosingTypeDeclaration(expr);
			
			String type = Signature.getSimpleName(enclTypeDeclaration.getFullyQualifiedName());
			sb.append(type);
			sb.append("\t");
			if (enclDecl instanceof MethodDeclaration) {
				MethodDeclaration md = (MethodDeclaration) enclDecl;
				sb.append(md.methodName);
			}
			if (enclDecl instanceof FieldDeclaration) {
				FieldDeclaration fd = (FieldDeclaration)enclDecl;
				sb.append(fd.fieldName);
			}
			sb.append("\t");
			sb.append(expr);
		}
		return sb.toString();
	}

	private String displayEdgeLabel(IEdge iEdge) {
		StringBuffer sb = new StringBuffer();
		if (iEdge instanceof DataFlowEdge){			
			DataFlowEdge dfEdge = (DataFlowEdge)iEdge;
			sb.append(displayObject(dfEdge.getFlow()));
		}
		if (iEdge instanceof CreationEdge){
			CreationEdge crEdge = (CreationEdge)iEdge;
			sb.append(displayObject(crEdge.getFlow()));
		}
		if (iEdge instanceof PointsToEdge){
			PointsToEdge ptEdge = (PointsToEdge)iEdge;
			sb.append(ptEdge.name);
		}
		if (iEdge instanceof ControlFlowEdge){
			ControlFlowEdge crEdge = (ControlFlowEdge)iEdge;
			sb.append(crEdge.name);
		}
		return sb.toString();
	}

	private String displayObject(IObject o) {
		StringBuffer sb = new StringBuffer();
		sb.append(o.getInstanceDisplayName());
		sb.append(":");
		// Convert fully qualified name to unqualified name
		String s = Signature.getSimpleName(o.getTypeDisplayName());
		sb.append(s);
		return sb.toString();
	}
}
