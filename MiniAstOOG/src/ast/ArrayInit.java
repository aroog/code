package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import adapter.Adapter;

public class ArrayInit extends Expression {

	// TODO: Move field up to Expression class?
	// Save the complex expression as a String
	@Element(required=false)
	public String complexExpression;
	
	protected ArrayInit() {
	    super();
    }
	
	static ArrayInit create() {
		return new ArrayInit();
	}

	// TODO: Should we allow null argument for ASTNode node?
	// Which will produce the degenerate form?
	// Depends on whether hashtable lookup supports null key
	public static ArrayInit createFrom(ASTNode node) {
		ArrayInit retNode = null;
		
		Adapter factory = Adapter.getInstance();

		// XXX. Check compatible types

		AstNode astNode = factory.get(node);
		if ( astNode instanceof ArrayInit ) {
			retNode = (ArrayInit)astNode;
		}
		else {
			retNode = ArrayInit.create();
			
			retNode.complexExpression = node.toString();
			factory.map(node, retNode);
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
	    return ExpressionType.ArrayCreation;
    }	
}
