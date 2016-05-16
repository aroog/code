package oog.itf;

import java.util.Set;

public interface IObjectHierarchy {
	public Set<? extends IObject> getAncestors();
	public Set<? extends IObject> getChildObjects();
	public Set<? extends IObject> getDescendants();
	public Set<? extends IObject> getParentObjects();
	
}
