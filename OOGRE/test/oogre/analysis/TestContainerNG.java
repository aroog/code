package oogre.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.PushIntoOwned;
import oogre.refinements.tac.Refinement;

public class TestContainerNG {
	
public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
	    
		Queue<Refinement> refQueue = new LinkedList<Refinement>();

		IObject consObj = facade.getObject("sequence.Cons", dShared, dShared);
		IObject sequenceObj = facade.getObject("sequence.Sequence", dShared, dShared);
		IObject iteratorObj = facade.getObject("sequence.SequenceIterator", dShared, dShared);
		IObject mainObj = facade.getObject("sequence.Main", dShared, dShared);
		IObject containerObj = facade.getObject("sequence.Container", dShared, dShared);


		HashSet<IObject> consObjContext = new HashSet<IObject>();
		consObjContext.add(sequenceObj);
		context.getPC().put(consObj, consObjContext);
		
		HashSet<IObject> sequenceObjContext = new HashSet<IObject>();
		sequenceObjContext.add(containerObj);
		context.getPC().put(sequenceObj, sequenceObjContext);

		
		Refinement ref1 = new PushIntoOwned(sequenceObj, containerObj);
		refQueue.add(ref1);

		return refQueue;

	}

}
