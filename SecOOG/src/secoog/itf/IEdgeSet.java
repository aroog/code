package secoog.itf;

import java.util.Iterator;

import oog.itf.IEdge;

/**
 * define a named set of edges that share the same properties
 * 
 * TODO: XXX Use me
 * */
public interface IEdgeSet extends IElemSet {
	Iterator<IEdge> edges();
}
