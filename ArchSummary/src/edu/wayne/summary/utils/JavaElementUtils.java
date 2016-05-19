package edu.wayne.summary.utils;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Return IJavaElements from Strings in the traceability information.
 * 
 * * NOTE: this is just a first approximation. Our traceability information contains things that are not exposed as IJavaElements,
 * like local variables, expressions inside method bodies, etc.
 * IJavaElements return only type declarations, field declarations and method declarations.
 *
 */
public class JavaElementUtils {

	private static IJavaProject javaProject;
	
	public static IJavaProject getJavaProject() {
    	return javaProject;
    }

	public static void setJavaProject(IJavaProject javaProject) {
    	JavaElementUtils.javaProject = javaProject;
    }

	/**
	 * Must use fully qualified name
	 * @param fullyQualifiedName
	 * @return
	 */
	public static IType findType(String fullyQualifiedName) {
		// TODO: workaround for bug 22883
		IType type = null;
        try {
	        type = javaProject.findType(fullyQualifiedName);
        }
        catch (JavaModelException e) {
	        e.printStackTrace();
        }

		return type;
	}
	
	/**
	 * Returns the field with the specified name in this type.
	 * 
	 * NOTE: This will not work for local variables...
	 * @param type
	 * @param name
	 * @return
	 */
	public static IField getField(IType type, String name) {
		return type.getField(name);
	}
	
	// TODO: Implement helper methods to generate String[] parameterTypeSignatures
	public static IMethod getMethod(IType type, String name, String[] parameterTypeSignatures) {
		return type.getMethod(name, parameterTypeSignatures);
	}
}
