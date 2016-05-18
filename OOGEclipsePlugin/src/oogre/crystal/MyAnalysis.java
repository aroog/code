package oogre.crystal;
import java.util.HashSet;
import java.util.Set;

import oogre.adapter.Adapter;
import oogre.adapter.RunChildAnalysis;
import oogre.adapter.WebFacade;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.wayne.ograph.GlobalState;


public class MyAnalysis extends AbstractCompilationUnitAnalysis {
	// XXX. Rename this ... -> OOGWeb?
	public static final String CRYSTAL_NAME = "MyAnalysis";

	private Set<ITypeRoot> compUnits = new HashSet<ITypeRoot>();
	
	private IAnalysisReporter savedReporter;
	private IAnalysisInput savedInput;

	private String projectPath;
	
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
		// XXX. I don't like this here...this is before we start server
		catch(Exception ex) {
			ex.printStackTrace();
		}

		// Give the WebFacade the ability invoke the analysis
		WebFacade facade = WebFacade.getInstance();
		facade.setAnalysis(this);
		
		// TODO: Run the Eclipse side of things
		System.err.println("Running!");
		
		// NOTE: Crystal framework creates separate worker threads to run each Crystal analysis. 
		// So this will not block the main UI thread.
		// Start the server
		Adapter.main(new String[0]);
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
		
		// XXX. This can still crash, e.g., junit.framework.AssertionFailedError: empty d in C::d
		// If bad annotation inserted by OOGRE
		WebFacade facade = WebFacade.getInstance();
		
		// Fresh the state:
		facade.initMotherFacade();
		facade.setPath(projectPath);
		// Load OOGstate.json (to retrieve global state that conctrols which analysis to run)
		facade.loadState();
		
		GlobalState globalState = facade.getGlobalState();
		boolean enableOOGRE = true; // Default
		boolean enablePointsTo = true; // Default

		if(globalState != null ) {
			enableOOGRE = globalState.isRunOOGRE();
			enablePointsTo = globalState.isRunPointsTo();
		}
		
		if(enableOOGRE) { // Is OOGRE enabled?
			runAnalysis.run("MOOGREX"); // run the whole stack
		}
		else if (enablePointsTo) { // Is PointsTo enabled?
			// Run only points-to to set the Graph on the facade
			runAnalysis.run("PointsTo");
		}
		else { 
			// Nothing is enabled...
			// Load the last saved graph from file
			facade.loadGraphFromFile();
			// XXX. Slightly inefficient to ask setFacade() to do remaining initialization
		}
		
		// Retrieve the latest graph from the facade after each run
		// Does extra initialization
		facade.setFacade();
		
		//XXX. Force DisplayGraph generation
		//facade.getSVG();
	}
	
	@Override
	public void runAnalysis(IAnalysisReporter reporter, IAnalysisInput input, ITypeRoot compUnit,
	        CompilationUnit rootNode) {
		this.savedReporter = reporter;
		this.savedInput = input;

		// Save the compilation units to analyze; analyze them later
		this.compUnits.add(compUnit);

		super.runAnalysis(reporter, input, compUnit, rootNode);
		
		// Must initialize the path on the facade
		// In case, we don't run anything!
		// XXX. Also, need root class...(less crucial)
		if (projectPath == null) {
			projectPath = compUnit.getJavaProject().getResource().getLocation().toOSString();
		}
	}
}
