package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.simpleframework.xml.Element;

import adapter.Adapter;
import adapter.TraceabilityFactory;

//For method declaration a formal parameter is a variable declaration
// In general a variable declaration is a TAC var decl. The name is not informative e.g., temp3233
// Currently used for MethodDeclarations
// TODO: LOW. Maybe combined with FieldDeclaration; and have a getter IsField
public class VariableDeclaration extends BodyDeclaration {
	@Element(required=false)
	public String varName;
	
	@Element(required=false)
	public Type varType;
	
	@Element(required=false)
	// TODO: Encapsulate field
	public String annotation;
	
	protected VariableDeclaration() {
	}

	protected VariableDeclaration(String name, Type type) {
		this.varName = name;
		this.varType = type;
    }
	
	static VariableDeclaration create(){
		return new VariableDeclaration();
	}
	
	public static VariableDeclaration createFrom(IVariableBinding varBinding) {
		VariableDeclaration retNode = null;

		Adapter factory = Adapter.getInstance();

		AstNode astNode = factory.get(varBinding);
		if (astNode instanceof VariableDeclaration) {
			retNode = (VariableDeclaration) astNode;
		}
		else {
			retNode = VariableDeclaration.create();
			retNode.varName = varBinding.getName();
			retNode.varType = retNode.varType = TraceabilityFactory.getType(varBinding.getType());
			factory.map(varBinding, retNode);
		}
		return retNode;
	}
	
	public static VariableDeclaration createFrom(ASTNode node){
		VariableDeclaration retNode = null;
		
		
		if(node instanceof org.eclipse.jdt.core.dom.VariableDeclaration){
			org.eclipse.jdt.core.dom.VariableDeclaration  domVDF = (org.eclipse.jdt.core.dom.VariableDeclaration)node;
			Adapter factory = Adapter.getInstance();
	
			AstNode astNode = factory.get(node);
			if ( astNode instanceof VariableDeclaration ) {
				retNode = (VariableDeclaration)astNode;
			}
			else {
				retNode = VariableDeclaration.create();
				retNode.varName = domVDF.getName().getFullyQualifiedName();
				retNode.varType = retNode.varType =  TraceabilityFactory.getType(domVDF.resolveBinding().getType());
				factory.map(node, retNode);
			}
		}
		else{
			throw new IllegalArgumentException();
		}
		return retNode;
		
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(varName);
		buffer.append(":");
		buffer.append(varType);
		return buffer.toString();
	}
}
