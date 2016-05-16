package ast;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Store the TypeHierarchy, in terms of Type objects
 * 
 * TODO: HIGH. Define the fields to remember the values passed to the methods.
 * Type ->1 SuperClass (immediate superclass)
 * Type ->* SubClasses (immediate subclasses)
 * Type ->* Implemented Interfaces (immediate interfaces)
 * 
 * TODO: HIGH. XXX. Figure out how to populate this thing, if it is not being persisted
 * - Define a method: reviveTypeFromBinding(Type type)
 * 
 * TODO: LOW. Move to util.typehierarchy package; but may be relying on methods being package-protected
 * 
 */
public class TypeInfo {

	private static TypeInfo instance = null;
	
	private TypeAdapter typeAdapter;
	
	public static TypeInfo getInstance(){
		if ( instance == null ) {
			instance = new TypeInfo();
		}
		
		// Hold on to TypeAdapter here. TypeAdapter is not exposed
		instance.typeAdapter = TypeAdapter.getInstance();
		
		return instance;
	}

	// TODO: HIGH. XXX. Write unit tests for this with the tricky cases:
	// - Primitive types
	// - java.lang.Object
	public void reviveTypeFromBinding(Type type, ITypeBinding typeBinding) {
		// First process superclass
		ITypeBinding superTypeBinding = typeBinding.getSuperclass();
		if (superTypeBinding != null) {
			String superTypeBindingName = superTypeBinding.getQualifiedName();
			Type superType = getType(superTypeBindingName);
			if (superType != null) {
				reviveSuperType(type, superTypeBinding, superType);
			}
			else {
				// TODO: Do something. This should not have happened!
				// Probably due to java.lang.Object
				superType = Type.createFrom(superTypeBinding);
				// NOTE: Type.createFrom calls TypeAdapter.addType()
				// typeAdapter.addType(superType);
				
				reviveSuperType(type, superTypeBinding, superType);				
			}

		}

		// Then process super interfaces
		for (ITypeBinding itfTypeBinding : typeBinding.getInterfaces()) {
			String itfName = itfTypeBinding.getQualifiedName();
			Type itfType = getType(itfName);
			if (itfType != null) {
				reviveSuperType(type, itfTypeBinding, itfType);
			}
			else {
				// TODO: Do something. This should not have happened!
				itfType = Type.createFrom(itfTypeBinding);
 				// NOTE: Type.createFrom calls TypeAdapter.addType()
				// typeAdapter.addType(itfType);
				
				reviveSuperType(type, itfTypeBinding, itfType);			
			}
		}
	}

	private void reviveSuperType(Type type, ITypeBinding superTypeBinding, Type superType) {
	    // TODO: MED. Do not call deprecated method
	    superType.addSubClass(type);
	    type.setSuperClass(superType);

	    // Recurse
	    reviveTypeFromBinding(superType, superTypeBinding);
    }

	private Type getType(ITypeBinding typeBinding) {
//	    return mapTypeNamesToTypes.get(superclass.getQualifiedName());
		return getType(typeBinding.getQualifiedName());
    }
	
	// TODO: HIGH. Move method to type Adapter
	// TODO: Use something more efficient, like a Hashtable, to avoid iterating over this set
	public Type getType(String fullyQualifiedName) {
		Type retType = null;
		
		for(Type type : typeAdapter.getTypes() ) {
			if (type.getFullyQualifiedName().compareTo(fullyQualifiedName) == 0 ) {
				retType = type;
				break;
			}
		}
		
		// TODO: Avoid silent failure!!! If we're looking for a Type, we should find it!
		// if ( retType == null ) {
		// 	System.err.println("TypeInfo. getType returning null on " + fullyQualifiedName );
		// }
		
		return retType;
	}
	
	public void addType(Type type) {
		typeAdapter.addType(type);
	}
	
	public Collection<Type> getAllTypes() {
		return typeAdapter.getTypes();
	}
	
	public void reset() {
		// Reset the TypeAdapter here. TypeAdapter is not exposed, so TypeAdapter.reset() cannot be called directly!
		this.typeAdapter.reset();
	}
}