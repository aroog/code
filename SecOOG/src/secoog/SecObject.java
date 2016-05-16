package secoog;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ast.BaseTraceability;
import ast.Type;
import edu.wayne.ograph.OGraphVisitor;
import oog.itf.IDomain;
import oog.itf.IObject;
import util.TraceabilityListSet;

// TODO: SecObject --> Process?
// TODO: LOW. For performance, cache the result. No need to re-compute the getParents(), getAncestors(), getDescendants()... 
// This code is getting called a lot, inside nested loops.
public class SecObject extends SecElement implements IObject{
	
	public ShapeType type = ShapeType.Unknown;
	
	public IObject getOObject() {
		return SecMap.getInstance().getOObject(this);
	}
	
	/**
	 * returns the immediate parent object or null if the parentDomain is Shared
	 * */
	public Set<SecObject> getParentObjects() {
		SecMap instance = SecMap.getInstance();
		Set<SecObject> parents = new HashSet<SecObject>();
		IObject oObject = getOObject();
		Set<? extends IObject> parentObjects = oObject.getParentObjects();
		for (IObject pObject:parentObjects) {
            parents.add(instance.getSecObject(pObject));
        }
		return parents;
	}

	/**
	 * Return the objects in the immediate child domains.
	 */
	public Set<SecObject> getChildObjects() {
		SecMap instance = SecMap.getInstance();
		Set<SecObject> returnSet = new HashSet<SecObject>();
		IObject oObject = getOObject();
		Set<? extends IObject> childObjects = oObject.getChildObjects();
		for (IObject childObject:childObjects) {
            returnSet.add(instance.getSecObject(childObject));
        }
		return returnSet;
	}

	/**
	 * Return the transitive parents
	 */
	public Set<SecObject> getAncestors() {
		SecMap instance = SecMap.getInstance();
		Set<SecObject> returnSet = new LinkedHashSet<SecObject>();
		//return empty set if the map does not contain this
		IObject oObject = getOObject();
		if (oObject == null) 
			return returnSet;
		Set<? extends IObject> ancestors = oObject.getAncestors();
		for (IObject iObject : ancestors) {
			returnSet.add(instance.getSecObject(iObject));
		}
		return returnSet;
	}

	public boolean hasParentObjects() {
		return !getParentObjects().isEmpty();
	}

	/**
	 * Return the transitive children
	 */
	public Set<SecObject> getDescendants() {
		SecMap instance = SecMap.getInstance();
		Set<SecObject> returnSet = new LinkedHashSet<SecObject>();
		if (getOObject() == null) 
			return returnSet;
		
		Set<? extends IObject> desc =  getOObject().getDescendants();		
		for (IObject o:desc){
			returnSet.add(instance.getSecObject(o));
		}
		return returnSet;
	}
	
	public Type getObjectType() {
		Type type = Type.getUnknownType();
		IObject oobject = getOObject();
		if (oobject != null)
			type = oobject.getC();
		return type;
	}
	
	// TODO: LOW. Delete this method.
	// Have its callers use the inline version. It's just one line!
	// Or keep it, for convenience!
	@Deprecated
	public boolean isSubtypeCompatible(SecObject secret) {
		return getObjectType().isSubtypeCompatible(secret.getObjectType());
	}
	
	// TODO: LOW. Delete this method.
	// Have its callers use the inline version. It's just one line!
	// Or keep it, for convenience!	
	@Deprecated
	public boolean isSubtypeCompatible(Type type) {
		return getObjectType().isSubtypeCompatible(type);
	}
	
	@Override
    public boolean accept(SecVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		
		if (visitChildren ) {
			for(SecObject child : getChildObjects()) {
				if (visitor.preVisit(child))
				// TODO: OK to discard return value of accept?
				child.accept(visitor);
			}
		}
		
		return true;
    }

	@Override
	public String toString() {
		return name;
	}

	public void clear() {
		// nothing to clear
	}

	@Override
	public Type getC() {		
		return getObjectType();
	}

	@Override
	public Set<IDomain> getChildren() {		
		return getOObject().getChildren();
	}

	@Override
	public String getInstanceDisplayName() {
		return getOObject().getInstanceDisplayName();
	}

	@Override
	public String getO_id() {
		return getOObject().getO_id();
	}

	@Override
	public IDomain getParent() {
		return getOObject().getParent();
	}

	@Override
	public String getTypeDisplayName() {
		return getOObject().getTypeDisplayName();
	}

	@Override
	public boolean hasChildren() {	
		return getOObject().hasChildren();
	}

	@Override
	public boolean hasParent() {	
		return getOObject().hasParent();
	}

	@Override
	public boolean isTopLevel() {	
		return getOObject().isTopLevel();
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		return getOObject().accept(visitor);
	}

	@Override
	public TraceabilityListSet getPath() {
		return getOObject().getPath();
	}

	@Override
	public Set<BaseTraceability> getTraceability() {
		return getOObject().getTraceability();
	}

	/***
	 * if any of the descendants has the property value set, return it. 
	 * TODO: what happen in case of a conflict - one child is trusted, one untrusted?
	 */
	@Override
	public boolean hasPropertyValue(Property type) {
		return hasPropertyValue(type, true);
	}


	/**
	 * check of this object or any of its descendants has this property
	 * */
	//TODO: add the dual - useAcestors
	// hasPropertyValueDueToAncs
	// hasPropertyValueDueToDescs
	public boolean hasPropertyValue(Property type, boolean useDescendants) {
		boolean hasProp = super.hasPropertyValue(type);
		if (hasProp)
			return true;
		else if (useDescendants){			
			Set<SecObject> descendants = getDescendants();
			for (SecObject o :descendants) {
				Property propertyValue = o.getPropertyValue(type);
				if (propertyValue!=null && propertyValue==type) 
					return true;			
			} 
		}
		return false;
	}

	@Override
    public boolean isMainObject() {
	    return getOObject().isMainObject();
    }
	
	@Override
    public boolean isUnique() {
	    return getOObject().isUnique();
    }
	
	@Override
    public boolean isLent() {
	    return getOObject().isLent();
    }

	@Override
    public String getObjectKey() {
	    return getOObject().getObjectKey();
    }
	
}

