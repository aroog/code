package ast;


import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestFDJ {

	@Test
	/**
	 * NOTE: Trivial visibility modifiers omitted
	 * Except for public/private domain
	 * 
	 * class Sequence<ELTS> {
	 * 	 private domain owned;
	 * 	 public domain ITERS;
	 *   
	 *   Object<ELTS> obj;
	 *   
	 *   Iterator<ITERS,ELTS> iterator() {
	 *   	return new SeqIterator<ITERS,ELTS,owned>();
	 *   }
	 * }
	 * 
	 * 
	 * Use syntax closer to concrete system:
	 *  
	 * class Sequence<ELTS> {
	 * 	 private domain owned;
	 * 	 public domain ITERS;
	 *   
	 *   ELTS Object obj;
	 *   
	 *   ITERS Iterator<ELTS> iterator() {
	 *   	ITERS SeqIterator<ELTS,owned> it = new SeqIterator<ELTS,owned>();
	 *   	return it; 
	 *   }
	 * }
	 */
	public void testSequence() {
		
		TypeDeclaration typeDecl = new TypeDeclaration();
		Type type = new Type("Sequence");
		typeDecl.type = type;

		String[] arr_ownedITERS = new String[] { "owned", "ITERS" };
		typeDecl.setDomains(arr_ownedITERS);
		String[] arr_ELTS = new String[]{ "ELTS"};
		typeDecl.setParameters(arr_ELTS);
		
		assertNotNull(typeDecl.getDomains());
		assertNotNull(typeDecl.getParameters());

//		assertEquals(typeDecl.getDomains().get(0), arr_ownedITERS[0]);
//		assertEquals(typeDecl.getParameters().get(0).getDomain(), arr_ELTS[0]);
		
		
		FieldDeclaration fieldDecl = new FieldDeclaration();
		fieldDecl.enclosingType = typeDecl;
		fieldDecl.enclosingDeclaration = typeDecl;
		fieldDecl.fieldName = "obj";
		fieldDecl.fieldType = new Type("Object");
		// TODO: Create a better way to construct AnnotationInfo objects instead of parsing from Strings!
		fieldDecl.annotation = "ELTS";
		
//		assertTrue( typeDecl.hasDomain(fieldDecl.getOwningDomain()) || 
//							typeDecl.hasDomainParam(fieldDecl.getAnnotation()));
		
		MethodDeclaration methodDecl = new MethodDeclaration();
		methodDecl.enclosingType = typeDecl;
		methodDecl.methodName = "iterator";
		methodDecl.returnType = new Type("Iterator");
		methodDecl.returnAnnotation = "ITERS<ELTS>";
		methodDecl.receiverAnnotation = "owner";
		
		// TODO: HIGH. XXX. I cannot state that this ClassInstanceCreation is within the body of MethodDeclaration!
		ClassInstanceCreation newIter = new ClassInstanceCreation();
		newIter.complexExpression = "new SeqIterator()";
		newIter.annotation = "ITERS<ELTS,owned>";
		
//		String annotation2 = newIter.annotation;
//		AnnotationInfo parseAnnotation = AnnotationInfo.parseAnnotation(annotation2);
//		assertEquals( parseAnnotation.getAnnotation().getDomain(), "ITERS");
//		assertEquals( parseAnnotation.getParameters().get(0), DomainParams.create("ELTS"));
//		assertEquals( parseAnnotation.getParameters().get(1), DomainParams.create("owned"));
		
	}
	
	
}
