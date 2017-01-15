package oogre.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.PushIntoOwned;
import oogre.refinements.tac.PushIntoPD;
import oogre.refinements.tac.Refinement;
import oogre.refinements.tac.SplitUp;

public class TestAphydsInheritance {

	public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
	    
    	Queue<Refinement> refQueue = new LinkedList<Refinement>();
    
    	IObject nodeObj = facade.getObject("Node", dShared, dShared);
    	IObject circuitObj = facade.getObject("Circuit", dShared, dShared);
    	IObject netObj = facade.getObject("Net", dShared, dShared);
    	IObject vElementObj = facade.getObject("VectorElement", dShared, dShared);
    	IObject vTerminalObj = facade.getObject("VectorTerminal", dShared, dShared);
    	IObject viewerObj = facade.getObject("Viewer", dShared, dShared);
    	IObject placerObj = facade.getObject("Placer", dShared, dShared);
    	IObject mainObj = facade.getObject("Main", dShared, dShared);
    
    	HashSet<IObject> vNodeContext = new HashSet<IObject>();
    	vNodeContext.add(circuitObj);
    	context.getPC().put(vElementObj, vNodeContext);
    
    	HashSet<IObject> vTerminalContext = new HashSet<IObject>();
    	vTerminalContext.add(netObj);
    	vTerminalContext.add(nodeObj);
    	context.getPC().put(vTerminalObj, vTerminalContext);
    
    	Refinement ref1 = new PushIntoOwned(vElementObj, circuitObj);
    	refQueue.add(ref1);
    	Refinement ref3 = new SplitUp(vTerminalObj, nodeObj, "private");
    	refQueue.add(ref3);
    	Refinement ref4 = new PushIntoOwned(vTerminalObj, netObj);
    	refQueue.add(ref4);
    	Refinement ref5 = new PushIntoPD(nodeObj, circuitObj, "DB");
    	refQueue.add(ref5);
    	Refinement ref6 = new PushIntoPD(viewerObj, mainObj, "VIEWER");
    	refQueue.add(ref6);
    	Refinement ref7 = new PushIntoPD(placerObj, mainObj, "MODEL");
    	refQueue.add(ref7);
    	Refinement ref8 = new PushIntoPD(circuitObj, mainObj, "MODEL");
    	refQueue.add(ref8);
    
    	return refQueue;
    
    }

	
}
