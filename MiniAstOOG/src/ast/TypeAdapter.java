package ast;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;

// TODO: LOW. Maybe combine with other Adapter
// DONE. Make a top-level type.
// TODO: Get rid of the extra hashtables (just optimization!): just keep allTypes.
// NOTE: Since we remember the Type object as soon as it is created, before it may have any of its fields set
class TypeAdapter {

	private static TypeAdapter instance = null;
	
	// String is an ITypeBinding key
	private Hashtable<String,Type> mapBindingKeyToType = new Hashtable<String, Type>();

	/**
	 * Map fully qualified typenames to Type objects.
	 * The mapping should be 1-to-1, since the Type objects are hashed.
	 */
	private Hashtable<String,Type> mapNameToType = new Hashtable<String, Type>();
	
	// TODO: Use something more efficient, like a Hashtable, to avoid iterating over this set
	private Set<Type> allTypes = new HashSet<Type>();

	
	private Type addType(ITypeBinding typeBinding) {
		Type type = getType(typeBinding);
		if (type == null) {
			type = new Type(typeBinding.getQualifiedName());
			// Remember to cache the type
			allTypes.add(type);
			mapNameToType.put(typeBinding.getQualifiedName(), type);
			mapBindingKeyToType.put(typeBinding.getKey(), type);
		}

		return type;
	}

	// TODO: Rename: getTypeFromBinding
	// NOTE: Make sure to look in all the hashtables!
	public Type getType(ITypeBinding typeBinding) {
		
		if (typeBinding == null) {
			// HACK: Do something more intelligent here!
			int debug = 0;
			debug++;
		}

		Type type = null;
		if (typeBinding != null) {
			type = mapBindingKeyToType.get(typeBinding.getKey());
			if ( type == null ) {
				// Try looking up by name
				String qualifiedName = typeBinding.getQualifiedName();
				type = getType(qualifiedName);
				// If that fails, go through the whole list!
				if ( type ==  null ) {
					for(Type aType : allTypes ) {
						if (aType.getFullyQualifiedName().equals(qualifiedName)) {
							type = aType;
							break;
						}
					}
				}
			}
		}
		
		return type;
	}

	// Called from Type.createFrom()
	Type createFrom(ITypeBinding typeBinding) {
		Type type = addType(typeBinding);
		// TODO: Comment out this Subclass/Superclass stuff for now, since no longer being persisted.
//		TypeInfo typeInfo = TypeInfo.getInstance();
		
		// First process superclass
		ITypeBinding superclass = typeBinding.getSuperclass();
		if (superclass != null) {
			Type superType = getType(superclass);
			if (superType == null) {
				superType = addType(superclass);
			}
			// TODO: MED. Do not call deprecated method
//			typeInfo.addSubClass(superType, type);
//			typeInfo.setSuperClass(type, superType);
		}
		
		// Then process super interfaces
		for (ITypeBinding itfBinding : typeBinding.getInterfaces()) {
			Type itfType = getType(itfBinding);
			if (itfType == null) {
				itfType = addType(itfBinding);
			}
			// TODO: MED. Do not call deprecated method
			// TODO: HIGH. XXX. Do we want to use addSubClass for interfaces?
//			typeInfo.addSubClass(itfType, type);
//			typeInfo.addImplementedInterface(type, itfType);
		}
		return type;
	}
	
	public static TypeAdapter getInstance(){
		if ( instance == null ) {
			instance = new TypeAdapter();
		}
		return instance;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		return buffer.toString();
	}

	public void addType(Type type) {
		// TODO: What if type.getFullyQualifiedName() has not been set yet!
		// NOTE: We use a set to avoid duplicates
		this.allTypes.add(type);
		String fullyQualifiedName = type.getFullyQualifiedName();
		if (fullyQualifiedName != null ) {
			mapNameToType(fullyQualifiedName, type);
		}
		
    }

	private void mapBindingKeyToType(String key, Type type) {
	    this.mapBindingKeyToType.put(key, type);
    }
	
	public void mapNameToType(String name, Type type ) {
		this.mapNameToType.put(name,  type);
	}
	
	public Enumeration<String> getTypeNames() {
		return mapNameToType.keys();
	}

	public Collection<Type> getTypes() {
		return this.allTypes;
	}

	/**
	 * Resolve a Type object from a Type
	 * @param name must be a fully qualified name
	 * @return
	 */
	public Type getType(String name) {
		return this.mapNameToType.get(name);
	}
	
	public void reset() {
		this.mapBindingKeyToType.clear();
		this.mapNameToType.clear();
		this.allTypes.clear();
	}


}