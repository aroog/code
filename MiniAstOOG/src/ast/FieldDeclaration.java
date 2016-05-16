package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.simpleframework.xml.Element;

import adapter.Adapter;
import adapter.TraceabilityFactory;

//TODO: MED: we may need to create a FieldDeclaration from a virtual field, i.e. from info in AliasXML
//we will not have IVariableBinding or AstNode available
//ensure that we still preserve uniqueness (maybe add/change the map where the key is (QUalifiedTypeDeclarationName:String, fieldName:String)
// we may get interesting numbers for virtual fields for PtEdge precision since virtual fields are usually in Collections. 

//TODO: LOW. Maybe combine FieldDeclaration with VariableDeclaration; and have a getter IsField
public class FieldDeclaration extends BodyDeclaration{

	// TODO: Move field up to base class?
	// TODO: Backpointer...keep in sync with forward pointer
	// XXX. This is redundant with AstNode.enclosingDeclaration
	// Just a different type. So for now, be sure to set them both (to the same object)!
	@Element(required=false)	
	public TypeDeclaration enclosingType;
	
	@Element(required=false)
	public String fieldName;
	
	@Element(required=false)
	public Type fieldType;

	@Element(required=false)
	// TODO: Encapsulate field
	public String annotation = null;
	
	protected FieldDeclaration() {
	    super();
    }
	 static FieldDeclaration create(){
		return new FieldDeclaration();
	}
	public static FieldDeclaration createFrom(ASTNode node) {
		FieldDeclaration retNode = null;
		if(node instanceof VariableDeclarationFragment){
			VariableDeclarationFragment domVDF = (VariableDeclarationFragment)node;
			Adapter factory = Adapter.getInstance();
	
			AstNode astNode = factory.get(node);
			if ( astNode instanceof FieldDeclaration ) {
				retNode = (FieldDeclaration)astNode;
			}
			else {
				retNode = FieldDeclaration.create();
				retNode.enclosingType = TraceabilityFactory.getEnclosingTypeDeclaration(domVDF);
				retNode.enclosingDeclaration = retNode.enclosingType;
				retNode.fieldName = domVDF.getName().getFullyQualifiedName();
				retNode.fieldType =  TraceabilityFactory.getType(domVDF.resolveBinding().getType());
				factory.map(node, retNode);
			}
		
		}else{
			throw new IllegalArgumentException(node.toString() + " must be of instance org.eclipse.jdt.core.dom.VariableDeclarationFragment");
		}
		return retNode;
	}
	
	public static FieldDeclaration createFrom(IVariableBinding fieldDecl) {
		FieldDeclaration retNode = null;
		Adapter factory = Adapter.getInstance();
		
		AstNode astNode = factory.get(fieldDecl);
		if ( astNode instanceof FieldDeclaration ) {
			retNode = (FieldDeclaration)astNode;
		}
		else {
			retNode = FieldDeclaration.create();
			retNode.enclosingType = TraceabilityFactory.getTypeDeclaration(fieldDecl.getDeclaringClass().getQualifiedName());
			retNode.enclosingDeclaration = retNode.enclosingType;
			retNode.fieldName = fieldDecl.getName();
			retNode.fieldType =  TraceabilityFactory.getType(fieldDecl.getType());
			factory.map(fieldDecl, retNode);
			factory.mapFieldDeclaration(retNode);
		}
		return retNode;
	}

	
	// XXX. TODO: Would be nice to use the non-fully qualified type name for the fieldType
	// XXX. Looks unreadable: maybe better to use: C1 {C f;}
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(enclosingType);
		buffer.append("::  ");
		buffer.append(fieldName);
		buffer.append(":");
		buffer.append(fieldType);
		return buffer.toString();
	}		

}
