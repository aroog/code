package edu.wayne.summary.strategies;

import oog.itf.IEdge;
import oog.itf.IElement;
import ast.BaseTraceability;
/**
 * Sub-Strategy To only count object creation for MIM's
 * @author Andrew
 *
 */
public class EdgeSummaryObjCreationCount extends EdgeSummaryAll{

	/**
	 * Don't add edge counts
	 */
	@Override
	protected void addMethodEdges(IEdge edge, BaseTraceability traceability,
			Info<IElement> infoIElement) {

	}


	

}
