package edu.wayne.summary.facade;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;


/**
 *  Just a stub for Laura's current code
 *
 */
//TODO: Delete me.
@Deprecated
public interface CurrentFacadeItf {

	Set<IMethodBinding> getMostImportantMethodsOfClass(ITypeBinding javaClass);
	 
	Set<ITypeBinding> getMostImportantClassesRelatedTo(ITypeBinding javaClass, RelationshipType type);

	Set<IType> getMostImportantClasses();

}
