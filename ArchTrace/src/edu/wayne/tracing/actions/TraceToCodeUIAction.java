package edu.wayne.tracing.actions;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import ast.BaseTraceability;
import edu.wayne.tracing.Crystal;
import edu.wayne.tracing.internal.WorkspaceUtilities;

// XXX. This will fail if at least one Java file is not open...
public class TraceToCodeUIAction implements IWorkbenchWindowActionDelegate {

	/**
	 * Set to true to display the paths of this compilation units (slows things down!)
	 */
	private final boolean DEBUG_OUTPUT_PATHS = false;
	
	private IWorkbenchWindow window = null;
	
	public static boolean  highlightCode =  true;
	private String selectedString = null;
	private BaseTraceability baseTraceabilityObject = null;

	private DeclarationVisitor visitor;

	// XXX. This is very hackish...
	// XXX. Every instance scans the workspace! 
	public TraceToCodeUIAction() {
		if(window==null){
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		// Check again that the window is not null!
		if ( window != null ) {
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);
		if (currentProject != null) {
			IJavaProject javaProject = WorkspaceUtilities.getJavaProject(currentProject.getName());
			Crystal.getInstance().setJavaProject(javaProject);
		}
		else {
	/*	 		
	 * MessageBox box = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
				box.setMessage("Please open a Java file in the project to analyze");
				box.open();*/
			return;
		}
/*		// Hackish way to get the selection; ideally, the input should come from some XML file.
		this.selectedString = getCurrentSelection();
		//Hackish way to test loading xml files the selected string to figure out which xml test file to load
		int selection = Integer.parseInt(selectedString);

		baseTraceabilityObject = TestUtils.getFile(selection);*/
		
		//TODO: HIGH. XXX. Move this else where, to ensure that it is run only once.
		// Technically, the Actions are not singletons; multiple instances could get created.
		Crystal crystal = Crystal.getInstance();
		crystal.scanWorkspace();
		}
	}
	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 * 
	 */
	public void run(IAction action) {
		
		Crystal crystal = Crystal.getInstance();
		PrintWriter output = crystal.userOut();
		if(baseTraceabilityObject!=null){

			Iterator<ICompilationUnit> unitIterator = crystal.getCompilationUnitIterator();
			ICompilationUnit compUnit = null;
			for (; unitIterator.hasNext();) {
				compUnit = unitIterator.next();
				if (compUnit == null) {
					output.println("AbstractCompilationUnitAnalysis: null CompilationUnit");
					continue;
				}
				// Retrieve the path of this compilation unit, and output it
				if (DEBUG_OUTPUT_PATHS) {
					try {
						IResource resource = compUnit.getCorrespondingResource();
						if (resource != null) {
							IPath path = resource.getLocation();
							if (path != null) {
								output.println(path.toPortableString());
							}
						}
					}
					catch (JavaModelException e) {
						output.println("AbstractCompilationUnitAnalysis: Unable to retrieve path of CompilationUnit"
								+ compUnit.getElementName());
					}
				}
				// Obtain the AST for this CompilationUnit and analyze it
				ASTNode node = crystal.getASTNodeFromCompilationUnit(compUnit);
				if ((node != null) && (node instanceof CompilationUnit)) {
					analyzeCompilationUnit((CompilationUnit) node, compUnit);
				}
				else {
					output.println("AbstractCompilationUnitAnalysis: Could not retrieve the ASTNode for CompilationUnit "
					        + compUnit.getElementName());
				}
			}
		}
	}

	// TODO: Start here.
	private void analyzeCompilationUnit(CompilationUnit unit, ICompilationUnit compilationUnit) {
		List types = unit.types();
		for (Iterator iter = types.iterator(); iter.hasNext();) {

			Object next = iter.next();
			if (next instanceof TypeDeclaration) {
				TypeDeclaration declaration = (TypeDeclaration) next;
				traverseType(declaration);
			}
		}

	}

	// At this point, the selectedString contains a "string"
	// TODO: Look for the selectedString in the TypeDeclaration
	// Traverse things recursively...
	private void traverseType(TypeDeclaration declaration) {
		if(baseTraceabilityObject !=null ){
			DeclarationVisitor visitor = new DeclarationVisitor(declaration, baseTraceabilityObject);
			if(visitor.getEnclosingDeclaration()!=null || visitor.getExpressions() !=null){
				this.visitor = visitor;
			}
		}	
	} 
	

	public DeclarationVisitor getVisitor() {
		return visitor;
	}

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	// TODO: Move to a Utils class
	private static String getCurrentSelection() {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
			IDocumentProvider prov = editor.getDocumentProvider();
			IDocument doc = prov.getDocument(editor.getEditorInput());
			ISelection sel = editor.getSelectionProvider().getSelection();
			if (sel instanceof TextSelection) {
				ITextSelection textSel = (ITextSelection) sel;
				return textSel.getText();
			}
		}
		return null;
	}
	
	public void setTraceability(BaseTraceability baseTraceability){
		this.baseTraceabilityObject = baseTraceability;
	}

	
	public static void setHighlightCode(boolean b){
		highlightCode = b;
	}

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void dispose() {

	}

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
