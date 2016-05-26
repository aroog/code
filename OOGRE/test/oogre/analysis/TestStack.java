package oogre.analysis;

import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.Heuristic;
import oogre.refinements.tac.PushIntoPD;
import oogre.refinements.tac.PushIntoParam;
import oogre.refinements.tac.Refinement;

public class TestStack {
	
public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
	
		context.setSelectedTyping("", "p", "p");
	    
		Queue<Refinement> refQueue = new LinkedList<Refinement>();

		IObject mainObj = facade.getObject("stack.Main", dShared, dShared);
		IObject stackObj = facade.getObject("stack.XStack", dShared, dShared);
		IObject dataObj = facade.getObject("stack.X", dShared, dShared);
//		IObject linkObj = facade.getObject("stack.Link", dShared, dShared);

		IDomain mainObjOwned = facade.getPrivateDomain(stackObj);
    	IObject linkObj = facade.getObject(stackObj,mainObjOwned);

// DONE. Remove PC here, used by PushIntoOwned.
//		HashSet<IObject> linkObjContext = new HashSet<IObject>();
//		linkObjContext.add(stackObj);
//		context.getPC().put(linkObj, linkObjContext);

// XXX. In OOGRE1.0 that we did not have heuristics to infer owned, we used PushIntoOwned refinement.
//		now it it commented out because it can be replaced by the result of running heuristics on the test case
//		Refinement ref0 = new PushIntoOwned(linkObj, stackObj);
//		refQueue.add(ref0);
		
// XXX. This is a refinement that says object of type C (data) in the stack example should be in the dopmain parameter of Link
//		The reason is the analysis cannot figure out this without this piece of information. It is highly design intent.
		Refinement ref1 = new PushIntoParam(dataObj, linkObj);
		refQueue.add(ref1);
		
		Refinement ref2 = new PushIntoPD(dataObj, mainObj,"DATA");
		refQueue.add(ref2);
		
		Refinement ref3 = new PushIntoPD(stackObj, mainObj,"STACK");
		refQueue.add(ref3);

		return refQueue;

	}
	
	/**
	 * Heuristics: figure out that Link is 'owned' by Stack.
	 * How:
	 * - private field that does not leak to outside
	 *  
	 */
	public static Queue<Heuristic> getHeuristics(OOGContext context){
		
		Queue<Heuristic> hQueue = new LinkedList<Heuristic>();
		//XXX. No heuristic to do pushIntoOwned
//		Heuristic h1 = new InferOwnedHeuristic("top", AnnotateUnitEnum.f, "stack.Link", "stack.XStack");
//		hQueue.add(h1);
		return hQueue;
	}

}
