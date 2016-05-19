package edu.wayne.summary.facade;

import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

// TODO: Delete me.
/**
 * Just a stub for Laura's current code
 * TOLAU: HIGH. Must implement facade!
 */
@Deprecated
public class CurrentCode {
	CurrentFacadeItf facade;

	 void computeSummary() {
		 IMethod m;
		 
		 // 1.  Extract method that captures heuristic
		 // 2. Move method into CurrentFacadeImpl
	 }
	 
	 public Set<IMethodBinding> getMostImportantMethodsOf(ITypeBinding javaClass) {
		 // code for getting the important methods using the facade
		 return facade.getMostImportantMethodsOfClass(javaClass);
	 }
	 
	 public Set<ITypeBinding> getMostImportantClassesRelatedTo(ITypeBinding javaClass) {
		 return facade.getMostImportantClassesRelatedTo(javaClass, RelationshipType.All);
	 }
}

//TODO: Delete me.
@Deprecated
class CurrentFacadeImpl implements CurrentFacadeItf {
	// 3. Extract interface from class
	// 4. Change the field declaration of "facade" to be the interface
	// 5. Swap out the Facade Implementation class to be the one using information from the OOG. 
	
	public void getSomething() {
	    
    }

	@Override
    public Set<IType> getMostImportantClasses() {
	    // TODO Auto-generated method stub
	    return null;
    }
	
	@Override
    public Set<ITypeBinding> getMostImportantClassesRelatedTo(ITypeBinding javaClass, RelationshipType type) {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public Set<IMethodBinding> getMostImportantMethodsOfClass(ITypeBinding javaClass) {
	    // TODO Auto-generated method stub
	    return null;
    }

	// OK. to have helper methods that are not part of the interface.
	
}
