package edu.wayne.ograph;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ast.Type;
import oog.itf.IDomain;
import oog.itf.IObject;

// DONE: Add traceability information to OObject
// - Maybe using abstract objects
//DONE: Define a base class? OElement which has the traceability stuff.
//DONE: Make OEdge extend from OElement

// Do not expose C<D> to outside; only public/protected methods available
public class OObject extends OElement implements IObject {

	@Element(required = true, name = "C")	
	protected Type C;
	
	@Attribute(required = true, name = "O_id")
	protected String O_id;

	@ElementList	
	private Set<IDomain> domains = new HashSet<IDomain>();

	@Transient
	private IDomain parent;

	@Attribute(required = true)
	private String instanceDisplayName;

	@Attribute(required = true)
	private String typeDisplayName;
	
	@Attribute(required = false)
	private boolean isTopLevel; 
	
	@Attribute(required = false)
	private boolean isMainObject; 
	
	@Attribute(required = false)
	private boolean isUnique;

	@Attribute(required = false)
	private boolean isLent; 

	// Do not save in XML; this is just for JSON
	@Transient
	private Set<ResourceLineKey> traces = new HashSet<ResourceLineKey>();

	// Do not save in XML; this is just for JSON
	@Transient
	private Set<AUString> expressions = new HashSet<AUString>();
	
	@Attribute(required = false)
	private String objectKey;
	
	// Add default constructor for serialization
	protected OObject() {
		 super();
	 }

	public OObject(@Attribute(required = true, name = "O_id") String O_id, 
			@Element(required = true, name = "C") Type C,
			IDomain parent) {
		super();
		this.O_id = O_id;
		this.C = C;

		// parent is the parent domain of this
		this.parent = parent;

		// NOTE: would be hackish to do the following:
		// parentDomain.addObject(this);
	}

	public boolean addDomain(ODomain childDomain) {
		// childDomain is a child of this object;
		domains.add(childDomain);
		// this object is a parent of the childDomain
		
		return childDomain.addParent(this);
	}

	public Set<IDomain> getChildren() {
		return domains;
	}

	@JsonBackReference("objectParent")
	public IDomain getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		boolean visitChildren = visitor.visit(this);

		if (visitChildren) {
			for (IDomain child : getChildren()) {
				if (visitor.preVisit(child))
					// TODO: OK to discard return value of accept?
					child.accept(visitor);
			}
		}

		return true;
	}

	@JsonIgnore
	public Type getC() {
		return C;
	}

	public String getO_id() {
		return O_id;
	}

	public void clear() {
		domains.clear();
		//parent.clear();
	}

	@Override
    public boolean hasChildren() {
	    return domains.size() > 0;
    }

	@Override
    public String getInstanceDisplayName() {
	    return instanceDisplayName;
    }

	public void setInstanceDisplayName(String instanceDisplayName) {
		this.instanceDisplayName = instanceDisplayName;
	}

	@Override
    public String getTypeDisplayName() {
	    return typeDisplayName;
    }

	public void setTypeDisplayName(String typeDisplayName) {
		this.typeDisplayName = typeDisplayName;
	}

	// Package protected on purpose; used by FinishingVisitor
	void setParent(IDomain parent) {
		this.parent = parent;
	}

	@Override
	public boolean isTopLevel() {
		return isTopLevel;
	}
	
	public void setTopLevel(boolean b) {
		isTopLevel = b;
	}

	/**
	 * Return the transitive parents
	 */
	@JsonIgnore
	public Set<IObject> getAncestors() {
		Set<IObject> returnSet = new HashSet<IObject>();
		returnSet.add(this);
		Set<IObject> parents = getParentObjects();
		if (parents != null) {
			returnSet.addAll(parents);
			for (IObject parent : parents) {
				if (!parent.equals(this))
					returnSet.addAll(parent.getAncestors());
			}
		}
		return returnSet;
	}

	/**
	 * @deprecated This is inefficient; implemented in terms of getParentObjects().
	 */
	@Deprecated
	public boolean hasParentObjects() {
		Set<IObject> parentObjects = getParentObjects();
		return parentObjects!=null && !parentObjects.isEmpty();
	}

	/**
	 * Return the objects in the immediate child domains.
	 */	
	@Override
	@JsonIgnore
	public Set<IObject> getChildObjects() {
		Set<IObject> returnSet = new HashSet<IObject>();
		Set<IDomain> childrenDomain = this.getChildren();
		for (IDomain oDomain:childrenDomain){
			Set<IObject> childObjects = oDomain.getChildren();
			for (IObject childObject:childObjects) {
				returnSet.add(childObject);
			}
		}
		return returnSet;
	}

	/**
	 * Return the transitive children
	 */
	@JsonIgnore
	public Set<IObject> getDescendants() {
		Set<IObject> returnSet = new LinkedHashSet<IObject>();
		returnSet.add(this);
		Set<IObject> children = getChildObjects();
		returnSet.addAll(children);		
		for (IObject o:children)
			if (!o.equals(this))
				returnSet.addAll(o.getDescendants());
		return returnSet;
	}

	/**
	 * returns the immediate parent object or null if the parentDomain is Shared
	 * */
	@JsonIgnore
	public Set<IObject> getParentObjects() {	
		Set<IObject> parents = new HashSet<IObject>();

		IDomain parentDomain = getParent();
		if (parentDomain!=null)
			for(IObject aParent : parentDomain.getParents() ) {
				// XXX. Can we check that we're getting OWorld more efficiently? Using instanceof?
				// do not add OWorld as ancestor, stop at main
				if (aParent.hasParent()) {
					parents.add(aParent);
				}
			}
		return parents;
	}

	@Override
	public String toString(){
		// TODO: HIGH. Come up with something better here:
		// Use O_id or obj: C
		return instanceDisplayName;
	}

	@Override
    public boolean isMainObject() {
	    return isMainObject;
    }

	public void setMainObject(boolean isMainObject) {
    	this.isMainObject = isMainObject;
    }
	
	@Override
    public boolean isUnique() {
	    return isUnique;
    }

	public void setAsUnique(boolean isUnique) {
    	this.isUnique = isUnique;
    }
	
	@Override
    public boolean isLent() {
	    return isLent;
    }

	public void setAsLent(boolean isLent) {
    	this.isLent = isLent;
    }
	
	/**
	 * Simple traceability information that does not use MiniAST
	 * XXX. Why not move to super class? Don't we need tracability from edges too? 
	 * 
	 * XXX. Why not cache this info?
	 * 
	 * Do not save this using Simple.XML as well. Or maybe do so?!
	 */
	@Transient
	public Set<ResourceLineKey> getTraceability2() {
		return traces;
	}

	@Transient
	/**
	 * Do not save this using Simple.XML as well. Or maybe do so?!
	 * @param set
	 */
	public void setTraceability2(Set<ResourceLineKey> set) {
		this.traces = set;
	}
	
	// XXX. Gotta expose getter on IObject interface
	public String getObjectKey() {
		return objectKey;
	}

	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}
	
	public Set<AUString> getExpressions() {
    	return expressions;
    }

	public void setExpressions(Set<AUString> expressions) {
    	this.expressions = expressions;
    }
}
