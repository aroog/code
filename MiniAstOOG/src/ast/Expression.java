package ast;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Transient;

/**
 * An Expression has a full Type, C<\ob{p}>. 
 */
// TODO: HIGH. XXX. Add enclosing scope, BodyDeclaration?
public abstract class Expression extends AstNode {
	
	@Transient 
	public abstract ExpressionType getExpressionType();
	
	/**
	 * The C
	 */
	// TODO: Do we need/use this?
	// TODO: this is not a source expression since the analysis operates at TAC. 
	// TODO: maybe use this as a type of the whole expression. May be redundant with some of the types already exported in subclasses. 
	public Type type;
	
	/**
	 * The \ob{p}
	 */
	// TODO: LOW. Rename: annotation.getAnnotation() sounds weird. Added delegate method.
	// TODO: LOW. Encapsulate field
	@Attribute(required=false)
	public String annotation;
	
}
