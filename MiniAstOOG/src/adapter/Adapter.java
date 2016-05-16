package adapter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import ast.AstNode;
import ast.FieldDeclaration;
import ast.MethodDeclaration;
import ast.TypeDeclaration;
import ast.VariableDeclaration;

// XXX. This class holds on to Eclipse ASTNode. Bad idea.
// 
// TONOTDO: Should this adapter also use bindings?
// - create Map<IBinding, AstNode> ...
// - create static factory methods ClassInstanceCreation.create(ITypeBinding binding);
// 
// TONOTDO: Create another map where the type of the key be an "IBinding"? and the value a Mini AstNode/
// -- VariableBinding, MethodBinding, TypeBinding
// Answer: No. The same binding can correspond to multiple ASTNodes. 
//         We still want to create multiple mini AstNodes
// If you have an IBinding (ITypeBinding, IVariableBinding, IMethodBinding), you can lookup the declaring ASTNode 
// (TypeDeclaration, VariableDeclaration, MethodDeclaration) node.
// Then use this code.
//
//
// Need also a reverse map to lookup the underlying Eclipse AST Node. Otherwise, need to have a tag in each MiniAstNode:
// 
// @Transient
// public Object tag;
public class Adapter {
	
	private static Adapter s_instance = null;

	// Map Eclipse ASTNode to Mini AStNode
	private Map<ASTNode, AstNode> mapNodes = new HashMap<ASTNode, AstNode>();
	// Reverse map
	private Map<AstNode, ASTNode> revMapNodes = new HashMap<AstNode, ASTNode>();

	//added to handle generics for which we do not have an AstNode
	private Map<String, AstNode> mapBindings = new HashMap<String, AstNode>();
	
	// DONE. Store separate maps to speed up lookup since the keys are strings
	// Fully qualified name -> TypeDecl
	private Map<String, ast.TypeDeclaration> mapTypeDecls = new HashMap<String, ast.TypeDeclaration>();
	// "Type.MethodName" -> MethodDecl
	private Map<String, ast.MethodDeclaration> mapMethDecls = new HashMap<String, ast.MethodDeclaration>();
	// "Type.FieldName" -> MethodDecl
	private Map<String, ast.FieldDeclaration> mapFieldDecls = new HashMap<String, ast.FieldDeclaration>();

	public void clear() {
		mapNodes.clear();
		revMapNodes.clear();
		mapBindings.clear();
		
		mapTypeDecls.clear();
		mapMethDecls.clear();
		mapFieldDecls.clear();
	}
	
	public void map(ASTNode key, AstNode value) {
		if(key !=null) {
			mapNodes.put(key, value);
			// Populate reverse map too
			revMapNodes.put(value, key);
		}
		//Do not add a null key to avoid overriding 
	}
	
	public AstNode get(ASTNode key) {
		return mapNodes.get(key);
	}

	public ASTNode get(AstNode key) {
		return revMapNodes.get(key);
	}

	/**
	 * ensure that we do not have the same type, one for binding, one for ASTNode
	 * null keys are discarded to avoid overriding
	 * @param key
	 * @param value
	 */
	public void map(ITypeBinding key, TypeDeclaration value) {
		if(key !=null)
			if (TraceabilityFactory.getTypeDeclaration(key.getQualifiedName())==null)			
				mapBindings.put(key.getKey(), value);			
	}

	public AstNode get(ITypeBinding typeBinding) {
		TypeDeclaration td = TraceabilityFactory.getTypeDeclaration(typeBinding.getQualifiedName());
		if (td!=null)
			return td;
		return mapBindings.get(typeBinding.getKey());
	}

	public AstNode get(IVariableBinding fieldDecl) {
//		FieldDeclaration fd = TraceabilityFactory.getFieldDeclaration(fieldDecl.getDeclaringClass().getQualifiedName(), fieldDecl.getName());
//		if (fd!=null)
//			return fd;
		return mapBindings.get(fieldDecl.getKey());
	}

	public void map(IVariableBinding fieldDecl, FieldDeclaration value) {
		if(fieldDecl !=null)
			if (TraceabilityFactory.getFieldDeclaration(fieldDecl.getDeclaringClass().getQualifiedName(), fieldDecl.getName())==null)			
				mapBindings.put(fieldDecl.getKey(), value);
		
	}
	
	public void map(IVariableBinding varDecl, VariableDeclaration value) {
		if(varDecl !=null)
			//if (TraceabilityFactory.getFieldDeclaration(varDecl.getDeclaringClass().getQualifiedName(), varDecl.getName())==null)			
				mapBindings.put(varDecl.getKey(), value);
	}
	
	
	/**
	 * return a readonly version of values.
	 * do NOT attempt to change the collection
	 * intended to use only to query the map
	 *  union of both maps
	 *
	 * @deprecated This is very slow! Iterates through all the mini AstNodes!
	 * 
	 * Break it up into separate maps.
	 * */
	// XXX. Why creating a copy?!??!?! Avoid the addAll...
	@Deprecated 
	private Set<AstNode> getValues() {
		Set<AstNode> set = new HashSet<AstNode>();
		set.addAll(mapNodes.values());
		set.addAll(mapBindings.values());
		return Collections.unmodifiableSet(set);
	}
	

	public static Adapter getInstance() {
		if ( s_instance == null ) {
			s_instance = new Adapter();
		}
		
		return s_instance;
	}
	//For Testing Purposes
	@Override
	public String toString() {
		
		StringBuffer buffer = new StringBuffer();
		for(AstNode node: mapNodes.values()){
			buffer.append(node).append("\n");
		}
		
		return buffer.toString();
	}


	public static void reset() {
		if (s_instance != null ) {
			s_instance.clear();
		}
		s_instance = null;
	}

	public void mapTypeDeclaration(ast.TypeDeclaration value) {
		String fullyQualifiedName = value.getFullyQualifiedName();
		if(fullyQualifiedName == null || fullyQualifiedName.length() == 0 ) {
			System.err.println("Empty type information: will lead to collision");
		}
		mapTypeDecls.put(fullyQualifiedName, value);
	}
	
	public ast.TypeDeclaration getTypeDeclaration(String fullyQualifiedName) {
	    return mapTypeDecls.get(fullyQualifiedName);
    }

	public void mapMethodDeclaration(ast.MethodDeclaration md) {
		ast.TypeDeclaration td = md.enclosingType;
		if (td!=null && td.type!=null)
			this.mapMethDecls.put(td.type + md.methodName, md);
    }

	public ast.MethodDeclaration getMethodDeclaration(String typeNamePlusMethodName) {
	    return this.mapMethDecls.get(typeNamePlusMethodName);
    }
	
	public void mapFieldDeclaration(ast.FieldDeclaration fd) {
		ast.TypeDeclaration td = fd.enclosingType;
		if (td!=null && td.type!=null)
			this.mapFieldDecls.put(td.type.getFullyQualifiedName() + fd.fieldName, fd);
	}

	public ast.FieldDeclaration getFieldDeclaration(String classNamePlusFieldName) {
	    return this.mapFieldDecls.get(classNamePlusFieldName);
    }

	public Collection<TypeDeclaration> getTypeDeclarations() {
    	return mapTypeDecls.values();
    }
	
	public Collection<MethodDeclaration> getMethodDeclarations() {
    	return mapMethDecls.values();
    }
	
}
