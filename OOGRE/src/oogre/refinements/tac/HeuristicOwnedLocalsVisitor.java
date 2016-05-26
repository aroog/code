package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.List;

import oogre.analysis.OOGContext;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.tac.model.Variable;

public class HeuristicOwnedLocalsVisitor extends ASTVisitor {
	
	private OOGContext context = OOGContext.getInstance();
		

	@Override
	public boolean visit(MethodDeclaration methodDecl) {

		int modifiers = methodDecl.getModifiers();
		if(Modifier.isPublic(modifiers) && !methodDecl.isConstructor() && !methodDecl.getReturnType2().isPrimitiveType()){
			Block body = methodDecl.getBody();
			if(body!=null){
				List<Statement> statements = body.statements();
				for (Statement stmnt : statements) {
					if(stmnt instanceof ReturnStatement){
						ReturnStatement retStmnt = (ReturnStatement)stmnt;
						Expression expression = retStmnt.getExpression();
						if(expression instanceof SimpleName){
							SimpleName simpleExpr = (SimpleName)expression;
							IBinding resolveBinding = simpleExpr.resolveBinding();
							Variable variable = context.getAllBindingKeyToVariableMap(resolveBinding.getKey());
							if(variable!=null){
								context.removeEncapsulatedVariable(variable);
							}
						}
					}
				}
			}

		}

		return super.visit(methodDecl);
		 
	}
}
