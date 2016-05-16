package edu.wayne.ograph;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IGraph;
import oog.itf.IObject;

/**
 * Finishing visitor to set the backpointers, based on the forward pointers!
 *
 */
public class FinishingVisitor extends OGraphVisitorBase {

	public FinishingVisitor() {
	}
	
	@Override
    public boolean visit(IDomain node) {
		// System.out.println("Finishing ODomain " + node);
		
		for(IObject iObject : node.getChildren() ) {
			if (iObject instanceof OObject ) {
				OObject oObject = (OObject)iObject;
				// Set the backpointers...
				oObject.setParent(node);
			}
			else {
				System.err.println("Ouch!");
			}
		}

	    return super.visit(node);
    }

	@Override
    public boolean visit(IEdge node) {
		// System.out.println("Finishing OEdge " + node);
	
	    return super.visit(node);
    }

	@Override
    public boolean visit(IObject node) {
		// System.out.println("Finishing OObject " + node.getO_id());

		for(IDomain iDomain: node.getChildren() ) {
			if (iDomain instanceof ODomain ) {
				ODomain oDomain = (ODomain)iDomain;
				// Set the backpointers...
				oDomain.addParent(node);
			}
			else {
				System.err.println("Ouch!");
			}
			
		}
		
	    return super.visit(node);
    }

	@Override
    public boolean visit(IGraph node) {
		// System.out.println("Finishing the OGraph into a SecGraph");
	    return super.visit(node);
    }

}
