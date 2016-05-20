package edu.wayne.metrics.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * A collection of methods used to extract useful data from the workspace. These methods are used by the framework and
 * should not be used by users of the framework. You can access must of the data collected from these methods via the
 * Crystal class.
 * 
 * @author David Dickey
 */
public class WorkspaceUtilities {

	private static IJavaProject javaProject = null;

	public static IClassFile getCurrentClassFile(IWorkbenchWindow window) {
		IClassFile file = null;
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IClassFileEditorInput) {
				file = ((IClassFileEditorInput) input).getClassFile();
			}
		}
		return file;
	}

	public static void init() {
	}

	public static IProject getCurrentProject(IWorkbenchWindow window) {
		IProject project = null;
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			IFile file = null;
			if (input instanceof IFileEditorInput) {
				file = ((IFileEditorInput) input).getFile();
			}
			if (file != null) {
				project = file.getProject();
			}
		}
		return project;
	}

	public static IJavaProject getJavaProject(String projectName) {
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
			project = javacore.getJavaProject(projectName); // this returns the specified project
		}

		WorkspaceUtilities.javaProject = project;
		
		return project;
	}

	/**
	 * Traverses the workspace for CompilationUnits.
	 * 
	 * @return the list of all CompilationUnits in the workspace
	 */
	public static List<ICompilationUnit> scanForCompilationUnits() {
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

		IJavaModel javaModel = JavaCore.create(root);
		if (javaModel == null) {
			System.out.println("No Java Model in workspace");
			return null;
		}

		// Get all CompilationUnits
		return WorkspaceUtilities.collectCompilationUnits(javaModel);
	}

	/**
	 * A recursive traversal of the IJavaModel to collect all ICompilationUnits. Each compilation unit corresponds to
	 * each java file.
	 * 
	 * @param javaElement a node in the IJavaModel that will be traversed
	 * @return a list of compilation units
	 */
	private static List<ICompilationUnit> collectCompilationUnits(IJavaElement javaElement) {
		List<ICompilationUnit> list = null, temp = null;
		// We are traversing the JavaModel for COMPILATION_UNITs
		if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			list = new ArrayList<ICompilationUnit>();
			list.add((ICompilationUnit) javaElement);
			return list;
		}

		// Non COMPILATION_UNITs will have to be further traversed
		if (javaElement instanceof IParent) {
			IParent parent = (IParent) javaElement;

			// Do not traverse PACKAGE_FRAGMENT_ROOTs that are ReadOnly
			if ((javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) && javaElement.isReadOnly()) {
				return null;
			}

			// Traverse
			try {
				if (parent.hasChildren()) {
					IJavaElement[] children = parent.getChildren();
					for (int i = 0; i < children.length; i++) {
						temp = WorkspaceUtilities.collectCompilationUnits(children[i]);
						if (temp != null) {
							if (list == null) {
								list = temp;
							}
							else {
								list.addAll(temp);
							}
						}
					}
				}
			}
			catch (JavaModelException jme) {
				System.out.println("JAVA MODEL EXCEPTION: " + jme);
				return null;
			}
		}
		else {
			throw new CrystalRuntimeException("There exists an IJavaElement that is not an instance of IParent!");
		}

		return list;
	}

	/**
	 * Goes through a list of compilation units and parses them. The act of parsing creates the AST structures from the
	 * source code.
	 * 
	 * @param compilationUnits the list of compilation units to parse
	 * @return the mapping from compilation unit to the AST roots of each
	 */
	public static Map<ICompilationUnit, ASTNode> parseCompilationUnits(List<ICompilationUnit> compilationUnits) {
		if (compilationUnits == null) {
			throw new CrystalRuntimeException("null list of compilation units");
		}

		ICompilationUnit[] compUnits = compilationUnits.toArray(new ICompilationUnit[0]);
		final Map<ICompilationUnit, ASTNode> parsedCompilationUnits = new HashMap<ICompilationUnit, ASTNode>();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setProject(WorkspaceUtilities.javaProject);
		parser.createASTs(compUnits, new String[0], new ASTRequestor() {
			@Override
            public final void acceptAST(final ICompilationUnit unit, final CompilationUnit node) {
				parsedCompilationUnits.put(unit, node);
			}

			@Override
            public final void acceptBinding(final String key, final IBinding binding) {
				// Do nothing
			}
		}, null);
		return parsedCompilationUnits;
	}

	/**
	 * Collects all top level methods from CompilationUnits. (Embedded Methods are currently not collected.)
	 * 
	 * @param compilationUnitToASTNode the mapping of CompilationUnits to preparsed ASTNodes
	 * @return the list of all top level methods within the CompilationUnits
	 */
	public static List<MethodDeclaration> scanForMethodDeclarations(
	        Map<ICompilationUnit, ASTNode> compilationUnitToASTNode) {
		if (compilationUnitToASTNode == null) {
			throw new CrystalRuntimeException("null map of compilation units to ASTNodes");
		}

		// Create an empty list
		List<MethodDeclaration> methodList = new LinkedList<MethodDeclaration>();
		List<MethodDeclaration> tempMethodList;
		// Get all CompilationUnits and look for MethodDeclarations in each
		Set<Entry<ICompilationUnit, ASTNode>> entrySet = compilationUnitToASTNode.entrySet();
		Iterator<Entry<ICompilationUnit, ASTNode>> iterator = entrySet.iterator();
		while(iterator.hasNext()) {
			Entry<ICompilationUnit, ASTNode> entry = iterator.next();
			ICompilationUnit icu = entry.getKey();
			tempMethodList = WorkspaceUtilities.scanForMethodDeclarationsFromAST(compilationUnitToASTNode.get(icu));
			methodList.addAll(tempMethodList);
		}
		return methodList;
	}

	/**
	 * Collects all top level methods from an AST. (Embedded Methods are currently not collected.)
	 * 
	 * @param node the root of an AST
	 * @return all top level methods within the AST
	 */
	public static List<MethodDeclaration> scanForMethodDeclarationsFromAST(ASTNode node) {
		if (node == null) {
			throw new CrystalRuntimeException("AST tree not found from ICompilationUnit");
		}

		// Visitor Class
		class MethodFindVisitor extends ASTVisitor {
			List<MethodDeclaration> methodList;

			public MethodFindVisitor(List<MethodDeclaration> inMethodList) {
				methodList = inMethodList;
			}

			// Visit MethodDeclarations
			@Override
            public boolean visit(MethodDeclaration methodDeclaration) {
				methodList.add(methodDeclaration);

				// false returns us back, instead of traversing further down
				return false;
			}
		}

		// Create an empty list, populate methods by traversing using the visitor
		List<MethodDeclaration> methodList = new LinkedList<MethodDeclaration>();
		MethodFindVisitor visitor = new MethodFindVisitor(methodList);
		node.accept(visitor);
		return methodList;
	}

	public static Map<String, ASTNode> scanForBindings(Map<ICompilationUnit, ASTNode> compilationUnitToASTNode,Map<String, ITypeBinding> mapKeyToType, Map<String, ITypeBinding> mapNameToType) {
		if (compilationUnitToASTNode == null) {
			throw new CrystalRuntimeException("null map of compilation units to ASTNodes");
		}

		Map<String, ASTNode> bindings = new HashMap<String, ASTNode>();
		// Get all CompilationUnits and look for MethodDeclarations in each
		Set<Entry<ICompilationUnit, ASTNode>> entrySet = compilationUnitToASTNode.entrySet();
		Iterator<Entry<ICompilationUnit, ASTNode>> iterator = entrySet.iterator();
		ITypeBinding objectTypeBinding = null;
		ITypeBinding stringTypeBinding = null;
		while(iterator.hasNext()) {
			Entry<ICompilationUnit, ASTNode> entry = iterator.next();
			ASTNode node = entry.getValue();
			BindingsCollectorVisitor bindingsCollectorVisitor = new BindingsCollectorVisitor(bindings, mapKeyToType,mapNameToType);
			node.accept(bindingsCollectorVisitor);
			
			// Do this once
			if (objectTypeBinding == null) {
				objectTypeBinding = node.getAST().resolveWellKnownType("java.lang.Object");
				mapNameToType.put("java.lang.Object", objectTypeBinding);
			}
			// Do this once
			if (stringTypeBinding == null) {
				stringTypeBinding = node.getAST().resolveWellKnownType("java.lang.String");
				mapNameToType.put("java.lang.String", stringTypeBinding);
			}
		}
		return bindings;
	}

	public static IJavaProject getJavaProject() {
		return WorkspaceUtilities.javaProject;
	}

	public static void setJavaProject(IJavaProject javaProject) {
		WorkspaceUtilities.javaProject = javaProject;
	}
}

class BindingsCollectorVisitor extends ASTVisitor {
	Map<String, ASTNode> bindings = null;

	Map<String, ITypeBinding> mapKeyToType = null;
	
	Map<String, ITypeBinding> mapNameToType = null;

	public BindingsCollectorVisitor(Map<String, ASTNode> bindingsIn, Map<String, ITypeBinding> mapKeyToType, Map<String, ITypeBinding> mapNameToType) {
		if (mapKeyToType == null) {
			throw new CrystalRuntimeException("BindingsCollectorVisitor:: Unexpected null mapping");
		}
		
		if (mapNameToType == null) {
			throw new CrystalRuntimeException("BindingsCollectorVisitor:: Unexpected null mapping");
		}

		bindings = bindingsIn;
		this.mapKeyToType = mapKeyToType;
		this.mapNameToType = mapNameToType;
	}

	protected void addNewBinding(IBinding binding, ASTNode node) {
		// TODO: When does this occur?
		if ( binding == null ) {
			return;
		}
		if (bindings == null) {
			throw new CrystalRuntimeException("BindingsCollectorVisitor::addNewBinding: Unexpected null mapping");
		}
		if (!bindings.containsKey(binding.getKey())) {
		bindings.put(binding.getKey(), node);
		}
	}

	protected void addNewTypeBinding(ITypeBinding typeBinding) {
		// Skip over primitive types, which includes void
		if (typeBinding == null || typeBinding.isPrimitive()) {
			return;
		}
		String key = typeBinding.getKey();
		if (!mapKeyToType.containsKey(key)) {
			mapKeyToType.put(key, typeBinding);
		}
		
		String qualifiedName = typeBinding.getQualifiedName();
		if (!mapNameToType.containsKey(qualifiedName)) {
			mapNameToType.put(qualifiedName, typeBinding);
		}
		
		// Add supertypes, and implemented interfaces as well!
		ITypeBinding superclassType = typeBinding.getSuperclass();
		if ( superclassType != null ) {
			addNewTypeBinding(superclassType);
		}
		
		ITypeBinding[] interfaces = typeBinding.getInterfaces();
		for(ITypeBinding itfBinding : interfaces)  { 
			addNewTypeBinding(itfBinding);
		}
	}

	@Override
    public boolean visit(AnonymousClassDeclaration node) {
		addNewBinding(node.resolveBinding(), node);
		return true;
	}

	@Override
    public boolean visit(EnumConstantDeclaration node) {
		addNewBinding(node.resolveVariable(), node);
		return true;
	}

	@Override
    public boolean visit(EnumDeclaration node) {
		addNewBinding(node.resolveBinding(), node);
		return true;
	}

	// FieldDeclaration - handled by VariableDeclarationFragment
	@Override
    public boolean visit(ImportDeclaration node) {
		// addNewBinding(node.resolveBinding(), node);
		return true;
	}

	@Override
    public boolean visit(LabeledStatement node) {
		SimpleName sn = node.getLabel();
		addNewBinding(sn.resolveBinding(), node);
		return true;
	}

	@Override
    public boolean visit(MethodDeclaration node) {
		addNewBinding(node.resolveBinding(), node);
		Type returnType = node.getReturnType2();
		if (returnType != null ) {
			addNewTypeBinding(returnType.resolveBinding());
		}
		return true;
	}

	@Override
    public boolean visit(PackageDeclaration node) {
		addNewBinding(node.resolveBinding(), node);
		return true;
	}

	@Override
    public boolean visit(SingleVariableDeclaration node) {
		IVariableBinding variableBinding = node.resolveBinding();
		addNewBinding(variableBinding, node);
		addNewTypeBinding(variableBinding.getType());
		return true;
	}

	@Override
    public boolean visit(TypeDeclaration node) {
		ITypeBinding typeBinding = node.resolveBinding();
		addNewBinding(typeBinding, node);
		addNewTypeBinding(typeBinding);
		
		return true;
	}

	
	@Override
    public boolean visit(TypeDeclarationStatement node) {
		ITypeBinding typeBinding = node.resolveBinding();
		addNewBinding(typeBinding, node);
		addNewTypeBinding(typeBinding);
		return true;
	}

	public boolean visit(VariableDeclaration node) {
		IVariableBinding variableBinding = node.resolveBinding();
		addNewBinding(variableBinding, node);
		addNewTypeBinding(variableBinding.getType());
		return true;
	}

	@Override
    public boolean visit(VariableDeclarationFragment node) {
		IVariableBinding variableBinding = node.resolveBinding();
		addNewBinding(variableBinding, node);
		addNewTypeBinding(variableBinding.getType());
		return true;
	}
	
	@Override
    public boolean visit(SimpleName node) {
		// HACK: Do we need to handle node.resolveBinding()?
		ITypeBinding typeBinding = node.resolveTypeBinding();
		if ( typeBinding != null ) {
			addNewTypeBinding(typeBinding);
		}
		return true;
	}

	@Override
    public boolean visit(CastExpression node) {
		addNewTypeBinding(node.getType().resolveBinding());
		return true;
    }

	@Override
    public boolean visit(InstanceofExpression node) {
		addNewTypeBinding(node.getRightOperand().resolveBinding());
		return true;
    }

	@Override
    public boolean visit(VariableDeclarationExpression node) {
		addNewTypeBinding(node.getType().resolveBinding());
		return true;
    }

	@Override
    public boolean visit(VariableDeclarationStatement node) {
		addNewTypeBinding(node.getType().resolveBinding());
		return true;
    }
}
