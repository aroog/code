package ast;

import java.util.HashSet;
import java.util.Set;

/**
 * Classtable to lookup a TypeDeclaration from a fully qualified name.
 * Also to lookup a TypeDeclaration from a Type.
 * 
 * TODO: HIGH. XXX. This Classtable is not being populated! Must call addType.
 * 
 * HACK: right now, just populate into a set. Cannot use a map.
 * Since addTypeDeclaration may be called with a TypeDeclaration object that is not yet fully initialized
 */
public class ClassTable {
	private static ClassTable instance = null;
	
	public static ClassTable getInstance(){
		if ( instance == null ) {
			instance = new ClassTable();
		}
		
		return instance;
	}
	
	private Set<TypeDeclaration> allTypeDecls  = new HashSet<TypeDeclaration>();
	
//	private Map<String, TypeDeclaration> mapTypeNameToTypeDecl = new HashMap<String, TypeDeclaration>();
//
//	// TODO: Do we need this too?
//	private Map<Type, TypeDeclaration> mapTypeToTypeDecl = new HashMap<Type, TypeDeclaration>();

	public TypeDeclaration getTypeDeclaration(String fullyQualifiedName) {
		TypeDeclaration retVal = null;
		
		for(TypeDeclaration typeDecl : allTypeDecls ) {
			if (typeDecl.getFullyQualifiedName().compareTo(fullyQualifiedName) == 0 ) {
				retVal = typeDecl;
				break;
			}
		}
		
		return retVal;
//		return mapTypeNameToTypeDecl.get(fullyQualifiedName);
	}

	public void addTypeDeclaration(TypeDeclaration typeDecl) {
		this.allTypeDecls.add(typeDecl);
//		// TODO: HACK: addType may be called before the TypeDeclaration.type had been set
//		if (typeDecl.type != null ) {
//			this.mapTypeNameToTypeDecl.put(typeDecl.type.fullyQualifiedTypeName, typeDecl);
//			this.mapTypeToTypeDecl.put(typeDecl.type, typeDecl);
//		}
	}

//	public TypeDeclaration getType(Type type) {
//		return mapTypeToTypeDecl.get(type);
//	}
	
	public void reset() {
//		this.mapTypeNameToTypeDecl.clear();
//		this.mapTypeToTypeDecl.clear();
		this.allTypeDecls.clear();
	}
	
	
	public Set<TypeDeclaration> getTypeDeclarations() {
		return this.allTypeDecls;
	}
	
}