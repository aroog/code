package oogre.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.analysis.OOGContext;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.PushIntoOwned;
import oogre.refinements.tac.PushIntoPD;
import oogre.refinements.tac.Refinement;
import oogre.refinements.tac.SplitUp;


public class TestCourSys {

	public static Queue<Refinement> getRefinements(Facade facade,OOGContext context,IDomain dShared) {
    
    		Queue<Refinement> refQueue = new LinkedList<Refinement>();
    
    		IObject oObj = facade.getObject("java.lang.Object", dShared, dShared);
    		IObject rwLockObj = facade.getObject("courses.RWLock", dShared, dShared);
    		IObject fileWriterObj = facade.getObject("java.io.FileWriter", dShared, dShared);
    		IObject logObj = facade.getObject("courses.Logging", dShared, dShared);
    		IObject logicObj = facade.getObject("courses.Logic", dShared, dShared);
    		IObject systemObj = facade.getObject("courses.Main", dShared, dShared);
    		IObject stringConsObj = facade.getObject("courses.StringCons", dShared, dShared);
    		IObject stringSequenceObj = facade.getObject("courses.StringSequence", dShared, dShared);
    		IObject courseConsObj = facade.getObject("courses.CourseCons", dShared, dShared);
    		IObject courseSequenceObj = facade.getObject("courses.CourseSequence", dShared, dShared);
    		IObject studentConsObj = facade.getObject("courses.StudentCons", dShared, dShared);
    		IObject studentSequenceObj = facade.getObject("courses.StudentSequence", dShared, dShared);
    		IObject dataObj = facade.getObject("courses.Data", dShared, dShared);
    		IObject studentObj = facade.getObject("courses.Student", dShared, dShared);
    		IObject studentIteratorObj = facade.getObject("courses.StudentIterator", dShared, dShared);
    		IObject courseIteratorObj = facade.getObject("courses.CourseIterator", dShared, dShared);
    		IObject stringIteratorObj = facade.getObject("courses.StringIterator", dShared, dShared);
    		IObject courseObj = facade.getObject("courses.Course", dShared, dShared);
    		IObject clientObj = facade.getObject("courses.Client", dShared, dShared);
    
    		HashSet<IObject> vObjectContext = new HashSet<IObject>();
    		vObjectContext.add(rwLockObj);
    		context.getPC().put(oObj, vObjectContext);
    		HashSet<IObject> fileWriterObjContext = new HashSet<IObject>();
    		fileWriterObjContext.add(logObj);
    		context.getPC().put(fileWriterObj, fileWriterObjContext);
    		HashSet<IObject> logObjContext = new HashSet<IObject>();
    		HashSet<IObject> lockObjContext = new HashSet<IObject>();
    		logObjContext.add(logicObj);
    		lockObjContext.add(logicObj);
    		context.getPC().put(logObj, logObjContext);
    		context.getPC().put(rwLockObj, logObjContext);
    		HashSet<IObject> stringConsObjContext = new HashSet<IObject>();
    		stringConsObjContext.add(stringSequenceObj);
    		context.getPC().put(stringConsObj, stringConsObjContext);
    		HashSet<IObject> courseConsObjContext = new HashSet<IObject>();
    		courseConsObjContext.add(courseSequenceObj);
    		context.getPC().put(courseConsObj, courseConsObjContext);
    		HashSet<IObject> studentConsObjContext = new HashSet<IObject>();
    		studentConsObjContext.add(studentSequenceObj);
    		context.getPC().put(studentConsObj, studentConsObjContext);
    		HashSet<IObject> courseSequenceObjContext = new HashSet<IObject>();
    		courseSequenceObjContext.add(dataObj);
    		courseSequenceObjContext.add(studentObj);
    		context.getPC().put(courseSequenceObj, courseSequenceObjContext);
    
    		Refinement ref1 = new PushIntoOwned(oObj, rwLockObj);
    		refQueue.add(ref1);
    		Refinement ref2 = new PushIntoOwned(fileWriterObj, logObj);
    		refQueue.add(ref2);
    		Refinement ref3 = new PushIntoOwned(logObj, logicObj);
    		refQueue.add(ref3);
    		Refinement ref4 = new PushIntoOwned(rwLockObj, logicObj);
    		refQueue.add(ref4);
    		Refinement ref5 = new PushIntoPD(logicObj, systemObj, "LOGIC");
    		refQueue.add(ref5);
    		Refinement ref6 = new PushIntoOwned(stringConsObj, stringSequenceObj);
    		refQueue.add(ref6);
    		Refinement ref7 = new PushIntoOwned(courseConsObj, courseSequenceObj);
    		refQueue.add(ref7);
    		Refinement ref8 = new PushIntoOwned(studentConsObj, studentSequenceObj);
    		refQueue.add(ref8);
    		Refinement ref9 = new SplitUp(courseSequenceObj, dataObj, "private");
    		refQueue.add(ref9);
    		Refinement ref10 = new PushIntoOwned(courseSequenceObj, studentObj);
    		refQueue.add(ref10);
    		Refinement ref11 = new PushIntoPD(studentIteratorObj, studentSequenceObj, "ITERS");
    		refQueue.add(ref11);
    		Refinement ref12 = new PushIntoPD(courseIteratorObj, courseSequenceObj, "ITERS");
    		refQueue.add(ref12);
    		Refinement ref13 = new PushIntoPD(stringIteratorObj, stringSequenceObj, "ITERS");
    		refQueue.add(ref13);
//    		Refinement ref14 = new PushIntoPD(studentSequenceObj, systemObj, "STATE");
//    		refQueue.add(ref14);
//    		Refinement ref15 = new PushIntoPD(courseIteratorObj, systemObj, "STATE");
//    		refQueue.add(ref15);
//    		Refinement ref16 = new PushIntoPD(stringSequenceObj, systemObj, "STATE");
//    		refQueue.add(ref16);

    		Refinement ref18 = new PushIntoPD(courseObj, systemObj, "STATE");
    		refQueue.add(ref18);
    		Refinement ref17 = new PushIntoPD(dataObj, systemObj, "DATA");
    		refQueue.add(ref17);
    		Refinement ref19 = new PushIntoPD(clientObj, systemObj, "USER");
    		refQueue.add(ref19);

    
    		return refQueue;
    
    	}

}
