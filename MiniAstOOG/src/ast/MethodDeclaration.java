package ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import adapter.Adapter;
import adapter.TraceabilityFactory;

// TODO: HIGH. HIGH. Add signature...
// - DONE. return type
// - DONE. parameters (might be enough to just have these; without the return type)
// Look at: org.eclipse.jdt.core.IType: public IMethod getMethod(String name, String[] parameterTypeSignatures)
// and org.eclipse.jdt.core.Signature
public class MethodDeclaration extends BodyDeclaration {

	// XXX. This is redundant with AstNode.enclosingDeclaration
	// Just a different type. So for now, be sure to set them both (to the same object)!
	// TODO: Move field up to base class?
	@Element(required=false)	
	public TypeDeclaration enclosingType;

	@Element(required=false)
	public String methodName;
	
	@Element(required=false)
	public Type returnType;
	
	/**
	 * Formal annotation on method return type
	 * XXX. Minor: inconsistent naming convention w.r.t. MethodInvocation: retAnnotation
	 */
	@Element(required=false)
	public String returnAnnotation;
	
	/**
	 * The annotations on the formal parameters is in the VariableDeclaration.annotation field
	 */
	@ElementList(required = false)
	public List<VariableDeclaration> parameters = new ArrayList<VariableDeclaration>();

	/**
	 * Formal annotation on the method receiver (optional)
	 * XXX. Minor: inconsistent naming convention w.r.t. MethodInvocation: recvAnnotation 
	 */
	@Element(required=false)
	public String receiverAnnotation = null;
	
	/**
	 * Flag to set the formals on a method declaration once
	 * The formals are set in PointsToOOG, not in MiniAST (which does not have access to the annotation DB).
	 * Some formals could be retrieved from AliasXML files.
	 */
	@Transient
	public boolean annotationsSet = false;
	
	protected MethodDeclaration() {
	    super();
    }

	 static MethodDeclaration create(){
		return new MethodDeclaration();
	}
	
	public static MethodDeclaration createFrom(ASTNode node){
		MethodDeclaration retNode = null;
		if(node instanceof org.eclipse.jdt.core.dom.MethodDeclaration){
			
			org.eclipse.jdt.core.dom.MethodDeclaration methDec = (org.eclipse.jdt.core.dom.MethodDeclaration) node;
			Adapter factory = Adapter.getInstance();
	
			AstNode astNode = factory.get(node);
			if ( astNode instanceof MethodDeclaration ) {
				retNode = (MethodDeclaration)astNode;
			}
			else {
				retNode = MethodDeclaration.create();
				
				retNode.parameters = TraceabilityFactory.getMethodParameters(methDec);
				retNode.methodName = methDec.getName().getFullyQualifiedName();
				if(methDec.getReturnType2() != null){
					retNode.returnType = TraceabilityFactory.getType(methDec.getReturnType2().resolveBinding());
				}
				retNode.enclosingType = TraceabilityFactory.getEnclosingTypeDeclaration(node);
				factory.map(node, retNode);
				factory.mapMethodDeclaration(retNode);
			}
		}else{
			throw new IllegalArgumentException();
		}
		return retNode;		
	}
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(enclosingType);
		buffer.append("::");
		buffer.append(returnType);
		buffer.append(" ");
		buffer.append(methodName);
		// buffer.append("(");
		buffer.append(parameters);
		//buffer.append(")");
		return buffer.toString();
	}	
	
	/**
	 * This is just test code
	 */
	public void addParameter(String paramName, String paramType) {
		// TODO: Instead of creating a new type, should we not lookup in a factory somewhere?
		this.parameters.add(new VariableDeclaration(paramName, new Type(paramType)));
	}
}
