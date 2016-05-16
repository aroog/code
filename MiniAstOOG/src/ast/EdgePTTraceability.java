package ast;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;

/**
 * Trace from RuntimeEdge (Point-to) to ... something in the code
 *     
 *      class DeclaringType [e.g., class Foo] {
 *      
 *      		FieldDeclaration { ClassInstanceCreation };
 *      		[e.g., C f]
 *      	
 *      		MethodDeclaration {
 *      		}
 *      }
 *      
 *      
 *      What we need to save:
 *      	- FullyQualifiedDeclaredType: C
 *      	- FullyQualifiedDeclaringType: Foo
 *      	- fieldName = "f"
 *      	FieldDeclaration
 *      
 *      Do we have a pattern?
 *      For Pt-Edge:
 *      	- Expression [null] (or use this to store the fieldname as a Variable?);
 *      	 [could use FieldWriteExpression?] 
 *      	- ExpressionType (Field Type)
 *      	- EnclosingDeclaration (FieldDeclaration)
 *      	- EnclosingType  (TypeDeclaration)     
 *      
 *      Question: can we define a VariableDeclaration?
 *      TODO: Define a new ASTNode subclass for variables, not just expressions.
 */
@Default
public class EdgePTTraceability extends EdgeTraceability {
	
	@Element(required = false, name = "expression") 
	protected AstNode expression;

	// TODO: Candidate for elimination; derived information; not used.
	// We added it thinking it would be helpful for ArchTrace to locate the expression
//	@Element(required=false)
//	private Type expressionType;

	// Add default constructor for serialization
	protected EdgePTTraceability() {
	    super();
    }
	
	public EdgePTTraceability(@Element(required = false, name = "expression") AstNode expression) {
	    super();
	    this.expression = expression;
    }

	// TODO: Come up with a shorter toString() representation
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(expression);
//		buffer.append(expressionType);
		return buffer.toString();
	}

	// Implements value equality
	@Override
	/**
	 *  NOTE: Excluding expressionType on purpose
	 *  AstNode does NOT implement equals/hashCode.
	 */
    public boolean equals(Object o) {
	    if (this == o) {
		    return true;
	    }
	    if (o == null) {
		    return false;
	    }
	    if (!(o instanceof EdgePTTraceability)) {
		    return false;
	    }
	    
	    EdgePTTraceability other = (EdgePTTraceability) o;
	    if (expression == null) {
		    if (other.expression != null) {
			    return false;
		    }
	    }
	    // XXX. Should we compare the String text version of the expressions? That's what the previous code was effectively doing!
	    // Can we assume that the factory is not creating different objects for the same expression?
	    // XXX. Maybe add a warning: if expr != other.expr expr.toString() == other.expr.
	    // - this could legitimately occur for the same expr occuring in different places
	    else if (!expression.equals(other.expression)) {
		    return false;
	    }
	    return true;
    }
	
	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		// NOTE: Excluding expressionType on purpose
		result = 37 * result + (expression == null ? 0 : expression.hashCode());
		return result;
	}	
	
	public AstNode getExpression() {
    	return expression;
    }
}

