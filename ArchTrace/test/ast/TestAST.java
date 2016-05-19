package ast;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class TestAST {

	// TODO: Use JUnit feature to create a temporary file in the right location
	String filename = "C:\\Temp\\TestAST.xml";
		
	BaseTraceability trace;
	
	@Before
	public void initTraceability() {
			
		TypeDeclaration typeDecl = new TypeDeclaration();
		Type type = new Type();
		
		type.fullyQualifiedName = "x.y.z.Foo";
		typeDecl.type = type; 
		
		MethodDeclaration methDecl = new MethodDeclaration();
		methDecl.methodName = "test";
		methDecl.enclosingType = typeDecl;
		methDecl.addParameter("p1", "int");
		methDecl.addParameter("p2", "boolean");
		
		FieldWrite fieldWrite = new FieldWrite();
		fieldWrite.enclosingDeclaration = methDecl;
		// TODO: Set the enclosing scope of the FieldWrite
		
		this.trace = new ObjectTraceability(fieldWrite);
	}
	
	
	@Test
	// NOTE: testSaving must have been called before, to create the file
	public void testSaving() {
		
		TestUtils.save(trace, filename);
	}
	
	@Test
	// NOTE: testSaving must have been called before, to create the file
	public void testLoading() {
		BaseTraceability trace2 = TestUtils.load(filename);

		// TODO: Requires equals to be implemented to be successful
//		assertEquals(trace.enclosingDeclaration, trace2.enclosingDeclaration);
		
		// DONE. Implement toString()
		assertEquals(trace.toString(), trace2.toString());
	}
	
	@Test
	/*
	 * Saves all xml files to test. RUN FIRST
	 */
	public void createAndSaveTraceabilityObjectsToFindInEclipseAST() {
		createAndSave("C:\\Temp\\");
		
		// ANDREW: Hook into your code in here.
	}


	public void createAndSave(String path) {
		String filename = path+"FindFieldDeclarationEclipseAST.xml";
		trace = BaseTraceabilityTestFactory.createFieldDeclaration();
		TestUtils.save(trace, filename);
		
		
		trace = BaseTraceabilityTestFactory.createMethodDeclaration();
		filename = path+"FindMethodDeclarationEclipseAST.xml";
		TestUtils.save(trace, filename);
		
		trace = BaseTraceabilityTestFactory.createTypeDeclaration();
		filename = path+"FindTypeDeclarationEclipseAST.xml";
		TestUtils.save(trace, filename);
		
		
		trace = BaseTraceabilityTestFactory.createDefaultPackageFieldDeclaration();
		filename = path+"FindDefaultPackageFieldDeclarationEclipseAST.xml";
		TestUtils.save(trace, filename);
		
		
		trace = BaseTraceabilityTestFactory.createInnerClassFieldDeclaration();
		filename = path+"FindInneClassFieldDeclarationEclipseAST.xml";
		TestUtils.save(trace, filename);
		
		
		
		trace = BaseTraceabilityTestFactory.createClassInstanceCreation();
		filename = path+"FindClassInstanceCreationEclipseAST.xml";
		TestUtils.save(trace,filename);
		
		
		trace = BaseTraceabilityTestFactory.createFieldWrite();
		filename = path+"FindFieldWriteEclipseAST.xml";
		TestUtils.save(trace,filename);
		
		
		trace = BaseTraceabilityTestFactory.createGenericType();
		filename = path+"GenericTypeEclipseAST.xml";
		TestUtils.save(trace,filename);
	}
	
	@Test
	/*
	 * Traverse the mini ast and locate the right node in the full Eclipse AST.
	 */
	public void testFindMiniAST() {
		
		// ANDREW: Hook into your code in here.
	}
	
	
}
