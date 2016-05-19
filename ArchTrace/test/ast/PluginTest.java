package ast;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



import edu.wayne.tracing.Crystal;
import edu.wayne.tracing.actions.DeclarationVisitor;
import edu.wayne.tracing.internal.WorkspaceUtilities;




/**
 * @author Radu
 * This test can be run this test as a JUnit Plugin test.
 * A JUnit Plugin test launches a new instance of eclipse which opens the workspace called junit-workspace
 * From the workspace, the test loads an existing project and creates the type hierarchy
 * The name of the project is currently hard coded in setupAll(). I guess we need one TestClass per project.
 *
 * Before you run this test:
 * 1. Open junit-workspace and import the project you want to load - in this case Test_Listeners_Sequence
 * 2. From the context menu of the test open Run As > Run Configurations
 * 3. create a new JUnit-Plugin Test
 * 4 IMPORTANT!!! from the Main tab UNCHECK the CLEAR Workspace checkbox, otherwise you need to repeat from step 1
 * 5. Run the test
 * 6. repeat step 6 until all your Plugin Junit test pass :)
 *
 * NOTE: whenever you create a new test class, repeat step 2-4, the checkbox is selected by default.
 */
public class PluginTest  {
	static IJavaProject project;

    @BeforeClass
    static public void setupAll() throws JavaModelException {
        

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        Assert.assertNotNull(workspace);
        IWorkspaceRoot root = workspace.getRoot();
        Assert.assertNotNull(root);
        project = (IJavaProject) JavaCore.create(root.getProject("TraceToCodeTest"));
        Assert.assertNotNull(project);
        IPath location = workspace.getRoot().getLocation();
        TestAST test= new TestAST();
        test.createAndSave( location.append(project.getPath()).toOSString()+"\\");
     
		if (project != null) {
			
			Crystal.getInstance().setJavaProject(project);
		}
		else {
			Assert.fail("Project not found");
			
		}
//        OGraph graph = OOGUtils.load(xmlFilename);
//        Assert.assertNotNull(graph.getRoot());
//  Assert.assertTrue(location.toOSString().startsWith("C:\\"));
    }
	private BaseTraceability baseTraceabilityObject;
	private ASTNode foundEnclosingDeclaration = null;
	private Set<ASTNode> expressionsFound = null;
    @Test
    public void findClass(){
    	String findClass = "foo.Bar";//LOAD XML CASE 3

    	baseTraceabilityObject = TestUtils.getFile(3);
		run();
		if(foundEnclosingDeclaration==null){
			Assert.fail("Type Declaration Not Found");
		}
    }
    
    
    @Test 
    public void findMethodDeclaration(){
    	String findMethodWithSameNameDiffParams = "Bar.m(String, int)";//LOAD XML CASE 2
    	baseTraceabilityObject = TestUtils.getFile(2);
		run();
    	if(foundEnclosingDeclaration == null){
    		Assert.fail("Method Declaration Not found");
    	}
    }
    
    
    @Test 
    public void findInnerClassField(){
		String findInnerClassField = "foo.Bar.Baz.b";//LOAD XML CASE 5
    	baseTraceabilityObject = TestUtils.getFile(5);
		run();
    	if(foundEnclosingDeclaration == null){
    		Assert.fail("Field Declaration Not found");
    	}
    }
    
    @Test 
    public void findField(){
		String findField = "foo.Bar.b";//LOAD XML CASE 1
    	baseTraceabilityObject = TestUtils.getFile(1);
		run();
    	if(foundEnclosingDeclaration == null){
    		Assert.fail("Field Declaration Not found");
    	}
    }
    
    @Test
    public void findClassInstanceCreation(){
		
		//Class instance creation Load XML Case 6
		//This is looking in the enclosing declaration Bar.m() for ClassInstanceCreation with complex expression "new Biff()"
    	baseTraceabilityObject = TestUtils.getFile(6);
		run();
    	if(expressionsFound == null){
    		Assert.fail("Class Instance Creation Expressions Not found");
    	}
    	
    }
    public void findFieldWrite(){
		//Field Write Load XML Case 7
		//This looks for enclosing declaration foo.Bar.m() for FieldWrite with complex expression "this.b=new Biff()"
    	baseTraceabilityObject = TestUtils.getFile(7);
		run();
    	if(expressionsFound == null){
    		Assert.fail("Field Write Expressions Not found");
    	}
    }
    
    private void run(){
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

			// Obtain the AST for this CompilationUnit and analyze it
			ASTNode node = crystal.getASTNodeFromCompilationUnit(compUnit);
			if ((node != null) && (node instanceof CompilationUnit)) {
				analyzeCompilationUnit((CompilationUnit) node);
			}
			else {
				output.println("AbstractCompilationUnitAnalysis: Could not retrieve the ASTNode for CompilationUnit "
				        + compUnit.getElementName());
			}
		}
    }
	private void analyzeCompilationUnit(CompilationUnit unit) {

		List types = unit.types();
		for (Iterator iter = types.iterator(); iter.hasNext();) {

			Object next = iter.next();
			if (next instanceof TypeDeclaration) {
				TypeDeclaration declaration = (TypeDeclaration) next;
				 traverseType(declaration);
			}
		}


	}
	private void traverseType(TypeDeclaration declaration) {
		if(baseTraceabilityObject !=null ){
			DeclarationVisitor myVisitor = new DeclarationVisitor(declaration, baseTraceabilityObject);
			if(myVisitor.getEnclosingDeclaration() !=null )
				foundEnclosingDeclaration = myVisitor.getEnclosingDeclaration();
			
			if(myVisitor.getExpressions() != null){
				expressionsFound = myVisitor.getExpressions();
			}
		}	
	}

}