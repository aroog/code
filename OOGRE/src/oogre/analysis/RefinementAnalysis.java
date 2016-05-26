package oogre.analysis;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import oog.common.OGraphFacade;
import oog.re.IRefinement;
import oog.re.RefinementModel;
import oog.re.RefinementState;
import oogre.actions.DebugInfo;
import oogre.actions.SaveAnnotations;
import oogre.annotations.MoreInfoNeededFromUser;
import oogre.annotations.SaveAnnotationsImpl;
import oogre.metrics.MetricsManager;
import oogre.plugin.Activator;
import oogre.refinements.tac.Facade;
import oogre.refinements.tac.Heuristic;
import oogre.refinements.tac.HeuristicOwnedLocalsVisitor;
import oogre.refinements.tac.HeuristicOwnedVisitor;
import oogre.refinements.tac.InferOptions;
import oogre.refinements.tac.InferOwnedHeuristic;
import oogre.refinements.tac.InferPDHeuristic;
import oogre.refinements.tac.InitializationTransferFunctions;
import oogre.refinements.tac.MoreInformationNeededException;
import oogre.refinements.tac.OType;
import oogre.refinements.tac.PushIntoOwned;
import oogre.refinements.tac.PushIntoOwnedTransferFunctions;
import oogre.refinements.tac.PushIntoPD;
import oogre.refinements.tac.RankingStrategy;
import oogre.refinements.tac.Refinement;
import oogre.refinements.tac.RefinementFactory;
import oogre.refinements.tac.RefinementUnsupportedException;
import oogre.refinements.tac.SingletonFacade;
import oogre.refinements.tac.TACMethod;
import oogre.refinements.tac.TM;
import oogre.refinements.tac.TM.TMPhase;
import oogre.refinements.tac.TMKey;
import oogre.refinements.tac.TMSolutionType;
import oogre.utils.Utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.eclipse.EclipseTAC;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.util.Pair;
import edu.wayne.ograph.OGraph;

public class RefinementAnalysis extends AbstractCrystalMethodAnalysis {
	public static final String CRYSTAL_NAME = "OOGRE";
	public static final String LOGGER = RefinementAnalysis.class.getName();
	private TACFlowAnalysis<OOGContext> flowAnalysis;
	private OOGContext context = OOGContext.getInstance();
	private Facade facade = SingletonFacade.getInstance();
	private Logger log;
	private IPath path;
	private boolean isFirstTime = true;

	//To be used in 2nd pass TFs
	CompilationUnitTACs compUnitTACs;
	EclipseTAC methodTAC;
	Set<MethodDeclaration> methodDecls = new HashSet<MethodDeclaration>();
	IAnalysisInput analysisInput = null;

	private static final boolean LOAD_FROM_XML = false;
	
	private static final boolean LACK_INFO_PROCESSING_ENABLED1 = true; // Pass1. Typechecking pass; pick highest ranked.
	private static final boolean LACK_INFO_PROCESSING_ENABLED2 = true; // Pass2. Pick other typings; see if they work.
	private static final boolean LACK_INFO_PROCESSING_ASK_USER = false; // Pass3. Give up and ask the user;
	private static final boolean DEBUGGING = true;
	
	private static final boolean ENFORCE_OVERRIDING = true; // Enforce equality constraints for overriding
	
	//A map that is going to store AUs that should be equated with an AU as the key
	private Map<Variable, Set<Variable>> equalityConstraints = new HashMap<Variable, Set<Variable>>();
	
	// Set of overridden methods
	private Set<Pair<IMethodBinding, IMethodBinding>> overriddenMethods = new HashSet<Pair<IMethodBinding, IMethodBinding>>();
	private Set<Pair<List<IVariableBinding>, List<IVariableBinding>>> overriddenMethodParameters = new HashSet<Pair<List<IVariableBinding>,List<IVariableBinding>>>(); 

	// Error conditions:
	private boolean inError = false;
	private String errorMessage = "";
	
	private TM initialTM = new TM(TM.KEY_INITIAL);
	
	public RefinementAnalysis() {
		log = Logger.getLogger(LOGGER);

	}
	
	@Override
    public String getName() {
	    return CRYSTAL_NAME;
    }

	@Override
	public void analyzeMethod(MethodDeclaration methodDecl) {
		// XXX. This should do nothing, really!
		// XXX. Why do we need to put this in here? Gets called once for every methoddecl
		if(getInput()!=null){
			analysisInput = getInput();
		}		
	}

	/**
	 * Now that we have a reporter, start reporting the errors.
	 * Tie them to the root class.
	 */
	private void reportErrors(MethodDeclaration methodDecl) {
		if (inError) {
			getReporter().reportUserProblem(errorMessage, methodDecl, getName());
			inError = false;
		}
		
    }
	
	/**
	 * @param methodDecl
	 */
	private void doAnalysis(MethodDeclaration methodDecl, PushIntoOwnedTransferFunctions tfs) {
//		if(getInput()!=null){
//			analysisInput = getInput();
//		}
//		else{
			flowAnalysis = new TACFlowAnalysis<OOGContext>(tfs, analysisInput);
			compUnitTACs = analysisInput.getComUnitTACs().unwrap();
			methodTAC = compUnitTACs.getMethodTAC(methodDecl);
			OOGContext endResults = flowAnalysis.getEndResults(methodDecl);
			
			if (DEBUGGING) {
				System.err.println("Processing TM " + tfs.getTM().getKey().toString());
			}
			
			if(DEBUGGING) {
				System.out.println("Result = " + endResults.toString()  + " " + methodDecl.getName());
			}
//		}
		
		// XXX. Do we need to save the TACs for future use? 
//		compUnitTACs = getInput().getComUnitTACs().unwrap();
//		methodTAC = compUnitTACs.getMethodTAC(methodDecl);
//		
//		//populateAUToInstruction(methodTAC,methodDecl);
//		
//		OOGContext endResults = flowAnalysis.getEndResults(methodDecl);
//
//		report(endResults, methodDecl);
	}
	
	private void initDoAnalysis(MethodDeclaration methodDecl, InitializationTransferFunctions tfs) {
		if(getInput()!=null){
			analysisInput = getInput();
		}
		else{
			flowAnalysis = new TACFlowAnalysis<OOGContext>(tfs, analysisInput);
			compUnitTACs = analysisInput.getComUnitTACs().unwrap();
			methodTAC = compUnitTACs.getMethodTAC(methodDecl);
			OOGContext endResults = flowAnalysis.getEndResults(methodDecl);
			
			if (DEBUGGING) {
				System.err.println("Processing TM " + tfs.getTM().getKey().toString());
			}
			
			if(DEBUGGING) {
				System.out.println("Result = " + endResults.toString()  + " " + methodDecl.getName());
			}
		}
	}

	@Override
	public void afterAllMethods(ITypeRoot compUnit, CompilationUnit rootNode) {
		super.afterAllMethods(compUnit, rootNode);
		
	}

	/** 
	 * Sequence of calls:
	 * - beforeAllMethods
	 * - analyzeMethod
	 * - afterAllMethods
	 */
	public void beforeAllMethods(ITypeRoot compUnit, CompilationUnit rootNode) {
		super.beforeAllMethods(compUnit, rootNode);


	}

    // XXX. Clean this up. Stop using Utils.getJavaProject, etc.
	private void _setProjectPath() {
		IJavaProject javaProject = Utils.getJavaProject();
		IProject currentProject = null;
		if (javaProject != null) {
			currentProject = javaProject.getProject();
			if (currentProject != null) {
				path = currentProject.getLocation();
			}
		}
		
		Utils.registerTypeHierarch(javaProject);
		
		// Requires new Crystal
		WorkspaceUtilities.javaProject = javaProject;
	}


	private String getGraphFile() {
		String oGraphPath = null;
		if (path != null) {
			String projectPath = path.toOSString();

			// DONE. Remove hard-coded path. Load relative to current project!
			oGraphPath = projectPath + "\\OOG.xml.gz";
		}
		return oGraphPath;
	}

	private String getHeuristicsFile() {
		String oGraphPath = null;
		if (path != null) {
			String projectPath = path.toOSString();

			// DONE. Remove hard-coded path. Load relative to current project!
			oGraphPath = projectPath + "\\heuristics.xml";
		}
		return oGraphPath;
	}

	
	@Override
	/**
	 * The analysis process is about to begin.
	 * This is called BEFORE beforeAllMethods
	 */
    public void beforeAllCompilationUnits() {
	    super.beforeAllCompilationUnits();

	    // Need to set the path for the Config
	    _setProjectPath();
	    
	    if ( LOAD_FROM_XML ) {
	    	String oGraphPath = getGraphFile();
	    	if (oGraphPath != null ) {
	    		// 	Set it on the Facade; load the OGraph once!
	    		// the first call to facade.getGraph(...) must have the project and path resolved.
	    		// XXX. We are eagerly trying to load a File here
	    		File file = new File(oGraphPath);
	    		if (file.exists()) {
	    			OGraph oGraph = facade.loadFromFile(oGraphPath);
	    			if (oGraph == null) {
	    				inError = true;
	    				errorMessage = "Cannot load OOG.xml.gz. Make sure the file exists and has the right version.";
	    			}
	    		}
	    		// XXX. If file does not exist, report info.
	    		// Should be OK if NOT applying refinements.
	    	}
	    	else {
	    		inError = true;

	    		// XXX. getReporter() will return null here; figure out another way to write to Eclipse Problems Window
	    		errorMessage = "Cannot find project to analyze. Make sure exactly ONE project is open.";
	    		// getReporter().reportUserProblem("Cannot set the path", null, getName());
	    	}
	    }
	    else {
	    	// Load from the OtherFacade
	    	// XXX. Remove cast when add to interface
			OGraph oGraph = facade.loadFromMotherFacade();

			// XXX. Graph may not be set yet.
//			if (oGraph == null) {
//				inError = true;
//				errorMessage = "Cannot load graph from Facade.";
//			}
	    }
	    
		
		//XXX.This needs to be set (select from user)
        //It used to be set from test driver in testing without UI
		context.setSelectedTyping("", "p", "p");
		context.setMainDefaultTyping("shared", "shared");
		
		
	    // Load the config (requires the path to be set)
	    _beforeAllCompilationUnits_loadOptions();
				
		// XXX. Careful: scanForCompilationUnits() gets called multiple times.
		List<ICompilationUnit> allCompilationUnits = WorkspaceUtilities.scanForCompilationUnits();
		// Parse all the units: in Crystal, resolve bindings are set to true.
		// XXX. Do we really need this?! WE don't use the return values!
		// Map<ICompilationUnit, ASTNode> parseCompilationUnits = WorkspaceUtilities.parseCompilationUnits(allCompilationUnits);
		
		parseCompilationUnits = WorkspaceUtilities.parseCompilationUnits(allCompilationUnits);
		
		_beforeAllCompilationUnits_setRootClass(allCompilationUnits);

		
		
		
		// Pre-processing for OOGRE
		if(isFirstTime ){
			populateTM(allCompilationUnits);
			//populateEqualityConstraint(initialTM);
			//Push the initialTM into the worklist
			workingTM = initialTM;
	    }
		
    }
	
	// Set of the most recent TMs
	// We use this to avoid ConcurrentModificationException
	private TM workingTM = null;
	
	boolean isPIOHeuristic = true;
	
	@Override
	/*
	 * Here, we do the top-level analysis:
	 * 
	 * The sequence of calls:
	 * - beforeAllCompilationUnits()
	 * - beforeAllMethods()
	 * - analyzeMethod(...)
	 * -- this is 1stPassTF
	 * - afterAllMethods()
	 * - afterAllCompilationUnits()
	 * 
	 * - call analyzeMethod() but with a different set of TFs
	 * -- this is nthPassTF (where n > 1)
	 * (for the nth refinement, we do the n+1 pass)
	 *  
	 * ISSUES:
	 * - do not try to build a complex data structure from the 1st pass that covers everything
	 * - you will need to do an (n+1)pass to go over things that may need to change
	 * -- call analyzeMethod with DIFFERENT TFs from the 1st pass
	 * 
	 * - CAREFUL: a global singleton is then a bad idea
	 * -- might be better to not use a singleton, but to create a fresh object each time.
	 * -- because you will not be able to distinguish between multiple passes
	 * -- Context' = TF([], Context)
	 * 
	 * TOEBI: Extract methods: this method is unbelievably long
	 * - applyHeuristicsInferOwned
	 * - applyHeuristicsInferPD.
	 * ...
	 * ...
	 * XXX. If these functions are stateless, no need to keep creating them inside a loop?
	 *  pushIntoOwnedTFs = new PushIntoOwnedTransferFunctions();
	 */
	
	public void afterAllCompilationUnits() {
		
		OGraphFacade facade = getFacade();
		RefinementModel facadeRefModel = facade.getRefinementModel();
		
		// Assume things will go wrong
		facade.setInferenceSuccess(false);
		// setFacadeState will update the value
		
		// Store on the facade
		facade.setPath(path.toOSString());
		facade.setRootClass(Config.MAINCLASS);
		
		long startTime = System.currentTimeMillis();
		
		if(this.isFirstTime){
			InitializationTransferFunctions pushIntoOwnedTFs = new InitializationTransferFunctions(initialTM);
			for (MethodDeclaration methodDcl : methodDecls) {
				initDoAnalysis(methodDcl, pushIntoOwnedTFs);
			}
			this.isFirstTime = false;
			setCurrentTM(initialTM);
			populateOverriding();
			populateEqualityConstraint(initialTM);
		}
		
		if(workingTM != null){
			TM copyWorking = workingTM.copyTypeMapping(workingTM.getKey());
			//Infer owned objects using heuristics
			//Move it here to be able to pass separate TM's for the next step (to start from new TM's)
			if((OptionsForHeuristics.enableHeuristicsOwned || OptionsForHeuristics.enableHeuristicsPD) && !context.isHeuristicOwnedRan() && !context.isHeuristicPDRan()){
				if(OptionsForHeuristics.enableHeuristicsOwned && !context.isHeuristicOwnedRan() ) {
					context.setHeuristicOwnedRan(true);
					Set<Variable> encapsulatedAUs = context.getEncapsulatedVars();
					for (Variable var : encapsulatedAUs) {
						boolean heuApplied =_afterAllCompilationUnitsHeuristics(var);
						if(heuApplied){
							// XXX. Split up _afterAllCompilationUnitsPostProcessing to not run Phase II here.
							// Run Phase II after all the heuristics, not after each one.
							boolean success = _afterAllCompilationUnitsPostProcessing();
							if (success) {
								// IF any of the 5 solutions of the heuristic is completed, then the state of the heuristic
								// is completed
								lastHeuristic.setState(RefinementState.Completed);
								setFacadeState(RefinementState.Completed);
								// record the completed heuristic
								context.addCompletedHeuristic(lastHeuristic);

								// Remove the encapsulated AU from the list of target AUs for PIP heuristic
								context.removeLogicalPartVariable(var);
							}
							else {
								lastHeuristic.setState(RefinementState.Unsupported);
								setFacadeState(RefinementState.Unsupported);

								// If the operation is unsupported then use add the last valid TM to the list of TMs that should be saved
								setCurrentTM(copyWorking);
							}
						}
						else {
							lastHeuristic.setState(RefinementState.Unsupported);
							setFacadeState(RefinementState.Unsupported);

							// If the operation is unsupported then use add the last valid TM to the list of TMs that should be saved
							setCurrentTM(copyWorking);
						}
						
						if (lastHeuristic != null ) {
							// Keep track of the heuristic being generated to display
							// It should get stored only once for each instance of heuristic
							facadeRefModel.addHeuristic(RefinementFactory.createFacadeHeuristic(lastHeuristic));
						}						
					}
				}
				if (OptionsForHeuristics.enableHeuristicsPD && !context.isHeuristicPDRan()) {
					isPIOHeuristic = false;
					context.setHeuristicPDRan(true);
					// XXX. HACK: to turnOFFowned = false, gotta change back this code
					Set<Variable> logicalPartVars = null;
					if (InferOptions.getInstance().turnOFFowned()) {
						logicalPartVars = context.getEncapsulatedVars();
					} else {
						logicalPartVars = context.getLogicalPartVars();
					}
					for (Variable var : logicalPartVars) {
						boolean heuApplied = _afterAllCompilationUnitsHeuristics(var);
						if(heuApplied){
							boolean success = _afterAllCompilationUnitsPostProcessing();
							if (success) {
								// IF any of the 5 solutions of the heuristic is completed, then the state of the heuristic
								// is completed
								lastHeuristic.setState(RefinementState.Completed);
								setFacadeState(RefinementState.Completed);
								// record the completed heuristic
								context.addCompletedHeuristic(lastHeuristic);
							}
							else {
								lastHeuristic.setState(RefinementState.Unsupported);
								setFacadeState(RefinementState.Unsupported);

								// If the operation is unsupported then use add the last valid TM to the list of TMs that should be saved
								setCurrentTM(copyWorking);
							}
						}
						else {
							lastHeuristic.setState(RefinementState.Unsupported);
							setFacadeState(RefinementState.Unsupported);
						}

						if (lastHeuristic != null ) {
							// Keep track of the heuristic being generated to display
							// It should get stored only once for each instance of heuristic
							facadeRefModel.addHeuristic(RefinementFactory.createFacadeHeuristic(lastHeuristic));
						}
					}
				}
				// Right before saving, need to run Phase I and II
				// Call the method after the last heuristic
				tmppPhases();
			}
			else{
				Refinement refinement = _afterAllCompilationUnitsRefinements();
				if(refinement!=null){
					boolean success = _afterAllCompilationUnitsPostProcessing();
					if(success) {
						if(refinement!= null) {
							refinement.setState(RefinementState.Completed);
							setFacadeState(RefinementState.Completed);
							//record the completed refinement
							context.addCompletedRefinement(refinement);
						}
					}
					else {
						// If the operation is unsupported then use add the last valid TM to the list of TMs that should be saved
						savedTMs.add(copyWorking);
						if(refinement!=null){
							refinement.setState(RefinementState.Unsupported);
							setFacadeState(RefinementState.Unsupported);
						}
					}
					// Right before saving, need to run Phase I and II
					// Call the method after each refinement.
					tmppPhases();
				}
				else{
					if(lastRefinement!=null){
						lastRefinement.setState(RefinementState.Unsupported);
						setFacadeState(RefinementState.Unsupported);
						setCurrentTM(copyWorking);
					}
				}
			}
		}
		long stopTime = System.currentTimeMillis();
		long msTime = stopTime - startTime;
		System.out.println("VayVayVay: Elapsed time was " + msTime + " ms (" + msTime/60000.0 + " min).");
		
		StringBuilder builder = new StringBuilder();
		TM tm = getTMToSave(builder);
		// Save the annotations, AFTER each refinement
		if (Config.SAVE_ANNOTATIONS && tm != null) {
			
			System.err.println("Saving TM " + tm.getKey().toString());
			
			// Save the annotations based on the post-processed TM
			SaveAnnotationsImpl strategy = new SaveAnnotationsImpl(tm);
			SaveAnnotations saveCmd = new SaveAnnotations(strategy);
			saveCmd.run(path.toOSString(), lastRefinement);
			saveCmd.finish();

			if (lastHeuristic != null ) {
				lastHeuristic.setData(builder.toString());
				lastHeuristic = null;
			}

			if (lastRefinement != null ) {
				lastRefinement.setData(builder.toString());
				lastRefinement = null;
			}
		}
		DebugInfo debugCmd = new DebugInfo(path.toOSString(), allHeuristics, allRefinements);
		debugCmd.finish();

		// ISSUE#1. The second pass transfer functions must be called after each refinement;
		// - See comment in TODO_TAC.txt about needing a top-level worklist
		// - The refinement queue is NOT the same as a worklist
		// -- One refinement can lead to many iterations until a fixed point is reached
		
		// ISSUE#2. What does it mean to store 1 MethodDecl and then call the TFs on that decl alone.
		// - analyzeMethod gets called on EVERY SINGLE MethodDecl in the program, not the last one.
		// - Gotta reanalyze the whole program
		// - Store the list of MethodDeclarations when traversing the graph
		// - Or use some helpers on WorkspaceUtilities (not sure)
	}
	
	/**
	 * Set the status on the facade:
	 * 
	 * @param state
	 */
	private void setFacadeState(RefinementState state) {
		OGraphFacade facade = getFacade();

		switch (state) {
		case Completed:
			facade.setInferenceSuccess(true);
			break;
		case Unsupported:
			facade.setInferenceSuccess(false);
			break;
		}
	}

	// Set of TMs that can be saved
	// They are the result of Phase I or Phase II
	// They are all type check
	private Set<TM> savedTMs = new HashSet<TM>();
	
	// OOGRE Heuristics (NON-facade)
	private List<Heuristic> allHeuristics = new ArrayList<Heuristic>();
	// OOGRE Refs (NON-facade)
	private List<Refinement> allRefinements = new ArrayList<Refinement>();
	
	// XXX. Pick which TM to save.
	// XXX. Just make we don't save the default more than once...
	private TM getTMToSave(StringBuilder writer) {
		TM retTM = null;
		
		// Use metrics/heuristics to pick between TMs
		final MetricsManager mm = new MetricsManager(writer);
		retTM = mm.getTMToSave(savedTMs);

		// Pick the first one
		//		for(TM tm : savedTMs ) {
		//			retTM = tm;
		//			break;
		//		}
		
		// If nothing, save the initial TM!
		// But NOT if we have done some refinements already
		// Otherwise, we may be overwriting everything!
		if (retTM == null && !context.isRefined()) {
			retTM = initialTM;
		}

		// Print diagnostics info
		System.err.println("SaveAnnotations: there are " + savedTMs.size() + " possible TMs to save");

		return retTM;
	}
	
	/**
	 * Runs the heuristics on the target AU
	 * changes global worklist that contains all the valid TMs and a local copy (allNewTMs)
	 * @param targetVar
	 */
	private boolean _afterAllCompilationUnitsHeuristics(Variable targetVar) {
		Heuristic heuristic = null;
		
		OGraphFacade facade = getFacade();

		facade.loadHeuristicsModel(getHeuristicsFile());
		// HeuristicsModel heuristicsModel = facade.getHeuristicsModel();
		
		// Collect all the new TMs so we can apply equality constraints
		
		//Use the heuristics to infer objects in owned or PD
		if(targetVar!=null ){

			// Infer owned objects using heuristics
			if(isPIOHeuristic){
				heuristic = new InferOwnedHeuristic(targetVar);
			}
			// Infer objects in PD using heuristics
			else{
				heuristic = new InferPDHeuristic(targetVar);
			}
			
			lastHeuristic = heuristic;
			
			allHeuristics.add(heuristic);

			TM aTM = currentTM;
			Set<TM> notDiscardedTMs = new HashSet<TM>();
			Set<TM> newTMs = inferHeuristic(aTM, heuristic);
			for(TM newTM : newTMs ) {
				if (newTM.isDiscarded) {
					continue;
				}
				else {
					notDiscardedTMs.add(newTM);
				}
			}

			//Pick the preferred ranked TM
			TM pickedTM = pickPreferredTM(notDiscardedTMs);

			// If all the five solutions of the heuristic are unsupported, the the state of the heuristic is unsupported
			// If there is no TM to pick the refinement is unsupported
			if(pickedTM == null){
				// XXX. This is hackish code. Fix it. Are you sure there's going to be one?!
				//If there is no new TM to pick, then set that last valid one.
				while(notDiscardedTMs.iterator().hasNext()){
					setCurrentTM(notDiscardedTMs.iterator().next());
					break;
				}
				return false;
			}
			else{
				context.setRefined(true);
				
				setCurrentTM(pickedTM);
			}
		}
		
		return true;
	}
	
	/**
	 * Runs a refinement
	 * changes global worklist that contains all the valid TMs and a local copy (allNewTMs)
	 * @param targetAU
	 */
	private Refinement _afterAllCompilationUnitsRefinements() {
		//Call to get the list of refinements provided by user
		//Can be pushIntoOwned or PushIntoPD refinements (what about pushIntoPR?)
		//If its pushIntoOwned, its set of TFs are run, the same style as for result of heuristics, worklist algorithm, fixed point analysis
		//If its PushIntoPD, its set of TFs (PushIntoPDTransferFunctions) are run
		//Apply refinements
		
		Queue<Refinement> refQueue = new LinkedList<Refinement>();
		refQueue = getRefinements(currentTM);
		
		while(!refQueue.isEmpty()){
			
			Refinement refinement = refQueue.poll();
			lastRefinement = refinement;
			
			// Remember it in the field
			allRefinements.add(lastRefinement);

			TM aTM = currentTM;
			Set<TM> notDiscardedTMs = new HashSet<TM>();
			Set<TM> newTMs = applyRefinement(refinement, aTM);

			// No exceptions, so set the currentTM
			for(TM newTM : newTMs ) {
				if (newTM.isDiscarded) {
					continue;
				}
				else {
					notDiscardedTMs.add(newTM);
				}
			}

			// Mark refinement as completed...or Unsupported
			// TOEBI: Double-check that's the only place where it needs to happen?

			// Move the logic to the type-checking phase
			// catch(MoreInformationNeededException mie) {
			//		refinement.setState(RefinementState.MoreInfoNeeded);
			//		refinement.setMoreInformation(aTM, mie.getAuSet());
			//		// Do not continue to save annotations
			//		return;
			//	}			
			
			//Pick the preferred ranked TM
			TM pickedTM = pickPreferredTM(notDiscardedTMs);

			// If there is no TM to pick the refinement is unsupported
			if(pickedTM==null){
				//If there is no new TM to pick, then set that last valid one.
				// XXX. This is hackish code. Fix it. Are you sure there's going to be one?! Always?
				while(notDiscardedTMs.iterator().hasNext()){
					setCurrentTM(notDiscardedTMs.iterator().next());
					break;
				}
				return null;
			}
			else{
				context.setRefined(true);
				
				// Add the current TM to this.tmSet
				setCurrentTM(pickedTM);
			}

			// Keep track of the heuristic being generated to display
			// It should get stored only once for each instance of heuristic
			//facadeRefModel.add(refinement);
		}
		
		return lastRefinement;
	}
	
	/**
	 * Runs post processing steps on each refinement or heuristic
	 * - Equality constraints
	 * - Remove owned
	 * 
	 * changes global worklist that contains all the valid TMs and a local copy (allNewTMs)
	 */
	private boolean _afterAllCompilationUnitsPostProcessing() {
		// After this point, tmSet no longer changes. We just update the existing TMs in the set.
		
		boolean isChanged = true; // flag to track fixed point of running TFs

		// If we are still stuck with the initial TM, no need to post-process!
		// XXX. Maybe compare keys?
		if (currentTM == initialTM) {
			return false;
		}
		
		// Apply equality constraints from ClsOK
		if(equalityConstraints.size()>0 && context.isRefined()){
			// XXX. Iterating over all the TM, not just the current one
			TM newTM = checkEqualityConstraints(currentTM);
			// No exceptions, so set the currentTM
			if(!newTM.isDiscarded){
				setCurrentTM(newTM);
			}
			else{
				//remove the discarded ones form this.tmSet
				//NOTE: really: removing oldTM (because removeFromCurrentTMSet checks the keys only)
				return false;
			}
		}
		
		// Remove owned form AUs with public visibility modifiers
		if(context.isRefined()){
			TM newTM = removePublicOwned(currentTM);
			// No exceptions, so set the currentTM
			if(!newTM.isDiscarded){
				setCurrentTM(newTM);
			}
		}

		return true;
	}
	
	private boolean isPostProcessingNeeded(TM copyTM) {
		boolean result = false;
		for (Entry<Variable, Set<OType>> entry : copyTM.entrySet()) {
			// XXX. Should not check ANY AU. Only a few ones...But which ones?
			if (entry.getValue().size() > 1) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Performs:
	 * - Typechecking (old Phase1)
	 * - Extracting typings from Set Mappings (Phase2) 
	 */
	private boolean tmppPhases(){
		// Clear the list
		savedTMs.clear();

		if(LACK_INFO_PROCESSING_ENABLED1 && context.isRefined()) {
			TM oldTM2 = currentTM;
			
			TM newTM = oldTM2.copyTypeMapping(oldTM2.getKey());
			TM typecheckedTM = null;
			if(isPostProcessingNeeded(newTM)){
				typecheckedTM = typecheck(newTM);
			}
			// typecheck does not touch isDiscarded so we can safely set newTM without checking its isDiscarded
			// NOTE: TMs where moreInfoNeeded = true still get saved, as they may get shown to the user

			if(!newTM.moreInfoNeeded){
				savedTMs.add(typecheckedTM);
			}

			setCurrentTM(newTM);
		}

		
		if(LACK_INFO_PROCESSING_ENABLED2 && context.isRefined()) {
			TM newTM = currentTM;
			
			if(newTM.moreInfoNeeded){
				//newTM = resolveMoreIntoNeeded(newTM);
				// NOT using the return value
				resolveMoreIntoNeeded2(newTM);
			}
		}
		
		// Ask developers if Phase II did not find any TM to save 
		if(LACK_INFO_PROCESSING_ASK_USER && context.isRefined()) {
			TM newTM = currentTM;
			if(newTM.moreInfoNeeded){
				newTM = askUser(newTM);
			}
		}
		
		return true;
	}
	
	private TM removePublicOwned(TM tm) {
		boolean isChanged  = true;
		TM newTM = tm.copyTypeMapping(tm.getKey());
		
		for (Entry<Variable, Set<OType>> entry : newTM.entrySet()) {
			Variable au = entry.getKey();
			Set<OType> typingSet = entry.getValue();
			// XXX. TOEBI: Switch on enum
			if(au instanceof SourceVariable){
				SourceVariable srcVariable = (SourceVariable)au;
				if(srcVariable.getBinding().isField()){
					Set<Variable> encapsulatedAUs = context.getEncapsulatedVars();
					if(encapsulatedAUs!=null && !encapsulatedAUs.contains(au) && !srcVariable.getBinding().getDeclaringClass().getQualifiedName().equals(Config.MAINCLASS)){
						// Getting a copy
						Set<OType> auTypeMappings = newTM.getTypeMapping(au);
						Iterator<OType> it = auTypeMappings.iterator();
						while(it.hasNext()) {
							OType oType = it.next();
							if(oType.getOwner().contains("owned")){
								if(lastRefinement == null){
									it.remove();
								}
								else{
									if(!lastRefinement.getVars().contains(au)){
										it.remove();
									}
								}
							}
						}
						// XXX. If owned<X> is the only typing, should not be removed.
						// Why not? It's a bad TM. Reject it!
						// It is not a bad TM. It is an AU that has only on typing in the set and that is owned.
						// We should not remove that only typing. It maked a valid refinement unsupported.
						newTM.putTypeMapping(au, auTypeMappings);
					}
				}
				else if(srcVariable.getBinding().isParameter()){
					if(Modifier.isPublic(srcVariable.getBinding().getDeclaringMethod().getModifiers())){
						Set<OType> auTypeMappings = newTM.getTypeMapping(au);
						Iterator<OType> it = auTypeMappings.iterator();
						while(it.hasNext()) {
							OType oType = it.next();
							if(oType.getOwner().contains("owned")){
								it.remove();
							}
						}
						// XXX. If owned<X> is the only typing, should not be removed.
						// Why not? It's a bad TM. Reject it!	
						// It is not a bad TM. It is an AU that has only on typing in the set and that is owned.
						// We should not remove that only typing. It maked a valid refinement unsupported.
						newTM.putTypeMapping(au, auTypeMappings);
					}
				}
			}
			else if(au instanceof TACMethod){
				TACMethod srcVariable = (TACMethod)au;
				if(Modifier.isPublic(srcVariable.getMethDecl().getModifiers())){
					Set<OType> auTypeMappings = newTM.getTypeMapping(au);
					Iterator<OType> it = auTypeMappings.iterator();
					while(it.hasNext()) {
						OType oType = it.next();
						if(oType.getOwner().contains("owned")){
							it.remove();
						}
					}
					// XXX. If owned<X> is the only typing, should not be removed.
					// Why not? It's a bad TM. Reject it!
					// It is not a bad TM. It is an AU that has only on typing in the set and that is owned.
					// We should not remove that only typing. It maked a valid refinement unsupported.
					newTM.putTypeMapping(au, auTypeMappings);
				}
			}
		}

		while(isChanged){
			// Take a snapshot of newTM before running the TFs 
			TM oldTM = newTM.copyTypeMapping(newTM.getKey());

			// Run the TFs on the temporary newTM
			PushIntoOwnedTransferFunctions pushIntoOwnedTFs = new PushIntoOwnedTransferFunctions(this, newTM, null);
			for (MethodDeclaration methodDcl : methodDecls) {
				doAnalysis(methodDcl, pushIntoOwnedTFs);
				if(newTM.isDiscarded){
					break;
				}
			}

			if(!newTM.isDiscarded){
				if(TM.isEqual(oldTM,newTM)){
					isChanged=false;
				}
			}
			else{
				break;
			}
		}

		return newTM;
	}

	/**
	 * Simply prompt the user.
	 * 
	 * XXX. This strategy is different;
	 * - does not require making copy of the TM
	 * - does not re-run the TFs
	 * 
	 * XXX. Right now, not doing anything other than showing the user the info; 
	 * Does not take into account user's choices.

	 */
	private TM askUser(TM aTM) {
		
		// Discard work of previous phase. Start anew.
		TM newTM = aTM.copyTypeMapping(aTM.getKey());

		MoreInfoNeededFromUser tmpp = new MoreInfoNeededFromUser();

		try {
			tmpp.promptUser(newTM);
		}
		catch(MoreInformationNeededException mie) {
			if (lastRefinement != null) {
				lastRefinement.setState(RefinementState.MoreInfoNeeded);
				lastRefinement.setMoreInformation(newTM, mie.getAuSet());
			}
		}
		catch(RefinementUnsupportedException rus) {
			// Discard this TM; try with another set of typings
			newTM.isDiscarded = true;
		}
		return newTM;
	}
	
	// NOTE: Not currently using the return value;
	// IF we were to prompt the user, maybe we need to get it.
	private TM resolveMoreIntoNeeded2(TM aTM) {
		
		// Discard work of previous phase. Start anew.
		TM newTM = aTM.copyTypeMapping(aTM.getKey());

		// Gotta use ranking to pick the highest ranked typings 
		final RankingStrategy ranking = RankingStrategy.getInstance();
		boolean done = false;
		for (Entry<Variable, Set<OType>> entry : newTM.entrySet()) {
			Variable suspiciousAU = entry.getKey();
			if(!(suspiciousAU instanceof TempVariable)){
			Set<OType> setOTypes = entry.getValue();
			// Use TreeSet which allows some ordering...
			// To traverse things in some order
			// XXX. Define ordering that uses the ranking strategy...
			SortedSet<OType> worklist = new TreeSet<OType>(new Comparator<OType>() {
				
				@Override
				public int compare(OType o1, OType o2) {
					return ranking.compareBetween(o1, o2);
				}
			});
			worklist.addAll(setOTypes);

			// XXX. Note: selecting a type without regard to ranking
			// NEXT: try higher ranked types first
			while(!worklist.isEmpty()) {
				// Pick the highest ranked typings if that is not already tried,
				// otherwise pick the next highest ranked one
				OType selectedType = worklist.first();
				worklist.remove(selectedType);

				done = false;
				Set<OType> newSetOTypes = new HashSet<OType>();
				newSetOTypes.add(selectedType);
				newTM.putTypeMapping(suspiciousAU, newSetOTypes);

				// 1. Run TFs;
				newTM.discardPhase = TMPhase.FindNextTM;
				TM outTM = runTFs(newTM);
				if(!outTM.isDiscarded) {
					TM aTyping = typecheck(outTM); 
					if(!aTyping.isDiscarded){
						savedTMs.add(aTyping);
					}
				}
			}
		}
	}

		return newTM;
	}


	/**
	 * Typecheck:
	 * - if this steps, we do not set isDiscarded to true
	 * - we only set moreInfoNeeded
	 * 
	 * XXX. This comment seems obsolete...
	 * 
	 * Just changes moreInfoNeeded on newTM
	 */
	private TM typecheck(TM newTM) {
		if(!newTM.isInitial()) {
			//			// Hold off on saving annotations.
			//			saveAnnotations = false;

			TM oldTM = null;
			// Pick some typings and changes the newTM!
			// XXX. pickHighestRanked can throw exception!
			TM aNewTM = newTM.pickHighestRanked(RankingStrategy.getInstance());
			if(aNewTM!=null){
				newTM.moreInfoNeeded = false;

				boolean isChanged = true; // flag to track fixed point of running TFs
				while(isChanged) 
				{
					// Take a snapshot of newTM before running the TFs
					oldTM = aNewTM.copyTypeMapping(aNewTM.getKey());

					// Re-run the TFs on the newTM
					PushIntoOwnedTransferFunctions pushIntoOwnedTFs = new PushIntoOwnedTransferFunctions(this, aNewTM, null);
					for (MethodDeclaration methodDcl : methodDecls) {
						doAnalysis(methodDcl, pushIntoOwnedTFs);
						if(aNewTM.isDiscarded){
							newTM.moreInfoNeeded = true;
							break;
						}
					}

					// NOTE: do NOT set the modified TM back on the context.

					// Did the TFs change TM? If no, no need to keep running TFs
					if(!aNewTM.isDiscarded){
						isChanged = !TM.isEqual(oldTM, aNewTM);
					}
					else{
						break;
					}
				}
				// DO we want to save this newTM (after TMPP) to the field?
				// I think yes since TMPP actually changes the typing set of some AUs
				// We did not want to save it for old TMPP since it was only picking one of the typings from the set
				// No exceptions, so set the currentTM
				// No need to set if the TM is type checked
				//setCurrentTM(newTM);
			}
			return aNewTM;
		}
		return null;
	}
	
	private TM checkEqualityConstraints(TM tm) {
		boolean isChanged  = true;
		TM newTM = tm.copyTypeMapping(tm.getKey());
		
		TM oldTM = null;
		// XXX. Have to completely change the overriding (equality constraints)
		for (Entry<Variable, Set<Variable>> eqConst : equalityConstraints.entrySet()) {
			Variable keyAU = eqConst.getKey();
			Set<OType> keySet = newTM.getTypeMapping(keyAU);
			for (Variable valueAU : eqConst.getValue()) {
				Set<OType> valueSet = newTM.getTypeMapping(valueAU);
				keySet.retainAll(valueSet);
				newTM.putTypeMapping(valueAU, keySet);
				newTM.putTypeMapping(keyAU, keySet);
			}
		}
		while(isChanged){
			oldTM = newTM.copyTypeMapping(newTM.getKey());
			PushIntoOwnedTransferFunctions pushIntoOwnedTFs = new PushIntoOwnedTransferFunctions(this, newTM, null);
			for (MethodDeclaration methodDcl : methodDecls) {
				doAnalysis(methodDcl, pushIntoOwnedTFs);
				if(newTM.isDiscarded){
					break;
				}
			}
			if(!newTM.isDiscarded){
				isChanged = !TM.isEqual(oldTM, newTM);
			}
			else{
				break;
			}
		}

		return newTM;
	}

	//A counter that is going to be used in the TMKey
	//Starting from 1, 0 is for the initial TM
	static int counter = 1;
	
	private Set<TM> applyRefinement(Refinement refinement, TM tm0 ) {
		
		Set<TM> newTMs = new HashSet<TM>();
		
		boolean isApplied = false;
		
		// Iterate through enums
		for(TMSolutionType sol : TMSolutionType.values() ) {
			if ( sol == TMSolutionType.INITIAL) {
				continue;
			}
			// XXX. Create the right key
			TMKey newTMKey = new TMKey(sol, counter);
			// XXX. we lose the key of the previous tm: tm.getKey()
			// Copy the initial values from the initialTM
			TM newTM = initializeTM(newTMKey);
			//TM newTM = tm0.copyTypeMapping(newTMKey);
			//TM newTM = new TM(TM.KEY_INITIAL);
			// Save it.
			//No need to set in here...It is going to get set in the caller if it is not a discarded TM
			//setCurrentTM(newTM);
			
			// XXX. Gotta remember what you are trying;
			// XXX. Also store it in the Refinement? Or just in one place (set of TMs) is enough.
			
			// XXX. Gotta try/catch. Refine may throw exception. No need to continue!
			isApplied = refinement.refine(newTM, sol );
			// isApplied will be false if newTM.isDiscarded
			if(isApplied){
				newTMs.add(newTM);
				counter++;
				if(refinement.getDomainName().equals("shared")){
					break;
				}
			}
		}
		
		for(TM newTM : newTMs) {
			context.clearNPDModifiers();
			boolean isChanged = true;
			while(isChanged)
			{
				// Take a snapshot of newTM before running the TFs 
				TM oldTM = newTM.copyTypeMapping(newTM.getKey());

				// Run the TFs on the temporary newTM
				PushIntoOwnedTransferFunctions pushIntoOwnedTFs = new PushIntoOwnedTransferFunctions(this, newTM, refinement);
				for (MethodDeclaration methodDcl : methodDecls) {
					doAnalysis(methodDcl, pushIntoOwnedTFs);
					if(newTM.isDiscarded){
						break;
					}
				}

				if(!newTM.isDiscarded){
					if(TM.isEqual(oldTM,newTM)){
						isChanged=false;
					}
				}
				else{
					break;
				}
			}
		}

		return newTMs;
	}

	/*
	 * Gotta call setCurrentTM(newTM) on the return of this method.
	 * 
	 */
	private Set<TM> inferHeuristic(TM tm, Heuristic heuristic) {
		boolean isChanged;
		boolean isApplied = false;
		
		Set<TM> newTMs = new HashSet<TM>();

		// Iterate through enums
		for(TMSolutionType sol : TMSolutionType.values() ) {
			if ( sol == TMSolutionType.INITIAL) {
				continue;
			}
			// XXX. Create the right key
			TMKey newTMKey = new TMKey(sol, counter);
			// Copy the initial values from the initialTM
			TM newTM = tm.copyTypeMapping(newTMKey);
			// Save it.
			//No need to set in here...It is going to get set in the caller if it is not a discarded TM
			//setCurrentTM(newTM);
			// XXX. Gotta remember what you are trying;
			// XXX. Also store it in the Refinement? Or just in one place (set of TMs) is enough.
			isApplied = heuristic.apply(newTM, sol );
			
			if(isApplied){
				newTMs.add(newTM);
				
				counter++;
			}
		}

		for(TM newTM : newTMs) {
			isChanged = true;
			// Really, this is the type of the AU being modified; we need a fully qualified name since the types in the heuristics file are.

			while(isChanged){
				// Take a snapshot of newTM before running the TFs 
				TM oldTM = newTM.copyTypeMapping(newTM.getKey());

				// Run the TFs on the temporary newTM
				PushIntoOwnedTransferFunctions pushIntoOwnedTFs = new PushIntoOwnedTransferFunctions(this, newTM, heuristic);
				for (MethodDeclaration methodDcl : methodDecls) {
					doAnalysis(methodDcl, pushIntoOwnedTFs);
					if(newTM.isDiscarded){
						break;
					}
				}

				if(!newTM.isDiscarded){
					if(TM.isEqual(oldTM,newTM)){
						isChanged=false;
					}
				}
				else{
					break;
				}
			}

			// Mark refinement as completed...or Unsupported
			// TOEBI: Double-check that's the only place where it needs to happen?
			// heuristic.setState(RefinementState.Completed);

		}
		return newTMs;
	}
	
	private void _beforeAllCompilationUnits_loadOptions() {
		if (path != null) {
			Config.loadConfig(path);
		}
	}
	
	private void _beforeAllCompilationUnits_setRootClass(List<ICompilationUnit> allCompilationUnits) {
		// XXX. getReporter() still null here
		
		IType mainType = null;
		
		for (ICompilationUnit compUnit : allCompilationUnits) {
			IType[] compUnitTypes = null;

			try {
				compUnitTypes = compUnit.getTypes();
			}
			catch (JavaModelException e) {
				e.printStackTrace();
			}
			for (IType iType : compUnitTypes) {
				String typeName = iType.getFullyQualifiedName();
				// XXX. MainClass not fully qualified?
				if (typeName.endsWith(Config.MAINCLASS)) {
					mainType = iType;
					break;
				}
			}
		}
		
		if (mainType == null) {
			// XXX. Use getReporter(); but here, still null. So using System.err
			// getReporter().reportUserProblem("Cannot find main type. Specify main type in properties file", null,
			// getName());
			System.err.println("Cannot find main type. Specify main type in properties file");
		}
	}
	
	private void reset() {
		path = null;
		log = null;
	}


	/**
	 * What we're trying to do:
	 * - traverse all CompilationUnits
	 * - build some mappings
	 */
	private void populateTM(List<ICompilationUnit> allCompUnits){
		// XXX. getReporter() still null here
		//PrintStream output = getReporter().userOut();
		PrintStream output = System.err;
		
		for (ICompilationUnit compUnit : allCompUnits) {
			ASTNode node = parseCompilationUnits.get(compUnit);
			if ((node != null) && (node instanceof CompilationUnit)) {
				analyzeCompilationUnit((CompilationUnit) node, compUnit);
			}
			else {
				output.println("AbstractCompilationUnitAnalysis: Could not retrieve the ASTNode for CompilationUnit "
				        + compUnit.getElementName());
			}
		}
		// Create pair of overridden and overriding methods
	}
	
	private void analyzeCompilationUnit(CompilationUnit unit, ICompilationUnit compilationUnit) {

		List types = unit.types();
		for (Iterator iter = types.iterator(); iter.hasNext();) {

			Object next = iter.next();
			if (next instanceof TypeDeclaration) {
				TypeDeclaration declaration = (TypeDeclaration) next;
				populateMethodDeclarations(declaration);
				visitLocals(declaration);
			}
		}
	}

	private void populateOverriding() {
		for (MethodDeclaration methDecl2 : this.methodDecls) {
			IMethodBinding methodBinding2 = methDecl2.resolveBinding();
			MethodDeclaration overriddenMethodDeclaration = null;
			IMethodBinding overriddenMethod = Utils.getOverriddenMethod(methDecl2.resolveBinding());
			if(overriddenMethod != null ) {
				overriddenMethods.add(new Pair<IMethodBinding, IMethodBinding>(methodBinding2, overriddenMethod));
				for (MethodDeclaration methDecl : this.methodDecls) {
					if(methDecl.resolveBinding().getKey().equals(overriddenMethod.getKey())){
						overriddenMethodDeclaration = methDecl;
					}
				}
				List<SingleVariableDeclaration> parametersList1 = methDecl2.parameters();
				if(overriddenMethodDeclaration!=null){
					List<SingleVariableDeclaration> parametersList2 = overriddenMethodDeclaration.parameters();
					if(parametersList1.size()>0 && parametersList2.size()>0 && parametersList1.size()==parametersList2.size()){
						List<IVariableBinding> paramterBindingList1 = new ArrayList<IVariableBinding>();
						for (SingleVariableDeclaration singleVariableDeclaration : parametersList1) {
							IVariableBinding parameterBinding = singleVariableDeclaration.resolveBinding();
							paramterBindingList1.add(parameterBinding);
						}
						List<IVariableBinding> paramterBindingList2 = new ArrayList<IVariableBinding>();
						for (SingleVariableDeclaration singleVariableDeclaration : parametersList2) {
							IVariableBinding parameterBinding = singleVariableDeclaration.resolveBinding();
							paramterBindingList2.add(parameterBinding);
						}
						this.overriddenMethodParameters.add(new Pair<List<IVariableBinding>, List<IVariableBinding>>(paramterBindingList1,paramterBindingList2));
					}
				}
			}
		}

	}

	private void visitLocals(TypeDeclaration declaration) {
		MethodDeclaration[] methods = declaration.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			methodDeclaration.accept(new HeuristicOwnedLocalsVisitor());
		}
	}

	private void populateMethodDeclarations(TypeDeclaration declaration){
		
		TypeDeclaration[] nestedTypes = declaration.getTypes();
		for (int i = 0; i < nestedTypes.length; i++) {
			TypeDeclaration nestedType = nestedTypes[i];
			populateMethodDeclarations(nestedType);
		}
		
		FieldDeclaration[] fields = declaration.getFields();
		for (FieldDeclaration fieldDeclaration : fields) {
			fieldDeclaration.accept(new HeuristicOwnedVisitor());
		}
		
		MethodDeclaration[] methods = declaration.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			methodDeclaration.accept(new HeuristicOwnedVisitor());
			methodDeclaration.accept(new HeuristicOwnedLocalsVisitor());
			this.methodDecls.add(methodDeclaration);
		}
	}
	
	// Initialize a newTM for the next refinement, based on baseTM and the previous refinements
	// DONE. What about solutionIndex?
	private TM initializeTM(TMKey newTMKey) {
		
		// Copy from the base;
		TM tm = initialTM.copyTypeMapping(newTMKey);
		
		// XXX. XXX. Do not reuse the same variable; pick a fresh variable name.
		changeBackRefinementAU(tm);
		//changeBackHeuristicAU(tm);
		return tm;
	}
	
	public void changeBackRefinementAU(TM tm){
		List<Refinement> completedRefinements = context.getCompletedRefinements();
		for (Refinement refinement : completedRefinements) {
			for (Variable var : refinement.getVars() ) {
				Set<OType> setOTypes = tm.getTypeMapping(var);
				if(setOTypes!=null){
					Set<OType> variableSet = refinement.getVariableSet(var);
					if(refinement instanceof PushIntoOwned){
						for (OType oType : setOTypes) {
							if(oType.getOwner().equals("this.owned")){
								variableSet.add(oType);
							}
						}
					}
					else if(refinement instanceof PushIntoPD){
						for (OType oType : setOTypes) {
							if(oType.getOwner().equals("this.PD")){
								variableSet.add(oType);
							}
						}
					}
					tm.putTypeMapping(var, variableSet);
//					Iterator<OType> iterator = setOTypes.asReadOnlySet().iterator();
//					if(iterator.hasNext()){
//						OType next = iterator.next();
//						// HACK:
//						OType newType = new OType("this." + refinement.getDomainName(),next.getAlpha(), next.getInner());
//						Set<OType> newSet = new HashSet<OType>();
//						newSet.add(newType);
//						tm.updateVariableTypingSet(au, newSet);
//					}
				}
			}
		}
	}
	
	public void changeBackHeuristicAU(TM tm){
		List<Heuristic> completedHeuristics = context.getCompletedHeuristics();
		for (Heuristic heuristic : completedHeuristics) {
			for (Variable var : heuristic.getVars() ) {
				Set<OType> setOTypes = tm.getTypeMapping(var);
				if(setOTypes!=null){
					Iterator<OType> iterator = setOTypes.iterator();
					if(iterator.hasNext()){
						OType next = iterator.next();
						// HACK:
						OType newType = new OType("this." + heuristic.getDomainName(),next.getAlpha(), next.getInner());
						Set<OType> newSet = new HashSet<OType>();
						newSet.add(newType);
						tm.putTypeMapping(var, newSet);
					}
				}
			}
		}
	}

	/**
	 * Read the list of refinements.
	 * Currently, hard-coded. But will either:
	 * -- get from the UI
	 * -- or from a replay queue
	 */
	
	private boolean TEST_MODE = false;
	
	private Queue<Refinement> getRefinements(TM tm) {
		Queue<Refinement> oogRefs = new LinkedList<Refinement>();

		if (!TEST_MODE) {
			OGraphFacade facade = getFacade();
			RefinementModel facadeRefModel = facade.getRefinementModel();
			List<IRefinement> facadeRefinements = facadeRefModel.getRefinements();
			System.err.println("There are " + facadeRefinements.size() + " refinements");
			// This is OOGRE's copy of the Mother Facade's refinements
			oogRefs = RefinementFactory.populateRefinements(facadeRefinements, tm);
			// DO NOT Clear the list of refinements maintained by the MotherFacade so we don't get them again
			// facadeRefModel.clear();
			return oogRefs;
		}
		else {
			// TEST MODE only
			Queue<Refinement> test = TestDriver.test(this.facade, this.context, this.facade.getDShared());
			// Queue<Refinement> test = new LinkedList<Refinement>();
			saveRefinements(test);
			return test;
		}
    }


	// XXX. Move method
	public static OGraphFacade getFacade() {
	    Activator default1 = oogre.plugin.Activator.getDefault();
	    OGraphFacade facade = default1.getMotherFacade();
	    return facade;
    }

	/**
	 * Use this to convert the hard-coded refinements in the testcases to OOGRE.xml files
	 * for regression testing
	 * 
	 * @param refQueue
	 */
	private void saveRefinements(Queue<Refinement> refQueue) {
	    // Save oogre.xml
		if (path != null) {
			String projectPath = path.toOSString();

			// DONE. Remove hard-coded path. Load relative to current project!
			String oogrePath = projectPath + "\\oogre.xml";
			
			RefinementModel  model = new RefinementModel();
			for(Refinement ref : refQueue ) {
				model.add(ref.getReal());
			}
			
			oog.re.Persist.save(model, oogrePath);
		}
    }
	
	private Queue<IRefinement> loadRefinements() {
		Queue<IRefinement> test = new LinkedList<IRefinement>();

		if (path != null) {
			String projectPath = path.toOSString();

			// DONE. Remove hard-coded path. Load relative to current project!
			String oogrePath = projectPath + "\\oogre.xml";

			RefinementModel load = oog.re.Persist.load(oogrePath);
			List<IRefinement> refinements = load.getRefinements();
			test.addAll(refinements);
		}

		return test;
	}
	
	private TM currentTM = null;
	private Refinement lastRefinement;
	private Heuristic lastHeuristic;
	
	private Map<ICompilationUnit, ASTNode> parseCompilationUnits;

	public TM getInitialTM() {
		return initialTM;
	}
	
	public TM getCurrentTM() {
		return this.currentTM;
	}
	
	public void setCurrentTM(TM currentTM) {
		this.currentTM = currentTM;
    }
	
	//Build the map that shows the AU that should have the same set of typings.
	public void populateEqualityConstraint(TM tm){
		if(ENFORCE_OVERRIDING) {
			for(Pair<IMethodBinding, IMethodBinding> pair : overriddenMethods) {
				// Equalize the method return
				IMethodBinding methodBinding1 = pair.fst();
				Variable methodVar1 = tm.getVarBindingMap(methodBinding1);
				IMethodBinding methodBinding2 = pair.snd();
				Variable methodVar2 = tm.getVarBindingMap(methodBinding2);

				// Add equality constraint between an overridden method and its overriding method from the super type 
				if(methodVar1 != null & methodVar2 != null ) {
					addEqualityConstraint(methodVar1, methodVar2);
				}
			}
			// Equalize the method params
			for (Pair<List<IVariableBinding>, List<IVariableBinding>> pair : overriddenMethodParameters) {
				List<IVariableBinding> parameterList1 = pair.fst();
				List<IVariableBinding> parameterList2 = pair.snd();
				if(parameterList1 != null && parameterList1 != null && parameterList1.size() == parameterList1.size() ) {
					for (int ii = 0; ii < parameterList1.size(); ii++) {
						IVariableBinding param1 = parameterList1.get(ii);
						Variable paramVariable1 = tm.getVariableFromBindingKey(param1.getKey());
						IVariableBinding param2 = parameterList2.get(ii);
						Variable paramVariable2 = tm.getVariableFromBindingKey(param2.getKey());
						if(paramVariable1!=null && paramVariable2!=null){
							addEqualityConstraint(paramVariable1, paramVariable2);
						}
					}
				}
			}
		}
	}

	// x -> Set<y>
	private void addEqualityConstraint(Variable var1, Variable var2) {
		Set<Variable> valueSet = this.equalityConstraints.get(var1);
		if(valueSet==null){
			valueSet = new HashSet<Variable>();
			this.equalityConstraints.put(var1, valueSet);
		}
		// XXX. TOEBI: IF you moved this here, it means you are not always *adding* the constraints.
		// In some cases, you are overwriting.
		valueSet.add(var2);
	}
	
	// Pick the preferred TM to continue, based on the ranking
	// E.g., P is ranked higher than PD, etc.
	// XXX. Right now, this ranking is hard-coded here. Could make it user-configurable.
	private TM pickPreferredTM(Set<TM> tmSet){
		TM resultTM = null;
		for (TM tm : tmSet) {
			if(tm.getKey().getSolIndex().equals(TMSolutionType.P)){
				resultTM = tm;
			}
			else{
				continue;
			}
		}
		if(resultTM==null){
			for (TM tm : tmSet) {
				if(tm.getKey().getSolIndex().equals(TMSolutionType.PD)){
					resultTM = tm;
				}
				else{
					continue;
				}
			}
		}
		if(resultTM==null){
			for (TM tm : tmSet) {
				if(tm.getKey().getSolIndex().equals(TMSolutionType.OWNER)){
					resultTM = tm;
				}
				else{
					continue;
				}
			}
		}
		if(resultTM==null){
			for (TM tm : tmSet) {
				if(tm.getKey().getSolIndex().equals(TMSolutionType.OWNED)){
					resultTM = tm;
				}
				else{
					continue;
				}
			}
		}
		// XXX. Comment out shared solution index.
		//		if(resultTM==null){
		//			for (TM tm : tmSet) {
		//				if(tm.getKey().getSolIndex().equals(TMSolutionType.SHARED)){
		//					resultTM = tm;
		//				}
		//				else{
		//					continue;
		//				}
		//			}
		//		}
		return resultTM;
	}

	// Makes a copy; returns a modified copy; does not change the argument tm
	private TM runTFs(TM tm){
		TM newTM = tm.copyTypeMapping(tm.getKey());
		boolean isChanged = true;
		while(isChanged){
			// Take a snapshot of newTM before running the TFs 
			TM oldTM = newTM.copyTypeMapping(newTM.getKey());

			// Run the TFs on the temporary newTM
			PushIntoOwnedTransferFunctions pushIntoOwnedTFs = new PushIntoOwnedTransferFunctions(this, newTM, null);
			for (MethodDeclaration methodDcl : methodDecls) {
				doAnalysis(methodDcl, pushIntoOwnedTFs);
				if(newTM.isDiscarded){
					break;
				}
			}

			if(!newTM.isDiscarded){
				if(TM.isEqual(oldTM,newTM)){
					isChanged=false;
				}
			}
			else{
				break;
			}
		}
		return newTM;
	}
}
