package edu.wayne.metrics.actions;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ast.BaseTraceability;
import ast.Type;
import ast.TypeInfo;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.datamodel.GraphMetricItem;
import edu.wayne.metrics.datamodel.NodeType;
import edu.wayne.metrics.internal.WorkspaceUtilities;
import edu.wayne.metrics.utils.ObjectsUtils;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OOGUtils;

// TORAD: TODO: HIGH. XXX. Detect the cycle in the runtimeModel. To avoid non-termination.
// TODO: LOW. Refactor this to just take an IProject
// TODO: LOW. Refactor. Create constant for magic string: src.ograph.xml
public class OOGMetricsOGraph implements IWorkbenchWindowActionDelegate {

	private static final String SRC_FOLDER = "src"; // by default
	private static final String OGRAPH_XML_GZIP = "OOG.xml.gz";
	private static final String OGRAPH_XML = "OOG.xml";
	
	private OGraph runtimeModel;

	private IPath currentProjectPath;

	private String statsOGraph;

	private PrintStream outStats;

	private Hashtable<String, GraphMetricItem> listOfObjects;

	private Hashtable<String, GraphMetricItem> listOfDomains;
	
	private Hashtable<String, IObject> listOfParentObjects;

	/**
	 * @return the listOfObjects
	 */
	public Hashtable<String, GraphMetricItem> getListOfObjects() {
		return listOfObjects;
	}

	/**
	 * @return the listOfDomains
	 */
	public Hashtable<String, GraphMetricItem> getListOfDomains() {
		return listOfDomains;
	}

	private IWorkbenchWindow window = null;

	boolean withDouplicate = true;

	int treeDepth = 0;

	int noOfAllObjects = 0;

	// int depthCounterTemp = 1;

	FileWriter writer;

	public OOGMetricsOGraph() {
	}

	private String getFilePath(IProject currentProject, String filename) {
		String path = null;
		
		IPath relativePath = new Path(filename);
		IResource findMember = currentProject.findMember(relativePath);
		if (findMember instanceof IFile) {
			IFile file = (IFile)findMember;
			if (file.exists() ) {
				path = file.getLocation().toOSString();
			}
		}
		return path;
	}
	
	@Override
	public void run(IAction action) {

		treeDepth = 1;
		noOfAllObjects = 0;
		// depthCounterTemp = 1;
		listOfObjects = null;
		listOfParentObjects = null;
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);

		if (currentProject != null) {
			currentProjectPath = currentProject.getLocation();
			
			String projectName = currentProject.getName();
			
			// First, look for the Zip file;
			String filePath = getFilePath(currentProject, OGRAPH_XML_GZIP);
			if (filePath != null ) {
					runtimeModel = OOGUtils.loadGZIP(filePath);
			}
			else {
				// If not, look for the non-zip file
				filePath = getFilePath(currentProject, OGRAPH_XML);
				if (filePath != null ) {
					runtimeModel = OOGUtils.load(filePath);
				}
			}

			if (runtimeModel != null) {
				initTypeStructures();

				try {
					statsOGraph = currentProjectPath.append(projectName + "_OGraph_Stats.txt").toOSString();
					outStats = new PrintStream(statsOGraph);

					// topLevelDomainsAndObjects();
					IObject rootObject = runtimeModel.getRoot();

					oOGObjectsReport(rootObject);
					outStats.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				MessageBox msgbox = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
				msgbox.setText("OOG Metrics");
				msgbox.setMessage("Cannot load an Ograph. Missing or bad file in: \n" + currentProject.getLocation().toOSString());
				msgbox.open();
			}
		}
		else {
			MessageBox msgbox = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
			msgbox.setText("OOG Metrics");
			msgbox.setMessage("Please open a Java  in the project to analyze");
			msgbox.open();
		}
	}

	private void initTypeStructures() {
		TypeInfo typeInfo = TypeInfo.getInstance();
		Crystal crystal = Crystal.getInstance();
		
		// Create a copy, since we may discover additional types, from super classes, and super-interfaces
		// To avoid concurrent modification exception
		Set<Type> allTypes = new HashSet<Type>();
		allTypes.addAll(typeInfo.getAllTypes());
		
		for (Type type : allTypes) {
			String fullyQualifiedName = type.getFullyQualifiedName();
			ITypeBinding typeBinding = crystal.getTypeBindingFromName(fullyQualifiedName);
			if (typeBinding != null) {
				typeInfo.reviveTypeFromBinding(type, typeBinding);
			}
			else {
				// HACK: Is it normal to not be able to resolve primitive types?
				// Cannot resolve typebinding: double[]
				System.err.println("Cannot resolve typebinding: " + fullyQualifiedName);
			}
		}
	}
	
	void topLevelDomainsAndObjects() {
		outStats.println("ObjectGraph (OGraph) metrics");
		outStats.println("Top-Level Domain    # of objects");
		int topLevelDomainsCounter = 0;
		int allTopLevelObjects = 0;
		IObject dummyObject = runtimeModel.getRoot();
		// dummy has 3 domains: D_SHARED, D_LENT, D_UNIQUE
		// D_SHARED contains the main object
		for (IDomain rootDomain : dummyObject.getChildren()) {

			for (IObject mainObject : rootDomain.getChildren()) {

				// Skip objects in the shared domain that are not the main object
				// TODO: Fix this if we we want to count the objects in shared.
				if (!mainObject.isMainObject()) {
					continue;
				}

				for (IDomain topLevelDomain : mainObject.getChildren()) {
					int topLevelObjectsCounter = 0;
					topLevelDomainsCounter++;
					outStats.println("domain: " + topLevelDomain.getD());
					
					for (IObject topLevelObject : topLevelDomain.getChildren()) {
						topLevelObjectsCounter++;
						allTopLevelObjects++;
						outStats.println("object: " + topLevelObject.getC());
					}
					outStats.print(topLevelDomain.getD());
					outStats.println("          " + topLevelObjectsCounter);
				}
			}
		}
		outStats.println("No. of top-level domains:" + topLevelDomainsCounter);
		outStats.println("No. of objects at the top level:" + allTopLevelObjects);
	}

	public void oOGObjectsReport(IObject rootObject) throws IOException {

		listOfObjects = new Hashtable<String, GraphMetricItem>();
		listOfObjects.clear();
		listOfDomains = new Hashtable<String, GraphMetricItem>();
		listOfDomains.clear();
		listOfParentObjects = new Hashtable<String, IObject>();
		listOfParentObjects.clear();
		
		String qualifiedTypeName = ObjectsUtils.extractQualifiedName(rootObject.getTraceability());
		int scattering = ObjectsUtils.getScattering(rootObject.getTraceability());
		GraphMetricItem rootMetricItem = new GraphMetricItem(rootObject.getO_id(),
		        NodeType.ORoot,
		        1,
		        getInDegreePtEdges(rootObject),
		        getOutDegreePtEdges(rootObject),
		        getInDegreePtEdges(rootObject),
		        getInOwnDegree(rootObject),
		        getOutOwnDegree(rootObject),
		        getNoPublicDomains(rootObject),
		        getNoPrivateDomains(rootObject),
		        false,
		        qualifiedTypeName,
		        getNoTrLinks(rootObject),
		        rootObject.getTypeDisplayName(), // TOMAR: TODO: Fix me.
		        scattering);

		listOfObjects.put(rootObject.getO_id() , rootMetricItem);
		listOfParentObjects.put(rootObject.getO_id(), rootObject /* TOMAR: TODO: HIGH. fix this*/); //dummy object does not have a parent (::SHARED)
		// System.out.println("OObject: " + rootObject.getO_id());
		visitAllObjects(rootObject.getChildren(), rootObject, 2);

		outStats.println("The OOG depth: " + getOOGDepth());
		outStats.println("No. of objects in the OGraph: " + noOfAllObjects);
	}

	private int getNoPrivateDomains(IObject runtimeObject) {
		if (runtimeObject.equals(runtimeModel.getRoot()))
			return 0; // we don't count top level domains as public domains
		if (runtimeObject.hasChildren()) {
			int count = 0;
			for (IDomain dom : runtimeObject.getChildren())
				// HACK: do not count domains that are empty to avoid "JFrame::"
				if (!dom.isPublic() && (dom.hasChildren()))
					count++;
			return count;
		}
		return 0;
	}

	private int getNoPublicDomains(IObject runtimeObject) {
		if (runtimeObject.equals(runtimeModel.getRoot()))
			return 0; // we don't count top level domains as public domains
		if (runtimeObject.hasChildren()) {
			int count = 0;
			for (IDomain dom : runtimeObject.getChildren())
				if (dom.isPublic())
					count++;
			return count;
		}
		return 0;
	}

	private int getInOwnDegree(IObject rootObject) {
		return 1;
	}

	/**
	 * @param rootObject
	 * @return
	 */
	private int getOutOwnDegree(IObject rootObject) {
		Set<IDomain> children = rootObject.getChildren();
		return children.size();
	}

	private int getOutDegreePtEdges(IObject oObject) {
		int outDegree = 0;
		Set<IEdge> edges = runtimeModel.getEdges();
		if (edges != null)
			for (IEdge edge : edges)
				if (edge.getOsrc().equals(oObject))
					outDegree++;
		return outDegree;
	}

	private int getInDegreePtEdges(IObject oObject) {
		int inDegree = 0;
		Set<IEdge> edges = runtimeModel.getEdges();
		if (edges != null)
			for (IEdge edge : edges)
				if (edge.getOdst().equals(oObject))
					inDegree++;
		return inDegree;
	}

	public long getTotalTCE() {
		long total = 0;
		Set<IEdge> edges = runtimeModel.getEdges();
		for (IEdge re : edges) {
			total += getNoTrLinks(re);
		}
		return total;
	}

	private long getNoTrLinks(IEdge re) {
		Set<BaseTraceability> traceability = re.getTraceability();
		if (traceability != null)
			return traceability.size();
		return 0;
	}
	
	private void visitAllObjects(Set<IDomain> domains, IObject parentObject, int depthCounterTemp)
	        throws IOException {

		for (IDomain runtimeDomain : domains) {
			// if you are about to visit a domain that was already visited cycle found
			// HACK: use D_ID instead after making sure it is fixed.
			// String runtimeDomainId = parentObject.getTypeDomainIds()+"_"+IDomain.getQualifiedName();
			String runtimeDomainId = runtimeDomain.getD_id();
			if (runtimeDomainId != null) {
				if (!listOfDomains.containsKey(runtimeDomainId)) {
					// depthCounterTemp++;
					NodeType nodeType = NodeType.PrD;
					if (runtimeDomain.isPublic())
						nodeType = NodeType.PD;
					
					if(runtimeDomain.getD().equals("SHARED")) 
						nodeType = NodeType.SHARED;
					
					// if (depthCounterTemp == 2) nodeType = NodeType.TLD; use depth to check for TLD
					GraphMetricItem gmid = new GraphMetricItem(runtimeDomainId,
					        nodeType,
					        depthCounterTemp,
					        0,
					        0,
					        1,
					        runtimeDomain.getChildren().size(),
					        runtimeDomain.isPublic());
					listOfDomains.put(runtimeDomainId, gmid);
					// DEBUG: check that domain id is uniquely identified
					// System.out.println("ODomain: " + runtimeDomainId);
					for (IObject runtimeObject : runtimeDomain.getChildren()) {
						// String objectid = runtimeObject.getObjectId()+"_"+runtimeObject.getInstanceDisplayName();
						listOfParentObjects.put(runtimeObject.getO_id(),parentObject);
						String objectid = runtimeObject.getO_id();
						noOfAllObjects++;

						// DEBUG: check that O_id is uniquely identified
						// System.out.println("OObject: " + runtimeObject.getO_id());
						nodeType = getNodeType(runtimeObject);
						String qualifiedTypeName = ObjectsUtils.extractQualifiedName(runtimeObject.getTraceability());
						int scattering = ObjectsUtils.getScattering(runtimeObject.getTraceability());
						GraphMetricItem gmi = new GraphMetricItem(objectid,
						        nodeType,
						        depthCounterTemp,
						        getInDegreePtEdges(runtimeObject),
						        getOutDegreePtEdges(runtimeObject),
						        getInOusidePtEdges(runtimeObject, parentObject),
						        getInOwnDegree(runtimeObject),
						        getOutOwnDegree(runtimeObject),
						        getNoPublicDomains(runtimeObject),
						        getNoPrivateDomains(runtimeObject),
						        runtimeDomain.isPublic(),
						        qualifiedTypeName,
						        getNoTrLinks(runtimeObject),
						        runtimeObject.getTypeDisplayName(), // TOMAR: TODO: Fix me
						        scattering);

						listOfObjects.put(objectid, gmi);

						if (runtimeObject.hasChildren()) {
							visitAllObjects(runtimeObject.getChildren(), runtimeObject, depthCounterTemp + 1);
						}
						else {

							if (treeDepth < depthCounterTemp)
								treeDepth = depthCounterTemp;
							// System.out.println("************************");
							// depthCounterTemp = 1;
						}
					}
				}
				else {
					System.out.println("OGraph: Cycle detected at depth:" + depthCounterTemp);
					System.out.println(parentObject.getO_id());
					System.out.println(runtimeDomainId);
					listOfDomains.get(runtimeDomainId).increaseInOwnDegree();
				}
			}
			else {
				// HACK: Why should this ever be null?
				int debug = 0;
				debug++;
				System.out.println("Unexpected null domain id");
				continue;

			}
		}
	}

	private Set<IObject> getChildrenObjects(IObject parentObject) {
		Set<IObject> childrenObjects = new HashSet<IObject>();
		for (IDomain dom : parentObject.getChildren()) {
			for (IObject child : dom.getChildren())
				childrenObjects.add(child);
		}
		return childrenObjects;
	}

	private int getInOusidePtEdges(IObject runtimeObject, IObject parentObject) {

		if (parentObject == null) // HACK: this should not happen
			return getInDegreePtEdges(runtimeObject);
		else {
			int inDegree = 0;
			Set<IEdge> edges = runtimeModel.getEdges();
			Set<IObject> siblings = getChildrenObjects(parentObject);
			if (siblings.size() == 0)
				return getInDegreePtEdges(runtimeObject);
			if (edges != null)
				for (IEdge edge : edges) {
					IObject toObject = edge.getOdst();
					if ((toObject.equals(runtimeObject)) && (!siblings.contains(edge.getOsrc())))
						inDegree++;
				}
			return inDegree;
		}
	}

	public int getNoTrLinks(IObject runtimeObject) {
		int number = 0;
		Set<BaseTraceability> traceability = runtimeObject.getTraceability();
		if (traceability != null)
			number = traceability.size();
		return number;
	}

//TODO: TORAD: HIGH. XXX. use the method from ObjectUtils 
	private NodeType getNodeType(IObject runtimeObject) {
		if (runtimeObject.equals(runtimeModel.getRoot()))
			return NodeType.ORoot;
		String qualfiedName = ObjectsUtils.extractQualifiedName(runtimeObject.getTraceability());
		if (ObjectsUtils.isLowLevelObject(qualfiedName))
			return NodeType.LLO;
		return NodeType.O;
	}

	public int getOOGDepth() {
		return treeDepth;
	}

	public OGraph getRuntimeModel() {
		return runtimeModel;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public long getNoOfEdges() {
		return runtimeModel.getEdges().size();
	}
}