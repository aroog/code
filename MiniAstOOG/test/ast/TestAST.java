package ast;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestAST {

	// TODO: Use JUnit feature to create a temporary file in the right location
	String filename = "C:\\temp\\TestAST.xml";
		
	BaseTraceability trace;
	
	public BaseTraceability getTrace() {
    	return trace;
    }

	@Before
	public void initTraceability() {
			
		TypeDeclaration typeDecl = new TypeDeclaration();
		Type type = new Type("x.y.z.Foo");
		typeDecl.type = type; 
		
		MethodDeclaration methDecl = new MethodDeclaration();
		methDecl.methodName = "test";
		methDecl.enclosingType = typeDecl;
		methDecl.addParameter("p1", "Int");
		methDecl.addParameter("p2", "Boolean");
		
		FieldWrite fieldWrite = new FieldWrite();
		// TODO: Set the enclosing scope of the FieldWrite
		
		fieldWrite.enclosingDeclaration = methDecl;
		methDecl.enclosingType = typeDecl;
		
		this.trace = new ObjectTraceability(fieldWrite);
	}
	
	@Test
	public void testSaving() {
		TestUtils.save(trace, filename);
	}
	
	@Test
	public void testLoading() {
		BaseTraceability trace2 = TestUtils.load(filename);

		// TODO: Requires equals to be implemented to be successful
//		assertEquals(trace.enclosingDeclaration, trace2.enclosingDeclaration);
		
		// DONE. Implement toString()
		assertEquals(trace.toString(), trace2.toString());
		
	}
}
