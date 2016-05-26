package oogre.analysis;

import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.Heuristic;
import oogre.refinements.tac.PushIntoPD;
import oogre.refinements.tac.Refinement;


public class TestAphyds {

	public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
    
		context.setSelectedTyping("", "p", "p");
    	Queue<Refinement> refQueue = new LinkedList<Refinement>();
    
    	IObject nodeObj = facade.getObject("Node", dShared, dShared);
    	IObject circuitObj = facade.getObject("Circuit", dShared, dShared);
    	IObject netObj = facade.getObject("Net", dShared, dShared);
    	IObject vNetObj = facade.getObject("VectorNet", dShared, dShared);
    	IObject vNodeObj = facade.getObject("VectorNode", dShared, dShared);
    	IObject vTerminalObj = facade.getObject("VectorTerminal", dShared, dShared);
    	IObject viewerObj = facade.getObject("Viewer", dShared, dShared);
    	IObject placerObj = facade.getObject("Placer", dShared, dShared);
    	IObject mainObj = facade.getObject("Main", dShared, dShared);
    
    	//XXX. this code is hard-coding the mappings form an OObject to its Context OObjects (OObjects that the object get created in their context)
    	//It is needed for pushIntoOwned.
    	//Since we do infer the owned using heuristics, no need to do this operations any more
/*    	HashSet<IObject> vNodeContext = new HashSet<IObject>();
    	vNodeContext.add(circuitObj);
    	context.getPC().put(vNodeObj, vNodeContext);
    
    	HashSet<IObject> vNetContext = new HashSet<IObject>();
    	vNetContext.add(circuitObj);
    	context.getPC().put(vNetObj, vNetContext);
    
    	HashSet<IObject> vTerminalContext = new HashSet<IObject>();
    	vTerminalContext.add(netObj);
    	vTerminalContext.add(nodeObj);
    	context.getPC().put(vTerminalObj, vTerminalContext);*/
    
    	//XXX. becuase of the same reason there is no need to do pushIntoOwned refinements and SplitUp which does a PushIntoOnwed
    	/*Refinement ref1 = new PushIntoOwned(vNodeObj, circuitObj);
    	refQueue.add(ref1);
    	Refinement ref2 = new PushIntoOwned(vNetObj, circuitObj);
    	refQueue.add(ref2);
    	Refinement ref3 = new SplitUp(vTerminalObj, nodeObj, "private");
    	refQueue.add(ref3);
    	Refinement ref4 = new PushIntoOwned(vTerminalObj, netObj);
    	refQueue.add(ref4);*/
    	
    	Refinement ref5 = new PushIntoPD(nodeObj, circuitObj, "DB");
    	refQueue.add(ref5);
    	
    	Refinement ref6 = new PushIntoPD(viewerObj, mainObj, "VIEWER");
    	refQueue.add(ref6);
    	Refinement ref7 = new PushIntoPD(placerObj, mainObj, "PLACER");
    	refQueue.add(ref7);
    	
    	//Commented out for the purpose of testing to see why TFs generate an unwanted constraint between Viewer and Circuit
    	Refinement ref8 = new PushIntoPD(circuitObj, mainObj, "MODEL");
    	refQueue.add(ref8);
    	
    
    	return refQueue;
    
    }
	
	public static Queue<Heuristic> getHeuristics(OOGContext context){
		
		Queue<Heuristic> hQueue = new LinkedList<Heuristic>();
//		Heuristic h1 = new InferOwnedHeuristic("sources", AnnotateUnitEnum.f, "VectorTerminal", "Net");
//		hQueue.add(h1);
//		Heuristic h2 = new InferOwnedHeuristic("inputs", AnnotateUnitEnum.f, "VectorTerminal", "Node");
//		hQueue.add(h2);
//		Heuristic h3 = new InferOwnedHeuristic("nodes", AnnotateUnitEnum.f, "VectorNode", "Circuit");
//		hQueue.add(h3);
//		Heuristic h4 = new InferOwnedHeuristic("nets", AnnotateUnitEnum.f, "VectorNet", "Circuit");
//		hQueue.add(h4);
		return hQueue;

	}

}
