package edu.wayne.tracing.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ast.AstNode;
import ast.BaseTraceability;
import edu.wayne.tracing.internal.TraceUtility;


public class ExpressionVisitor extends ASTVisitor {
	private ExpressionType expressionType;
	private ASTNode enclosingDeclaration;

	Set<ASTNode> expressionsFound = new HashSet<ASTNode>();
	private enum ExpressionType{
		CLASS_INSTANCE_CREATION,
		FIELD_ACCESS,
		FIELD_WRITE,
		METHOD_INVOCATION
	}
	
	private String expressionToFind;
	

	public ExpressionVisitor(ASTNode enclosingDeclartion, BaseTraceability trace) {
		AstNode expression = trace.getExpression();
		if(expression instanceof ast.ClassInstanceCreation){
			expressionType = ExpressionType.CLASS_INSTANCE_CREATION;
			expressionToFind =((ast.ClassInstanceCreation) expression).complexExpression;
		}else if(expression instanceof ast.FieldWrite){
			expressionType = ExpressionType.FIELD_WRITE;
			expressionToFind = ((ast.FieldWrite) expression).complexExpression;
		}else if(expression instanceof ast.FieldAccess){
			expressionType = ExpressionType.FIELD_ACCESS;
			expressionToFind = ((ast.FieldAccess) expression).complexExpression;
		}else if(expression instanceof ast.MethodInvocation){
			expressionToFind =((ast.MethodInvocation) expression).complexExpression;
			expressionType = ExpressionType.METHOD_INVOCATION;
		}
		enclosingDeclaration = enclosingDeclartion;
		enclosingDeclaration.accept(this);
	}

	@Override
	public  boolean visit(VariableDeclarationFragment node){
		if(expressionType == ExpressionType.FIELD_WRITE){
			if(node.getInitializer()!=null){
				addExpressionNode(node);
			}
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		if(expressionType == ExpressionType.CLASS_INSTANCE_CREATION){
			addExpressionNode(node);
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(Assignment node){
		if(expressionType == ExpressionType.FIELD_WRITE){
			Expression lhs = node.getLeftHandSide();
			if(lhs instanceof FieldAccess ){
				addExpressionNode(node);
			}else if(lhs instanceof QualifiedName){
				IVariableBinding varBinding = (IVariableBinding) ((QualifiedName) lhs).resolveBinding();
				if(varBinding.isField()){
					addExpressionNode(node);
				}
			}else if(lhs instanceof SimpleName){
				IVariableBinding varBinding = (IVariableBinding) ((SimpleName) lhs).resolveBinding();
				if(varBinding.isField()){
					addExpressionNode(node);
				}
			}
			
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodInvocation node){
		if(expressionType == ExpressionType.METHOD_INVOCATION){
			addExpressionNode(node);
		}
		return super.visit(node);
	}
	
	
	
	
	@Override
	public boolean visit(SuperMethodInvocation node) {
		if(expressionType == ExpressionType.METHOD_INVOCATION){
			addExpressionNode(node);
		}
		return super.visit(node);
	}

	private boolean addExpressionNode(ASTNode node){
		if(node.toString().equals(expressionToFind)){
			expressionsFound.add(node);
			TraceUtility.highlightInEditor(enclosingDeclaration, node);
			return true;
		}
		return false;
	}
	
	public Set<ASTNode> getExpressions(){
		return expressionsFound;
	}
	
	
	
	
	
	
	
}
