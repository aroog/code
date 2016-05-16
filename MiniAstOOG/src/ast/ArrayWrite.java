package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import adapter.Adapter;

/*this is a special Assignment in AST*/
// TODO: Don't we need Assignment as intermediate class between FieldWrite and Expression ?
public class ArrayWrite extends Expression {
	
	// TODO: Move field up to Expression class?
	// Save the complex expression as a String
	@Element(required=false)
	public String complexExpression;

	public ArrayWrite() {
	    super();
    }
	
	
	static ArrayWrite create(){
		return new ArrayWrite();
	}
	
	public static ArrayWrite createFrom(ASTNode node){
		ArrayWrite retNode = null;

		// XXX. Check compatible types!
		Adapter factory = Adapter.getInstance();

		AstNode astNode = factory.get(node);
		if (astNode instanceof ArrayWrite) {
			retNode = (ArrayWrite) astNode;
		}
		else {
			retNode = ArrayWrite.create();
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
	    return ExpressionType.ArrayWrite;
    }
}
