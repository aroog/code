package edu.wayne.metrics.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.wayne.metrics.Config;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.internal.WorkspaceUtilities;
import edu.wayne.metrics.mapping.DefaultingModel;
import edu.wayne.metrics.mapping.DefaultingPersist;
import edu.wayne.metrics.mapping.Entry;
import edu.wayne.metrics.mapping.Model;
import edu.wayne.metrics.mapping.PackageType;
import edu.wayne.metrics.mapping.Persist;

// TODO: Need to completely get rid of hard-coded things
// - Come up with alternative techniques/heuristics to get those lists of types
public class GenerateDefaultMap implements IWorkbenchWindowActionDelegate {

	private static final String METRICS_MAP_XML = "\\metrics_map.xml";
	private static final String DEFAULTING_METRICS_MAP_XML = "\\metrics_map_defaults.xml";

	private class ImportsVisitor extends ASTVisitor {

		Set<String> filters;

		ImportsVisitor(Set<String> filters) {
			this.filters = filters;
		}

		@Override
		public void endVisit(ImportDeclaration node) {
			boolean filtered = false;
			if (!node.isOnDemand()) { // if it is not x.y.*
				ITypeBinding tb = (ITypeBinding) node.resolveBinding();
				String qualifiedName = tb.getQualifiedName();
				for (String filter : filters) {
					Pattern patternJava = Pattern.compile(filter);
					Matcher matcherJava = patternJava.matcher(qualifiedName);
					if (matcherJava.matches()) {
						filtered = true;
					}
				}
				/*
				 * if (!model.hasMapping(qualifiedName) && !filtered) {
				 * model.addMapping(new Entry(qualifiedName)); }
				 */

			}
		}

	}

	/**
	 * Set to true to display the paths of this compilation units (slows things down!)
	 */

	private String mapping_file_path;
	
	private String defaulting_file_path;

	private Model model = new Model();

	private DefaultingModel defaultingModel = new DefaultingModel();

	
	private IWorkbenchWindow window = null;

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void run(IAction action) {

		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);
		IPath projectPath = currentProject.getLocation();
		mapping_file_path = projectPath.toOSString() + METRICS_MAP_XML;
		defaulting_file_path = projectPath.toOSString() + DEFAULTING_METRICS_MAP_XML;

		// Load it.
		defaultingModel = DefaultingPersist.load(defaulting_file_path);
		
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

		// XXX. Finish work on config file.
		try {
			Config.loadConfig(projectPath);
		}
		catch (IOException ex) {
			System.err.println("Configuration file not found. Create one for current project");
		}

		Crystal crystal = Crystal.getInstance();
		PrintWriter output = crystal.userOut();
		crystal.scanWorkspace();
		Iterator<ICompilationUnit> unitIterator = crystal.getCompilationUnitIterator();
		ICompilationUnit compUnit = null;
		for (; unitIterator.hasNext();) {
			compUnit = unitIterator.next();
			if (compUnit == null) {
				output.println("AbstractCompilationUnitAnalysis: null CompilationUnit");
				continue;
			}
			// Retrieve the path of this compilation unit, and output it
			if (Config.DEBUG_OUTPUT_PATHS) {
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
			try {
				// HACK: hackFrameworkPackages();
				setFrameworkPackageTypes();
				// HACK: hackApplicationTypes();
				setApplicationTypes();
			}
			catch (JavaModelException e) {
				e.printStackTrace();
			}
		}

		// TOSUM: DONE: Populate all the fields of the Model.
		// TOSUM: TODO: Now the package names are hard coded to decide between
		// Framework types and Application default types.
		// Added hard-coded Container List

		//HACK: hackContainerTypes();
		
		// Copy from the defaulting model to the model
		for(String containerType : defaultingModel.getContainerTypes() ) {
			model.addContainerType(containerType);
		}

		// Added hard-coded Java Library types
		
		// TOSUM: XXX. XXX. XXX. REMOVE system-specific stuff from the defaulting model.
		
		//HACK: hackJavaLibTypes();
		
		// Copy from the defaulting model to the model
		for(String containerType : defaultingModel.getJavaLibTypes()) {
			model.addJavaLibType(containerType);
		}
		
		// Added hard-coded data types

		// HACK: hackDataTypes();
		
		// Copy from the defaulting model to the model
		for(String dataType : defaultingModel.getDataTypes() ) {
			model.addDataType(dataType);
		}

		// Added hard-coded exception types

		// HACK: hackExceptionTypes();
		
		// Copy from the defaulting model to the model
		for(String exnType : defaultingModel.getExceptionTypes() ) {
			model.addExceptionType(exnType);
		}		

		// Save the mapping at the end
		Persist.save(model, mapping_file_path);
		// NOTE: Do NOT save the template model. That was just for moving hard-coded stuff out of here!  
		//DefaultingPersist.save(defaultingModel, defaulting_file_path);
		// model.finish();
		// Cleanup
		crystal.reset();
	}

	private void hackExceptionTypes() {
	    defaultingModel.addExceptionType("java.lang.Exception");
		defaultingModel.addExceptionType("java.lang.RuntimeException");
		defaultingModel.addExceptionType("java.lang.Throwable");
		defaultingModel.addExceptionType("java.lang.UnsupportedOperationException");
		defaultingModel.addExceptionType("java.lang.IllegalArgumentException");
		defaultingModel.addExceptionType("java.lang.InternalError");
		defaultingModel.addExceptionType("java.lang.IllegalStateException");
		defaultingModel.addExceptionType("java.lang.NullPointerException");
    }

	private void hackDataTypes() {
	    defaultingModel.addDataType("java.awt.Rectangle");
		defaultingModel.addDataType("java.awt.Point");
		defaultingModel.addDataType("java.io.File");
		defaultingModel.addDataType("java.awt.Dimension");
		defaultingModel.addDataType("java.lang.String");
		defaultingModel.addDataType("java.util.Date");
		defaultingModel.addDataType("java.sql.Timestamp");
		defaultingModel.addDataType("java.awt.Polygon");
		defaultingModel.addDataType("java.awt.Font");
		defaultingModel.addDataType("org.apache.ftpserver.ftplet.FtpFile");
    }

	// XXX. Why list all the Java lib types?
	// Why not list anything that starts with java, javax?
	// Use regular expressions: java.*, javax.*
	private void hackJavaLibTypes() {
	    defaultingModel.addJavaLibType("java.awt.Component");
		defaultingModel.addJavaLibType("javax.swing.JPanel");
		defaultingModel.addJavaLibType("java.util.EventObject");
		defaultingModel.addJavaLibType("java.io.Serializable");
		defaultingModel.addJavaLibType("javax.crypto.spec.SecretKeySpec");
		defaultingModel.addJavaLibType("java.lang.reflect.Method");
		defaultingModel.addJavaLibType("java.beans.PropertyChangeListener");
		defaultingModel.addJavaLibType("java.lang.Object");
		defaultingModel.addJavaLibType("java.awt.GridBagConstraints");
		defaultingModel.addJavaLibType("java.awt.Button");
		defaultingModel.addJavaLibType("java.lang.Class");
		defaultingModel.addJavaLibType("java.lang.String");
		defaultingModel.addJavaLibType("java.beans.PropertyChangeEvent");
		defaultingModel.addJavaLibType("java.beans.PropertyChangeListener");
		defaultingModel.addJavaLibType("java.lang.reflect.Method");
		
		// Added socket library classes for AFS
		defaultingModel.addJavaLibType("java.net.ServerSocket");
		defaultingModel.addJavaLibType("java.util.StringTokenizer");
		defaultingModel.addJavaLibType("java.io.FileInputStream");
		defaultingModel.addJavaLibType("java.lang.Runnable");
		defaultingModel.addJavaLibType("java.io.FileOutputStream");
		
    }

	private void hackContainerTypes() {
	    defaultingModel.addContainerType("java.util.List");
		defaultingModel.addContainerType("java.util.ArrayList");
		defaultingModel.addContainerType("java.util.HashSet");
		defaultingModel.addContainerType("java.util.Vector");
		defaultingModel.addContainerType("java.util.HashMap");
		defaultingModel.addContainerType("java.util.LinkedHashMap");
		defaultingModel.addContainerType("java.util.EnumMap");
		defaultingModel.addContainerType("java.util.TreeMap");
		defaultingModel.addContainerType("java.util.AbstractMap");
		defaultingModel.addContainerType("java.util.Map");
		defaultingModel.addContainerType("java.util.Hashtable");
    }

	public List<Type> getAllSuperTypes(TypeDeclaration typeDeclaration) {
		List<Type> list = new ArrayList<Type>();

		Type superclassType = typeDeclaration.getSuperclassType();
		if (superclassType != null) {
			ITypeBinding type = ((Type) superclassType).resolveBinding();
			if (!isFiltered(type))
				list.add(superclassType);
		}

		List superInterfaceTypes = typeDeclaration.superInterfaceTypes();
		for (Iterator itSuperInterfacesIterator = superInterfaceTypes.iterator(); itSuperInterfacesIterator.hasNext();) {
			Object next = itSuperInterfacesIterator.next();
			if (next instanceof SimpleType) {
				Type nextType = (Type) next;
				ITypeBinding type = nextType.resolveBinding();

				if (!isFiltered(type))
					list.add(nextType);
			}
		}
		return list;

	}

	/**
	 * @param type
	 * @param filtered
	 * @return
	 */
	private boolean isFiltered(ITypeBinding type) {
		boolean filtered = false;
		for (String filter : Config.FILTER_DECL) {
			Pattern patternJava = Pattern.compile(filter);
			Matcher matcherJava = patternJava.matcher(type.getQualifiedName().toString());
			if (matcherJava.matches())
				filtered = true;
		}
		return filtered;
	}

	private void analyzeCompilationUnit(CompilationUnit unit, ICompilationUnit compilationUnit) {
		Set<String> filters = loadFilters();
		ASTVisitor importsVisitor = new ImportsVisitor(filters);
		unit.accept(importsVisitor);

		List types = unit.types();
		for (Iterator iter = types.iterator(); iter.hasNext();) {
			Object next = iter.next();
			if (next instanceof TypeDeclaration) {
				// declaration: Contains one file content at a time.
				TypeDeclaration declaration = (TypeDeclaration) next;
				// traverseType(declaration,true);

			}
		}
	}

	/**
	 * @return
	 */
	private Set<String> loadFilters() {
		String[] defFilters = Config.FILTER_IMPORTS;
		Set<String> filters = new HashSet<String>();
		for (String filter : defFilters)
			filters.add(filter);
		return filters;
	}

	// Framework types added into the XML list
	private void setFrameworkPackageTypes() throws JavaModelException {
		String className = null;

		// To append the packageName - get the fullyQualified type name
		StringBuilder typeName = new StringBuilder();

		// Current project open
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);
		
		Set<String> frameworkPackages = defaultingModel.getFrameworkPackages();

		// Get a IcompilationUnit of the current project
		IPackageFragment[] packages = JavaCore.create(currentProject).getPackageFragments();
		for (IPackageFragment mypackage : packages) {
			// Hard-coded package names for now - Until we get logic for the
			// distinguishing Framework Types and ApplicationDefault types
			// Only for MD_Summary for now
			// TOSUM: TODO: Get a logic to distinguish between
			// Added in the package names for CDB, AFS and DL

			if (frameworkPackages.contains(mypackage.getElementName())) {
				for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
					IPackageDeclaration[] pck = unit.getPackageDeclarations();
					for (IPackageDeclaration ipack : pck) {
						IResource resource = ipack.getResource();

						// Get the type Name
						className = resource.getName();
						// Remove the extensions from the typeName
						int indexOf = className.indexOf(".java");
						typeName.append(mypackage.getElementName());
						typeName.append(".");
						typeName.append(className.substring(0, indexOf));

						// XXX. Fix this. No point in storing the package name and the fully qualified type
						model.addFrameworkPackageType(new PackageType(mypackage.getElementName(), typeName.toString()));
					}
					// Clean up StringBuilder
					typeName.setLength(0);
				}
				typeName.setLength(0);
			}
			typeName.setLength(0);
		}
	}

	// XXX. Revisit this list
	private void hackFrameworkPackages() {
		// MD
		defaultingModel.addFrameworkPackage("minidraw.framework");
		defaultingModel.addFrameworkPackage("minidraw.standard");
		defaultingModel.addFrameworkPackage("minidraw.standard.handlers");
		// CDB
		defaultingModel.addFrameworkPackage("cryptodb");
		defaultingModel.addFrameworkPackage("cryptodb.core");
		// AFS
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.ftplet");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.filesystem.nativefs.impl");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.filesystem.nativefs");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.util");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.usermanager.impl");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.ssl.impl");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.message.impl");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.impl");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.ftpletcontainer.impl");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.listener");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.config.spring");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.command.impl");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.command.impl.listing");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.config.spring.factorybeans");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.command");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.ssl.impl");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.ftpletcontainer");
		defaultingModel.addFrameworkPackage("org.apache.ftpserver.ipfilter");
		defaultingModel.addFrameworkPackage("com.rolemodelsoft.drawlet.util");
		defaultingModel.addFrameworkPackage("com.rolemodelsoft.drawlet.shapes");
		// XXX. Why is awt a fwk package?
		defaultingModel.addFrameworkPackage("java.awt");
		// DL
		defaultingModel.addFrameworkPackage("com.rolemodelsoft.drawlet.awt");
		defaultingModel.addFrameworkPackage("com.rolemodelsoft.drawlet");
		defaultingModel.addFrameworkPackage("com.rolemodelsoft.drawlet.examples.awt");
	}
	
	
	private void hackApplicationTypes() {
		// XXX. boardgame package is fwk!!! Command interface is definitely framework.
		// MD
		defaultingModel.addApplicationPackage("minidraw.boardgame");
		defaultingModel.addApplicationPackage("minidraw.breakthrough");
		// CDB
		defaultingModel.addApplicationPackage("cryptodb.test");
		// AFS
		defaultingModel.addApplicationPackage("org.apache.ftpserver.main");
		// DL
		defaultingModel.addApplicationPackage("com.rolemodelsoft.drawlet.text");
		defaultingModel.addApplicationPackage("com.rolemodelsoft.drawlet.shapes.rectangles");
		defaultingModel.addApplicationPackage("com.rolemodelsoft.drawlet.shapes.polygons");
		defaultingModel.addApplicationPackage("com.rolemodelsoft.drawlet.shapes.ellipses");
		defaultingModel.addApplicationPackage("com.rolemodelsoft.drawlet.examples");
	}
	
	private void setApplicationTypes() throws JavaModelException {
		String className = null;

		// To append the packageName - get the fullyQualified type name
		StringBuilder typeName = new StringBuilder();

		// Current project open
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);

		HashSet<String> appPackages = defaultingModel.getApplicationPackages();
		
		// Get a IcompilationUnit of the current project
		IPackageFragment[] packages = JavaCore.create(currentProject).getPackageFragments();
		for (IPackageFragment mypackage : packages) {
			// Hard-coded package names for now - Until we get logic for the
			// distinguishing Framework Types and ApplicationDefault types
			// Only for MD_Summary for now
			// TODO: Get a logic to distinguish between
			// Added in the package names for CDB
			
			// TOSUM: XXX. What the HELL is this? Get the Element name, remove .java, add .java?!?!?
			// Surely, there has to be a MUCH cleaner way of doing this
			// the package declaration has the fully qualified name!!!
			if (appPackages.contains(mypackage.getElementName())) {
				for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
					IPackageDeclaration[] pck = unit.getPackageDeclarations();
					for (IPackageDeclaration ipack : pck) {
						IResource resource = ipack.getResource();
						// Get the type Name
						className = resource.getName();
						// Remove the extensions from the typeName
						int indexOf = className.indexOf(".java");
						typeName.append(mypackage.getElementName());
						typeName.append(".");
						typeName.append(className.substring(0, indexOf));

						// XXX. Fix this. No point in storing the package name and the fully qualified type
						model.addApplicationPackageType(new PackageType(mypackage.getElementName(), typeName.toString()));
					}
					typeName.setLength(0);
				}
				typeName.setLength(0);
			}
			typeName.setLength(0);
		}
	}

	private void traverseType(TypeDeclaration declaration, boolean takesParams) {

		// Add default mapping to model
		String typeName = null;
		ITypeBinding tb = declaration.resolveBinding();
		if (Config.USE_QUALIFIED_TYPENAME) {
			typeName = tb.getQualifiedName();
		}
		else {
			typeName = declaration.getName().toString();
		}
		// Add the interfaces and the classes without superClass to the map
		// file.
		// HACK: This is a very bad idea. The tool does not generate
		// @DomainParams for classes that inherit from a
		// built-in type! Fix it!
		// This is an example of a premature optimization (minimizing the size
		// of the map) gone bad. The focus should be
		// on correctness first.
		if (!model.hasMapping(typeName) && getAllSuperTypes(declaration).size() == 0) {
			model.addMapping(new Entry(typeName));
		}

		List<Type> allSuperTypes = getAllSuperTypes(declaration);
		for (Type type : allSuperTypes) {
			ITypeBinding stb = type.resolveBinding();
			String superTypeName = stb.getQualifiedName();
			if (!model.hasMapping(superTypeName)) {
				model.addMapping(new Entry(superTypeName));
			}
		}

		TypeDeclaration[] nestedTypes = declaration.getTypes();
		for (int i = 0; i < nestedTypes.length; i++) {
			TypeDeclaration nestedType = nestedTypes[i];
			traverseType(nestedType, false);
		}
	}

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void selectionChanged(IAction action, ISelection selection) {

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
