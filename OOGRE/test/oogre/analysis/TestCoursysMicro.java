package oogre.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.Heuristic;
import oogre.refinements.tac.PushIntoPD;
import oogre.refinements.tac.Refinement;

public class TestCoursysMicro {
	
	public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
	    
		context.setSelectedTyping("", "p", "p");
		
		Queue<Refinement> refQueue = new LinkedList<Refinement>();

		IObject logicObj = facade.getObject("courses.Logic", dShared, dShared);
		IObject systemObj = facade.getObject("courses.Main", dShared, dShared);
		IObject courseConsObj = facade.getObject("courses.CourseCons", dShared, dShared);
		IObject courseSequenceObj = facade.getObject("courses.CourseSequence", dShared, dShared);
		IObject dataObj = facade.getObject("courses.Data", dShared, dShared);
		IObject courseIteratorObj = facade.getObject("courses.CourseIterator", dShared, dShared);
		IObject courseObj = facade.getObject("courses.Course", dShared, dShared);
		IObject clientObj = facade.getObject("courses.Client", dShared, dShared);

		HashSet<IObject> courseConsObjContext = new HashSet<IObject>();
		courseConsObjContext.add(courseSequenceObj);
		context.getPC().put(courseConsObj, courseConsObjContext);

		//XXX. This PushIntoOwned refinement is replaced by the heuristic
//		Refinement ref9 = new PushIntoOwned(courseConsObj, courseSequenceObj);
//		refQueue.add(ref9);
		//XXX. problematic PushIntoPD...commented out for now.
//		Refinement ref12 = new PushIntoPD(courseIteratorObj, courseSequenceObj, "ITERS");
//		refQueue.add(ref12);
		Refinement ref5 = new PushIntoPD(logicObj, systemObj, "DATA");
		refQueue.add(ref5);
		Refinement ref19 = new PushIntoPD(clientObj, systemObj, "USER");
		refQueue.add(ref19);
		Refinement ref17 = new PushIntoPD(dataObj, systemObj, "DATA");
		refQueue.add(ref17);
		Refinement ref18 = new PushIntoPD(courseObj, dataObj, "STATE");
		refQueue.add(ref18);

		return refQueue;

	}
	
	public static Queue<Heuristic> getHeuristics(OOGContext context){
		Queue<Heuristic> hQueue = new LinkedList<Heuristic>();
//		Heuristic h1 = new InferOwnedHeuristic("head", AnnotateUnitEnum.f, "courses.CourseCons", "courses.CourseSequence");
//		hQueue.add(h1);
		return hQueue;
	}

}
