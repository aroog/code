package edu.wayne.generics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class GenHelper {
	
	private static IJavaProject javaProject;

	private static IJavaProject getJavaProject() {
		return javaProject;
	}

	// HACK: I don't think this should be static. Allow it to be set on instances?
	public static void setJavaProject(IJavaProject javaProject) {
		GenHelper.javaProject = javaProject;
	}
	
	// Hashtable used to cache the constructed ITypeBindings
	private static Map<String, ITypeBinding> mapKeyToTypeBinding = new Hashtable<String, ITypeBinding>();

	/**
	 * Translate a typeBinding with a formal type parameter into one with fully substituted type
	 * based on the enclosing type
	 * @param typeBinding: e.g., virtual field of type <E extends java.lang.Object>
	 * @param actualBinding: the enclosing type, e.g., class ArrayList<courses.Course> 
	 * 						where the generic type declaration is class ArrayList<E> {...}
	 * @return typeBinding with the type substitution performed, e.g., courses.Course
	 */
	public static ITypeBinding translateFtoA(ITypeBinding typeBinding, ITypeBinding actualBinding) {
		ITypeBinding toTypeBindingActual = typeBinding;

		// HACK: Introduced bugs!		
		// Check that it is a generic type, to avoid generating a type that does not exist!
		// if(!typeBinding.isGenericType()) {
		// 	return typeBinding;
		// }
		
		// HACK: Introduced bugs!
		 if (typeBinding.isEqualTo(actualBinding)) {
			return typeBinding;
		 }
		
		// Instantiate generic types...
		if (actualBinding.isParameterizedType()) {
			ITypeBinding[] typeParameters = actualBinding.getErasure().getTypeParameters();
			int pIndex = -1;
			int index = 0;
			for(ITypeBinding typeParameter : typeParameters ) {
				if ( typeParameter.isEqualTo(typeBinding) ) {
					pIndex = index;
					break;
				}
				index++;
			}
			ITypeBinding[] typeArguments = actualBinding.getTypeArguments();
			if ( typeBinding.isTypeVariable() && typeArguments != null && pIndex != -1  && pIndex < typeArguments.length) {
				toTypeBindingActual = typeArguments[pIndex];
			}
			else {
				String[] typeArgumentString = getParameters(typeArguments);
				String bindingKey = BindingKey.createParameterizedTypeBindingKey(typeBinding.getKey(), typeArgumentString);
				toTypeBindingActual = GenHelper.mapBindingKeyToTypeBinding(bindingKey);
			}
		}
		


		return toTypeBindingActual;
	}
	
	private static String[] getParameters(ITypeBinding[] typeBindings) {
		List<String> list = new ArrayList<String>();
		
		for(ITypeBinding typeBinding : typeBindings ) {
			list.add(typeBinding.getKey());	
		}
		
		return list.toArray(new String[0]);
	}

	// XXX. This is very inefficient. Minimize calls to this.
	public static IBinding createBindingFromKey(String bindingKey) {
		final Map<String, IBinding> mapping = new HashMap<String, IBinding>();
		List<String> keys = new ArrayList<String>();
		keys.add(bindingKey);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		parser.setCompilerOptions(options);
		parser.setProject(getJavaProject());
		parser.setResolveBindings(true);
		parser.createASTs(new ICompilationUnit[0], keys.toArray(new String[keys.size()]), new ASTRequestor() {
			@Override
            public void acceptBinding(String bindingKey, IBinding binding) {
				mapping.put(bindingKey, binding);
			}
		}, null);
		return mapping.get(bindingKey);
	}

	private static ITypeBinding mapBindingKeyToTypeBinding(String bindingKey) {

		ITypeBinding typeBinding = mapKeyToTypeBinding.get(bindingKey);
		if ( typeBinding  == null ) {
			// IMPORTANT: Minimize the calls to createBindingFromKey(...)
			// As each call may create bindings that are incompatible, i.e., for which we cannot
			// use isSubTypeCompatible(...), isAssignmentCompatible(...), etc.
			IBinding binding = GenHelper.createBindingFromKey(bindingKey);
			if (binding instanceof ITypeBinding) {
				typeBinding = (ITypeBinding) binding;
				// Cache for performance
				mapKeyToTypeBinding.put(bindingKey, typeBinding);
			}
		}
		return typeBinding;
	}


	private static ITypeBinding getCorrespondingArrayType(ITypeBinding elementType) {
		String bindingKey = BindingKey.createArrayTypeBindingKey(elementType.getKey(), 1);
		ITypeBinding typeBinding = GenHelper.mapBindingKeyToTypeBinding(bindingKey);

		return typeBinding;
	}


	public static void reset() {
		mapKeyToTypeBinding.clear();
	}
}
