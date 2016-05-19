package edu.wayne.summary.facade;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ITypeBinding;

import edu.wayne.summary.Crystal;
import edu.wayne.summary.utils.JavaElementUtils;

// TODO: Consolidate/refactor with JavaElementUtils.java
public class TypeStringConverterImpl implements TypeStringConverter {
	
	// Use lazy instantiation
	private static TypeStringConverter sFacade = null;
	
	// Private constructor to enforce singleton
	private TypeStringConverterImpl() {
	}
	
	public static TypeStringConverter getInstance() {
		if (sFacade == null ) {
			sFacade = new TypeStringConverterImpl();
		}
		
		return sFacade;
	}
	
	private IJavaProject project;

	@Override
    public IType getIType(String fullyQualifiedName) {
	    IType findType = null;
	    try {
	    	findType = this.project.findType(fullyQualifiedName);
	    }
	    catch (JavaModelException e) {
	    	e.printStackTrace();
	    }
	    return findType;
    }

	@Override
    public ITypeBinding getITypeBinding(String importantClass) {
		// TODO: Implement me!
		Crystal crystal = Crystal.getInstance();
		ITypeBinding typeBinding = crystal.getTypeBindingFromName(importantClass);
        return typeBinding;
    }

	/**
     * Convert Set<String> to Set<ITypeBindings>
     * The Set must contain fully qualified type names.
     */	
    @Override
    public Set<ITypeBinding> getITypeBindings(Set<String> importantClasses) {
    	Crystal crystal = Crystal.getInstance();
    	Set<ITypeBinding> types = new HashSet<ITypeBinding>();
    	for (String fullyQualifiedName : importantClasses) {
    		ITypeBinding typeBinding = crystal.getTypeBindingFromName(fullyQualifiedName);
    		if (typeBinding != null) {
    			types.add(typeBinding);
    		}
    	}
    	return types;
    }

	/**
     * Convert Set<String> to Set<IType>
     * The Set must contain fully qualified type names. 
     */
    @Override
    public Set<IType> getITypes(Set<String> importantClasses) {
    
    	Set<IType> types = new HashSet<IType>();
    	for (String fullyQualifiedName : importantClasses) {
    		IType findType = getIType(fullyQualifiedName);
    		if (findType != null) {
    			types.add(findType);
    		}
    	}
    	return types;
    }

	@Override
	/**
	 * Convert String into IJavaElement
	 * 
	 */
    public IJavaElement getJavaElement(String declaredType) {
    	IType declaredTypeObject = JavaElementUtils.findType(declaredType);
    	if (declaredTypeObject != null ) {
    		int debug = 0; debug++;
    	}
    	return declaredTypeObject;
    }

	@Override
    public void setProject(IJavaProject project) {
    	this.project = project;	    
    }

	@Override
    public IJavaProject getProject() {
        return project;
    }

}
