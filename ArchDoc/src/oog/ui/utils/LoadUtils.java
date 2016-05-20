package oog.ui.utils;


import java.util.HashSet;
import java.util.Set;

import oog.common.OGraphFacade;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import ast.Type;
import ast.TypeInfo;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OOGUtils;
import edu.wayne.summary.Crystal;
import edu.wayne.summary.internal.WorkspaceUtilities;
import edu.wayne.summary.traceability.ReverseTraceabilityMap;
import edu.wayne.summary.utils.JavaElementUtils;



/**
 * XXX. Lots of hacks here. Need deep cleaning.
 *
 */
public class LoadUtils {
	// XXX. Turn off ReverseTraceabilityMap to speed things up. But disables Related Objects/Edges
	private static final boolean ENABLE_REVERSE_TRACEABILITY = false;
	
	private static final boolean LOAD_FROM_XML = false;
	
	// HACK: Avoid all these static fields
	private static IJavaProject javaProject;
	private static IPath location;

	private static String path;
	
	/**
	 * This method does a bunch of initialization. It requires the OOG.xml.gz to be present.
	 * But does not actually load it. 
	 *
	 * XXX. Make this less brittle.
	 * 
	 */
	public static OGraph loadModel() {
		OGraph oGraph = null;
		
		// XXX. Locate the file, but do not load it! getXMLFile() has a number of other side-effects
		// like setting the Java project, etc. 
		// XXX. If we stop generating OOG.xml.gz, path will be null, and the rest of the initialization will stop!
		String path = getXMLFile();
		
		// Load from XML
		if (LOAD_FROM_XML) {
			
			if(path == null)
				return null;
			
			oGraph = OOGUtils.loadGZIP(path);
			
			if (oGraph == null ) {
				System.err.println("Could not load the graph from " + path);
			}			
		}
		else { 		// Load from Facade
			oog.ui.Activator default1 = oog.ui.Activator.getDefault();
			OGraphFacade motherFacade = default1.getMotherFacade();
			oGraph = motherFacade.getGraph();
			if (oGraph == null ) {
				String errorMsg = "No graph is set on the Facade. Make sure to extract it first!";
				System.err.println(errorMsg);
				
				// XXX. Could have Invalid thread access. AVOID
				// Shell activeShell = Display.getDefault().getActiveShell();
				// if (activeShell != null) {
				// MessageBox box = new MessageBox(activeShell, SWT.ICON_ERROR);
				// box.setMessage(errorMsg);
				// box.open();
				// }
			}
		}

		// XXX. Check that WorkspaceUtilities.javaProject has been set
		// XXX. Run ArchSummary's Crystal2. This is a bad idea here.
		if (oGraph != null && javaProject != null ) {
		Crystal crystal = Crystal.getInstance();
		crystal.runAnalyses();
		crystal.finish();
		
		// Initialize ArchSummary engine
		ReverseTraceabilityMap.enableMappingASTNodes(ENABLE_REVERSE_TRACEABILITY);
		edu.wayne.summary.strategies.Utils.loadSummary(oGraph, javaProject);

		// Load information about the types to enable using TypeInfo.
		// XXX. Should we do this multiple times? The type info is unlikely to change during refinements?
		initTypeStructures();
		}
		else {
			System.err.println("Null Java project");
		}

		return oGraph;
	}
	
	private static void initTypeStructures() {
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
	
	private static String getFilePath(IProject currentProject, String filename) {
		String path = null;

		IPath relativePath = new Path(filename);
		IResource findMember = currentProject.findMember(relativePath);
		if (findMember instanceof IFile) {
			IFile file = (IFile) findMember;
			if (file.exists()) {
				path = file.getLocation().toOSString();
			}
		}
		return path;
	}

	/**
	 * Get the first Java project
	 * @return
	 */
	// XXX. Use version: WorkspaceUtilities.getFirstJavaProject
	public static String getXMLFile() {
	
		if (javaProject == null ) {
		// Redo this initialization only if the project or path has not been set already
		// XXX. How will storing the project work when switching projects?
		// Will have to re-start Eclipse.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null) {
			System.out.println("No workspace");
			return null;
		}
		IWorkspaceRoot root = workspace.getRoot();
		if (root == null) {
			System.out.println("No workspace root");
			return null;
		}

		IJavaModel javacore = JavaCore.create(root);// this gets you the java version of the workspace
		if (javacore != null) {
			IJavaProject[] javaProjects = null;
            try {
	            javaProjects = javacore.getJavaProjects();
            }
            catch (JavaModelException e) {
	            e.printStackTrace();
            }
			if (javaProjects != null && javaProjects.length == 1) {
				javaProject = javaProjects [0];
				if (javaProjects.length > 1 ) {
					System.err.println("Multiple open projects");
				}
				if ((javaProject != null) && javaProject.exists()) {
					IPackageFragmentRoot[] allpackroots = null;
					try {
						allpackroots = javaProject.getAllPackageFragmentRoots();
					}
					
					catch (JavaModelException e) {
						e.printStackTrace();
					}
					
					// Locate SRC folder under this IJavaProject
					if ((allpackroots != null) && (allpackroots.length > 0)) {
						IPackageFragmentRoot packFragRoot = allpackroots[0];
						location = packFragRoot.getResource().getLocation();
						
						IProject currentProject = javaProject.getProject();
						path = getFilePath(currentProject, "OOG.xml.gz");
					}
				}
			}
		}

		WorkspaceUtilities.setJavaProject(javaProject);
		JavaElementUtils.setJavaProject(javaProject);
		}
		
		return path;
	}

//	public static IPath getLocation() {
//    	return location;
//    }
	
}
