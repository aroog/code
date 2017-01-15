package oogre.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.Heuristic;
import oogre.refinements.tac.PushIntoOwned;
import oogre.refinements.tac.PushIntoPD;
import oogre.refinements.tac.Refinement;

public class TestSequenceNG {
	
public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
	    
		Queue<Refinement> refQueue = new LinkedList<Refinement>();

		IObject consObj = facade.getObject("sequence.Cons", dShared, dShared);
		IObject sequenceObj = facade.getObject("sequence.Sequence", dShared, dShared);
		IObject iteratorObj = facade.getObject("sequence.SequenceIterator", dShared, dShared);
		IObject mainObj = facade.getObject("sequence.Main", dShared, dShared);


		HashSet<IObject> ConsObjContext = new HashSet<IObject>();
		ConsObjContext.add(sequenceObj);
		context.getPC().put(consObj, ConsObjContext);

		
		Refinement ref1 = new PushIntoOwned(consObj, sequenceObj);
		refQueue.add(ref1);
		Refinement ref2 = new PushIntoPD(iteratorObj, sequenceObj, "ITERS");
		refQueue.add(ref2);

		return refQueue;

	}

	public static Queue<Heuristic> getHeuristics(OOGContext context){
		Queue<Heuristic> hQueue = new LinkedList<Heuristic>();
		return hQueue;
	}

}
