package edu.wayne.ograph;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IGraph;
import oog.itf.IObject;

/**
 * Sanity checking visitor to make sure that no unexpected pointers are encountered
 * 
 * TODO: LOW. List all the rules
 * - No empty O_ids
 * - No null pointers where we do not expect them, etc.
 *
 */
public class SanityCheckVisitor extends OGraphVisitorBase {

	private IObject root;

	public SanityCheckVisitor() {
		System.err.println("Sanity Check Visitor");
	}
	
	@Override
    public boolean visit(IDomain node) {
		// System.err.println("Checking ODomain " + node);
		
	    return super.visit(node);
    }

	@Override
    public boolean visit(IEdge node) {
		// System.err.println("Checking OEdge " + node);
	
		// TODO: MED. Check that the edge has no dangling Osrc or Odst
		// node.getOsrc() must be in the set of OObjects
		// node.getOdst() must be in the set of OObjects
	    return super.visit(node);
    }

	@Override
    public boolean visit(IObject node) {
		// System.err.println("Checking OObject " + node.getO_id());

		if ( node.getParent()  == null ) {
			System.err.println("Sanity Check: Null getParent(): " + node.getO_id());
		}
		
	    return super.visit(node);
    }

	@Override
    public boolean visit(IGraph node) {
		// System.err.println("Checking the OGraph");
		
		this.root = node.getRoot();
		
		if ( this.root == null ) {
			System.err.println("OGraph error: root node is null");
		}
	    return super.visit(node);
    }

}
