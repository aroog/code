package oogre.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class RemoveAnnotationsVisitor extends ASTVisitor {

	ASTRewrite rewrite;

	@Override
	public boolean visit(CatchClause node) {
		
		SingleVariableDeclaration param = node.getException();

		ITypeBinding type = param.resolveBinding().getType();
		SingleMemberAnnotation annot = hasAnnotation(param.modifiers());
		if (annot != null) {
			ListRewrite paramRewrite = rewrite.getListRewrite(param, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.remove(annot, null);
		}

		
		return super.visit(node);
	}

	private SingleMemberAnnotation hasAnnotation(List paramModifiers) {
		for (Iterator itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			Object o = itParamModifiers.next();
			if (o instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annot = (SingleMemberAnnotation) o;
				String name = annot.getTypeName().toString();
				if (name.compareTo("Domain") == 0) {
					return annot;
				}
			}
		}
		return null;
	}

}
