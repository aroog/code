package edu.wayne.ograph;

import java.util.HashSet;
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

public class ODomain extends OElement implements IDomain {

	@Attribute(required = true, name = "D_id")	
	protected String D_id;

	@Element(required = false, name = "C")	
	protected Type C;
	
	@Attribute(required = true, name = "d")	
	protected String d;
	
	@Attribute(required = false)
	protected boolean isPublic = false;

	@Attribute(required = false)
	protected boolean isTopLevel = false;
	
	@ElementList	
	private Set<IObject> objects = new HashSet<IObject>();

	@Transient
	private Set<IObject> parents = new HashSet<IObject>();

	// Add default constructor for serialization
	protected ODomain() {
		super();
	}
	
	// For serialization only
	// The "String" version of C, to avoid dependency on MiniAst
	// XXX. Why not expose in the interface!
    public String getTypeName() {
	    return C.getFullyQualifiedName();
    }

	// NOTE: XXX. Do not use required, read-only fields;
	// They cause problems when loading from XML, since there can be cycles in the object graph
	/**
	 * Client code should use this constructor. D_id and d are immutable! Do NOT use the setters below!
	 */
    // XXX. C param is not used?!
	public ODomain(String D_id, Type C, String d) {
		super();
		this.D_id = D_id;
		this.d = d;
	}

	// Package protected on purpose; used by FinishingVisitor
	boolean addParent(IObject oObject) {
		return this.parents.add(oObject);
	}

	public void addObject(IObject oObject) {
		this.objects.add(oObject);
	}

	/**
	 * returns children oobjects
	 * */
	public Set<IObject> getChildren() {
		return objects;
	}

	// NOTE: An ODomain does not have a unique parent
	@JsonBackReference("domainParents")
	public Set<IObject> getParents() {
		return parents;
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		boolean visitChildren = visitor.visit(this);

		if (visitChildren) {
			for (IObject child : getChildren()) {
				child.accept(visitor);
			}
		}

		return true;
	}

	@Override
	public String getD_id() {
		return D_id;
	}
	
	// NOTE: Not part of the interface! Used by the serialization!
	/**
	 * Client code should not use this directly!
	 */
	public void setD_id(String dId) {
    	D_id = dId;
    }

	@Override
	public String getD() {
		return d;
	}

	@Override
	@JsonIgnore
	public Type getC() {
		return C;
	}
	
	// NOTE: Not part of the interface! Used by the serialization!
	/**
	 * Client code should not use this directly!
	 */
	public void setD(String d) {
    	this.d = d;
    }
	
	// NOTE: Not part of the interface! Used by the serialization!
	/**
	 * Client code should not use this directly!
	 */
	@JsonIgnore
	public void setC(Type C) {
    	this.C = C;
    }
	
	public void clear() {
//		for (IObject o : parents)
//			((OObject)o).clear();
		this.parents.clear();
//		for (IObject o : objects)
//			((OObject)o).clear();
		this.objects.clear();		
	}

	@Override
    public boolean isPublic() {
	    return this.isPublic;
    }

	public void setPublic(boolean isPublic) {
    	this.isPublic = isPublic;
    }
	
	@Override
    public boolean hasChildren() {
	    return objects.size() > 0;
    }

	@Override
	public boolean isTopLevel() {
		return isTopLevel;
	}
	
	public void setTopLevel(boolean b) {
		isTopLevel = b;
	}
}
