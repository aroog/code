package adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ast.AstNode;
import ast.BodyDeclaration;
import ast.Type;
import ast.VariableDeclaration;


public class TraceabilityFactory {



	/**
	 * traverse the parents until the type of Parent is a TypeDeclaration;
	 * */
	public static ast.TypeDeclaration getEnclosingTypeDeclaration(ASTNode node) {
		if (node == null)
			// this should never happen
			return null;
		else if (node.getParent() instanceof TypeDeclaration) {
			TypeDeclaration domTD = (TypeDeclaration) node.getParent();
			ast.TypeDeclaration td = ast.TypeDeclaration.createFrom(domTD);
			return td;
		} else
			return getEnclosingTypeDeclaration(node.getParent());
	}

	/**
 	* 
	 * traverse the parents until the type of Parent is either a
	 * MethodDeclaration or FieldDeclaration;
	 * 
	 * XXX. This not just returning only Field Or MethodDeclaration.
	 * Could also return variable declaration!
	 * 
	 * TODO: Rename -> getEnclosingDeclaration
	 */
	public static BodyDeclaration getEnclosingFieldMethodDeclaration(ASTNode node) {
		if (node != null) {
			ASTNode parent = node.getParent();
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration domMD = (MethodDeclaration) parent;
				ast.MethodDeclaration md = ast.MethodDeclaration.createFrom(domMD);//getMethodDeclaration(domMD.resolveBinding());
				md.enclosingType = TraceabilityFactory.getEnclosingTypeDeclaration(node);
				return md;
			} else if (parent instanceof FieldDeclaration) {
				FieldDeclaration domFD = (FieldDeclaration) parent;
				VariableDeclarationFragment domVDF = (VariableDeclarationFragment) node;
				ast.FieldDeclaration fd = getFieldDeclaration(domVDF);
				fd.enclosingType = TraceabilityFactory.getEnclosingTypeDeclaration(node);
				fd.enclosingDeclaration = fd.enclosingType;
				return fd;
			} else if (parent instanceof VariableDeclarationFragment ) {
				VariableDeclarationFragment vdFrag = (VariableDeclarationFragment)parent;
				ast.VariableDeclaration vd = getVariableDeclaration(vdFrag);
				vd.enclosingDeclaration = getEnclosingFieldMethodDeclaration(parent);
				return vd;
			}
			else if (parent instanceof Assignment ) {
				Assignment assign = (Assignment)parent;
				Expression lhs = assign.getLeftHandSide();
				if (lhs instanceof SimpleName ){
					SimpleName sn = (SimpleName)lhs;
					IBinding binding = sn.resolveBinding();
					if(binding instanceof IVariableBinding) {
						IVariableBinding varBinding = (IVariableBinding)binding;
						if(varBinding.isField()) {
							ast.FieldDeclaration fd = ast.FieldDeclaration.createFrom(varBinding);
							return fd;
						}
						else {
							return getEnclosingFieldMethodDeclaration(parent);
							// ast.VariableDeclaration vd = ast.VariableDeclaration.createFrom(varBinding);
							// return vd;
						}
					}
					else {
						int debug = 0; debug++;
						return null;
					}
				}
				// Handle case of "this.f"
				else if (lhs instanceof FieldAccess) {
					FieldAccess fa = (FieldAccess)lhs;
					IVariableBinding fieldBinding = fa.resolveFieldBinding();
					ast.FieldDeclaration fd = ast.FieldDeclaration.createFrom(fieldBinding);
					return fd;
				}
				else {
					int debug = 0; debug++;
					return null;
				}
			}
			else
				return getEnclosingFieldMethodDeclaration(parent);
			// No need for SingleVariableDeclaration....used in a limited number of places, including formal parameter lists and catch clauses
			// else if(parent instanceof SingleVariableDeclaration ) {
			// }
		}
		else
			// this should never happen
			return null;
	}

	public static ast.FieldDeclaration getFieldDeclaration(VariableDeclarationFragment domVDF) {
		return ast.FieldDeclaration.createFrom(domVDF);
	}

	
	public static ast.VariableDeclaration getVariableDeclaration(VariableDeclarationFragment domVDF) {
		return ast.VariableDeclaration.createFrom(domVDF);
	}

	/**
	 * @param methodBinding
	 * @return
	 */
	public static Type getType(ITypeBinding typeBinding) {
		return ast.Type.createFrom(typeBinding);
	}

	/**
	 * @param instr
	 * @return
	 */
/*	private static ast.TypeDeclaration getTypeDeclaration(ITypeBinding typBinding) {
		ast.TypeDeclaration declaredType = ast.TypeDeclaration.create();
		declaredType.type = getType(typBinding);
		declaredType.fields = getDeclaredFields(typBinding);
		return declaredType;
	}*/
//TORAD: remove this method since it creates FieldDeclarations without using the factory
/*	@Deprecated
	public static List<ast.FieldDeclaration> getDeclaredFields(ITypeBinding declaringClass) {
		List<ast.FieldDeclaration> fields = new ArrayList<ast.FieldDeclaration>();
		IVariableBinding[] declaredFields = declaringClass.getDeclaredFields();
		for (IVariableBinding iVariableBinding : declaredFields) {
			ast.FieldDeclaration fd = ast.FieldDeclaration.create();
			fd.fieldName = iVariableBinding.getName();
			fd.fieldType = getType(iVariableBinding.getType());
			fields.add(fd);
		}

		return fields;
	}*/

/*	private ast.MethodDeclaration getMethodDeclaration(IMethodBinding methodBinding) {
		ast.MethodDeclaration methodDeclaration = new ast.MethodDeclaration();
		methodDeclaration.methodName = methodBinding.getName();
		methodDeclaration.parameters = getMethodParameters(methodBinding);
		methodDeclaration.returnType = getType(methodBinding.getReturnType());
		methodDeclaration.enclosingType = getEnclosingType(methodBinding);
		return methodDeclaration;
	}*/

	/**
	 * @param methodBinding
	 * @return
	 */
//	private ArrayList<ast.VariableDeclaration> getMethodParameters(IMethodBinding methodBinding) {
//		ArrayList<ast.VariableDeclaration> params = new ArrayList<ast.VariableDeclaration>();
//		ITypeBinding[] typeParameters = methodBinding.getTypeParameters();
//		for (ITypeBinding iTypeBinding : typeParameters) {
//			ast.Type type = getType(iTypeBinding);
//			ast.VariableDeclaration vd = new ast.VariableDeclaration();
//			vd.varType = type;
//			params.add(vd);
//		}
//		return params;
//	}

	
	/**
	 * returns the method Parameters as a list of ast.VariableDeclarataion 
	 * */
	public static List<ast.VariableDeclaration> getMethodParameters(MethodDeclaration md) {
		List<ast.VariableDeclaration> params = new ArrayList<ast.VariableDeclaration>();
		IMethodBinding methodBinding = md.resolveBinding();
		if(methodBinding != null ) {
			ITypeBinding[] typeParameters = methodBinding.getTypeParameters();
			List<SingleVariableDeclaration> svdList = md.parameters();
			for (SingleVariableDeclaration svd : svdList) {
				ast.Type type = getType(svd.getType().resolveBinding());
				ast.VariableDeclaration vd = VariableDeclaration.createFrom(svd);
				vd.varType = type;
				vd.varName = svd.getName().getFullyQualifiedName();
				params.add(vd);
			}
		}
		return params;
	}
	
	
	/**
	 * TODO: return a methodDeclaration from a methodInvocation
	 * 		CompilationUnit cu;
     * 		cu.findDeclaringNode(mInvk.resolveMethodBinding());
     * needed to assign to ast.MethodInvocation.methodDeclaration
     * 
     * TOMAR: TODO: How do we get hold of CompilationUnit here?
     * TOMAR: TODO: Find code to do: IBinding -> DeclarationNode 
	 * 
	 * */
	public static MethodDeclaration getDeclarationFromInvocation(MethodInvocation mInvk){
		//TODO: implement me;
		return null;
	}
/*	*//**
	 * @param methodBinding
	 * @return
	 *//*
	private ast.TypeDeclaration getEnclosingType(IMethodBinding methodBinding) {
		ast.TypeDeclaration typeDeclaration = getTypeDeclaration(methodBinding.getDeclaringClass());
		return typeDeclaration;
	}*/

	public static List<ast.FieldDeclaration> getDeclaredFields(
			TypeDeclaration td) {
		List<ast.FieldDeclaration> fieldList = new ArrayList<ast.FieldDeclaration>();
		for(FieldDeclaration fieldDec: td.getFields()){
			List<VariableDeclarationFragment> fragments = fieldDec.fragments();
			for(VariableDeclarationFragment varDecFrag: fragments){
				fieldList.add(getFieldDeclaration(varDecFrag));
			}
		}
		return fieldList;
	}
	
	public static List<ast.FieldDeclaration> getDeclaredFields(ITypeBinding typeBinding) {
		List<ast.FieldDeclaration> fieldList = new ArrayList<ast.FieldDeclaration>();
		
		for(IVariableBinding fieldDecl: typeBinding.getDeclaredFields()){
			fieldList.add(getFieldDeclaration(fieldDecl));
		}
		return fieldList;
	}


	private static ast.FieldDeclaration getFieldDeclaration(IVariableBinding fieldDecl) {
		return ast.FieldDeclaration.createFrom(fieldDecl);
	}

	// TODO: See if we can pick a more specific type than 'ASTNode'
	// TODO: 'node' is instanceof Assignment?
	public static TypeDeclaration getDeclarationFromAssignment(ASTNode node) {
		// TODO Implement me
		return null;
	}

	public static TypeDeclaration getDeclarationFromCreation(ClassInstanceCreation node) {
		// TODO implement me
		return null;
	}

	public static ast.MethodDeclaration getMethodDeclaration(ASTNode node) {
		MethodDeclaration md = null;
		String name = null;
		Type retType = null;
		if (node instanceof MethodInvocation){
			MethodInvocation domMethodInvk = (MethodInvocation)node;
			md = getDeclarationFromInvocation(domMethodInvk);
			name = domMethodInvk.getName().getFullyQualifiedName();
			retType = getType(domMethodInvk.resolveMethodBinding().getReturnType());
		}
		else if (node instanceof SuperMethodInvocation){
			SuperMethodInvocation domMethodInvk = (SuperMethodInvocation)node;
			md = getDeclarationFromSuperInvocation(domMethodInvk);
			name = domMethodInvk.getName().getFullyQualifiedName();
			retType = getType(domMethodInvk.resolveMethodBinding().getReturnType());
		}	
		
		ast.MethodDeclaration methodDeclaration = null; 
		if(md != null){
			methodDeclaration = ast.MethodDeclaration.createFrom(md);
			methodDeclaration.methodName = name;
			methodDeclaration.parameters = getMethodParameters(md);
			methodDeclaration.returnType = retType;
			methodDeclaration.enclosingType = getEnclosingTypeDeclaration(node);
		}	
		return methodDeclaration;
	}

		
	private static MethodDeclaration getDeclarationFromSuperInvocation(SuperMethodInvocation domMethodInvk) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param instr
	 * @return
	 */
	public static ast.TypeDeclaration getTypeDeclaration(TypeDeclaration node) {
		ast.TypeDeclaration declaredType = ast.TypeDeclaration.createFrom(node);
		declaredType.type = getType(node.resolveBinding());
		declaredType.fields = getDeclaredFields(node);
		return declaredType;
	}

	// TODO: 'node' is instanceof QualifiedName?
	@Deprecated	
	/**
	 * @deprecated Do NOT use; always returns null  
	 */
	public static VariableDeclarationFragment getDeclarationFromFieldRead(ASTNode node) {
		// TODO Implement me
		return null;
	}

	@Deprecated
	/**
	 * @deprecated Do NOT use; always returns null  
	 */
	public static VariableDeclarationFragment getDeclarationFromFieldWrite(ASTNode node) {
		// TODO Implement me
		return null;
	}	
	
	/***
	 * 
	 * @param fullyQualifiedName as in package.Foo
	 * @return an ast.TypeDeclaration already stored in the map
	 * @return null if no such declaration exists in the map 
	 */
	public static ast.TypeDeclaration getTypeDeclaration(String fullyQualifiedName) {
		return Adapter.getInstance().getTypeDeclaration(fullyQualifiedName);
		
//		Set<AstNode> allNodes  = Adapter.getInstance().getValues();
//		for (AstNode astNode : allNodes) {
//			if (astNode instanceof ast.TypeDeclaration){
//				ast.TypeDeclaration td = (ast.TypeDeclaration)astNode;
//				if (td.type.getFullyQualifiedName().equals(fullyQualifiedName))
//					return td;
//			}
//		}
//		return null;
	}

	/***
	 * Lookup an ast.FieldDeclaration based on qualifiedClassName and fieldName
	 * @param qualifiedClassName - fully qualified typeName name as in package.Foo.f
	 * @param fieldName  - simple field name as in f
	 * @return an ast.FieldDeclaration already stored in the map
	 * @return null if no such declaration exists in the map 
	 */
	public static ast.FieldDeclaration getFieldDeclaration(String qualifiedClassName, String fieldName) {
		ast.FieldDeclaration fieldDeclaration = Adapter.getInstance().getFieldDeclaration(qualifiedClassName + fieldName);
		if(fieldDeclaration != null)
			return fieldDeclaration;
		
		// If not found, it may not have been added yet?
		// XXX. But why not do this once?
		Collection<ast.TypeDeclaration> allNodes = Adapter.getInstance().getTypeDeclarations();
		for (AstNode astNode : allNodes) {
			ast.TypeDeclaration td = (ast.TypeDeclaration) astNode;
			if (td.type.getFullyQualifiedName().equals(qualifiedClassName)) {
				List<ast.FieldDeclaration> fields = td.fields;
				if (fields != null)
					for (ast.FieldDeclaration fd : fields) {
						if (fd.fieldName.endsWith(fieldName)) {
							Adapter.getInstance().mapFieldDeclaration(fd);
							return fd;
						}
					}
			}
		}
		return null;
	}

	/***
	 * Lookup an ast.MethodDeclaration based on className and methodName
	 * 
	 * @param qualifiedTypeName
	 * @param methodName
	 * @return ast.MethodDeclaration
	 * 
	 * HACK: XXX, also include in the search type of parameters. 
	 * Currently the search does not distinguish between C.m and C.m(A)
	 * class C{
	 * 	 void m(){}
	 * 	 void m(A a){}
	 * }
	 */
	public static ast.MethodDeclaration getMethodDeclaration(String qualifiedTypeName, String methodName) {
		ast.MethodDeclaration methodDeclaration = Adapter.getInstance().getMethodDeclaration(qualifiedTypeName + methodName);
		if (methodDeclaration != null)
			return methodDeclaration;
		
		// If not found, it may not have been added yet?
		// XXX. But why not do this once?
		Collection<ast.MethodDeclaration> allNodes = Adapter.getInstance().getMethodDeclarations();
		for (ast.MethodDeclaration md : allNodes) {
			ast.TypeDeclaration td = md.enclosingType;
			if (td != null && td.type != null)
				if (td.type.getFullyQualifiedName().equals(qualifiedTypeName))
					if (md.methodName.equals(methodName)) {
						Adapter.getInstance().mapMethodDeclaration(md);
						return md;
					}
		}
		return null;		
	}
}