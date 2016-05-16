package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import adapter.Adapter;

/*this is a special Assignment in AST*/
// TODO: Don't we need Assignment as intermediate class between FieldWrite and Expression ?
public class FieldWrite extends Expression {
	
	// TODO: Move field up to Expression class?
	// Save the complex expression as a String
	@Element(required=false)
	public String complexExpression;

	
	/**
	 * e.f = e' , class of e
	 * */
	@Element(required=false)
	public Type recvType;
	
	/**
	 * e.f = e' , class of e'
	 * */
	@Element(required=false)
	public Type valueType;
	
	// TODO: Add enclosing scope

	// The resolved FieldDeclaration of this FieldWrite expression.
	@Element(required=false)
	public FieldDeclaration fieldDeclaration;

	public FieldWrite() {
	    super();
    }
	
	
	static FieldWrite create(){
		return new FieldWrite();
	}
	
	public static FieldWrite createFrom(ASTNode node){
		FieldWrite retNode = null;
		boolean compatibleType = false;
		if(node instanceof Assignment){
			compatibleType = true;
		} else if (node instanceof VariableDeclarationFragment){
			VariableDeclarationFragment vdf = (VariableDeclarationFragment)node;
			if (vdf.getInitializer()!=null){
				compatibleType = true;
			}
		} 
		if (compatibleType){
			Adapter factory = Adapter.getInstance();

			AstNode astNode = factory.get(node);
			if ( astNode instanceof FieldWrite ) {
				retNode = (FieldWrite)astNode;
			}
			else {
				retNode = FieldWrite.create();
				retNode.complexExpression = node.toString();
				factory.map(node, retNode);
			}
		} else {
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
	    return ExpressionType.FieldWrite;
    }
}
