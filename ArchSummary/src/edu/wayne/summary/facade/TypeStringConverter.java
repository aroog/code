package edu.wayne.summary.facade;

import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Helper component to convert from a fully qualified type name to an IType, ITypeBinding, IJavaElement, etc. 
 * TODO: LOW. Could do some caching to optimize the conversion.
 */
public interface TypeStringConverter {
	// Get access to the Java project; Must be set before calling other methods!
	IJavaProject getProject();
	void setProject(IJavaProject project);
	
	IType getIType(String importantClass);
	Set<IType> getITypes(Set<String> importantClasses);

	ITypeBinding getITypeBinding(String importantClass);
	Set<ITypeBinding> getITypeBindings(Set<String> importantClasses);
	
	
	IJavaElement getJavaElement(String declaredType);
}
