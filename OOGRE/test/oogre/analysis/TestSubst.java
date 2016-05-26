package oogre.analysis;

import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.PushIntoPD;
import oogre.refinements.tac.Refinement;

public class TestSubst {

	public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
		Queue<Refinement> refQueue = new LinkedList<Refinement>();
	    
    	IObject mainObj = facade.getObject("Main", dShared, dShared);
    	IObject itemObj = facade.getObject("SItem", dShared, dShared);
    	
    	Refinement ref1 = new PushIntoPD(itemObj, mainObj, "STATE");
    	refQueue.add(ref1);
    	
    	return refQueue;
	}
}
