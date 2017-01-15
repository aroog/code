package oogre.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.PushIntoOwned;
import oogre.refinements.tac.Refinement;

// TOEBI: Cleanup this testcase...do we still need getPC()
// - seems to work without it?
public class TestQuadTree {
	
	public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
		Queue<Refinement> refQueue = new LinkedList<Refinement>();
	    
    	IObject mainObj = facade.getObject("Main", dShared, dShared);
    	IObject quadObj = facade.getObject("QuadTree", dShared, dShared);
    	
    	HashSet<IObject> quadObjContext = new HashSet<IObject>();
    	quadObjContext.add(mainObj);
    	quadObjContext.add(quadObj);
    	context.getPC().put(quadObj, quadObjContext);
    	
//    	Refinement ref1 = new SplitUp(quadObj, mainObj, "private");
//    	refQueue.add(ref1);
    	
    	IDomain mainOwned = facade.getPrivateDomain(mainObj); 
    	IObject newQuadObj = facade.getObject(mainObj,mainOwned);
    	
    	quadObjContext.add(newQuadObj);
    	context.getPC().put(quadObj, quadObjContext);
    	
    	Refinement ref2 = new PushIntoOwned(quadObj, newQuadObj);
    	refQueue.add(ref2);
    	
    	return refQueue;
	}

}
