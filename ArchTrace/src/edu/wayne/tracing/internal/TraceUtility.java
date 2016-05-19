package edu.wayne.tracing.internal;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.wayne.tracing.actions.TraceToCodeUIAction;

public class TraceUtility {

	
	public static boolean matchMethodParams(List<ast.VariableDeclaration> paramsToFind,ITypeBinding[] paramsFound ) {

		if(paramsToFind.size() != paramsFound.length){
			return false;
		}else{
			for(int i = 0; i < paramsToFind.size(); i ++){
				
				
				ast.VariableDeclaration variableDeclaration = paramsToFind.get(i);
				ITypeBinding iTypeBinding = paramsFound[i];
				String fullyQualifiedName = variableDeclaration.varType.getFullyQualifiedName();
				String qualifiedName = iTypeBinding.getQualifiedName();
				
				if(fullyQualifiedName.compareTo(qualifiedName)!= 0){
					return false;
				}
			}
		}
		return true;
	}
	public static boolean match(String toFind, String found){
		if(toFind ==null || found==null){
			return false;
		}
		return found.contentEquals(toFind);
		
	}
	


	/*
	 * ASTNode will have method resolveBinding() on these classes
	 * AbstractTypeDeclaration 
	 * AnnotationType 
	 * MemberDeclaration
	 * AnonymousClassDeclaration 
	 * ImportDeclaration 
	 * MemberRef 
	 * MethodDeclaration
	 * MethodRef 
	 * Name 
	 * PackageDeclaration 
	 * Type 
	 * TypeDeclarationStatement
	 * TypeParameter 
	 * VariableDeclaration
	 */

	public static void selectInEditor(ASTNode node) {
		if(TraceToCodeUIAction.highlightCode){
			IJavaElement javaElement = getIJavaElement(node);
			if (javaElement != null) {
				try {
	
					EditorUtility.openInEditor(javaElement);
					IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					if (part instanceof ITextEditor) {
						((ITextEditor) part).selectAndReveal(node.getStartPosition(), node.getLength());
					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void highlightInEditor(ASTNode enclosingDeclaration , ASTNode expressionNode){
		if(TraceToCodeUIAction.highlightCode){
			IJavaElement javaElement = getIJavaElement(enclosingDeclaration);
			if (javaElement != null) {
				try {
	
					EditorUtility.openInEditor(javaElement);
					IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					if (part instanceof ITextEditor) {
						((ITextEditor) part).selectAndReveal(expressionNode.getStartPosition(), expressionNode.getLength());
					
					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static IJavaElement getIJavaElement(ASTNode node){
		IJavaElement javaElement = null;
		// Find IJavaElement corresponding to the ASTNode
		if (node instanceof MethodDeclaration) {
			javaElement = ((MethodDeclaration) node).resolveBinding()
					.getJavaElement();
		} else if (node instanceof VariableDeclaration) {
			javaElement = ((VariableDeclaration) node).resolveBinding()
					.getJavaElement();
		}else if(node instanceof TypeDeclaration){
			javaElement = ((TypeDeclaration)node).resolveBinding()
					.getJavaElement();
		}else if(node instanceof ClassInstanceCreation){
			javaElement = ((ClassInstanceCreation)node).resolveConstructorBinding().getJavaElement();
		}
		
		return javaElement;
	}
}
