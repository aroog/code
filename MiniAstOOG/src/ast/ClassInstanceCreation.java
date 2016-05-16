package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import adapter.Adapter;

public class ClassInstanceCreation extends Expression {

	// TODO: Move field up to Expression class?
	// Save the complex expression as a String
	@Element(required=false)
	public String complexExpression;
	
	// The resolved TypeDeclaration of this ClassInstanceCreation expression.
	// The class of the instance being created is on the TypeDeclaration.
	// XXX. Refactor: This is misleading; it is the same as the Expression.type. Clean this up.
	@Element(required=false)
	public TypeDeclaration typeDeclaration;
	
	protected ClassInstanceCreation() {
	    super();
    }
	
	static ClassInstanceCreation create() {
		return new ClassInstanceCreation();
	}

	// TODO: Should we allow null argument for ASTNode node?
	// Which will produce the degenerate form?
	// Depends on whether hashtable lookup supports null key
	public static ClassInstanceCreation createFrom(ASTNode node) {
		ClassInstanceCreation retNode = null;
		
		Adapter factory = Adapter.getInstance();

		AstNode astNode = factory.get(node);
		if ( astNode instanceof ClassInstanceCreation ) {
			retNode = (ClassInstanceCreation)astNode;
		}
		else {
			retNode = ClassInstanceCreation.create();
			
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
	    return ExpressionType.NewExpression;
    }	
}
