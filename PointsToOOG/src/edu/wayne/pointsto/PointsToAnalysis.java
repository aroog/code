package edu.wayne.pointsto;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import oog.common.OGraphFacade;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import util.typehierarchy.TypeHierarchyFactory;
import adapter.Adapter;
import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.internal.CrystalRuntimeException;
import edu.cmu.cs.crystal.tac.ITACTransferFunction;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.util.TypeHierarchy;
import edu.wayne.auxiliary.Config;
import edu.wayne.auxiliary.Utils;
import edu.wayne.dot.DotFlowGraphExport;
import edu.wayne.dot.ExportRefinements;
import edu.wayne.export.DgmlExport;
import edu.wayne.export.DotExport;
import edu.wayne.export.ExportTemplate;
import edu.wayne.flowgraph.FlowGraph;
import edu.wayne.generics.GenHelper;
import edu.wayne.moogdb.MOOGDB;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphState;
import edu.wayne.ograph.OGraphStateMgr;
import edu.wayne.ograph.OOGUtils;
import edu.wayne.ograph.PropertyList;
import edu.wayne.ograph.analysis.CfOOGTransferFunctions;
import edu.wayne.ograph.analysis.CrOOGTransferFunctions;
import edu.wayne.ograph.analysis.DfOOGTransferFunctions;
import edu.wayne.ograph.analysis.NodesOOGTransferFunctions;
import edu.wayne.ograph.analysis.PtOOGTransferFunctions;
import edu.wayne.ograph.analysis.TraceabilityFactory;
import edu.wayne.ograph.analysis.ValueFlowTransferFunctions;
import edu.wayne.ograph.internal.OOGContext;
import edu.wayne.ograph.internal.ResourceLineKeyMgr;
import edu.wayne.ograph.traceability.TraceUtils;
import edu.wayne.pointsto.plugin.Activator;

public class PointsToAnalysis extends AbstractCrystalMethodAnalysis {
	
	private static final boolean EXPORT_ANNOTATIONS_AS_REFINEMENTS = false;

	public static final String CRYSTAL_NAME = "PointsTo";
	
	public static final String POINTSTO_LOGGER = "edu.wayne.pointsto";

	private enum EdgeOptions {
		DF, CF, PT, CR, NONE, FLOW
	};

	private TACFlowAnalysis<OOGContext> flowAnalysis;
	private TypeHierarchy hierarchy;
	private Logger log;
	private Map<ast.Type, TypeDeclaration> types;
	private CompilationUnitTACs savedInput;
	private AnnotationDatabase annoDB;
	private IPath path;
	private boolean isChanged = true;
	private ITACTransferFunction<OOGContext> aliasTF;
	private IAnalysisReporter savedReporter;
	private List<ITypeRoot> savedUnits;
	private OOGContext resultContext;
	public Map<ASTNode, String> warnings;
	// use to decide which edges you want to extract (empty array extracts just
	// object hierarchy)
	// Uncomment the line to select edge type.
	// obsolete: Use config.properties file instead
	// public static EdgeOptions[] selectedOptions = {EdgeOptions.PT};
	// public static EdgeOptions[] selectedOptions = {EdgeOptions.DF};
	public static EdgeOptions[] selectedOptions = { EdgeOptions.PT, EdgeOptions.DF, EdgeOptions.CR, EdgeOptions.CF };

	// public static EdgeOptions[] selectedOptions = {}; //no edges

	public PointsToAnalysis() {
		log = Logger.getLogger(POINTSTO_LOGGER);
		types = new Hashtable<ast.Type, TypeDeclaration>();
		savedUnits = new LinkedList<ITypeRoot>();
		warnings = new Hashtable<ASTNode, String>();		
	}

	public TypeHierarchy getHierarchy() {
		return hierarchy;
	}

	public  CompilationUnitTACs getSavedInput(){
		return savedInput;
	}
	public IAnalysisReporter getSavedReporter() {
		return savedReporter;
	}

	public Map<ast.Type, TypeDeclaration> getTypes() {
		return types;
	}

	public AnnotationDatabase getAnnoDB() {
		return annoDB;
	}

	private void reset() {
		// Clear the annotation cache
		MOOGDB.reset();
		
		// Clear data structures that hold on to ASTNodes, etc.
		Adapter.reset();
		TypeHierarchyFactory.reset();
		TraceabilityFactory.reset();
		
		// XXX. May not need to reset this if preserving types across invocations
		// GenHelper creates ITypeBinding from keys and is very expensive.
		GenHelper.reset();
		
		path = null;
		log = null;
		// Clear this map as it contains Eclipse ASTNode objects (TypeDeclarations)
		types.clear();
		savedInput = null;
		annoDB = null;
		aliasTF = null;
		hierarchy = null;
		savedUnits.clear();
		warnings.clear();
		
		// Deep cleanup to promote garbage collection:
		flowAnalysis = null; 		// This has a map of IMethodBinding -> EclipseTACs
	}

	@Override
	public String getName() {
		return CRYSTAL_NAME;
	}

	@Override
	// XXX. TODO: Move parts of this code to beforeAllCompUnits()
	public void beforeAllMethods(ITypeRoot compUnit, CompilationUnit rootNode) {
		savedUnits.add(compUnit);
		try {
			path = compUnit.getCorrespondingResource().getProject().getLocation();
		} catch (JavaModelException ex) {
			throw new IllegalStateException("project path not found");
		}
		// XXX. Why does visiting root expression need to happen before each Compilation unit?
		rootNode.accept(new RootExpressionVisitor(types));
		// NOTE: we check hierarchy == null so we can initialize this once.
		// XXX. Ideally, move this code OUT of beforeAllMethods. Into beforeAllCompilationUnits
		// This check is being done for each CompilationUnit
		if (hierarchy == null) {
			try {
				// Choose between simple typeHierarchy and type hierarchy with
				// OwnershipDomains
				IJavaProject javaProject = compUnit.getJavaProject();
				GenHelper.setJavaProject(javaProject);
				// Give MiniAst the chance to initialize its type hierarchy
				TypeHierarchyFactory.getInstance().registerProject(javaProject);
				hierarchy = TypeHierarchyFactory.getInstance().getHierarchy();

				// hierarchy = new
				// OwnershipDomainsTypeHierarchy(compUnit.getJavaProject());
			} catch (JavaModelException e) {
				log.log(Level.SEVERE, "Could not set up compilation unit for analysis", e);
			}
		}
	}

	@Override
    public void beforeAllCompilationUnits() {
		// 1. Get the JavaPRoject
		// 2. Move some of the code for building the TypeHierarchy from beforeAllMethods...
	    super.beforeAllCompilationUnits();
    }

	/**
	 * @param df
	 * 
	 */
	private void addEdgeOption(EdgeOptions opt) {
		boolean found = false;
		for (EdgeOptions option : selectedOptions) {
			if (option.equals(opt))
				found = true;
		}
		if (!found) {
			int optsize = selectedOptions.length;
			EdgeOptions[] aux = new EdgeOptions[optsize + 1];
			for (int i = 0; i < selectedOptions.length; i++)
				aux[i] = selectedOptions[i];
			selectedOptions = aux;
			selectedOptions[optsize] = opt;
		}
	}

	@Override
	public void analyzeMethod(MethodDeclaration methodDecl) {
		if (savedInput == null) {
			savedInput = getInput().getComUnitTACs().unwrap(); // needed to get TAC
			annoDB = getInput().getAnnoDB();
			savedReporter = getReporter();
		}
		
		ast.MethodDeclaration miniASTDecl = ast.MethodDeclaration.createFrom(methodDecl);

		
		Adapter.getInstance().map(methodDecl, miniASTDecl);
		
		// Set the formals on the MethodDeclaration
		// Retrieve annotations from Eclipse AST and store in MiniAst
		TraceUtils.setMethDeclFormals(methodDecl, miniASTDecl, annoDB);
	}

	// XXX. Delete me.
	public void runAnalysis(MethodDeclaration methodDecl) {
		// nothing to be done yet.
	}

	/***
	 * use for debugging purposes only.
	 * 
	 * @param result
	 * @param node
	 */
	private void report(OOGContext result, ASTNode node) {
		// System.out.println("O=" + result.getO() + " " + ((MethodDeclaration)
		// node).getName());
		// System.out.println("DO=" + result.getG().getDO() + " " +
		// ((MethodDeclaration) node).getName());
		// System.out.println("DD=" + result.getG().getDD() + " " +
		// ((MethodDeclaration) node).getName());
		// System.out.println("DE=" + result.getG().getDE() + " " +
		// ((MethodDeclaration) node).getName());
		// System.out.println("Upsilon=" + result.getUpsilon() + " " +
		// ((MethodDeclaration) node).getName());
		// System.out.println("Gamma=" + result.getGamma() + " " +
		// ((MethodDeclaration) node).getName());
		// int index = 0;
		// for(OEdge e: result.getG().getDE()){
		// for (List<ASTNode> nList: e.getFullPath())
		// for (ASTNode n: nList)
		// savedReporter.reportUserProblem(e.getFullPath().toString(), n,
		// "traceability" + index);
		// index++;
		// }
		// for(OEdge e: result.getG().getDE()){
		// System.out.println(e.getOsrc()+"->"+e.getOdst());
		// for (BaseTraceability nList: e.getTraceability())
		// System.out.println(nList);
		// }
	}

	@Override
	public void afterAllCompilationUnits() {
		
		Activator default1 = edu.wayne.pointsto.plugin.Activator.getDefault();
		OGraphFacade motherFacade = null;
		if (default1 != null) {
			motherFacade = default1.getMotherFacade();
			if (motherFacade != null) {
				// prepare for the worst...
				motherFacade.setExtractionSuccess(false);
			}
		}
		
		long time = new Date().getTime();
		ResourceLineKeyMgr.getInstance().setBasePath(path.toOSString());
		Config.loadConfig(path);
		if (Config.EDGEOPTIONS_DF) {
			addEdgeOption(EdgeOptions.DF);
		}
		else 
			removeEdgeOption(EdgeOptions.DF);
		if (Config.EDGEOPTIONS_PT) {			
			addEdgeOption(EdgeOptions.PT);
		}
		else 
			removeEdgeOption(EdgeOptions.PT);
		if (Config.EDGEOPTIONS_CR) {
			addEdgeOption(EdgeOptions.CR);
		}
		else {
			removeEdgeOption(EdgeOptions.CR);
		}
		if (Config.EDGEOPTIONS_CF) {
			addEdgeOption(EdgeOptions.CF);
		}
		else 
			removeEdgeOption(EdgeOptions.CF);
		for (ITypeRoot compUnit : savedUnits)
			savedReporter.clearMarkersForCompUnit(compUnit);

		// Populate domains and domain params on the type declaration
		for (TypeDeclaration typeDecl : types.values()) {
			ast.TypeDeclaration td = ast.TypeDeclaration.createFrom(typeDecl);
			if (td != null) {
				td.setDomains(Utils.getDomainDecls(typeDecl.resolveBinding(), annoDB));
				td.setParameters(Utils.getDomainParamsDecl2(typeDecl.resolveBinding(), annoDB));
			}
			Adapter.getInstance().map(typeDecl, td);
		}

		TypeDeclaration mainType = null;
		ast.Type mainClassName = null;
		for (ast.Type t : types.keySet()) {
			System.out.println(t);
			// TODO: HIGH. Convert to a constant
			if (t.toString().endsWith(Config.MAINCLASS)) {
				mainType = types.get(t);
				mainClassName = t;
			}
		}
		if (mainType == null) {
			for (TypeDeclaration td : types.values()) {
				addWarning(td, "Cannot find main type. Specify main type in config.properties");
				displayWarnings();
				break; // display only 1 error
			}
		}
		else {
			System.out.println("Found main type, searching for root expression");
			resultContext = new OOGContext(hierarchy);
			boolean foundMainMethod = false;
			
			try {
			for (MethodDeclaration md : mainType.getMethods()) {
				if (Config.isMainMethod(md)) {
					foundMainMethod = true;
					System.out.println("Found root expression");
					selectEdges(EdgeOptions.NONE);
					System.out.println("OObjects BEGIN :");
					resultContext = doAccept(md, new OOGContext(hierarchy));
					int i = 2;
					i = beginAnalysisLoop(md, i);
					
					if (Config.HANDLE_LENT_UNIQUE) {
						System.out.println("FLOW BEGIN");
						selectEdges(EdgeOptions.FLOW);
						resultContext = doAccept(md, new OOGContext(hierarchy));
					}

					for (EdgeOptions option : selectedOptions) {
						isChanged = true;
						selectEdges(option);
						System.out.println(option + "Edges BEGIN :");
						i = beginAnalysisLoop(md, i);
					}
					System.out.println("DONE");
					System.out.println("==================");
//					System.out.println(resultContext.getG().getDO());
//					System.out.println(resultContext.getG().getDD());
					
				}
			}
			}
			catch (CrystalRuntimeException cre) {
				// Interrupted computation
				System.err.println("INTERRUPTED:" + cre.getMessage());
			}
			if (!foundMainMethod) {
				addWarning(mainType, "Cannot find main method. Specify main method in config.properties");
				displayWarnings();
			} else {
				displayWarnings();
				// rma.getRootObject();
				OGraph real = resultContext.getG().getReal();
				
				if (Config.DOT) {
					ExportTemplate dotExport = new DotExport(real, OOGContext.getDShared());
					String pathOsString = path.append("dfOgraph.dot").toOSString();
					if (motherFacade != null)
						dotExport.setGraphState(motherFacade.getGraphState());
					dotExport.writeToFile(pathOsString);
					
					// Optionally, generate the flow graph
					if (Config.HANDLE_LENT_UNIQUE) {
						DotFlowGraphExport fgExport = new DotFlowGraphExport(resultContext.getFG());
						// TODO: make exporting FG a separate option
						String pathOsString2 = path.append("flowGraph.dot").toOSString();
						fgExport.writeToFile(pathOsString2);
						// XXX. Report cycle in Eclipse problem window too!
					}
				}
				// CUT For now: 
				// TORAD: TODO: add another option in the config file.
				if (Config.DGML) {
					String pathOsStringDGML = path.append("dfOgraph.dgml").toOSString();
					DgmlExport dgmlExport = new DgmlExport(real, real.getDShared());
					dgmlExport.writeToFile(pathOsStringDGML);
				}

				// XXX. Make this a Config option. Otherwise, slowing down extraction
				if(EXPORT_ANNOTATIONS_AS_REFINEMENTS) {
					ExportRefinements exportRefinements = new ExportRefinements();
					exportRefinements.export(real);
					exportRefinements.outputRefs();
				}
				
				if (motherFacade != null) {
					motherFacade.setGraph(real);
					System.out.println("OGraph set on facade.");
					// Only here, declare success
					motherFacade.setExtractionSuccess(true);
				}
				
				// TODO: Get filename from config file;
				// TODO: HIGH. Avoid hard-coded filenames
				if (Config.EXPORT_TO_XML)  {
					// XXX. Swap out order.
					if (!Config.GZIP) {
						String pathOsXML = path.append("OOG.xml").toOSString();
						OOGUtils.save(real, pathOsXML);
					} else {
						String pathOsXML = path.append("OOG.xml.gz").toOSString();
						OOGUtils.saveGZIP(real, pathOsXML);
						
						// XXX. Have a separate config option for export to JSON.
						// No need to export both!!! It's slow I/O.

						// XXX. When OGraph contains cycles, cannot serialize using JSON! StackOverflow
						String pathOsJSON = path.append("OOGexport.json").toOSString();
						OOGUtils.saveJSON(real, pathOsJSON);
						
						OGraphState state1 = new OGraphState();
						PropertyList props = new PropertyList();
						// props.addProperty("propName", "propValue");
						// state1.setProperties("objKey", props);
						props.addProperty(OGraphStateMgr.RUN_POINTS_TO, OGraphStateMgr.TRUE);
						props.addProperty(OGraphStateMgr.RUN_OOGRE, OGraphStateMgr.TRUE);
						state1.setProperties(OGraphStateMgr.ANALYSIS_STATE, props);
						
						String pathOsState = path.append("OOGstate.json").toOSString();
						File state = new File(pathOsState);
						// Do not overwrite the state. But make sure it is there
						if (!state.exists()) {
							OOGUtils.saveState(state1, pathOsState);
						}

						// CUT: debugging
						// OGraphState state2 = OOGUtils.loadState(pathOsState);
						// int debug = 0; debug++;
					}
					System.out.println("OGraph saved to folder: " + path.toOSString());
				}
				// System.out.println(resultContext.getFG().print());
				String endtime = ""+(new Date().getTime() - time);
				savedReporter.reportUserProblem("__FINISHED (Config := " + Config.toConfigString() + ") in "+endtime+ " (ms)", mainType, "STATS");
			}
			resultContext.clear();
			resultContext = null;
		}
		reset();
		savedReporter = null;
		savedUnits.clear();
	}

	private void removeEdgeOption(EdgeOptions opt) {
		boolean found = false;
		for (EdgeOptions option : selectedOptions) {
			if (option!=null && option.equals(opt))
				found = true;
		}
		if (found) {
			int optsize = selectedOptions.length;
			EdgeOptions[] aux = new EdgeOptions[optsize-1];
			int j = 0;
			for (int i = 0; i < selectedOptions.length; i++)
				if (!selectedOptions[i].equals(opt)){
					aux[j] = selectedOptions[i];
					j++;
				}
			selectedOptions = aux;			
		}
		
	}

	private void displayWarnings() {
		for (ASTNode node : warnings.keySet()) {
			getSavedReporter().reportUserProblem(warnings.get(node), node,	getName());	
		}
		warnings.clear();
	}

	/**
	 * @param md
	 * @param i
	 * @return
	 */
	private int beginAnalysisLoop(MethodDeclaration md, int i) {
		while (isChanged) {			
			System.out.println("BEGIN " + i++ + " PASS");
			System.out.println("==================");
			warnings.clear();
			
			FlowGraph fg = null;
			if (Config.HANDLE_LENT_UNIQUE)
				fg = resultContext.getFG().propagateAll();
			
			OOGContext oldContext = new OOGContext(hierarchy, resultContext.getG().clone(), fg != null? fg.clone() : null);
			//System.out.println(fg.print());
			OOGContext newContext = doAccept(md, new OOGContext(hierarchy, resultContext.getG(), fg));
			if (aliasTF.getLatticeOperations().atLeastAsPrecise(newContext, oldContext, md))
				isChanged = false;
		}
		return i;
	}

	/**
	 * 
	 */
	private void selectEdges(EdgeOptions key) {
		// DONE: Move this setting somewhere more global
		// EdgeOptions key = EdgeOptions.DF;

		switch (key) {
		case DF:
			aliasTF = new DfOOGTransferFunctions(this, Config.HANDLE_LENT_UNIQUE);			
			break;
		case CF:
			aliasTF = new CfOOGTransferFunctions(this);
			break;
		case PT:
			aliasTF = new PtOOGTransferFunctions(this);
			break;
		case CR:
			aliasTF = new CrOOGTransferFunctions(this);
			break;
		case NONE:
			aliasTF = new NodesOOGTransferFunctions(this);
			break;
		case FLOW:{
			ValueFlowTransferFunctions vftf = new ValueFlowTransferFunctions(this);
			vftf.collectFlow(true);
			aliasTF = vftf;
			break;
		}
		default:
			aliasTF = new DfOOGTransferFunctions(this);
			break;
		}
	}

	@Deprecated
	public OOGContext getCurrentContext() {
		return resultContext;
	}

	public OOGContext doAccept(MethodDeclaration md, OOGContext value) {
		resultContext = value;		
		flowAnalysis = new TACFlowAnalysis<OOGContext>(aliasTF, savedInput);

		// flowAnalysis.getEndResults(md);
		OOGContext endResults = flowAnalysis.getEndResults(md);
		report(endResults, md);
		return endResults;
	}

	// TODO: Rename this: --> TypeVisitor
	// MiniAST will need to be rebuilt, because the TypeDeclarations ASTNode may change...
	// Do not hold on to them across invocations...
	// XXX. But more often than not, the type information does not change. So it makes sense to cache it.
	class RootExpressionVisitor extends ASTVisitor {

		private Map<ast.Type, TypeDeclaration> types;

		RootExpressionVisitor(Map<ast.Type, TypeDeclaration> types) {
			this.types = types;
		}

		@Override
		public void endVisit(TypeDeclaration node) {
			types.put(ast.Type.createFrom(node.resolveBinding()), node);
		}
	}

	@Override
	public void afterAllMethods(ITypeRoot compUnit, CompilationUnit rootNode) {
		super.afterAllMethods(compUnit, rootNode);
	}

	/**
	 * @param nodesOOGTransferFunctions
	 * @param qualifiedClassName
	 * @return 
	 * 
	 * TODO Deal here with generics - make a copy for every qualified name.
	 */
	// XXX. This is very inefficient...linear search through a map.
	public TypeDeclaration getTypeDecl(ast.Type qualifiedClassName) {
		TypeDeclaration typeDeclaration = types.get(qualifiedClassName);
		if (typeDeclaration != null)
			return typeDeclaration;
		else {
			for (TypeDeclaration qcn : types.values()) {
				String qualifiedName = qualifiedClassName.getFullyQualifiedName();
				String qcnName = qcn.resolveBinding().getQualifiedName().split("<")[0];
				if (qualifiedName.startsWith(qcnName) && getHierarchy().isSubtypeCompatible(qualifiedName, qcnName)
				// qualifiedClassName.isSubTypeCompatible(qcn)
				) {
					TypeDeclaration typeDecl = types.get(ast.Type.createFrom(qcn.resolveBinding()));
					types.put(qualifiedClassName, typeDecl);
					System.out.println("Found declaration for generic type :" + qualifiedName);
					return typeDecl;
				}
			}
		}
		return null;// throw new
		// IllegalStateException("Type Declaration not found for: "+qualifiedClassName);//search
		// in types for a declaration that can be used, i.e., the
		// raw type.
	}

	public String addWarning(ASTNode node, String msg) {
		return warnings.put(node,msg);
	}

}