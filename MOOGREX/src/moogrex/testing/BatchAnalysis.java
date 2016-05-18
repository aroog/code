package moogrex.testing;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import moogrex.analysis.RunChildAnalysis;
import oog.re.IRefinement;
import oog.re.Refinement;
import oog.re.RefinementModel;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.IAnalysisReporter;


public class BatchAnalysis extends AbstractCompilationUnitAnalysis {
	public static final String CRYSTAL_NAME = "MOOGREX [TESTING]";

	private Set<ITypeRoot> compUnits = new HashSet<ITypeRoot>();
	
	private IAnalysisReporter savedReporter;
	private IAnalysisInput savedInput;

	BatchSetup setup = new BatchSetup();
	
	@Override
    public String getName() {
	    return CRYSTAL_NAME;
    }
	
	@Override
	public void analyzeCompilationUnit(CompilationUnit d) {
		// Do nothing for now
	}

	@Override
	public void beforeAllCompilationUnits() {
		super.beforeAllCompilationUnits();
	}

	/*
	 * This method is called ONCE after all compilation units.
	 * 
	 * The server must be started ONCE.
	 */
	@Override
    public void afterAllCompilationUnits() {
		try {
			// Run initially to save default annotations, run heuristics, extract initial graph, etc. 
			run();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

		setup.setFacade();
		
		// XXX. This may be called extra times
		setup.loadGraph();
		
		setup.loadModel();
		
		RefinementModel batchModel = setup.getBatchModel();
		if(batchModel != null) {
			List<IRefinement> batchRefs = batchModel.getRefinements();
			// Sort by RefId?
			
			// XXX. Reset all states?
			// batchRef.setState(RefinementState.Deferred);
			
			for(IRefinement batchRef : batchRefs) {
				// Skip implicit
				// XXX. Use isImplict flag, but right now, not being persisted
				if(batchRef.getRefID().startsWith("Auto")) {
					continue;
				}
				if(batchRef instanceof Refinement) {
					PrintWriter userOut = savedReporter.userOut();
					
					Refinement replayRef = setup.doRefinement((Refinement) batchRef);
					if(replayRef != null ) {
						userOut.println("\nReplay refinement...");
						run();
						reportOutput((Refinement) batchRef, replayRef);
					}
					else {
						userOut.println("\nCannot replay refinement. Unresolved Object Key: " + batchRef.toDisplayName());
					}
				}
			}
		}
    }

	/**
	 * Report output on user console
	 * 
	 * Compare the state of the expected (batchRef) to the actual state of the replayed refinement (replayRef) 
	 * @param batchRef 
	 * @param replayRef
	 */
	private void reportOutput(Refinement batchRef, Refinement replayRef) {
		PrintWriter userOut = savedReporter.userOut();
		
		StringBuilder builder = new StringBuilder();
		if ( replayRef.getState() == batchRef.getState() ) {
			builder.append("   PASS :-)) ");
		}
			else {
			builder.append("   FAIL -:(( ");
		}
		
		builder.append(" Actual=");
		builder.append(replayRef.getState());
		builder.append("; Expected=");
		builder.append(batchRef.getState());
		builder.append("; ");
		builder.append(replayRef.toDisplayName());
		
		userOut.println(builder.toString());
		userOut.flush();
    }

	/*
	 * Run the whole stack (OOGRE, PointsTo, ...) initially and after each refinement.
	 * Get the updated OGraph.
	 * 
	 * XXX. Can we return a value here? E.g., from facade?
	 * 
	 * NOTE: This is called after: afterAllCompilationUnits()
	 */
	public void run() {
		RunChildAnalysis runAnalysis = new RunChildAnalysis();
		runAnalysis.setCompilationUnits(compUnits);
		runAnalysis.setInput(savedInput);
		runAnalysis.setReporter(savedReporter);
		
		runAnalysis.run("MOOGREX"); // run the whole stack

		// Retrieve the latest graph from the facade after each run
		// Does extra initialization
		setup.setFacade();
		
		setup.loadGraph();
	}
	
	@Override
	public void runAnalysis(IAnalysisReporter reporter, IAnalysisInput input, ITypeRoot compUnit,
	        CompilationUnit rootNode) {
		this.savedReporter = reporter;
		this.savedInput = input;

		// Save the compilation units to analyze; analyze them later
		this.compUnits.add(compUnit);

		super.runAnalysis(reporter, input, compUnit, rootNode);
	}
}
