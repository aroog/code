package edu.wayne.auxiliary;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.cmu.cs.crystal.util.TypeHierarchy;
import edu.wayne.ograph.internal.QualifiedClassName;

/**
 * This class will define static methods for auxiliary judgments mbody, mtype,
 * fields, etc.
 * */
public class AuxJudgements {

	public static boolean hasConstructor(TypeDeclaration typeDecl, Map<ast.Type, TypeDeclaration> types,
			QualifiedClassName cThis) {
		boolean hasConstr = false;
		if (!hasFieldInitializers(typeDecl))
			hasConstr = true;
		else {
			for (MethodDeclaration md : typeDecl.getMethods()) {
				if (md.isConstructor())
					hasConstr = true;
			}
		}
		Type superclassType = typeDecl.getSuperclassType();
		if (superclassType == null)
			return hasConstr;
		if (superclassType.resolveBinding().getQualifiedName().equals(Utils.JAVA_LANG_OBJECT))
			return hasConstr;
		TypeDeclaration superTypeDecl = types.get(new QualifiedClassName(superclassType.resolveBinding(), cThis)
				.getType());
		if (superTypeDecl != null) {
			hasConstr = hasConstr && hasConstructor(superTypeDecl, types, cThis);
		}
		return hasConstr;

	}

	private static boolean hasFieldInitializers(TypeDeclaration typeDecl) {
		boolean returnFlag = false;
		for (FieldDeclaration fd : typeDecl.getFields()) {
			// Skip over primitive types
			if (fd.getType().isPrimitiveType() ) {
				continue;
			}
			if (fd.fragments().size() > 0)
				if (fd.fragments().get(0) instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) fd.fragments().get(0);
					if (vdf.getInitializer() != null)
						returnFlag = true;
				}
		}
		return returnFlag;
	}

	/**
	 * returns all method declarations in a type, and its supertype recursively.
	 * call MethodDeclaration.getBody() to get the body of the method. TODO:
	 * actual/formal substitution
	 * */
	public static Set<MethodDeclaration> mBody(TypeDeclaration typeDecl, TypeHierarchy hierarchy,
			Map<ast.Type, TypeDeclaration> types, QualifiedClassName cThis) {
		Set<MethodDeclaration> returnSet = new HashSet<MethodDeclaration>();
		for (MethodDeclaration md : typeDecl.getMethods()) {
			returnSet.add(md);
		}
		Type superclassType = typeDecl.getSuperclassType();
		if (superclassType == null)
			return returnSet;
		if (superclassType.resolveBinding().getQualifiedName().equals(Utils.JAVA_LANG_OBJECT))
			return returnSet;

		TypeDeclaration superTypeDecl = types.get(new QualifiedClassName(superclassType.resolveBinding(), cThis)
				.getType());
		if (superTypeDecl != null) {
			Set<MethodDeclaration> auxSet = mBody(superTypeDecl, hierarchy, types, cThis);
			returnSet.addAll(auxSet);
			// TODO: here you don't want to add OverrideMethod
			// TODO: find a way to uniquely identify methods: define mtype.
		}
		return returnSet;
	}

	/**
	 * returns all field bodies declared in a type, and its supertype,
	 * recursively TODO: actual/formal substitution
	 * */
	public static Set<FieldDeclaration> fieldBody(TypeDeclaration typeDecl, TypeHierarchy hierarchy,
			Map<ast.Type, TypeDeclaration> types, QualifiedClassName cThis) {
		Set<FieldDeclaration> returnSet = new HashSet<FieldDeclaration>();
		for (FieldDeclaration fd : typeDecl.getFields())
			returnSet.add(fd);
		Type superclassType = typeDecl.getSuperclassType();
		if (superclassType == null)
			return returnSet;
		if (superclassType.resolveBinding().getQualifiedName().equals(Utils.JAVA_LANG_OBJECT))
			return returnSet;

		TypeDeclaration superTypeDecl = types.get(new QualifiedClassName(superclassType.resolveBinding(), cThis)
				.getType());
		if (superTypeDecl != null) {
			Set<FieldDeclaration> auxSet = fieldBody(superTypeDecl, hierarchy, types, cThis);
			returnSet.addAll(auxSet);
		}
		return returnSet;
	}
}
