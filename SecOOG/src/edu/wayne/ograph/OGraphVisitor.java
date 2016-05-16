package edu.wayne.ograph;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IGraph;
import oog.itf.IObject;

public interface OGraphVisitor {

	boolean visit(IObject node);

	boolean visit(IEdge node);

	boolean visit(IDomain node);

	boolean visit(IGraph node);

	boolean preVisit(IElement node);

}
