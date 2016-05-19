package ast;

import ast.BaseTraceability;
import ast.FieldDeclaration;
import ast.Type;
import ast.TypeDeclaration;


public class BaseTraceabilityTestFactory {

	
	public static BaseTraceability createFieldDeclaration(){
		//Creating BaseTraceability for field foo.Bar.b
		BaseTraceability trace = new ObjectTraceability();
		
		TypeDeclaration typeDecl = new TypeDeclaration();
		Type type = new Type();
		type.fullyQualifiedName = "foo.Bar";
		typeDecl.type = type; 
		
		FieldDeclaration fieldDecl = new FieldDeclaration();
		fieldDecl.enclosingType = typeDecl;
		fieldDecl.fieldName = "b";
		fieldDecl.enclosingDeclaration = typeDecl;
		
		return trace;
	}
	
	public static BaseTraceability createMethodDeclaration(){
		//Creating BaseTraceability for method Bar.m(String s, int i)
		BaseTraceability trace = new ObjectTraceability();
		
		TypeDeclaration typeDecl = new TypeDeclaration();
		Type type = new Type();
		type.fullyQualifiedName = "Bar";
		typeDecl.type = type; 
		
		MethodDeclaration methodDec = new MethodDeclaration();
		methodDec.enclosingType = typeDecl;
		methodDec.methodName = "m";
		methodDec.addParameter("s","String" );
		methodDec.addParameter("i", "int");
		methodDec.enclosingDeclaration = typeDecl;
		return trace;
	}	
	
	public static BaseTraceability createTypeDeclaration(){
		//Creating BaseTraceability for TypeDeclaration foo.Bar
		BaseTraceability trace = new ObjectTraceability();
		
		TypeDeclaration typeDecl = new TypeDeclaration();
		Type type = new Type();
		type.fullyQualifiedName = "foo.Bar";
		typeDecl.type = type; 
		typeDecl.enclosingDeclaration = typeDecl;
		
		return trace;
	}
	
	
	public static BaseTraceability createDefaultPackageFieldDeclaration(){
		//Creating BaseTraceability for field Bar.b
		BaseTraceability trace = new ObjectTraceability();
		TypeDeclaration typeDecl = new TypeDeclaration();
		Type type = new Type();
		type.fullyQualifiedName = "Bar";
		typeDecl.type = type; 
		FieldDeclaration fieldDecl = new FieldDeclaration();
		fieldDecl.enclosingType = typeDecl;
		fieldDecl.fieldName = "b";
		fieldDecl.enclosingDeclaration = typeDecl;
		return trace;
	}
	
	
	public static BaseTraceability createInnerClassFieldDeclaration(){
		// find foo.Bar.Baz.b with inner classs Baz in Bar
		BaseTraceability trace = new ObjectTraceability();
		TypeDeclaration typeDec = new TypeDeclaration();
		Type type = new Type();
		type.fullyQualifiedName = "foo.Bar.Baz";
		typeDec.type = type;
		FieldDeclaration fieldDec = new FieldDeclaration();
		fieldDec.enclosingType = typeDec;
		fieldDec.fieldName = "b";
		fieldDec.enclosingDeclaration = typeDec;
		return trace;
	}
	
	
	public static BaseTraceability createClassInstanceCreation(){
		TypeDeclaration typeDec = new TypeDeclaration();
		Type type = new Type();
		type.fullyQualifiedName = "Bar";
		typeDec.type = type;
		MethodDeclaration methodDec = new MethodDeclaration();
		methodDec.enclosingType = typeDec;
		methodDec.methodName = "m";
		

		ast.ClassInstanceCreation classInst = new ast.ClassInstanceCreation();
		classInst.complexExpression = "new Biff()";
		
		BaseTraceability trace = new ObjectTraceability(classInst);
		classInst.enclosingDeclaration = typeDec;
		return trace;
	}
	
	
	public static BaseTraceability createFieldWrite(){
		TypeDeclaration typeDec = new TypeDeclaration();
		Type type = new Type();
		type.fullyQualifiedName = "foo.Bar";
		typeDec.type = type;
		MethodDeclaration methodDec = new MethodDeclaration();
		methodDec.enclosingType = typeDec;
		methodDec.methodName = "m";
		methodDec.enclosingDeclaration = typeDec;
		
		ast.FieldWrite fieldWrite = new ast.FieldWrite();
		fieldWrite.complexExpression = "this.b=new Biff()";
		fieldWrite.enclosingDeclaration = methodDec;
		
		BaseTraceability trace = new ObjectTraceability(fieldWrite);
		return trace;
	}
	
	public static BaseTraceability createGenericType(){
		BaseTraceability trace = new ObjectTraceability();
		TypeDeclaration typeDec = new TypeDeclaration();
		Type type = new Type();
		type.fullyQualifiedName = "foo.GenericBar";
		typeDec.type = type;
		
		FieldDeclaration fieldDec = new FieldDeclaration();
		fieldDec.enclosingType = typeDec;
		fieldDec.fieldName = "key";
		fieldDec.enclosingDeclaration = typeDec;
		
		return trace;
	}
	
	
	
	
}
