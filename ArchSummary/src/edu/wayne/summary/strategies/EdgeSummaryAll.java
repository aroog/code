package edu.wayne.summary.strategies;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IObject;
import secoog.EdgeType;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import ast.FieldAccess;
import ast.FieldWrite;
import ast.MethodInvocation;
import ast.Type;

public class EdgeSummaryAll extends EdgeSummaryBase {

	/**
	 * Count the number of elements created by an expression. Add them all.
	 * 
	 * look for all expressions in mc that are of the following types:
	 * 
	 * FieldRead FieldWrites MethodInvocation ClassInstanceCreation: (creates
	 * objects, not edges)
	 */
	protected void addMethodEdges(IEdge edge, BaseTraceability traceability,
			Info<IElement> infoIElement) {
		ast.AstNode expression = traceability.getExpression();
		if (expression instanceof FieldWrite) {
			infoIElement.add(edge);
		} else if (expression instanceof FieldAccess) {
			infoIElement.add(edge);
		} else if (expression instanceof MethodInvocation) {
			infoIElement.add(edge);
		} else {
			System.err.println(expression);
		}
	}

	/**
	 * Count both incoming and outgoing edges. This also includes self edges
	 */
	@Override
	protected Set<IEdge> getSummaryEdges(String javaClass,
			Set<EdgeType> edgeTypes) {
		Set<IEdge> edges = new HashSet<IEdge>();
		for (IEdge edge : this.getEdges(edgeTypes)) {
			Type src = edge.getOsrc().getC();
			Type dst = edge.getOdst().getC();

			if (Utils.isSubtypeCompatible(src, javaClass)
					|| Utils.isSubtypeCompatible(dst, javaClass)) {
				addEdge(edges, edge);
			}
		}
		return edges;
	}

	/**
	 * Add both classes incoming and outgoing
	 */
	@Override
	protected Set<IObject> getClassesFromEdges(Set<IEdge> edges, String javaClass) {
		Set<IObject> objects = new HashSet<IObject>();
		for (IEdge edge : edges) {
			if(Utils.isSubtypeCompatible(edge.getOdst().getC(), javaClass)){
				objects.add(edge.getOsrc());
			}
			if(Utils.isSubtypeCompatible(edge.getOsrc().getC(), javaClass)){
				objects.add(edge.getOdst());
			}
		}
		return objects;
	}

	@Override
	protected void addMethodCreatedObj(IObject obj, BaseTraceability trace,
			Info<IElement> infoIElement) {

		ast.AstNode expression = trace.getExpression();
		if (expression instanceof ClassInstanceCreation) {
			infoIElement.add(obj);

		}
	}





}
