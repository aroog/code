package edu.wayne.metrics.adb;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ast.AstNode;
import ast.BaseTraceability;
import ast.BodyDeclaration;
import ast.FieldDeclaration;
import ast.MethodDeclaration;
import ast.Type;
import ast.TypeDeclaration;
import ast.VariableDeclaration;
import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.aliasjava.parser.DomainParams;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.mapping.Model;
import edu.wayne.metrics.mapping.ModelManager;

// XXX. Cleanup static instance fields.
public class Util {

	/**
	 * Helper method to get the edges between a pair of source and destination OObjects: If isDirected if false, ignore
	 * the direction of the edge, to disregard whether it is an import or export edge. If isDirected is true, use the
	 * direction src -> dst. Return all the multi-edges in the set. TODO: Move this method into the OGraph jar. NOTE:
	 * For now, returning all the edge types; caller can filter out the edges, e.g,. PTEdge, DFEdge
	 */
	// XXX. Remove
	private static Model model = new Model();

	// XXX. Remove
	private static ModelManager m = ModelManager.getInstance();

	// XXX. Remove
	private static Util instance = null;

	private Util() {
	}

	public static Util getInstance() {
		if (instance == null) {
			instance = new Util();
		}
		return instance;
	}

	public static Set<IEdge> getEdgesBetween(Set<IEdge> allEdges, IObject src, IObject dst, boolean isDirected) {
		Set<IEdge> edges = new HashSet<IEdge>();
		for (IEdge edge : allEdges) {
			if (edge.getOsrc() == src && edge.getOdst() == dst) {
				edges.add(edge);
			}
			if (!isDirected) {
				if (edge.getOdst() == src && edge.getOsrc() == dst) {
					edges.add(edge);
				}
			}
		}

		return edges;
	}

	// TODO: Move this method elsewhere
	public static String toString(IEdge edge) {
		StringBuilder builder = new StringBuilder();

		builder.append(ADBTriplet.getTripletFrom(edge.getOsrc()));
		builder.append(" -> ");
		builder.append(ADBTriplet.getTripletFrom(edge.getOdst()));

		return builder.toString();
	}

	// TODO: HIGH. Double-check this works for all the cases we will encounter:
	// - Generic types
	// - Fully qualified types
	public static String getPackageName(String typeName) {
		String qualifier = org.eclipse.jdt.core.Signature.getQualifier(typeName);
		return qualifier;
	}

	/**
	 * Strip out the generic part, if applicable. If not, return the same string.
	 * 
	 * @param genericType
	 * @return
	 */
	public static String getRawTypeName(String genericType) {
		String rawName = org.eclipse.jdt.core.Signature.getTypeErasure(genericType);
		return rawName;
	}

	// Build list of subclasses that TypeA can refer to, transitively
	public static void getSubClasses(Type type, Set<String> allSubClasses) {
		// append the current type.
		allSubClasses.add(type.toString());

		for (Type subType : type.getSubClasses()) {
			getSubClasses(subType, allSubClasses);
		}
	}

	/**
	 * Build list of superclasses of the Type, transitively. TODO: MED. Go all the way up to Object? What about the
	 * other types, like Serializable, Cloneable, etc.? TODO: Build a list of excluded types. Exclude
	 * "marker interfaces" with no methods? Exclude "abstract classes"?
	 * 
	 * @param type
	 * @param allSuperClasses
	 * @param includeItfs: if true, include implemented interfaces. TODO: Count separately -- AllTypes, Classes,
	 *            AbstractClasses, Interfaces
	 */
	public static void getSuperClasses(Type type, Set<String> allSuperClasses, boolean includeItfs) {
		if (type == null) {
			return;
		}

		// TODO: HIGH. XXX. Does this handle java.lang.Object properly
		// append the current type.
		allSuperClasses.add(type.toString());

		getSuperClasses(type.getSuperClass(), allSuperClasses, includeItfs);

		for (Type subType : type.getImplementedInterfaces()) {
			getSuperClasses(subType, allSuperClasses, includeItfs);
		}

		if (includeItfs) {
			for (Type itf : type.getImplementedInterfaces()) {
				getSuperClasses(itf, allSuperClasses, includeItfs);
			}
		}
	}

	/**
	 * Return B <: C ?
	 * 
	 * @param typeB
	 * @param typeC
	 * @return
	 */
	public static boolean isSubtypeCompatible(Type typeB, Type typeC) {
		// return typeC.isSubtypeCompatible(typeB);
		String typeBName = typeB.getFullyQualifiedName();
		Crystal crystal = Crystal.getInstance();
		ITypeBinding typeBindingB = crystal.getTypeBindingFromName(typeBName);

		String typeCName = typeC.getFullyQualifiedName();
		ITypeBinding typeBindingC = crystal.getTypeBindingFromName(typeCName);

		if (typeBindingB != null && typeBindingC != null) {
			return typeBindingB.isSubTypeCompatible(typeBindingC);
		}

		return false;
	}

	/**
	 * Extract the owning domain from an annotation
	 * TODO: Rename 
	 */
	public static String getDomainString(String annotation) {
		String domain = annotation;
		// Find a more efficient way of doing this than trying to get a string from an AnnotationInfo object!
		if (annotation != null) {
			int indexOf = annotation.indexOf("<");
			if (indexOf != -1) {
				domain = annotation.substring(0, indexOf);
			}
			// TODO: Include the rest of the params?
		}
		return domain;
	}

	public static boolean isContainerType(String fullyQualifiedName) {
		boolean containerType = false;
		String containerofType = Util.getRawTypeName(fullyQualifiedName);
		model = m.getModel();
		Set<String> containerList = model.getContainerTypes();

		if (containerList.contains(containerofType)) {
			containerType = true;
		}

		return containerType;
	}

	public static boolean isSubtypeCompatible(ITypeBinding typeBindingB, ITypeBinding typeBindingC) {

		if (typeBindingB != null && typeBindingC != null) {
			return typeBindingB.isSubTypeCompatible(typeBindingC);
		}

		return false;
	}

	// get <E> of a container
	public ITypeBinding getElementOfContainer(String fullyQualifiedName) {
		ITypeBinding typeBinding = getTypeBinding(fullyQualifiedName);
		ITypeBinding typeArg = null;
		if (typeBinding != null) {
			ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
			if (typeArguments != null) {
				for (int ii = 0; ii < typeArguments.length; ii++) {
					ITypeBinding typeArgument = typeArguments[ii];
					typeArg = typeArgument;
				}
			}
		}
		return typeArg;

	}

	public ITypeBinding getTypeBinding(String fullyQualifiedName) {
		ITypeBinding typeBinding = Crystal.getInstance().getTypeBindingFromName(fullyQualifiedName);
		return typeBinding;
	}


    // XXX. TOMAR: There are more cases to handle
	/**
	 * Return the enclosing type from a Traceability entry.
	 * Goes through the expression.
	 */
	public static TypeDeclaration getEnclosingTypeDeclaration(BaseTraceability traceability) {
		return getEnclosingTypeDeclaration(traceability.getExpression());
    }


	// XXX. Use MiniAstUtils. Avoid duplication
	/**
	 * Return the enclosing type from an expression.
	 */
	public static TypeDeclaration getEnclosingTypeDeclaration(AstNode expression) {
		TypeDeclaration typeDecl = null;

		if (expression != null) {
			typeDecl = getEnclosingTypeDeclaration(expression.enclosingDeclaration);
		}
		return typeDecl;
    }

	private static TypeDeclaration getEnclosingTypeDeclaration(BodyDeclaration enclDecl) {
		TypeDeclaration typeDecl = null;
		
	    if (enclDecl instanceof MethodDeclaration) {
	    	MethodDeclaration methDecl = (MethodDeclaration) enclDecl;
	    	typeDecl = methDecl.enclosingType;
	    }
	    else if (enclDecl instanceof FieldDeclaration) {
	    	FieldDeclaration fieldDecl = (FieldDeclaration) enclDecl;
	    	typeDecl = fieldDecl.enclosingType;
	    }
	    else if (enclDecl instanceof TypeDeclaration) {
	    	typeDecl = (TypeDeclaration) enclDecl;
	    }
	    // Recursively chase the parent.
	    else if (enclDecl instanceof VariableDeclaration) {
	    	VariableDeclaration varDecl = (VariableDeclaration) enclDecl;
	    	typeDecl = getEnclosingTypeDeclaration(varDecl.enclosingDeclaration);
	    }
	    return typeDecl;
    }	
}
