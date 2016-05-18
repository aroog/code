package moogrex.testing;

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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import edu.wayne.ograph.OGraph;
import oog.common.OGraphFacade;
import oog.itf.IObject;
import oog.re.PushIntoOwned;
import oog.re.PushIntoPD;
import oog.re.PushIntoParam;
import oog.re.Refinement;
import oog.re.RefinementModel;
import oog.re.SplitUp;

public class BatchSetup {

	
	// Maps Oid -> OObject, for the OGraph (for OOGRE refinements)
	private OObjectKeyMapper helper;
	private OGraph oGraph;
	private RefinementModel model;
	private OGraphFacade facade;

	// TODO: Rename: reloadGraph?
	// Factor out initMotherFacade()
	public void setFacade() {
		// Get the facade
		moogrex.plugin.Activator default1 = moogrex.plugin.Activator.getDefault();
		facade = default1.getMotherFacade();
		
//		facade = MotherFacade.getInstance();
	}
	
	public void loadGraph() {
		oGraph = facade.getGraph();
		
		if (oGraph == null ) {
			String errorMsg = "No graph is set on the Facade. Make sure to extract it first!";
			System.err.println(errorMsg);
			return;
		}
		
		// Update helper since the mappings may have changed
		helper = new OObjectKeyMapper(oGraph);
	}
	
	public void loadModel() {
		IJavaProject javaProjext  = getFirstJavaProject();
		if(javaProjext != null ) {
			IProject project = javaProjext.getProject();
			String filePath = getFilePath(project, "oogre.xml");
			
			if (filePath != null) {
				model = oog.re.Persist.load(filePath);
			}
		}
    }
	
	/**
	 * TODO: Rename:
	 * @param bRef batch refinement
	 * 
	 * @return Facade refinement object or null
	 */
	public Refinement doRefinement(Refinement bRef)  {
		Refinement facadeObject = getFacadeObject(bRef);
		
		if(facadeObject != null ) {
			RefinementModel refinementModel = facade.getRefinementModel();
			// Add to the facade
			refinementModel.add(facadeObject);
		}
		else {
			System.err.println("XXX. CANNOT resolve Facade Object");
		}
		
		return facadeObject;
	}
	
	
	/**
	 * Copy the batch refinement into a new refinement object, resolving ObjectKeys to IObjects in the process
	 * 
	 * @param bRef
	 * @return
	 */
	public Refinement getFacadeObject(Refinement bRef) {
		String refType = bRef.getClass().getSimpleName();
		String srcObjectKey = bRef.getSrcObject();
		String dstObjectKey = bRef.getDstObject();
		String dstDomain = bRef.getDomainName(); 
		
		Refinement ref = null;

		// Gotta set the IObjects. Setting string's is not going to work.
		IObject srcObject = helper.getElement(srcObjectKey);
		
		// Could not find these objects. 
		if (srcObject == null && !refType.equals("SplitUp"))  {
			System.err.println("Cannot resolve IObject from key " + srcObjectKey);
			return null;
		}
		
		IObject dstObject = helper.getElement(dstObjectKey);
		if (dstObject == null )  {
			System.err.println("Cannot resolve IObject from key" + dstObjectKey);
			return null;
		}
		
		if (refType.equals("PushIntoOwned") && dstDomain.equals("owned")) {
			ref = new PushIntoOwned(srcObject, dstObject, dstDomain);
		}
		else if (refType.equals("PushIntoPD") && dstDomain.equals("PD")) {
			ref = new PushIntoPD(srcObject, dstObject, dstDomain);
		}
		else if (refType.equals("PushIntoParam") &&  dstDomain.equals("PARAM")) {
			ref = new PushIntoParam(srcObject, dstObject, dstDomain);
		}
		else if (refType.equals("SplitUp") ){
				SplitUp spu = new SplitUp(srcObject, dstObject, dstDomain);
				
				SplitUp bSpu = (SplitUp)bRef;
				// Copy the extended properties from  SplitUp
				spu.setName(bSpu.getName());
				spu.setKind(bSpu.getKind());
				spu.setType(bSpu.getType());
				spu.setEnclosingMethod(bSpu.getEnclosingMethod());
				spu.setEnclosingType(bSpu.getEnclosingType());
				
				ref = spu;
		}
		
		return ref;
	}
	
	// XXX. Duplicated code
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


	// XXX. Duplicated code
	private static IJavaProject getFirstJavaProject() {
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
		IJavaProject project = null;
		if (javacore != null) {
			IJavaProject[] javaProjects = null;
            try {
	            javaProjects = javacore.getJavaProjects();
            }
            catch (JavaModelException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
			if (javaProjects != null && javaProjects.length == 1) {
				project = javaProjects [0];
				if (javaProjects.length > 1 ) {
					System.err.println("Multiple open projects");
				}
			}
		}

		return project;
	}

	public RefinementModel getBatchModel() {
    	return model;
    }

}
