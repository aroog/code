package oog.itf;

import java.util.Set;

import ast.BaseTraceability;
import edu.wayne.ograph.OGraphVisitor;
import util.TraceabilityListSet;

// XXX. Should we add here: isVisible, showSubstructure? Or turn them into properties?
public interface IElement {

	public Set<BaseTraceability> getTraceability();

	public TraceabilityListSet getPath();
	
	public boolean accept(OGraphVisitor visitor);

}