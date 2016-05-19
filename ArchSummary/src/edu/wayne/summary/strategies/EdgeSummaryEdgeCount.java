package edu.wayne.summary.strategies;

import oog.itf.IElement;
import oog.itf.IObject;
import ast.BaseTraceability;
/**
 * Sub-Strategy To only count edges and not object creation
 * @author Andrew
 *
 */
public class EdgeSummaryEdgeCount extends EdgeSummaryAll {
	/**
	 * Don't add created objects
	 */
	@Override
	protected void addMethodCreatedObj(IObject obj, BaseTraceability trace,
			Info<IElement> infoIElement) {
	
	}



}
