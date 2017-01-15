package oogre.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class DefaultingVisitor extends ASTVisitor {

	ASTRewrite rewrite;

	@Override
	public boolean visit(CatchClause node) {
		SingleVariableDeclaration param = node.getException();

		ITypeBinding type = param.resolveBinding().getType();
		if (!hasAnnotation(param.modifiers())) {
			if (!type.isPrimitive()) {
				if (type.getName().compareTo("String") != 0) {
					setParameterAnnotation(rewrite, param, "lent");
				}
			}
		}

		return super.visit(node);
	}


	private void setParameterAnnotation(ASTRewrite rewrite, SingleVariableDeclaration param, String annotation) {
		SingleMemberAnnotation newParamAnnotation = param.getAST().newSingleMemberAnnotation();
		newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName("Domain"));
		StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
		newStringLiteral.setLiteralValue(annotation);
		newParamAnnotation.setValue(newStringLiteral);
		ListRewrite paramRewrite = rewrite.getListRewrite(param, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
		paramRewrite.insertFirst(newParamAnnotation, null);
	}

	private boolean hasAnnotation(List paramModifiers) {
		boolean found = false;
		for (Iterator itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			Object o = itParamModifiers.next();
			if (o instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annot = (SingleMemberAnnotation) o;
				String name = annot.getTypeName().toString();
				if (name.compareTo("Domain") == 0) {
					found = true;
				}
			}
		}
		return found;
	}

}
