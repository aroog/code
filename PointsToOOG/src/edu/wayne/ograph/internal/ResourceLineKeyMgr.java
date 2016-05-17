package edu.wayne.ograph.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.wayne.ograph.ResourceLineKey;

public class ResourceLineKeyMgr {
	
	private static ResourceLineKeyMgr instance;
	private ResourceLineKeyMgr() {
		
	}
	
	public static ResourceLineKeyMgr getInstance() {
		if (instance == null ) {
			instance = new ResourceLineKeyMgr();
		}
		return instance;
	}

	private String basePath;
	
	/**
	 * HACK: Optimize this method, as it is called a lot! 
	 * We could consider turning off the generation of traceability information to speed things up, while the OOG is being refined.
	 */
	public ResourceLineKey getResourceLine(ASTNode node) {
		
		// XXX. Why should this be null? Trace to code from AliasXML?
		if (node != null) {
			// Retrieve the IResource that contains the ASTNode.
			IResource resource = null;

			ASTNode root = node.getRoot();
			CompilationUnit cu = null;

			// Identify the closest resource to the ASTNode,
			// otherwise fall back to using the high-level workspace root.
			if (root != null && root.getNodeType() == ASTNode.COMPILATION_UNIT) {
				cu = (CompilationUnit) root;
				IJavaElement je = cu.getJavaElement();
				resource = je.getResource();
			}
			else {
				// Use the high-level Workspace
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}

			if (resource != null && cu != null) {
				int startPosition = node.getStartPosition();
				String absolutePath = resource.getLocation().toOSString();
				// Make relative; otherwise, path will get very bloated...
				return new ResourceLineKey(makeRelative(absolutePath, basePath), cu.lineNumber(startPosition));
			}
		}

		return new ResourceLineKey();
	}
	
	// XXX. Add counterpart to make absolute...
	// So we can restore full path and simply read the file at that location...
	private static String makeRelative(String path, String origin) {
		String relative = path;
		if ( relative.startsWith(origin) ) {
			relative = relative.substring(origin.length());
		}
		
		return relative;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath; 
    }

}
