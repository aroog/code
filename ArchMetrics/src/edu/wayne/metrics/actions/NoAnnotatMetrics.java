package edu.wayne.metrics.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import ast.Type;
import ast.TypeInfo;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.adb.QualAnalyzer;
import edu.wayne.metrics.internal.WorkspaceUtilities;
import edu.wayne.metrics.mapping.Model;
import edu.wayne.metrics.mapping.ModelManager;
import edu.wayne.metrics.mapping.Persist;
import edu.wayne.metrics.qual.QualUtils;
import edu.wayne.metrics.utils.AnnotationsUtils;

public class NoAnnotatMetrics implements IWorkbenchWindowActionDelegate {

	// Current project path and the path of the file
	private String currentProjectPath;

	private IWorkbenchWindow window = null;

	private String noannosMetricPath;

	private String noInstanceFilePath;

	private TypeInfo typeInfo = TypeInfo.getInstance();

	private ArrayList<IVariableBinding> varDeclaration = new ArrayList<IVariableBinding>();

	private ArrayList<ITypeBinding> newExpression = new ArrayList<ITypeBinding>();

	private HashMap<ITypeBinding, String> isNewExpression = new HashMap<ITypeBinding, String>();

	private boolean isInstantiated = false;

	QualUtils utils = QualUtils.getInstance();

	public NoAnnotatMetrics() {

		super();
	}

	public void run(IAction action) {

		AnnotationsUtils.classInfo.clear();
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);
		currentProjectPath = currentProject.getLocation().toOSString();
		String projectName = currentProject.getName();
		noannosMetricPath = currentProjectPath + "\\" + projectName + "_WithoutAnnotat.csv";
		noInstanceFilePath = currentProjectPath + "\\" + projectName + "_Noinstancetypes.csv";

		// Check if one Java file is open
		if (currentProject != null) {
			IJavaProject javaProject = WorkspaceUtilities.getJavaProject(currentProject.getName());
			Crystal.getInstance().setJavaProject(javaProject);
		}
		else {
			MessageBox box = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
			box.setMessage("Please open a Java file in the project to analyze");
			box.open();
			return;
		}
		Crystal crystal = Crystal.getInstance();
		PrintWriter output = crystal.userOut();

		String mapping_file_path = currentProject.getLocation().toOSString() + "\\metrics_map.xml";
		Model model = Persist.load(mapping_file_path);
		ModelManager.getInstance().setModel(model);

		crystal.scanWorkspace();
		Iterator<ICompilationUnit> unitIterator = crystal.getCompilationUnitIterator();
		ICompilationUnit compUnit = null;

		// TODO: do things more efficiently, without multiple passes

		// Pass 1: pre-populate the types
		for (; unitIterator.hasNext();) {
			compUnit = unitIterator.next();
			if (compUnit == null) {
				output.println("AbstractCompilationUnitAnalysis: null CompilationUnit");
				continue;
			}
			// Obtain the AST for this CompilationUnit and analyze it
			ASTNode node = crystal.getASTNodeFromCompilationUnit(compUnit);
			if ((node != null) && (node instanceof CompilationUnit)) {
				try {
					// Convert the ICompilcationUnit to CompilationUnit
					prepopulateTypes((CompilationUnit) node, compUnit);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				output.println("AbstractCompilationUnitAnalysis: Could not retrieve the ASTNode for CompilationUnit "
				        + compUnit.getElementName());
			}
		}

		// Pass 2: analyze the data
		// Reset the iterator
		unitIterator = crystal.getCompilationUnitIterator();
		for (; unitIterator.hasNext();) {
			compUnit = unitIterator.next();
			if (compUnit == null) {
				output.println("AbstractCompilationUnitAnalysis: null CompilationUnit");
				continue;
			}
			// Obtain the AST for this CompilationUnit and analyze it
			ASTNode node = crystal.getASTNodeFromCompilationUnit(compUnit);
			if ((node != null) && (node instanceof CompilationUnit)) {
				try {
					// Convert the ICompilcationUnit to CompilationUnit
					analyzeCompilationUnit((CompilationUnit) node, compUnit);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				output.println("AbstractCompilationUnitAnalysis: Could not retrieve the ASTNode for CompilationUnit "
				        + compUnit.getElementName());
			}
		}

		MessageBox msgbox = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
		msgbox.setMessage("The metrics are classified and written into CVS files");
		System.out.println("*******    ArchMetrics: Visited a system with no annotations and categorized sucessfully    *******");
		msgbox.open();
		crystal.reset();
	}

	// Analysing compilation Units
	private void prepopulateTypes(CompilationUnit unit, ICompilationUnit compilationUnit) throws IOException {
		List types = unit.types();
		for (Iterator iter = types.iterator(); iter.hasNext();) {
			Object next = iter.next();
			if (next instanceof TypeDeclaration) {
				// declaration: Contains one file content at a time.
				TypeDeclaration declaration = (TypeDeclaration) next;
				traverseType(declaration);
			}
		}
	}

	private void traverseType(TypeDeclaration declaration) throws IOException {

		TypeDeclaration[] nestedTypes = declaration.getTypes();
		for (int i = 0; i < nestedTypes.length; i++) {
			traverseType(nestedTypes[i]);
		}
		traverseTypeDecl(declaration);
	}

	@SuppressWarnings("unchecked")
	private void traverseTypeDecl(TypeDeclaration declaration) {

		// Create the types from the ITypeBinding
		ITypeBinding typeBinding = declaration.resolveBinding();
		Type astType = Type.createFrom(typeBinding);
		typeInfo.reviveTypeFromBinding(astType, typeBinding);
	}

	// Analysing compilation Units
	private void analyzeCompilationUnit(CompilationUnit unit, ICompilationUnit compilationUnit) throws IOException {

		CompilationUnitVisitor visitor = new CompilationUnitVisitor();
		unit.accept(visitor);
		// // TODO: Why calling this FOR EACH compilation unit separately
	QualAnalyzer qual = new QualAnalyzer(getMetricsFilePath(), getNoInstanceFilePath(),  visitor.getVar(), visitor.getinstantiatedType());
	}

	public String getMetricsFilePath() {
		return noannosMetricPath;
	}

	public void setMetricsFilePath(String noannosMetricPath) {
		this.noannosMetricPath = noannosMetricPath;
	}

	public String getNoInstanceFilePath() {
		return noInstanceFilePath;
	}

	public void setgetNoInstance(String noInstance) {
		this.noInstanceFilePath = noInstance;
	}

	// Local visitor
	private class CompilationUnitVisitor extends ASTVisitor {

		@Override
		public boolean visit(VariableDeclarationFragment node) {
			IVariableBinding resolveBinding = node.resolveBinding();
			varDeclaration.add(resolveBinding);
			return super.visit(node);
		}

		@Override
		public boolean visit(ClassInstanceCreation node) {
			ITypeBinding newExpr = node.resolveTypeBinding();
			// System.out.println("newExpr" + newExpr.getQualifiedName());
			newExpression.add(newExpr);
			return super.visit(node);
		}

		@Override
		public boolean visit(TypeDeclaration node) {

			ITypeBinding resolveBinding = node.resolveBinding();
			for (ITypeBinding a : newExpression) {
				isInstantiated = true;
				isNewExpression.put(a, String.valueOf(isInstantiated));
			}

			if (!newExpression.contains(resolveBinding.getQualifiedName())) {
				isInstantiated = false;
				isNewExpression.put(resolveBinding, String.valueOf(isInstantiated));
			}
			
			//System.out.println("sdfgfg" + isNewExpression);
			return super.visit(node);
		}

		public ArrayList<IVariableBinding> getVar() {
			return varDeclaration;
		}

		public ArrayList<ITypeBinding> getNewExpr() {
			return newExpression;
		}

		public HashMap<ITypeBinding, String> getinstantiatedType() {
			return isNewExpression;
		}
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
}