package edu.wayne.tracing.actions;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ast.AstNode;
import ast.BaseTraceability;
import ast.FieldDeclaration;
import ast.MiniAstUtils;
import ast.Type;
import ast.VariableDeclaration;
import edu.wayne.tracing.internal.TraceUtility;

public class DeclarationVisitor extends ASTVisitor {

	private enum DeclarationType {
		FIELD_DECLARATION, METHOD_DECLARATION, TYPE_DECLARATION

	}

	private DeclarationType enclosingDeclarationType;

	private String typeDeclarationToFind;
	private String typeDeclarationFound;

	private String variableToFind;
	private String variableFound;

	private String methodNameToFind;
	private String methodNameFound;

	private ASTNode foundEnclosingDeclaration;
	private ITypeBinding[] methodParamasFound;
	private List<VariableDeclaration> methodParamsToFind;
	private ExpressionVisitor expressionVisitor;

	public DeclarationVisitor(ASTNode node, BaseTraceability trace) {
		super();
		
		AstNode expression = trace.getExpression();
		ast.TypeDeclaration enclosingType = MiniAstUtils.getEnclosingTypeDeclaration(expression);

		if (enclosingType != null) {

			Type type = enclosingType.type;
			if (type != null) {
				typeDeclarationToFind = type.getFullyQualifiedName();
				if (expression instanceof ast.FieldDeclaration) {
					enclosingDeclarationType = DeclarationType.FIELD_DECLARATION;
					FieldDeclaration fieldDec = ((ast.FieldDeclaration) expression);
					variableToFind = fieldDec.fieldName;
				} else if (expression.enclosingDeclaration instanceof ast.MethodDeclaration) {
					enclosingDeclarationType = DeclarationType.METHOD_DECLARATION;
					ast.MethodDeclaration methDec = (ast.MethodDeclaration) expression.enclosingDeclaration;
					methodNameToFind = methDec.methodName;
					methodParamsToFind = methDec.parameters;
				} else if (expression.enclosingDeclaration instanceof ast.TypeDeclaration) {
					enclosingDeclarationType = DeclarationType.TYPE_DECLARATION;
				}
				else {  // Not a declaration; could be an expression, e.g,. ClassCreationExpression 
					enclosingDeclarationType = DeclarationType.TYPE_DECLARATION;
				}
				if (enclosingDeclarationType == null ) {
					int debug = 0; debug++;
					System.err.println("Buggy...");
				}
				node.accept(this);
				if (foundEnclosingDeclaration != null) {
					expressionVisitor = new ExpressionVisitor(
							foundEnclosingDeclaration, trace);
				}
			}
		}

	}

	@Override
	public boolean visit(TypeDeclaration node) {
		typeDeclarationFound = node.resolveBinding().getQualifiedName();
		if (isDeclarationTarget(DeclarationType.TYPE_DECLARATION)) {
			if (matchTypeDeclaration()) {
				TraceUtility.selectInEditor(node);
				setEnclosingDeclaration(node);
			}
		}

		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {

		if (isDeclarationTarget(DeclarationType.FIELD_DECLARATION)) {
			IVariableBinding variableBinding = node.resolveBinding();

			if (variableBinding != null) {
				if (variableBinding.isField()) {
					variableFound = variableBinding.getName();
					if (matchTypeDeclaration()
							&& TraceUtility
									.match(variableToFind, variableFound)) {
						TraceUtility.selectInEditor(node);
						setEnclosingDeclaration(node);
					}

				}
			}
		}

		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {

		if (isDeclarationTarget(DeclarationType.METHOD_DECLARATION)) {
			IMethodBinding methodBinding = node.resolveBinding();

			if (methodBinding != null) {
				ITypeBinding declaringClass = methodBinding.getDeclaringClass();

				typeDeclarationFound = declaringClass != null ? declaringClass
						.getQualifiedName() : "";
				methodParamasFound = methodBinding.getParameterTypes();
				methodNameFound = methodBinding.getName();
				if (matchTypeDeclaration()
						&& TraceUtility
								.match(methodNameToFind, methodNameFound)
						&& TraceUtility.matchMethodParams(methodParamsToFind,
								methodParamasFound)) {
					TraceUtility.selectInEditor(node);
					setEnclosingDeclaration(node);
				}
			}

		}
		return super.visit(node);
	}

	private boolean matchTypeDeclaration() {
		return TraceUtility.match(typeDeclarationFound, typeDeclarationToFind);
	}

	private boolean isDeclarationTarget(DeclarationType t) {
		return enclosingDeclarationType != null && enclosingDeclarationType.equals(t);

	}

	public ASTNode getEnclosingDeclaration() {
		return foundEnclosingDeclaration;
	}

	private void setEnclosingDeclaration(ASTNode foundEnclosingDeclaration) {
		this.foundEnclosingDeclaration = foundEnclosingDeclaration;
	}

	public Set<ASTNode> getExpressions() {
		if (expressionVisitor != null) {

			if (expressionVisitor.getExpressions() != null) {
				return expressionVisitor.getExpressions();
			}
		}
		return null;
	}

}
