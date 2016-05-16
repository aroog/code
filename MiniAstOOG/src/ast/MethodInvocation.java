package ast;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import adapter.Adapter;
import adapter.TraceabilityFactory;

// Question: do we need to store the arguments of the invocation as "nested/children" expressions?
// TODO: HIGH. XXX. Need the list of actuals/arguments to store the actual domains being passed for the method arguments.
public class MethodInvocation extends Expression {

	// TODO: Move field up to Expression class?
	// Save the complex expression as a String
	@Element(required=false)
	public String complexExpression;

	// The resolved MethodDeclaration of this MethodInvocation expression.
	// The name of the method being invoked is on the MethodDeclaration
	@Element(required=false)
	public MethodDeclaration methodDeclaration;
	
	/**
	 * actual argument type 
	 * x = er.m(e1, e2), returns classes of e1, e2, or empty list 
	 * */
	@ElementList(required=false)
	public List<Type> argumentTypes;

	/**
	 * actual domains on the arguments
	 * TODO: Use something better than String? AnnotationInfo? Too cumbersome
	 */
	@ElementList(required=false)
	public List<String> argumentAnnotations;
	
	/**
	 * actual receiver type 
	 * x = er.m(e), returns class of er
	 * */
	@Element(required=false)
	public Type recvType;
	
	/**
	 * actual receiver annotation
	 */
	@Element(required=false)
	public String recvAnnotation;
	
	/**
	 * actual return type 
	 * x = er.m(e), 
	 * for generics does the type substitution of formal type parameter to actual type argument
	 * */
	@Element(required=false)
	public Type retType;
	
	/**
	 * actual annotation on the return type
	 */
	@Element(required=false)
	public String retAnnotation;
	
	// TODO: HIGH. What about missing properties:
	// - arguments():List<VariableDeclaration> or List<Type>   as-needed in ArchMetrics 
	// - receiver; (): VariableDeclaration or just Type as-need in ArchMetrics 
	// - getExpressions()
	// - resolveMethodBinding
	// MethodInvocation should probably not have more than the expression.
	// Needed in ArchMetrics: list of arguments  e.m(e1,e2) ==> [e1:T_e,, e2:T_e1,, e3:T_e2]
	// or list of argument types: e.m(e1.e2) --> [C_e, C_e1, C_e2]
	// the annotations for the actual type may not be available. 
	protected MethodInvocation() {
		super();
	}
	static MethodInvocation create(){
		return new MethodInvocation();
	}
	
	public static MethodInvocation createFrom(ASTNode node) {
		MethodInvocation retNode = null;
		
		if(node instanceof org.eclipse.jdt.core.dom.MethodInvocation
				|| node instanceof SuperMethodInvocation){
			Adapter factory = Adapter.getInstance();
	
			AstNode astNode = factory.get(node);
			if ( astNode instanceof MethodInvocation ) {
				retNode = (MethodInvocation)astNode;
			}
			else {
				retNode = MethodInvocation.create();
				ast.MethodDeclaration methDec = TraceabilityFactory.getMethodDeclaration(node);
				if(methDec!= null){
					retNode.methodDeclaration = methDec;
				}
				retNode.complexExpression = node.toString();
				factory.map(node, retNode);
			}
		}else{
			throw new IllegalArgumentException(node.toString());
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
	    return ExpressionType.MethodInvocation;
    }
	
}
