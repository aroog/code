package edu.wayne.ograph;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IGraph;
import oog.itf.IObject;

// DONE: Add cycle detection
// TODO: LOW. On demand, we may want to ensure that no element is visited twice, not only ODomains
public class OGraphVisitorBase implements OGraphVisitor {

	private Set<IDomain> visitedElements = new HashSet<IDomain>();

	@Override
	public boolean visit(IObject node) {
		return true;
	}

	@Override
	public boolean visit(IEdge node) {
		return true;
	}

	@Override
	public boolean visit(IDomain node) {
		if (visitedElements.contains(node))
			return false;
		visitedElements.add(node);
		return true;
	}

	@Override
	public boolean visit(IGraph node) {
		return true;
	}

	@Override
	public boolean preVisit(IElement node) {
		if (visitedElements.contains(node))
			return false;
		return true;
	}

}
