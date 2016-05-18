package oogre.adapter;

import static edu.wayne.ograph.OGraphStateMgr.ANALYSIS_STATE;
import static edu.wayne.ograph.OGraphStateMgr.FALSE;
import static edu.wayne.ograph.OGraphStateMgr.RUN_OOGRE;
import static edu.wayne.ograph.OGraphStateMgr.RUN_POINTS_TO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oog.common.OGraphFacade;
import oog.itf.IEdge;
import oog.itf.IObject;
import oog.re.IHeuristic;
import oog.re.IRefinement;
import oog.re.PushIntoOwned;
import oog.re.PushIntoPD;
import oog.re.PushIntoParam;
import oog.re.Refinement;
import oog.re.RefinementModel;
import oog.re.RefinementState;
import oog.re.SplitUp;
import oogre.crystal.MyAnalysis;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.json.JSONArray;
import org.json.JSONObject;

import secoog.CopyOGraphVisitor;
import secoog.DataFlowEdge;
import secoog.IsConfidential;
import secoog.IsSanitized;
import secoog.Property;
import secoog.SecEdge;
import secoog.SecGraph;
import secoog.TrustLevelType;
import conditions.Condition;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;
import edu.cmu.cs.viewer.objectgraphs.VisualReportOptions;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayDomain;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayEdge;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayGraph;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.cmu.cs.viewer.objectgraphs.runtimegraph.RuntimeReport;
import edu.wayne.export.DotExport;
import edu.wayne.export.ExportTemplate;
import edu.wayne.ograph.DisplayState;
import edu.wayne.ograph.GlobalState;
import edu.wayne.ograph.OEdge;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphState;
import edu.wayne.ograph.OGraphStateMgr;
import edu.wayne.ograph.OOGUtils;
import edu.wayne.ograph.OObject;
import edu.wayne.ograph.PropertyList;

/**
 * Facade to interface between jobs from the web and the Eclipse stack.
 * Some responsibilities:
 * - convert OGraph to JSON through serializer
 * - generate SVG 
 * - return Java file as string
 * - return refinements from facade
 * - send refinements coming from web:
 * -- construct a refinement object and set it on facade
 * -- trigger the analysis to run
 * --- everytime there is something on the refinement queue, run the analysis
 * 
 *  TOAM: Put in this facade all the filenames
 *  XXX. Extract constants
 *  
 *  XXX. Move code for managing display graph out of here!
 *  deals with IObject, IEdge, DisplayObject, DisplayEdge
 *  
 */
public class WebFacade {

	private static final String UNTRUSTED = "Untrusted";
	private static final String TRUSTED = "Trusted";
	private static final String TRUST_LEVEL = "trustLevel";
	private static final String IS_CONFIDENTIAL = "isConfidential";
	private static final String IS_SANITIZED = "isSanitized";

	// XXX. CAREFUL: The constants must match name of property in JSON/JS 
	private static final String TRUE = "true";
	// NOTE: in the UI, this shows up as "showInternals"
	private static final String SHOW_INTERNAL = "showInternal";
	private static final String VISIBLE = "visible";

	static WebFacade instance = null;
	
	private OGraphFacade motherFacade;
	
	private RefinementModel refinementModel;
	
	private OGraph oGraph;
	
	private OGraphState state = new OGraphState();
	// XXX. Could create a wrapper that looks only at the global state here...
	private OGraphStateMgr stateMgr = new OGraphStateMgr(state);

	// XXX. Store only OEdges. Not SecEdges. Should we replace with keys or some other string? 
	private Set<OEdge> highlightedEdges;
	// XXX. Store only OObjects. Not SecObjects.  Should we replace with keys, e.g., OObjectKey?
	private Set<OObject> highlightedObjects = new HashSet<OObject>();
	
	private MyAnalysis myAnalysis;
	
	// Constants for the file name that we store in each project folder
	private static final String DOT_FILE_NAME = "OOG.dot";
	private static final String DOTFILE_NAME_NO_EXT = "OOG";

	// Maps Oid -> OObject, for the OGraph (for OOGRE refinements)
	private RuntimeModelHelper helper;
	
	// Maps Oid -> SecObject, for the SecGraph (for queries)
	private SecModelHelper secHelper;
	
	private SecGraph secGraph;
	
	// Singleton
	private WebFacade() {
	    super();
    }

	public static WebFacade getInstance() {
		if (instance == null ) {
			instance = new WebFacade();
		}
		return instance;
	}
	
	/*
	 * When not running OOGRE/PointsTo, load last OOG from file
	 * 
	 * XXX. Unify: just load JSON for the OGraph. Avoid XML intermediary.
	 */
	public void loadGraphFromFile() {
		String path = getPath();
		
		path = path + File.separator + "OOG.xml.gz";
		
		// XXX. What if the file doesn't exist, what to do?!? Create a blank OGraph?
		File file = new File(path);
		if (file.exists()) {
			OGraph fileGraph = OOGUtils.loadGZIP(path);
			if (fileGraph != null) {
				motherFacade.setGraph(fileGraph);
			}
		}
		
	}
	
	// TODO: Rename: reloadGraph?
	// Factor out initMotherFacade()
	public void setFacade() {
		// Get the facade
		oog.eclipse.Activator default1 = oog.eclipse.Activator.getDefault();
		motherFacade = default1.getMotherFacade();

		// Load the latest list of refinements and heuristics from the analysis
		refinementModel = motherFacade.getRefinementModel();
		
		// Load the latest OGraph from the facade
		oGraph = motherFacade.getGraph();
		
		if (oGraph == null ) {
			String errorMsg = "No graph is set on the Facade. Make sure to extract it first!";
			System.err.println(errorMsg);
			return;
		}
		
		// Update helper since the mappings may have changed
		helper = new RuntimeModelHelper(oGraph);
		
		// XXX. Add an option here to not always create SecGraph, unless we use the security analysis
		// Create SecGraph from OGraph
		// XXX. Call SecGraph.clear()?
		CopyOGraphVisitor copyVisitor = new CopyOGraphVisitor();
		oGraph.accept(copyVisitor);
		secGraph = SecGraph.getInstance();
		
		// Update SecGraph helper since the mappings may have changed
		secHelper = new SecModelHelper(secGraph);
		
		// TODO: Give the OGraph to a JSON serializer, to get a JSON object
		// TODO: How to get an SVG
		// TODO: How to get the source code 
		// -- using Eclipse ASTNodes, etc.
		// -- extract buffer for Java file
	}
	
	/**
	 * Return a JSONObject by loading from file
	 * 
	 * Can fall back on this without running OOGRE/MOOGREX
	 * Will return the last generated file
	 * 
	 * Return a string of the JSON object
	 */
	String getOOGFromFile() {
		String path = motherFacade.getPath();
		String jsonFile;		 
		byte[] oogData;
		try {
			oogData = Files.readAllBytes(Paths.get(path + "\\OOGexport.json"));
		} catch (IOException e1) {				
			e1.printStackTrace();
			return null;
		}
		jsonFile = new String(oogData, StandardCharsets.UTF_8);
		return jsonFile;
	}

	/**
	 * Return a JSONObject using JSON serialization by serializing the OGraph from the facade
	 * 
	 * Return a string of the JSON object
	 */
	String getOOGFromFacade() {
		return OOGUtils.saveJSON(oGraph);
	}

	/**
	 * Create a SVG based on the current OGraph.
	 * Creates a DisplayGraph first from the OGraph.
	 * 
	 * This method only generates the SVG file and returns a filename.
	 * Caller will load contents of file, encode and return

	 * XXX. OK to not cache the DisplayGraph and always re-generate it.
	 * Even if just expanding/collapsing.
	 * The underlying OGraph will be constantly changing anyway.
	 * 
	 * @return SVG filename
	 */
	public String getSVG() {
		// Load the latest OGraphState, since it impacts the SVG generation
		applyGraphState();
		
		// Gets the model
		if (oGraph != null) {
			DisplayModel displayModel = new DisplayModel();
			RuntimeReport visualReport = new RuntimeReport(oGraph, displayModel);
			visualReport.generateGraph();
			
			// Update the hashtables
			displayModel.finish();
		
			// Clear the summary edges each time, before re-generating the DOT
			if (displayModel != null) {
				displayModel.clearSummaryEdges();

				// String path = "C:\\temp"; // HACK: temp., hard-coded location
				// Use correct location in project
				String path = getPath();

				String dotFullFilename = path + File.separator + DOT_FILE_NAME;
				// Gets the model
				String reportFullPath = path + File.separator + DOTFILE_NAME_NO_EXT;

				DisplayGraph displayGraph = new DisplayGraph(displayModel, dotFullFilename);
				// Set the state to get the display settings (trustLevel, sanitized, etc.) on the OOG
				displayGraph.setGraphState(state);
				
				// Expand/collapse the objects
				applyState(displayGraph);
				
				displayGraph.generateGraph();
				
				return buildImage(reportFullPath, dotFullFilename);
			}
		}
		
		return null;
	}


	/**
	 * This is the Driver function.
	 * 
	 * Apply the state of an object, such as:
	 * - Expand the objects in the nested boxes.
	 * - Visible (true/false) 
	 */
	private void applyState(DisplayGraph displayGraph) {
		
		// Set general options
		DisplayState displayState = getDisplayState();
		VisualReportOptions options = VisualReportOptions.getInstance();
		options.setShowReferenceEdges(displayState.isShowPtEdges());
		options.setShowUsageEdges(displayState.isShowDfEdges());
		options.setShowCreationEdges(displayState.isShowCrEdges());
		options.setShowEdgeLabels(displayState.isShowEdgeLabels());
		
		// Set the object-specific options
		// XXX. Alt. version
		// for (DisplayDomain domain : displayGraph.getRootDisplayDomains()) {
		// 		applyState(domain);
		// }
		applyState(displayGraph.getRootDisplayObject());
		applyState(displayGraph.getEdges());
	}

	private void applyState(Set<DisplayEdge> edges) {
		for(DisplayEdge edge: edges) {
			applyState(edge);
		}
    }

	private void applyState(DisplayEdge edge) {
		// Is the underlying IEdge highlighted?
		if(highlightedEdges != null ) {
			// Lookup an OEdge
			edge.setHighlighted(highlightedEdges.contains(edge.getElement()));
		}
		else {
			// Reset highlighting
			edge.setHighlighted(false);
		}
    }

	private void applyState(DisplayObject displayObject) {
		for (DisplayDomain domain : displayObject.getDomains()) {
			applyState(domain);
		}
	}
	
	/**
	 * This is the Recursive function.
	 * 
	 * Apply the state of an object, such as:
	 * - Expand the objects in the nested boxes.
	 * - Visible (true/false) 
	 */
	private void applyState(DisplayDomain domain) {
		for(DisplayObject object : domain.getObjects() ) {

			// XXX. Can we find a way to avoid near duplication of these methods?
			object.setShowInternals(getStateShowInternals(object));
			object.setVisible(getStateVisible(object));
			// Is the OObject underlying a DisplayObject highlighted?
			object.setHighlighted(highlightedObjects.contains(object.getElement()));
			
			// Recursively, call for child objects
			applyState(object);
		}
	}
	
	/**
	 * Retrieve expand/collapse state for this object from the OGraphState based on the ObjectKey.
	 */
	boolean getStateShowInternals(DisplayObject displayObject) {
		boolean showInternals = true; // default is expanded
		// Could change the default to just expand the root object

		// Go to underlying IObject to do getObjectKey().
		IObject oObject = (IObject) displayObject.getElement();
		if(oObject != null ) {
			String objectKey = oObject.getObjectKey();
			if(objectKey != null && state != null) {
				PropertyList properties = state.getProperties(objectKey);
				if(properties != null ) {
					String property = properties.getProperty(SHOW_INTERNAL);
					showInternals = TRUE.equals(property);
				}
			}
		}
		
		return showInternals;
	}

	/**
	 * Retrieve visible state for this object from the OGraphState based on the ObjectKey.
	 */
	boolean getStateVisible(DisplayObject displayObject) {
		boolean visible = true; // default is expanded

		// Go to underlying IObject to do getObjectKey().
		IObject oObject = (IObject) displayObject.getElement();
		if(oObject != null ) {
			String objectKey = oObject.getObjectKey();
			if(objectKey != null && state != null) {
				PropertyList properties = state.getProperties(objectKey);
				if(properties != null ) {
					String property = properties.getProperty(VISIBLE);
					visible = TRUE.equals(property);
				}
			}
		}
		
		return visible;
	}
	
	/**
	 * Generate SVG file
	 * @param reportFullPath
	 * @param dotFullFilename
	 * @return SVG filename
	 */
	private String buildImage(String reportFullPath, String dotFullFilename) {
		String svgImageFileName = reportFullPath + ".svg";

		// If PointsTo has not run yet, no graph, no dot
		if (dotFullFilename == null) {
			return null;
		}
		
		try {
			generateImageFile(dotFullFilename, "-Tsvg", svgImageFileName);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return svgImageFileName;
	}

	/**
	 * Convert DOT to SVG, by launching GraphViz dot.exe process.
	 * NOTE: Requires ATT_DOT_DIR env. variable to be set! Follow GraphViz setup instructions.

	 * @param dotFileName
	 * @param imageType
	 * @param imageFileName
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void generateImageFile(String dotFileName, String imageType, String imageFileName) throws IOException,
	        InterruptedException {
		VisualReportOptions instance = VisualReportOptions.getInstance();

		// Always use DOT for this
		java.util.List<String> command = new ArrayList<String>();
		// XXX. Fix signature of getExecutable: remove dependency on Shell just to display message box!
		command.add(instance.getExecutable(null));
		command.add(imageType);
		command.add(instance.getEngine());
		command.add(dotFileName);
		command.add("-o");
		command.add(imageFileName);

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(instance.getTempDirectory());

		final Process process = builder.start();
		if (process.waitFor() == 0) {
			// Normal termination...
		}
	}
	
	/**
	 * Re-export GraphViz dot from the Facade OGraph, to apply the visual settings.
	 * Without re-extracting the OGraph.
	 */
	private void exportOGraph() {
		String path = getPath();

		String dotFullFilename = path + File.separator + "dfOGraph.dot";
		
		ExportTemplate dotExport = new DotExport(oGraph, oGraph.getDShared());
		String pathOsString = dotFullFilename;
		if (motherFacade != null)
			dotExport.setGraphState(motherFacade.getGraphState());
		dotExport.writeToFile(pathOsString);
	}
	

	/**
	 * Create an SVG based on the current OGraph.
	 * 
	 * This method only generates the SVG file and returns a filename.
	 * 
	 * The OGraph should have already been generated by PointsTo.
	 * Assumes that PointsTo config.properties has: export.dot = true
	 * 
	 * Caller will load contents of file, encode and return
	 * 
	 * @return
	 */
	public String getSVG2() {
		applyGraphState();
		if (oGraph != null) {
			String path = getPath();

			String dotFullFilename = path + File.separator + "dfOGraph.dot";
			String reportFullPath = path + File.separator + "dfOGraph";

			return buildImage(reportFullPath, dotFullFilename);
		}
		return null;
	}

	/**
	 * Return the fully qualified classname of the root class.
	 * Use to display "default" Java code, if nothing else is running.
	 * 
	 * Can get used as argument to call getJava(...)
	 * 
	 * @return
	 */
	public String getRootClass() {
		// Look up the name of the root class from the config file
		return motherFacade.getRootClass();
	}

	/**
	 * Return the path of the project in the child instance
	 * You need to append filenames to the path (add separator)
	 */
	public String getPath() {
		return motherFacade.getPath();
	}

	/**
	 * Select an appropriate source code based on the traceability info
	 * parameter and return it as base64 encoded string.
	 * 
	 * @param fully qualified class name "x.y.z.Foo";
	 * @return Base64 encoded string of the contents of the Java file containing "x.y.z.Foo";
	 */
	public String getJava(String fullyQualifiedClassName) {
		
		String path = getPath();
		
		path += "\\" + fullyQualifiedClassName;

		byte[] svgData = null;
		try {
			svgData = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String result = "";
		if (svgData != null) {
			result = Base64.encodeBase64String(svgData);
		}
		// XXX. Remove double encode...
		return result;
	}
	
	// XXX. Get rid of this function
	/**
	 * Return Java file based on fully qualified type name
	 * @deprecated Avoid using. Traceability information contains names of compilation units: src\C.java
	 * But root class is stored as a fully qualified name
	 * @param fullyQualifiedTypeName
	 * @return
	 */
	public String getJava2(String fullyQualifiedTypeName) {
		
		List<ICompilationUnit> scanForCompilationUnits = WorkspaceUtilities.scanForCompilationUnits();

		// Look for CU which contains fullyQualifiedClassName
		ICompilationUnit foundCU = null;
		for (ICompilationUnit cu : scanForCompilationUnits) {
			try {
				for (IType type : cu.getTypes()) {
					String key = type.getFullyQualifiedName();
					if (key.equals(fullyQualifiedTypeName)) {
						foundCU = cu;
						break;
					}
				}
			}
			catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		if (foundCU != null) {
			IBuffer buffer = null;
			try {
				buffer = foundCU.getBuffer();
			}
			catch (JavaModelException e) {
				e.printStackTrace();
			}
			if (buffer != null) {
				char[] bufferContents = buffer.getCharacters();
				// generatedSource contains the source code of the classes
				String generatedSource = new String(bufferContents);

				// NOTE: Caller will encode to Base64.
				return generatedSource;
			}
		}

		return null;
	}

	/**	  
	 * Take refinement object received from the web.
	 * Construct a refinement object in Java and add it to the Java facade
	 * 
	 * @param srcObjectID of the source object
	 * @param dstObjectID of the destination domain
	 * @param dstDomain name of the destination domain
	 * 
	 * @return
	 */
	public Object addRefinement(String refType, String srcObjectId, String dstObjectId, String dstDomain) {
		Refinement ref = null;

		// Gotta set the IObjects. Setting string's is not going to work.
		IObject srcObject = helper.getElement(srcObjectId);
		
		// Could not find these objects. 
		// XXX. For SplitUp, the srcObjectId needs extra processing...
		if (srcObject == null && !refType.equals("SplitUp"))  {
			System.err.println("Cannot resolve IObject from" + srcObjectId);
			return null;
		}
		
		IObject dstObject = helper.getElement(dstObjectId);
		if (dstObject == null )  {
			System.err.println("Cannot resolve IObject from" + dstObjectId);
			return null;
		}
		
		
		if (refType.equals("PushIntoOwned") && dstDomain.equals("owned")) {
			ref = new PushIntoOwned(srcObject, dstObject, dstDomain);
		}
		else if (refType.equals("PushIntoPD") && dstDomain.equals("PD")) {
			ref = new PushIntoPD(srcObject, dstObject, "this.PD");
		}
		else if (refType.equals("PushIntoPD") && dstDomain.equals("SHARED")) {
			ref = new PushIntoPD(srcObject, dstObject, "shared");
		}
		else if (refType.equals("PushIntoParam") &&  dstDomain.equals("PARAM")) {
			ref = new PushIntoParam(srcObject, dstObject, dstDomain);
		}
		else if (refType.equals("SplitUp") ){
			// For SplitUp only, in srcObjectId store delimited values: |
			// name | kind | type | enclosingMethod | enclosingType
			String[] splits = srcObjectId.split("\\|");
			if (splits != null && splits.length == 6) {
				
				srcObject = helper.getElement(splits[0]);
				// Could not find these objects. 
				if (srcObject == null )  {
					System.err.println("Cannot resolve IObject from" + splits[0]);
					return null;
				}
				
				SplitUp spu = new SplitUp(srcObject, dstObject, dstDomain);
				spu.setName(splits[1]);
				spu.setKind(splits[2]);
				spu.setType(splits[3]);
				spu.setEnclosingMethod(splits[4]);
				spu.setEnclosingType(splits[5]);
				
				ref = spu;
			}
		}
		
		// Add to the facade
		refinementModel.add(ref);
		return ref;
	}
	
	/**
	 * Populate a JSON object from the facade heuristics and refinements objects and send it back to the client.
	 * (note: in order to get data, MOOGREX must have run once!)
	 * 
	 * The Web UI uses this JSON object to populate the list of refinements.
	 * 
	 * XXX. Alternate impl. is to persist RefinementModel to JSON (just like we can go from oogre.xml -> JSON)
	 * But this requires adding JSON annotations to MOOG objects.
	 * 
	 * NOTE: Could merge the heuristics and the refinements into the same entry, because they get merged in the web UI.
	 */
	// TOAM: Should we return a JSON object instead of JSONObject.toString()? 
	// Does not matter. Will be converted to String before being sent over. 
	public String getRefinements() {
		
		List<IHeuristic> heuristics = refinementModel.getHeuristics();
		
		JSONObject jsonAll = new JSONObject();
		
		JSONArray jsonHeus = new JSONArray();
		int i = 1;
		for (IHeuristic heuristic : heuristics) {
				
				String heuId = ((oog.re.Heuristic)heuristic).getRefID();
				// I tried to use this value. But It is null so doesn't exist in the last result.
				System.out.println(heuId);
				String srcObject = heuristic.getSrcObject();
				String dstObject = heuristic.getDstObject();
				String dstDomain= heuristic.getDomainName();
				RefinementState state = heuristic.getState();
				
				// TOAM: create a JSON object here
				JSONObject jsonHeu = new JSONObject();
				jsonHeu.put("type", "Heuristic");
				jsonHeu.put("refId", heuId == null? Integer.toString(i) : heuId);
				jsonHeu.put("srcObject", srcObject);
				jsonHeu.put("dstObject", dstObject);
				jsonHeu.put("dstDomain", dstDomain);
				jsonHeu.put("state", state.toString());
				
				jsonHeus.put(jsonHeu);
				i++;
		}
		jsonAll.put("heuristics", jsonHeus);

		
		JSONArray jsonRefs = new JSONArray();
		List<IRefinement> refinements = refinementModel.getRefinements();
		int j = 1;
		
		for (IRefinement refinement : refinements) {
				String refId = ((oog.re.Refinement)refinement).getRefID();
				System.out.println(refId);
				String srcObject = refinement.getSrcObject();
				String dstObject = refinement.getDstObject();
				String dstDomain= refinement.getDomainName();
				RefinementState state = refinement.getState();
				
				String refType = refinement.getClass().getSimpleName();

				// TOAM: create a JSON object here
				JSONObject jsonRef = new JSONObject();
				jsonRef.put("type", refType);
				jsonRef.put("refId", refId == null? Integer.toString(j) : refId);
				jsonRef.put("srcObject", srcObject);
				jsonRef.put("dstObject", dstObject);
				jsonRef.put("dstDomain", dstDomain);
				jsonRef.put("state", state.toString());

				jsonRefs.put(jsonRef);
				j++;
		}
		jsonAll.put("refinements", jsonRefs);
		
		return jsonAll.toString();
	}

	/**
	 * Apply the refinement by running the whole stack. Return value reflects whether the refinement succeeded.
	 * 
	 * TOAM: if the return is false, only refresh the refinement list.
	 * Do not refresh the graph and the code. 
	 * The refinement list will reflect that refinement is unsupported.
	 * Do not need to display message box.
	 * 
	 * XXX. Can we show reasons for refinement failure. The reason for failure should be on the refinement object.
	 * XXX. May need to change to from boolean JSON object
	 */
	public boolean doRefinement() {

		// Catch exceptions to avoid crashing the plugin
		try {
			myAnalysis.run();
		}
		// XXX. I don't like this here...
		catch (Exception ex) {
			ex.printStackTrace();
		}

		// Retrieve status whether last refinement was successful from the facade
		boolean status = motherFacade.isInferenceSuccess();
		return status;		
	}

	/*
	 * Set the analysis object; so the server can re-run the analysis to process refinements
	 */
	public void setAnalysis(MyAnalysis myAnalysis) {
		this.myAnalysis = myAnalysis;
    }

	/**
	 * Remove refinement that have State == Pending from refinement queue.
	 * Presumably, there can be only one Pending. But this will remove all such refinements.
	 * 
	 * NOTE: We don't need params: 
	 * srcObjectId
	 * dstObjectId
	 * dstDomain
	 *
	 * @return
	 */
	public Object removePendingRefinement() {
		// NOTE: This works because of representation exposure in getRefinements()!
		List<IRefinement> refinements = refinementModel.getRefinements();
		Iterator<IRefinement> it = refinements.iterator();
		while(it.hasNext()) {
			IRefinement refinement = it.next();
			if(refinement.getState() == RefinementState.Pending) {
				it.remove();
			}
		}
		
		// TOAM: What to return here?
		return new Object();
	}

	/**
	 * Must be called once
	 */
	public void initMotherFacade() {
		// Get the facade
		oog.eclipse.Activator default1 = oog.eclipse.Activator.getDefault();
		motherFacade = default1.getMotherFacade();
	}
	
	/**
	 * Load the OGraphState from file.
	 * 
	 * @return
	 */
	public String loadState() {
		String pathOfState =  getPath() + "\\OOGstate.json";
		byte[] stateData = null;
		try {
			stateData = Files.readAllBytes(Paths.get(pathOfState));
		} catch (IOException e1) {				
			e1.printStackTrace();
			return null;
		}
		return new String(stateData, StandardCharsets.UTF_8);
	}
	
	/**
	 * Save the OGraphState to file.
	 * 
	 * Return the updated data. But currently, we don't use it.
	 * Generally, it's a good pattern. 
	 * It's not terribly inefficient because these JSON objects are small
	 * 
	 * @param state
	 * @return
	 */
	public String saveState(JSONObject state) {
		String pathOfState =  getPath() + "\\OOGstate.json";
		try {
			Files.write(
					Paths.get(pathOfState), 
					state.toString().getBytes(StandardCharsets.UTF_8)
			);
		} catch (IOException e1) {				
			e1.printStackTrace();
			return null;
		}

		// After setting properties, gotta refresh SVG.
		refreshDisplay();
		
		return loadState();
	}

	/**
	 * Refresh the display by re-generating the dfOGraph.dot, OOG.dot, etc.
	 * Take into account properties set, highlights based on query results, etc.
	 */
	private void refreshDisplay() {
	    exportOGraph(); 
		getSVG();
		getSVG2();
    }
	
	@Deprecated
	/**
	 * Set an individual property.
	 * NOTE: We do not use this. We just save the entire JSON object.
	 * 
	 * @param objectKey
	 * @param name
	 * @param value
	 */
	public void setProperty(String objectKey, String name, String value) {
		PropertyList props = new PropertyList();
		props.addProperty(name, value);
		state.setProperties(objectKey, props);
	}
	
	/**
	 * Hook to apply the state
	 * Call back from getSVG() and getSVG2()
	 */
	private void applyGraphState() {
		// Read the graph state
		String pathOfState =  getPath() + "\\OOGstate.json";
		
		// De-serialize JSON object
		OGraphState newState = OOGUtils.loadState(pathOfState);
		if (newState != null ) {
			// Update the field
			this.state = newState;
			this.stateMgr= new OGraphStateMgr(newState); 
		}

		// Set the state on the facade
		// It will impact the OGraph generation, etc.
		motherFacade.setGraphState(newState);
	}

	
	// TODO: maybe change signature to be: String types, List<String> params
	// TOAM: Should we return a JSON object instead of JSONObject.toString()?
	/**
	 * Run security query/constraint on the SecGraph and return the results (edges or objects of interest).
	 * expect type for each node. 
	 * type can be one of: "Object","Domain","Property"
	 * 
	 * @param params The contents of the JSON object. For example:
		{
		   "nodes":[
		      {
		         "id":".simple.Main::owned.simple.PieChart",
		         "view":"pieChart",
		         "type":"Object"
		      },
		      {
		         "id":".simple.Listener::owned.simple.MsgMtoV",
		         "view":"mTOv",
		         "type":"Object"
		      },
		      {
		         "id":".simple.Listener::owned.simple.ListenerList",
		         "view":"lstnrs",
		         "type":"Object"
		      }
		   ],
		   "type":"Transitivity"
		}    
 
	 * @return 	Return a JSONObject as a string to display in the Messages window
	 */
	public String analyzeSec(JSONObject params) {
		JSONObject retVal = null;
		
		if (secGraph == null) return null;
		
		JSONArray nodes = params.getJSONArray("nodes");
		String type = params.getString("type");
		
		final String aId, bId, cId, dId;
		IObject aObj = null, bObj = null, cObj = null, dObj = null;

		switch(type){
		
		case "Provenance":
			aId = nodes.getJSONObject(0).getString("id");
			// Convert Oid to IObject
			aObj = secHelper.getElement(aId);
			
			bId = nodes.getJSONObject(1).getString("id");
			// Convert Oid to IObject
			bObj = secHelper.getElement(bId);
			
			cId = nodes.getJSONObject(2).getString("id");
			// Convert Oid to IObject
			cObj = secHelper.getElement(cId);
			
			dId = nodes.getJSONObject(3).getString("id");
			// Convert Oid to IObject
			dObj = secHelper.getElement(dId);
			// Do the call
			
			
			if (secGraph != null && aObj != null && bObj != null && cObj != null && dObj != null ) {
				Set<DataFlowEdge> provEdges = secGraph.checkObjectProvenance(aObj, bObj, cObj, dObj);
				retVal = createJSON(provEdges);
			}
			break;
		
		// XXX. What do we need to support additional information? E.g., edge type: "DF", "PT", etc.
		case "EdgesBetween":
			aId = nodes.getJSONObject(0).getString("id");
			// Convert Oid to IObject
			aObj = secHelper.getElement(aId);
			
			bId = nodes.getJSONObject(1).getString("id");
			// Convert Oid to IObject
			bObj = secHelper.getElement(bId);

			if (secGraph != null && aObj != null && bObj != null) {
				// NOTE: Generalize set of edges to avoid ClassCastException.
				// XXX. Why empty
				// Order does not matter: a->b or b->a. Using any type of edge
				// Careful here: The result is always empty if you pass OObjects instead of SecObjects !!!
				Set<? extends IEdge> abEdges = secGraph.connectedByUndirected(aObj, bObj, null);
			
				retVal = createJSON(abEdges);
			}
			break;
			
		// XXX. Implement me.
		case "Hierarchy":
			break;
		
		// XXX. Implement me.
		case "Transitivity":
			break;

		// XXX. Implement me.
		case "Reachability":
			break;

		// XXX. Implement me.
		case "IndirectComm":
			break;
		

		case "InformationDisclosure":
			//TODO: XXX this is slow. traverse the secGraph for every property, then again for the query. 
			// Save them once at loading, then update one secObject at a time.
			// see also the TODOs in SecElement, to allow the user can add new custom properties
			secGraph.setObjectProperty(IsConfidential.True, new Condition<IObject>() {
				
				@Override
				// XXX. Copy the properties from OGraphState to the SecGraph
				public boolean satisfiedBy(IObject obj) {
					PropertyList properties = state.getProperties(obj.getObjectKey());
					return properties!= null && TRUE.equalsIgnoreCase(properties.getProperty(IS_CONFIDENTIAL));
				}
			});
			secGraph.setObjectProperty(TrustLevelType.Low, new Condition<IObject>() {
				
				@Override
				// XXX. Copy the properties from OGraphState to the SecGraph				
				public boolean satisfiedBy(IObject obj) {
					PropertyList properties = state.getProperties(obj.getObjectKey());
					return properties!= null && UNTRUSTED.equalsIgnoreCase(properties.getProperty(TRUST_LEVEL));
				}
			});
			Property[] snkProps = {TrustLevelType.Low};
			Property[] flowProps = {IsConfidential.True};
			Set<SecEdge> flowIntoSink = secGraph.getFlowIntoSink(snkProps, flowProps);
			retVal = createJSON(flowIntoSink);
			break;
		case "Tampering":
			//TODO: XXX this is slow. traverse the secGraph for every property, then again for the query. 
			// Save them once at loading, then update one secObject at a time.
			// see also the TODOs in SecElement, to allow the user can add new custom properties
			secGraph.setObjectProperty(IsSanitized.False, new Condition<IObject>() {
				
				@Override
				public boolean satisfiedBy(IObject obj) {
					PropertyList properties = state.getProperties(obj.getObjectKey());
					return properties!= null && FALSE.equalsIgnoreCase(properties.getProperty(IS_SANITIZED));
				}
			});
			secGraph.setObjectProperty(TrustLevelType.Full, new Condition<IObject>() {
				
				@Override
				public boolean satisfiedBy(IObject obj) {
					PropertyList properties = state.getProperties(obj.getObjectKey());
					return properties!= null && TRUSTED.equalsIgnoreCase(properties.getProperty(TRUST_LEVEL));
				}
			});
			Property[] tamperingSnkProps = {TrustLevelType.Full};
			Property[] tamperingFlowProps = {IsSanitized.False};
			Set<SecEdge> flowIntoSink2 = secGraph.getFlowIntoSink(tamperingSnkProps, tamperingFlowProps);
			retVal = createJSON(flowIntoSink2);
			break;

		}
		
		return retVal != null ? retVal.toString() : null;
	}

	/**
	 * Return a JSON object that contains "edges" or "objects" of interest.
	 * 
	 * Must contain Oid's so we can tie the Message back to object in the Object Tree.
	 * Instead of trace to code from Edge of interest, go to Object then Trace to code.
	 * 
	 * XXX. Should we use ObjectKeys instead of Oids, so  we can export queries?
	 * XXX. This method handles sets of edges. Add another one to handle sets of objects.
	 * 
	 * XXX. Fix the output: do not use O_id!
	 * 
	 * @return JSON object to be displayed in Messages window.
	 */
	private JSONObject createJSON(Set<? extends IEdge> edges) {
		JSONObject jsonAll = new JSONObject();
		
		// Report edges of interest
		JSONArray jsonEdges = new JSONArray();
		jsonAll.put("edges", jsonEdges);		
		
	    for(IEdge edge : edges ) {
	    	IObject osrc = edge.getOsrc();
	    	IObject odst = edge.getOdst();

	    	// Skip over self-edges
	    	if(osrc == odst) {
	    		continue;
	    	}
	    	
	    	JSONObject jsonEdge = new JSONObject();
	    	jsonEdge.put("src", osrc.getInstanceDisplayName());
	    	// Convert IObject's to Oid's
			jsonEdge.put("srcOid", osrc.getO_id());
			jsonEdge.put("dst", odst.getInstanceDisplayName());
			jsonEdge.put("dstOid", odst.getO_id());
			// XXX. Look at SecurityAnalysis.displayWarnings 
			// XXX. Add more: label
			// XXX. Add more: edge type 
			// XXX. Add more: flowObject?
			//Type flowType = edge.getFlowType();
			jsonEdges.put(jsonEdge);
	    }
	    
	    setHighlightedEdges(edges);
	    
	    return jsonAll;
     }

	// XXX. Gotta reset the highlights on teh other edges.
	private void setHighlightedEdges(Set<? extends IEdge> highlightedEdges) {
		// Cannot assign directly!!!
		
		this.highlightedEdges = new HashSet<OEdge>();
		
		// Query result returns SecEdges. Gotta map back to OEdges
		for(IEdge secEdge : highlightedEdges ) {
			// Sets this on the underlying OEdge.
			secEdge.setHighlighted(true);
			this.highlightedEdges.add((OEdge) ((SecEdge)secEdge).getOEdge());
		}
		
		// Highlight src, dst
		// XXX. Highlight flow?
		// for(IEdge edge : highlightedEdges) {
		// highlightedObjects.add(edge.getOsrc());
		// highlightedObjects.add(edge.getOdst());
		// // XXX. Add flow
		// }
		
		
		// After changing highlighting, gotta refresh SVG.
		refreshDisplay();
    }


	/**
	 * Return the current state
	 * @return
	 */
	public OGraphState getState() {
    	return state;
    }
	
	public GlobalState getGlobalState() {
    	if(stateMgr != null ) {
    		return stateMgr.getGlobalState();
    	}
    	
    	return null;
    }
	
	public DisplayState getDisplayState() {
    	if(stateMgr != null ) {
    		return stateMgr.getDisplayState();
    	}
    	
    	return null;
    }
	
	
	/*
	 * HACK: Turn off everything, just to play with graph queries...
	 * NOT CALLED.
	 */
	public void initGlobalState() {
		if(stateMgr != null ) {
			stateMgr.setGlobalPropertyValue(ANALYSIS_STATE, RUN_POINTS_TO, FALSE);
			stateMgr.setGlobalPropertyValue(ANALYSIS_STATE, RUN_OOGRE, FALSE);
		}
	}
	
	public void setPath(String path ) {
		motherFacade.setPath(path);
	}
	
	
}
