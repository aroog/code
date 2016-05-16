package ast;

/**
 *
 * A MethodDeclaration has an enclosingType
 * A FieldDeclaration has an enclosingType
 * So the base class has nothing to avoid refused bequest.
 */
// DONE. Should this be abstract? Yes.
public abstract class BodyDeclaration extends AstNode{

	// TODO: Do not duplicate Eclipse AST. Keep this minimal.
	// The constraints can always reach into Eclipse ASTNode and get everything, including ITypeBindings, IVariableBindings, etc.
}
