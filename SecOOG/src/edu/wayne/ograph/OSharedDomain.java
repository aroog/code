package edu.wayne.ograph;

import ast.Type;


/**
 * XXX. Why do we need this?
 * DONE. Exposed OGraph.getDShared().
 */
public class OSharedDomain extends ODomain {
	private static OSharedDomain s_singleton = null;

	// Add default constructor for serialization
	protected OSharedDomain () {
	}
	
	// NOTE: XXX. Do not use required, read-only fields;
	// They cause problems when loading from XML, since there can be cycles in the object graph
	/**
	 * Client code should use this constructor. D_id and d are immutable! Do NOT use the setters below!
	 */
	private OSharedDomain(String D_id, String d) {
		super(D_id, Type.getUnknownType(), d);
	}

	public static OSharedDomain getInstance() {
		if (s_singleton == null) {
			s_singleton = new OSharedDomain("::SHARED", "SHARED");
		}
		return s_singleton;
	}

	@Override
	public void clear() {		
		super.clear();
	}

}
