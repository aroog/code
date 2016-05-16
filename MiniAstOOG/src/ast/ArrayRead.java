package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import adapter.Adapter;

// TODO: LOW. Rename: -> FieldRead
// Called this way to be consistent with Eclipse AST
public class ArrayRead extends Expression {

	// TODO: Move field up to Expression class?
	// Save the complex expression as a String
	@Element(required=false)
	public String complexExpression;
	
	public ArrayRead() {
	    super();
    }
	static ArrayRead create(){
		return new ArrayRead();
	}
	
	public static ArrayRead createFrom(ASTNode node){
		ArrayRead retNode = null;
		
		// XXX. Check compatible types
		Adapter factory = Adapter.getInstance();

		AstNode astNode = factory.get(node);
		if (astNode instanceof ArrayRead) {
			retNode = (ArrayRead) astNode;
		}
		else {
			retNode = ArrayRead.create();
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
	    return ExpressionType.ArrayRead;
    }


}
