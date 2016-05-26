package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oogre.analysis.Config;
import oogre.analysis.OOGContext;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.cmu.cs.crystal.tac.model.Variable;

import ast.FieldAccess;

public class HeuristicOwnedVisitor extends ASTVisitor {

	private OOGContext context = OOGContext.getInstance();
	
	@Override
	public boolean visit(FieldDeclaration node) {

		int modifiers = node.getModifiers();
		if (Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers)) {
			List<VariableDeclarationFragment> fragments = node.fragments();
			for (VariableDeclarationFragment varDeclFrag : fragments) {
				IVariableBinding varBinding = varDeclFrag.resolveBinding();
				String enclosingClass = varBinding.getDeclaringClass().getQualifiedName();
				if(!varBinding.getType().isPrimitive() && !varBinding.getType().getQualifiedName().equals("java.lang.String")){
					final TACVariable fieldVar = new TACVariable(varBinding);
					if(!enclosingClass.equals(Config.MAINCLASS)){
						context.addEncapsulatedVariable(fieldVar);
					}
				}
			}
		}
		return super.visit(node);
	}


	@Override
	public boolean visit(MethodDeclaration methodDecl) {
		
		IMethodBinding methodBinding = methodDecl.resolveBinding();
		String enclosingClass = methodBinding.getDeclaringClass().getQualifiedName();
		
		int modifiers = methodDecl.getModifiers();
		if(Modifier.isPublic(modifiers)){
			Block body = methodDecl.getBody();
			if(body!=null){
				List<Statement> statements = body.statements();
				for (Statement stmnt : statements) {
					if(stmnt instanceof ReturnStatement){
						ReturnStatement retStmnt = (ReturnStatement)stmnt;
						Expression expression = retStmnt.getExpression();
						if(expression instanceof SimpleName){
							SimpleName simpleExpr = (SimpleName)expression;
							IBinding seBinding = simpleExpr.resolveBinding();
							if(seBinding instanceof IVariableBinding){
								final TACVariable fieldVar = new TACVariable((IVariableBinding)seBinding);
								context.removeEncapsulatedVariable(fieldVar);
							}
						}
						else if(expression instanceof org.eclipse.jdt.core.dom.FieldAccess){
							org.eclipse.jdt.core.dom.FieldAccess fldAcssExpr = (org.eclipse.jdt.core.dom.FieldAccess)expression;
							IVariableBinding fieldBinding = fldAcssExpr.resolveFieldBinding();
							final TACVariable fieldVar = new TACVariable(fieldBinding);
							context.removeEncapsulatedVariable(fieldVar);
						}
					}
					else if(stmnt instanceof VariableDeclarationStatement){
						VariableDeclarationStatement varDeclStmnt = (VariableDeclarationStatement)stmnt;
						List<VariableDeclarationFragment> fragments = varDeclStmnt.fragments();
						for (VariableDeclarationFragment fragment : fragments) {
							VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment)fragment;
							Expression initializer = varDeclFrag.getInitializer();
							IVariableBinding resolveBinding = varDeclFrag.resolveBinding();
							Variable variable = context.getAllBindingKeyToVariableMap(resolveBinding.getKey());
							
							if(variable != null && !resolveBinding.getType().isPrimitive() && !resolveBinding.getType().getQualifiedName().equals("java.lang.String") && !enclosingClass.equals(Config.MAINCLASS)){
								context.addEncapsulatedVariable(variable);
							}
							
							if(initializer instanceof ClassInstanceCreation){

								if(variable != null && !resolveBinding.getType().isPrimitive() && !resolveBinding.getType().getQualifiedName().equals("java.lang.String") && !enclosingClass.equals(Config.MAINCLASS)){
									context.addLogicalPartVariable(variable);
								}
							}
						}
					}
					else if(stmnt instanceof ExpressionStatement){
						ExpressionStatement exprStmnt = (ExpressionStatement)stmnt;
						Expression expression = exprStmnt.getExpression();
						if(expression instanceof Assignment){
							Assignment assignmentExpr = (Assignment)expression;
							Expression rightHandSide = assignmentExpr.getRightHandSide();
							if(rightHandSide instanceof ClassInstanceCreation){
								ClassInstanceCreation rhsExpr = (ClassInstanceCreation)rightHandSide;
								ITypeBinding rhsVariableBinding = rhsExpr.resolveTypeBinding();
								Variable rhsVariable = context.getAllBindingKeyToVariableMap(rhsVariableBinding.getKey());
								Expression leftHandSide = assignmentExpr.getLeftHandSide();
								if(leftHandSide instanceof org.eclipse.jdt.core.dom.FieldAccess){
									org.eclipse.jdt.core.dom.FieldAccess lhsFldAccs = (org.eclipse.jdt.core.dom.FieldAccess)leftHandSide;
									IVariableBinding resolveFieldBinding = lhsFldAccs.resolveFieldBinding();
									final TACVariable fieldVar = new TACVariable(resolveFieldBinding);
									if(!resolveFieldBinding.getType().isPrimitive() && !resolveFieldBinding.getType().getQualifiedName().equals("java.lang.String") && !enclosingClass.equals(Config.MAINCLASS)){
										context.addEncapsulatedVariable(fieldVar);
										context.addLogicalPartVariable(fieldVar);
									}
								}
							}
						}
					}
				}
			}

		}

		
		return super.visit(methodDecl);
	}


/*	@Override
	public boolean visit(VariableDeclarationStatement varDeclStmnt) {
		
		//Finding the declaring class
		ASTNode parent= varDeclStmnt.getParent();
		while(!(parent instanceof TypeDeclaration)){
			parent=parent.getParent();
		}
		TypeDeclaration enclosingType = (TypeDeclaration)parent;
		String enclosingClass = enclosingType.resolveBinding().getQualifiedName();
		
		if(!enclosingClass.equals(Config.MAINCLASS)){
			if(!varDeclStmnt.getType().isPrimitiveType()){
				String vartypeName = varDeclStmnt.getType().resolveBinding().getQualifiedName();
				List<VariableDeclarationFragment> fragments = varDeclStmnt.fragments();
				for (VariableDeclarationFragment vareDeclFrag : fragments) {
					String varName = vareDeclFrag.getName().toString();
					AnnotatableUnit varAU = new AnnotatableUnit(varName,AnnotateUnitEnum.v,vartypeName,enclosingClass);
					if(!varDeclStmnt.getType().resolveBinding().isPrimitive() && !context.getEncapsulatedAUs().contains(varAU)){
						context.addEncapsulatedAU(varAU);
					}
					///Add locals that are instantiated in the class
					Expression initializer = vareDeclFrag.getInitializer();
					if(initializer!=null){
						if(initializer instanceof ClassInstanceCreation){
							List<AnnotatableUnit> createdLocals = context.getCreatedLocals(enclosingClass);
							if(createdLocals == null){
								createdLocals = new ArrayList<AnnotatableUnit>();
								createdLocals.add(varAU);
							}
							else{
								if(!createdLocals.contains(varAU)){
									createdLocals.add(varAU);
								}
							}
							context.addCreatedLocals(enclosingClass, createdLocals);
						}
					}
				}
			}
		}
		
		return super.visit(varDeclStmnt);
	}*/
	
}
