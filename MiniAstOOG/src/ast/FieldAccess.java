package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import adapter.Adapter;

// TODO: LOW. Rename: -> FieldRead
// Called this way to be consistent with Eclipse AST
public class FieldAccess extends Expression {

	// TODO: Move field up to Expression class?
	// Save the complex expression as a String
	@Element(required=false)
	public String complexExpression;
	
	/**
	 * e.f , class of e 
	 * */
	@Element(required=false)
	public Type recvType;
	
	/**
	 *x = e.f , actual type of x. 
	 * for generics does the type substitution of formal type parameter to actual type argument  
	 * */
	@Element(required=false)
	public Type fieldType;
	
	// The resolved FieldDeclaration of this FieldAccess expression.
	@Element(required=false)
	public FieldDeclaration fieldDeclaration;
	
	public FieldAccess() {
	    super();
    }
	static FieldAccess create(){
		return new FieldAccess();
	}
	
	public static FieldAccess createFrom(ASTNode node){
		FieldAccess retNode = null;
		
		if(node instanceof org.eclipse.jdt.core.dom.FieldAccess){
			Adapter factory = Adapter.getInstance();
	
			AstNode astNode = factory.get(node);
			if ( astNode instanceof FieldAccess ) {
				retNode = (FieldAccess)astNode;
			}
			else {
				retNode = FieldAccess.create();
				retNode.complexExpression = node.toString();
				factory.map(node, retNode);
			}
		}else{
			throw new IllegalArgumentException();
		}
		return retNode;
	}
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(complexExpression);
		return buffer.toString();
	}
	
	@Override
	@Transient
    public ExpressionType getExpressionType() {
	    return ExpressionType.FieldRead;
    }


}
