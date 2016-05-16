package ast;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Transient;

import util.typehierarchy.TypeHierarchyFactory;

/*
 * Define a type, as just a fully qualified type name.
 * 
 * TODO: Why not just use a String?
 * TODO: Combine this class with TypeDeclaration?
 * Or still useful for when talking about MethodDeclaration?
 * 
 * DONE. Use a factory method to be able to compare references of Type.
 * 
 * TOMAR: TODO: HIGH. Delete unwanted fields. Adding the Type stuff to the XML file is causing it to become bloated.
 * - Serializing way too much information
 * - Just store superclass, superinterfaces;
 * - No need to store subclasses!
 * - Computer subclasses on the fly 
 * 
 *  Type is just the C part of the C<\ob{p}>.
 *  
 *  ODType is the full C<\ob{p}>
 *  
 *  TODO: HIGH. Does a Type need to point to a resolved TypeDeclaration?
 *  
 *  TODO: HIGH. Also store the short name; it is not obvious to compute from the fully qualified typename,
 *  e.g., for java.util.ArrayList<java.lang.Object>
 */
public class Type {
	
	@Transient
	private Type superClass;
	
	@Transient
	private Set<Type> implementedInterfaces = new HashSet<Type>();
	
	@Transient
	private Set<Type> subClasses = new HashSet<Type>();
	
	/**
	 * Null object implementation of  Type
	 */
	static class UnknownType extends Type {
		
		public UnknownType(@Attribute(required = true, name = "fullyQualifiedName") String fullyQualifiedName) {
			super(fullyQualifiedName);
		}
	}

	@Transient
	private static Type unknownType = null;
	
	@Transient
	public static Type getUnknownType() { 
		if ( unknownType == null ) {
			unknownType = new UnknownType("Unknown");
		}
		return unknownType;
	}
	
	// TODO: HIGH. Define constants for the base types, and "void"
	// TODO: HIGH. Do we need a NullType?
	
	@Attribute(required = true, name = "fullyQualifiedName")	
	public String fullyQualifiedName;

	// TODO: HIGH. Make private once factory method is working
	// Serialization requires a default constructor
	/**
	 * @deprecated Use factory method.
	 * Used by test code only.
	 */
	@Deprecated
	protected Type() {
		
		// Register the type with the TypeInfo
	    // NOTE: the object may not have been fully initialized yet; some fields are still null!
		// TODO: HIGH. Not sure we need to populate both the TypeInfo and the TypeAdapter!
		TypeInfo.getInstance().addType(this);
	}
	
	/**
	 * For most uses, use the factory method
     * Used by persistence code.
	 */
	// TODO: HIGH. Make private once factory method is working
	public Type(@Attribute(required = true, name = "fullyQualifiedName") String fullyQualifiedName) {
		// Call default constructor, which does additional work
		this();

		this.fullyQualifiedName = fullyQualifiedName;

		// Add to the map
		// TODO: HIGH. Not sure we need to populate both the TypeInfo and the TypeAdapter!
		
		TypeAdapter typeAdapter = TypeAdapter.getInstance();
		typeAdapter.addType(this);
	}
	
	public static Type createFrom(ITypeBinding typeBinding) {
		TypeAdapter adapter = TypeAdapter.getInstance();
		Type type = adapter.getType(typeBinding);
		if(type == null){
			type = adapter.createFrom(typeBinding);
		}
		return type;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(fullyQualifiedName);
		return buffer.toString();
	}
	
	public String getFullyQualifiedName(){
		return fullyQualifiedName;
	}
	
	public boolean isSubtypeCompatible(Type C) {

		//easy stuff first
		if (fullyQualifiedName.equals(C.fullyQualifiedName)) 
			return true;
		//otherwise let's use the inheritance hierarchy
		TypeHierarchyFactory factory = TypeHierarchyFactory.getInstance();
		if (factory.getHierarchy() != null)
			return factory.getHierarchy().isSubtypeCompatible(this.fullyQualifiedName, C.fullyQualifiedName);
		else // type hierarchy has not been initialized - maybe throw an exception?
			return false;
	}

	@Transient
	// NOTE: Keep this Transient so it does not get persisted. Type information is persisted elsewhere.
	public Set<Type> getSubClasses() {
		return subClasses;
	}

	@Transient
	// NOTE: Keep this Transient so it does not get persisted. Type information is persisted elsewhere.	
	public Set<Type> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	@Transient
	// NOTE: Keep this Transient so it does not get persisted. Type information is persisted elsewhere.	
	public Type getSuperClass() {
		return superClass;
	}
	
	@Transient
	public void setSuperClass(Type superClass) {
		this.superClass = superClass;
	}

	// TODO:  HIGH. Make this method non-public; call it automatically from setSuperClass; and addImplementedInterface
	@Deprecated
	public boolean addSubClass(Type itf) {
		return subClasses.add(itf);
	}
	
	public boolean addImplementedInterface(Type itf) {
		return implementedInterfaces.add(itf);
	}
}
