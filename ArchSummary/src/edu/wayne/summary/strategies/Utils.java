package edu.wayne.summary.strategies;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import ast.MethodDeclaration;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.ograph.OGraph;
import edu.wayne.summary.Crystal;
import edu.wayne.summary.facade.TypeStringConverter;
import edu.wayne.summary.facade.TypeStringConverterImpl;
import edu.wayne.summary.traceability.ReverseTraceabilityMap;

public class Utils {
	
	public static void loadSummary(OGraph graph, IJavaProject javaProject) {
		OGraphSingleton.getInstance().setGraph(graph);

		TypeStringConverter converter = TypeStringConverterImpl.getInstance();
		converter.setProject(javaProject);

		ReverseTraceabilityMap.getInstance().init(graph);
	}

	/**
	 * Return B <: C ?
	 * @param typeB
	 * @param typeC
	 * @return
	 */
	public static boolean isSubtypeCompatible(Type typeB, Type typeC) {
	    // return typeC.isSubtypeCompatible(typeB);
		String typeBName = typeB.getFullyQualifiedName();
		Crystal crystal = Crystal.getInstance();
		ITypeBinding typeBindingB = crystal.getTypeBindingFromName(typeBName);
	
		String typeCName = typeB.getFullyQualifiedName();
		ITypeBinding typeBindingC = crystal.getTypeBindingFromName(typeCName);
		
		if (typeBindingB != null && typeBindingC != null ) { 
			return typeBindingB.isSubTypeCompatible(typeBindingC);
		}
		
		return false;
	}

	/**
	 * Return B <: C ?
	 * @param typeB
	 * @param typeC
	 * @return
	 */
	public static boolean isSubtypeCompatible(Type typeB, String typeC) {
	    // return typeC.isSubtypeCompatible(typeB);
		String typeBName = typeB.getFullyQualifiedName();
		Crystal crystal = Crystal.getInstance();
		ITypeBinding typeBindingB = crystal.getTypeBindingFromName(typeBName);
		
		ITypeBinding typeBindingC = crystal.getTypeBindingFromName(typeC);
		
		if (typeBindingB != null && typeBindingC != null ) { 
			return typeBindingB.isSubTypeCompatible(typeBindingC);
		}
		
		return false;
	}
	/**
	 * Return B <: C ?
	 * @param typeB
	 * @param typeC
	 * @return
	 */
	public static boolean isSubtypeCompatible(String typeB, String typeC) {;
		Crystal crystal = Crystal.getInstance();
		ITypeBinding typeBindingB = crystal.getTypeBindingFromName(typeB);
		
		ITypeBinding typeBindingC = crystal.getTypeBindingFromName(typeC);
		
		if (typeBindingB != null && typeBindingC != null ) { 
			return typeBindingB.isSubTypeCompatible(typeBindingC);
		}
		
		return false;
	}

	public static boolean isMethodCompatible(MethodDeclaration methodDeclaration) {
		Crystal instance = Crystal.getInstance();
		TypeDeclaration enclosingType = methodDeclaration.enclosingType;
		if(enclosingType!=null){
			ITypeBinding typeBinding = instance
					.getTypeBindingFromName(enclosingType
							.getFullyQualifiedName());
			for(IMethodBinding methodBinding : typeBinding.getDeclaredMethods()){
				if(methodDeclaration.methodName.compareTo(methodBinding.getName()) == 0){
					 if(!isMethodCompatible(methodBinding)){
						 return false;
					 }
				}
			}
		}
		
		return true;
		
	}

	public static boolean isMethodCompatible(IMethodBinding methodBinding) {
	
		Options options = Options.getInstance();
		if (!options.includeConstructors()
				&& (methodBinding.isConstructor() || methodBinding
						.isDefaultConstructor())) {
			return false;
		}
		if (!options.includeStaticMethods()
				&& Modifier.isStatic(methodBinding.getModifiers())) {
			return false;
		}
	
		return true;
	
	}

	/**
	 * This returns the number of methods in a class that are compatible with the options that have been set in the Options singleton
	 * Some options are to include/exclude constructors and static methods
	 * @param javaClass, the java class in which we are counting the number of methods
	 * @return
	 */
	public static int getCompatibleMethodSize(
			String javaClass) {
		Crystal crystal = Crystal.getInstance();
		ITypeBinding typeBinding1 = crystal.getTypeBindingFromName(javaClass);
		int size = 0;
		for(IMethodBinding methodBinding :typeBinding1
				.getDeclaredMethods()){
			if(isMethodCompatible(methodBinding)){
				size++;
			}
			
		}
		return size;
	}

}
