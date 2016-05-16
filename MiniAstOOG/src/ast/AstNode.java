package ast;

import org.simpleframework.xml.Element;

// TODO: Rename 'Ast' -> AST since it is an acronym.
public class AstNode {

	@Element(required=false)
	public BodyDeclaration enclosingDeclaration;


}
