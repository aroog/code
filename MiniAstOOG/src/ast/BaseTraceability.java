package ast;


/**
 *      	- Expression
 *      	- ExpressionType
 *      	- EnclosingDeclaration
 *      	- EnclosingType 
 *
 *		TODO: HIGH. Still missing Variable class.
 */

// TODO: Convert this to an interface?
// Also define an abstract class that provides a default implementation of the interface
// TODO: Should this be abstract? Yes. Should only create instances of subclasses: ObjectTraceability, etc.

/**
	TODO: HIGH. XXX. After discussion, refactor this BaseTraceability business:
	
	Right now, BaseTraceability is being used to tie a ClassInstanceCreation inside a MethodDeclaration
	Why not have each Expression/AstNode know its enclosingDeclaration,
	And have each BodyDeclaration know its enclosingType (which is already does!).
	
	We recently added to each Expression its type.
	
	NOTE: The more elegant design will probably produce more serialization stuff.
	Except that right now, the design is a mixture of the two styles.
	E.g., why does MethodDeclaration have an enclosingType?
	(forward and back pointers)
	
	We can always zip the XML file.
	
	Once we do that, the Traceability information will consist of a set of Expressions.
	Or a set of Declarations.
	
	We will no longer need most of  the fields in the BaseTraceability class.
	
	NOTE:XXX: cannot implement equals here since AstNodes do not implement equals
 *
 */
public abstract class BaseTraceability {

	public abstract AstNode getExpression();
	
}
