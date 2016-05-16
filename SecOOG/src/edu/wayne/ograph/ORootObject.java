package edu.wayne.ograph;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import ast.Type;
import oog.itf.IDomain;

// Note: this is O_world
// XXX. Add a marker interface for this special OObject, e.g., IRootObject, similarly to IObject
public class ORootObject extends OObject {

	// Add default constructor for serialization
	protected ORootObject() {
		super();
	}
	
	// Constructor that calls the super constructor
	public ORootObject(@Attribute(required = true, name = "O_id") String O_id,
	        @Element(required = true, name = "C") Type C, IDomain parent) {
		super(O_id, C, parent);
	}
	
	// NOTE: the parent argument is ignored
	public ORootObject(IDomain parent) {
		super("O_world", new Type("DUMMY"), null);

		// CUT: NO NEED. We end up with 2 shared domains!
		// TODO: HIGH. Can we move this elsewhere?
		// this.addDomain(OSharedDomain.getInstance());
	}

	@Override
	// Special case: the root object has no parent
	public ODomain getParent() {
		return null;
	}
	
	public void clear(){
		super.clear();
// CUT: NO NEED. We end up with 2 shared domains!
//		OSharedDomain.getInstance().clear();
//		this.addDomain(OSharedDomain.getInstance());
//		OSharedDomain.getInstance().addParent(this);
	}

}
