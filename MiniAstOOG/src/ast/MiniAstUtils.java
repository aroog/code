package ast;

public class MiniAstUtils {
	/**
	 * Return the enclosing type from an expression.
	 * XXX. This code is weird. Does not handle the expression directly. Starts by looking at the backpointer.
	 * Or change signature: this should take a BodyDeclaration! Caller must call expr.bodyDeclaration!
	 */
	public static TypeDeclaration getEnclosingTypeDeclaration(AstNode expression) {
		TypeDeclaration typeDecl = null;

// XXX. Cleanup commented out code.
//		if (expression instanceof MethodDeclaration) {
//			MethodDeclaration methDecl = (MethodDeclaration) expression;
//			typeDecl = methDecl.enclosingType;
//		} else
//		if (expression instanceof FieldDeclaration) {
//			FieldDeclaration fieldDecl = (FieldDeclaration) expression;
//			typeDecl = fieldDecl.enclosingType;
//		}
//		else if (expression instanceof TypeDeclaration) {
//			typeDecl = (TypeDeclaration) expression;
//		}
//		else if (expression instanceof VariableDeclaration) {
//
//		}
//		else
		if (expression != null) {
			BodyDeclaration enclDecl = expression.enclosingDeclaration;
			if (enclDecl instanceof MethodDeclaration) {
				MethodDeclaration methDecl = (MethodDeclaration) enclDecl;
				typeDecl = methDecl.enclosingType;
			}
			else if (enclDecl instanceof FieldDeclaration) {
				FieldDeclaration fieldDecl = (FieldDeclaration) enclDecl;
				typeDecl = fieldDecl.enclosingType;
			}
			else if (enclDecl instanceof TypeDeclaration) {
				typeDecl = (TypeDeclaration) enclDecl;
			}
			// XXX. Can we avoid these instanceof checks with dispatch?!
			else if (enclDecl instanceof VariableDeclaration) {
				VariableDeclaration varDecl = (VariableDeclaration)enclDecl;
				BodyDeclaration varEnclosingDecl = varDecl.enclosingDeclaration;
				if ( varEnclosingDecl instanceof MethodDeclaration ) {
					MethodDeclaration methDec = (MethodDeclaration)varEnclosingDecl;
					typeDecl = methDec.enclosingType;
				}
				else if ( varEnclosingDecl instanceof FieldDeclaration ) {
					FieldDeclaration methDec = (FieldDeclaration)varEnclosingDecl;
					typeDecl = methDec.enclosingType;
				}
				

			}
		}
		return typeDecl;
    }
}
