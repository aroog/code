package edu.wayne.metrics.adb;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IGraph;
import oog.itf.IObject;
import edu.wayne.ograph.OGraphVisitorBase;
import edu.wayne.ograph.ORootObject;

// TODO: HIGH. Try to refactor output of triplets. Too much near code duplication.
// TODO: MED. Create a mapping between OObjects and triplets:
// - So the EdgeInfo triplets don't create new ADBTriplet objects?

/**
 * NOTE: This visitor uses OGraphVisitorBase that includes cycle detection
 */
public class TripletVisitor extends OGraphVisitorBase {
	private Set<ADBTriplet> allTriplets = new HashSet<ADBTriplet>();
	private Set<EdgeInfo> allEdgeTriplets = new HashSet<EdgeInfo>();
	
	// TODO: There has got to be a better way to get all Objects!
	// Maybe root.getDescendants()? Probably not more efficient.
	private Set<IObject> allObjects = new HashSet<IObject>();
	
	private IObject root;
	
	public TripletVisitor() {
	}
	
	@Override
    public boolean visit(IEdge edge) {
		// System.out.println("Visiting OEdge " + edge);
		
	    return super.visit(edge);
    }

	@Override
    public boolean visit(IObject objectA) {
		// Collect all the objects here.
		if ( !(objectA instanceof ORootObject) ) {
			// DO NOT include the root object.
			// I don't want to deal with getParent() returning null! Not relevant here
			// Every object is in domain
			// TODO: HIGH. When Radu fixes SecOOG to change "root" ...we should get rid of this hack
			allObjects.add(objectA);
		}
		
		if (objectA.getParent() == null ) {
			int debug = 0; debug++;
		}
		
		// Skip the root object
		if (objectA != this.root) {
			// System.out.println("Visiting OObject " + objectA.getO_id());

			ADBTriplet triplet = ADBTriplet.getTripletFrom(objectA);
			if (triplet != null) {
				allTriplets.add(triplet);
			}
		}
	    return super.visit(objectA);
    }

	@Override
    public boolean visit(IGraph node) {
		// System.out.println("Visiting the OGraph");
		
		// Set the root
		this.root = node.getRoot();
		
	    return super.visit(node);
    }
	

	public Set<ADBTriplet> getAllTriplets() {
    	return allTriplets;
    }

	public Set<EdgeInfo> getAllEdgeTriplets() {
    	return allEdgeTriplets;
    }

	public Set<IObject> getAllObjects() {
    	return allObjects;
    }
}
