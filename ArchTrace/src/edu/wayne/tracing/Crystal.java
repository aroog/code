package edu.wayne.tracing;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import edu.wayne.tracing.internal.CrystalRuntimeException;
import edu.wayne.tracing.internal.StudentRuntimeException;
import edu.wayne.tracing.internal.WorkspaceUtilities;

/**
 * Maintains a list of analyses to perform on each build. Provides output mechanisms for both the Static Analysis
 * developer and the Static Analysis user. Also maintains several useful data structures concerning the target
 * workspace. They can be accessed through several "get*" methods.
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 */
public class Crystal {
	/**
	 * Static. Singleton
	 */
	private static Crystal theInstance = null;

	private IJavaProject javaProject = null;

	private HashSet<UserProblemKey> hashSet = new HashSet<UserProblemKey>();
	
	/**
	 * Static. Retrieves the Crystal singleton object
	 */
	public static Crystal getInstance() {
		if (Crystal.theInstance == null) {
			Crystal.theInstance = new Crystal();
		}
		return Crystal.theInstance;
	}

	// XXX: Make these data structures immutable (ie unchangable)
	/**
	 * the list of analyses to perfrom
	 */
	private LinkedList<ICrystalAnalysis> analyses = null;

	/**
	 * the list of all compilation units in the workspace
	 */
	private List<ICompilationUnit> compilationUnits = null;

	/**
	 * a mapping between all CompilationUnits and their corresponding ASTNode.
	 */
	private Map<ICompilationUnit, ASTNode> parsedCompilationUnits = null;

	/**
	 * the list of all methods in the workspace
	 */
	private List<MethodDeclaration> methods = null;

	/**
	 * the list of all bindings in the workspace
	 */
	private Map<String, ASTNode> bindings = null;

	private Map<String, ITypeBinding> mapNameToTypeBinding = new Hashtable<String, ITypeBinding>();
	
	private Map<String, ITypeBinding> mapKeyToTypeBinding = new Hashtable<String, ITypeBinding>();

	private IFolder aliasXML;
	
	private IFolder srcFolder;

	/**
	 * Clear the hashtables to reclaim some memory
	 */
	public void reset() {

		if (bindings != null) {
			bindings.clear();
			bindings = null;
		}

		if (mapNameToTypeBinding != null) {
			mapNameToTypeBinding.clear();
		}
		
		if (mapKeyToTypeBinding != null) {
			mapKeyToTypeBinding.clear();
		}

		if (compilationUnits != null) {
			compilationUnits.clear();
			compilationUnits = null;
		}

		if (parsedCompilationUnits != null) {
			parsedCompilationUnits.clear();
			parsedCompilationUnits = null;
		}

		if (methods != null) {
			methods.clear();
			methods = null;
		}
		
		if ( hashSet != null ) {
			hashSet.clear();
		}
	}

	/**
	 * Constructor
	 */
	private Crystal() {
		analyses = new LinkedList<ICrystalAnalysis>();
	}

	/**
	 * Returns a PrintWriter that can be used to ouptut text to a console the user can see. Currently this text will
	 * goto the "UserConsole", a console in the child-eclipse window that the user must enable through the "Window"
	 * menu.
	 */
	public PrintWriter userOut() {
		return new PrintWriter(System.out, true);
	}

	/**
	 * Returns a PrintWriter that can be used to ouptut text to a console the static-analysis developer can see.
	 * Currently this text will goto the parent-eclipse's standard console.
	 */
	public PrintWriter debugOut() {
		return new PrintWriter(System.out, true);
	}

	/**
	 * Reports a problem in the problems window the user can see. Providing relevant problem information like
	 * "Description", "Resource", "Resource Folder" and "Line Location" whenever possible.
	 * 
	 * @param problemDescription the text describing the problem
	 * @param node the {@link #ASTNode} where the problem occured
	 * @param analysis the analysis where the problem occured
	 */
	public void reportUserProblem(String problemDescription, ASTNode node, ICrystalAnalysis analysis) {
		// if(node == null)
		// throw new StudentRuntimeException("null ASTNode argument in reportUserProblem");
		// if(analysis == null)
		// throw new StudentRuntimeException("null analysis argument in reportUserProblem");

		// HACK: Why intern() strings here? This is pretty expensive!
		UserProblemKey key = new UserProblemKey(node, analysis, problemDescription.intern());
		hashSet.add(key);
	}

	/**
	 * Report the errors, after filtering out the duplicates
	 */
	public void finish() {

		// Retrieve the IResource that contains the ASTNode.
		IResource resource = null;

//		int count = 0;
		Iterator<UserProblemKey> iter = hashSet.iterator();
		while(iter.hasNext()) {
			UserProblemKey element = iter.next();
			// TODO: Come up with a better solution for this!
			// M.A.A.: TEMPORARY. Fix to avoid generating too many errors!
//			count++;
			// Show only 5,000 errors
//			if ( count == 5000 ) {
//				break;
//			}
			ASTNode node = element.getNode();
			ICrystalAnalysis analysis = element.getAnalysis();
			String problemDescription = element.getProblemDescription();

			if ( node != null ) {
				ASTNode root = node.getRoot();
				// Identify the closest resource to the ASTNode,
				// otherwise fall back to using the high-level workspace root.
				if (root != null && root.getNodeType() == ASTNode.COMPILATION_UNIT) {
					CompilationUnit cu = (CompilationUnit) root;
					IJavaElement je = cu.getJavaElement();
					resource = je.getResource();
				}
			}
			else {
				// Use the high-level Workspace
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
			
			// Create the marker
			try {
				IMarker marker = resource.createMarker(IMarker.PROBLEM);
				if (node != null) {
					int startPosition = node.getStartPosition();
					int length = node.getLength();
					marker.setAttribute(IMarker.CHAR_START, startPosition);
					marker.setAttribute(IMarker.CHAR_END, startPosition + length);
					CompilationUnit cu = (CompilationUnit) node.getRoot();
					marker.setAttribute(IMarker.LINE_NUMBER, cu.lineNumber(startPosition));
				}
				StringBuffer buffer = new StringBuffer();
				// NOTE: Do not append analysis name!
				// if ( analysis != null ) {
				// buffer.append("[" + analysis.getName() + "]: ");
				// }
				buffer.append(problemDescription);
				marker.setAttribute(IMarker.MESSAGE,buffer.toString());
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			}
			catch (CoreException ce) {
				System.out.println("CoreException when creating marker");
				return;
			}
		}

		// Clear the hashtable!
		hashSet.clear();
	}

	/**
	 * Registers an analysis with the framework. All analyses must be registered in order for them to be invoked.
	 * 
	 * @param analysis the analysis to be used
	 */
	public void registerAnalysis(ICrystalAnalysis analysis) {
		analyses.add(analysis);
	}
	
	/**
	 * Unregisters an analysis with the framework. All analyses must be registered in order for them to be invoked.
	 * 
	 * @param analysis the analysis to be unregistered
	 */
	public void unregisterAnalysis(ICrystalAnalysis analysis) {
		analyses.remove(analysis);
	}


	/**
	 * Executes all registered analyses
	 */
	public void runAnalyses() {
		PrintWriter output = debugOut();
		PrintWriter user = userOut();

		if (analyses == null) {
			output.println("Crystal::runAnalyses() No analyses registered");
			return;
		}

		try {
			// TODO: Do not just clear all markers. Clear Crystal markers only
			// XXX. Clear Markers
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			try {
				root.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
			}
			catch (CoreException ce) {
				output.println("Could not clear markers");
			}
			/*
			 * Reset and gather important workspace structures
			 */
			if (scanWorkspace()) {
				
				/*
				 * Iterate through all analyses in the list, and run them.
				 */
				Iterator<ICrystalAnalysis> i = analyses.iterator();
				ICrystalAnalysis crystalAnalysis;
				String analysisName;
				for (; i.hasNext();) {
					crystalAnalysis = i.next();
					analysisName = crystalAnalysis.getName();
					output.println("Crystal::runAnalyses() Begin [" + analysisName + "] Analysis");
					crystalAnalysis.runAnalysis();
					output.println("Crystal::runAnalyses() End [" + analysisName + "] Analysis");
				}
			}
		}
		catch (Exception e) {
			String message = "";

			if (e instanceof CrystalRuntimeException) {
				message += "\n*** INTERNAL CRYSTAL EXCEPTION ***";
			}
			else if (e instanceof StudentRuntimeException) {
				message += "\n*** ANALYSIS EXCEPTION ***";
			}
			else {
				message += "\n*** EXCEPTION ***";
			}
			message += "\nStack Trace:";
			output.println(message);
			e.printStackTrace(output);
			user.println(message);
			e.printStackTrace(user);
		}
	}

	/**
	 * Updates the Crystal knowledge of the workspace. If the workspace changes, then this method must be recalled in
	 * order for Crystal to get the changes. Unexpected behavior may result from not rescanning the workspace. return
	 * true if the scan is successful, false if there were problems
	 */
	public boolean scanWorkspace() {
		// Clear Bindings cache
		bindings = null;

		compilationUnits = WorkspaceUtilities.scanForCompilationUnits();
		if (compilationUnits == null) {
			System.out.println("No Compilation Units found");
			return false;
		}
		parsedCompilationUnits = WorkspaceUtilities.parseCompilationUnits(compilationUnits);
		if (parsedCompilationUnits == null) {
			System.out.println("Could not parse compilation units");
			return false;
		}

		bindings = WorkspaceUtilities.scanForBindings(parsedCompilationUnits,mapKeyToTypeBinding,mapNameToTypeBinding);
		if (bindings == null) {
			System.out.println("Could not parse type bindings");
			return false;
		}

		methods = WorkspaceUtilities.scanForMethodDeclarations(parsedCompilationUnits);
		if (methods == null) {
			System.out.println("Could not find any methods in the parsed compilation units");
			return false;
		}

		return true;
	}

	/**
	 * Returns an iterator for the list of all methods in the workspace.
	 * 
	 * @return the iterator for all methods in the workspace, null if no methods
	 */
	public Iterator<MethodDeclaration> getMethodListIterator() {
		if (methods == null) {
			return null;
		}
		return methods.iterator();
	}

	/**
	 * Returns an iterator for the list of all compilation units in the workspace.
	 * 
	 * @return the iterator of compilation units, null if no compilation units
	 */
	public Iterator<ICompilationUnit> getCompilationUnitIterator() {
		if (compilationUnits == null) {
			return null;
		}
		return compilationUnits.iterator();
	}

	/**
	 * Retrieves the ASTNode that resulted from parsing a Compilation unit.
	 * 
	 * @param compUnit the compilation unit whose ASTNode is desired
	 * @return the ASTNode of the compilation unit, or null if the mapping doesn't exist
	 */
	public ASTNode getASTNodeFromCompilationUnit(ICompilationUnit compUnit) {
		if ((compUnit == null) || (parsedCompilationUnits == null)) {
			return null;
		}
		return parsedCompilationUnits.get(compUnit);
	}

	/**
	 * Retrieves the declaring ASTNode of the binding. The first time this method is called, the mapping between
	 * bindings and nodes is created. The creation time will depend on the size of the workspace. Subsequent calls will
	 * simply look up the values from a mapping.
	 * 
	 * @param binding the binding from which you want the declaration
	 * @return the declaration node
	 */
	public ASTNode getASTNodeFromBinding(IBinding binding) {
		ASTNode node = null;
		if (bindings == null) {
			throw new CrystalRuntimeException("Crystal::getASTNodeFromBinding: An error occured while creating the binding -> declarations mapping");
		}
		
		if (binding != null) {
			node = bindings.get(binding.getKey());
		}
		return node;
	}

	// HACK: Avoid scanning linearly the list of compilation units; there could be hundreds of them
	// Maybe cache the result
	// Avoid premature optimization for now
	public ASTNode getDeclaringNode(IBinding binding) {
		ASTNode node = null;

		for (ASTNode compUnit : parsedCompilationUnits.values()) {
			if (compUnit instanceof CompilationUnit) {
				CompilationUnit compilationUnit = (CompilationUnit) compUnit;
				node = compilationUnit.findDeclaringNode(binding);
				if (node != null) {
					break;
				}
			}
		}

		return node;
	}

	// HACK: Avoid scanning linearly the list of compilation units; there could be hundreds of them
	// Maybe cache the result
	// Avoid premature optimization for now
	public ASTNode getDeclaringNode(String key) {
		ASTNode node = null;

		for (ASTNode compUnit : parsedCompilationUnits.values()) {
			if (compUnit instanceof CompilationUnit) {
				CompilationUnit compilationUnit = (CompilationUnit) compUnit;
				node = compilationUnit.findDeclaringNode(key);
				if (node != null) {
					break;
				}
			}
		}

		return node;
	}

	public ASTNode getASTNodeFromBindingKey(String key) {
		if (bindings == null) {
			throw new CrystalRuntimeException("Crystal::getASTNodeFromBinding: An error occured while creating the binding -> declarations mapping");
		}
		return bindings.get(key);
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public void setJavaProject(IJavaProject javaProject) {
		this.javaProject = javaProject;
		WorkspaceUtilities.setJavaProject(javaProject);
		WorkspaceUtilities.init();
		updateAliasXML();
	}

	private void updateAliasXML() {
		if (javaProject != null) {
			aliasXML = javaProject.getProject().getFolder("aliasxml");
			if ( !aliasXML.exists() ) {
				try {
					aliasXML.create(true, true, null);
				}
				catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public IFolder getAliasXML() {
		return aliasXML;
	}

	public MethodDeclaration getMethodDeclaration(IMethodBinding constructorBinding) {
		// return (MethodDeclaration) getASTNodeFromBinding( constructorBinding );
		return (MethodDeclaration) getDeclaringNode(constructorBinding);
	}

	public TypeDeclaration getTypeDeclaration(ITypeBinding binding) {
		// ASTNode nodeFromBinding = crystal.getASTNodeFromBinding(binding);
		// if ( nodeFromBinding instanceof TypeDeclaration )
		// return (TypeDeclaration) nodeFromBinding;
		//		
		// return null;

		ASTNode nodeFromBinding = getDeclaringNode(binding);
		if (nodeFromBinding instanceof TypeDeclaration) {
			return (TypeDeclaration) nodeFromBinding;
		}

		return null;
	}

	public ICompilationUnit getComplationUnit(CompilationUnit node) {
		Set<Entry<ICompilationUnit, ASTNode>> entrySet = parsedCompilationUnits.entrySet();
		Iterator<Entry<ICompilationUnit, ASTNode>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<ICompilationUnit, ASTNode> entry = iterator.next();
			if (entry.getValue() == node) {
				return entry.getKey();
			}
		}

		return null;
	}

	public VariableDeclaration getVariableDeclaration(IVariableBinding binding) {
		ASTNode declaringNode = getDeclaringNode(binding);
		if ( declaringNode instanceof VariableDeclaration) {
			return (VariableDeclaration) declaringNode;
		}
		return null;
	}

	public IFolder getSrcFolder() {
		return srcFolder;
	}

	public void setSrcFolder(IFolder srcPath) {
		this.srcFolder = srcPath;
	}

	public Map<String, ITypeBinding> getMapKeyToTypeBindings() {
    	return mapKeyToTypeBinding;
    }
	
	public Map<String, ITypeBinding> getMapNameToTypeBindings() {
    	return mapNameToTypeBinding;
    }
}