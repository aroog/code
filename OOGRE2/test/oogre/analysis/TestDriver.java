package oogre.analysis;

import java.util.LinkedList;
import java.util.Queue;

import oog.itf.IDomain;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.Heuristic;
import oogre.refinements.tac.Refinement;

public class TestDriver {
	
	public static final int TEST_Empty = 0;
	public static final int TEST_Aphyds = 10;
	public static final int TEST_CourSys = 20;
	public static final int Test_Subst = 30;
	public static final int Test_CourSys_Micro = 40;
	public static final int Test_Sequence = 50;
	public static final int TEST_Aphyds_Inherit = 60;
	public static final int TEST_QuadTree = 70;
	public static final int TEST_Container_NG = 80;
	public static final int TEST_Stack = 90;

	private static int currentTest = TEST_Empty;
	
	public static Queue<Refinement> test(Facade facade, OOGContext context, IDomain dShared) {

		Queue<Refinement> refinements = new LinkedList<Refinement>();

		switch (currentTest) {
		case TEST_Empty:
			// NO refinements to test the default annotations
			refinements = new LinkedList<Refinement>();
			break;

		case TEST_CourSys:
			refinements = TestCourSys.getRefinements(facade, context, dShared);
			break;

		case TEST_Aphyds:
			refinements = TestAphyds.getRefinements(facade, context, dShared);
			break;
			
		case Test_Subst:
			refinements = TestSubst.getRefinements(facade, context, dShared);
			break;
		
		case Test_CourSys_Micro:
			refinements = TestCoursysMicro.getRefinements(facade, context, dShared);
			break;
			
		case Test_Sequence:
			refinements = TestSequenceNG.getRefinements(facade, context, dShared);
			break;
			
		case TEST_Aphyds_Inherit:
			refinements = TestAphydsInheritance.getRefinements(facade, context, dShared);
			break;
			
		case TEST_QuadTree:
			refinements = TestQuadTree.getRefinements(facade, context, dShared);
			break;
		
		case TEST_Container_NG:
			refinements = TestContainerNG.getRefinements(facade, context, dShared);
			break;
			
		case TEST_Stack:
			refinements = TestStack.getRefinements(facade, context, dShared);
			break;
		
		default:
			refinements = new LinkedList<Refinement>();
			System.err.println("Unknown testcase.");
		}

		return refinements;
	}


	public static Queue<Heuristic> getHeuristics(OOGContext context){
		
		Queue<Heuristic> heuristics = new LinkedList<Heuristic>();
		
		switch (currentTest) {
		
		case TEST_Empty:
			break;
		
		case TEST_Stack:
			heuristics = TestStack.getHeuristics(context);
			break;
			
		case TEST_Aphyds:
			heuristics = TestAphyds.getHeuristics(context);
			break;
			
		case Test_CourSys_Micro:
			heuristics = TestCoursysMicro.getHeuristics(context);
			break;
			
		case Test_Sequence:
			heuristics = TestSequenceNG.getHeuristics(context);
			break;
		}
		return heuristics;
		
	}
}
