package secoog;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IObject;

// Special SecObject, used for the root
public class SecRootObject extends SecObject {
	
	public ShapeType type = ShapeType.Unknown; 
	
	public IObject getOObject() {
		return SecMap.getInstance().getOObject(this);
	}
	
	/**
	 * returns the parent object or null if the parentDomain is Shared
	 * */
	public Set<SecObject> getParentObjects() {
		// TODO: LOW. For performance, cache the result. No need to create a new object each time.
		// This code is getting called a lot, inside nested loops.
		return new HashSet<SecObject>();
	}

	@Override
	public Set<SecObject> getAncestors() {
		// TODO: LOW. For performance, cache the result. No need to create a new object each time.
		// This code is getting called a lot, inside nested loops.
		return new HashSet<SecObject>();
	}

	public boolean hasParentObjects() {
		return false;
	}

}

