package oog.ui.utils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import edu.wayne.summary.Crystal;

public class ASTUtils {

	public static MethodDeclaration getMethodDeclaration(String methodName) {
		IJavaElement javaElem = EditorUtility.getActiveEditorJavaInput();
		if (javaElem.getElementType() == IJavaElement.COMPILATION_UNIT) {
			ICompilationUnit iCompUnit = (ICompilationUnit) javaElem;
			ASTNode astNode = Crystal.getInstance()
					.getASTNodeFromCompilationUnit(iCompUnit);
			if (astNode != null
					&& astNode.getNodeType() == ASTNode.COMPILATION_UNIT) {
				CompilationUnit compUnit = (CompilationUnit) astNode;
				for (Object declaration : compUnit.types()) {
					if (declaration instanceof TypeDeclaration) {
						for (MethodDeclaration method : ((TypeDeclaration) declaration)
								.getMethods()) {
							if (methodName.contentEquals(method.getName()
									.getFullyQualifiedName())) {
								return method;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static ASTNode getASTNode(ITextSelection selection) {
		return ASTUtils.getASTNode(selection.getOffset(),
				selection.getLength(), null);

	}

	public static ASTNode getASTNode(int offset, int length, IEditorPart part) {
		IJavaElement activeEditorJavaInput = null;
		if (part != null) {
			IEditorInput editorInput = part.getEditorInput();
			if (editorInput != null) {
				activeEditorJavaInput = JavaUI
						.getEditorInputJavaElement(editorInput);
			}
		} else {
			activeEditorJavaInput = EditorUtility.getActiveEditorJavaInput();
			part = getActivePart();
		}

		if (activeEditorJavaInput != null
				&& activeEditorJavaInput.getElementType() == IJavaElement.COMPILATION_UNIT) {

			ICompilationUnit compilationUnit = (ICompilationUnit) activeEditorJavaInput;
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(compilationUnit);
			parser.setResolveBindings(true);
			ASTNode root = parser.createAST(null);

			ASTNode node = NodeFinder.perform(root, offset, length);
			return node;
		}
		return null;
	}

	public static IWorkbenchPage getActivePage(){
		IWorkbench workbench = PlatformUI.getWorkbench();
		if(workbench!=null){
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			if(activeWorkbenchWindow!=null){
				return activeWorkbenchWindow.getActivePage();
			}
		}
		return null;
	}
	private static IEditorPart getActivePart() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			IWorkbenchWindow activeWorkbenchWindow = workbench
					.getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				IWorkbenchPage activePage = activeWorkbenchWindow
						.getActivePage();
				if (activePage != null) {
					return activePage.getActiveEditor();
				}

			}

		}
		return null;
	}

	public static boolean isFromSource(ITypeBinding typeBinding) {
		if (typeBinding != null) {
			return typeBinding.isFromSource();
		}
		return false;
	}

	public static boolean isFromSource(String fullyQualifiedName) {
		ITypeBinding typeBinding = Crystal.getInstance()
				.getTypeBindingFromName(fullyQualifiedName);
		if (typeBinding != null) {
			return isFromSource(typeBinding);
		}
		return false;
	}

//	public static String getFullyQualifiedNameOfOpenJavaEditor() {
//		IJavaElement activeEditorJavaInput = EditorUtility.getActiveEditorJavaInput();
//		if (activeEditorJavaInput != null) {
//			if (activeEditorJavaInput.getElementType() == IJavaElement.COMPILATION_UNIT) {
//				try {
//					ICompilationUnit iCompilationUnit = (ICompilationUnit) activeEditorJavaInput;
//					String elementName = iCompilationUnit.getElementName();
//					elementName = elementName.substring(0,
//							elementName.lastIndexOf('.'));
//					IType[] types = iCompilationUnit.getTypes();
//					for (IType type : types) {
//						String fullyQualifiedName = type
//								.getFullyQualifiedName();
//						if (fullyQualifiedName.contains(elementName)) {
//							return fullyQualifiedName;
//						}
//					}
//				} catch (JavaModelException e1) {
//					e1.printStackTrace();
//				}
//			}
//		} else {
//			System.err.println("Java Editor is not open");
//		}
//		return null;
//	}
	
	public static IType getTypeOfOpenJavaEditor() {
		IJavaElement activeEditorJavaInput = EditorUtility.getActiveEditorJavaInput();
		if (activeEditorJavaInput != null) {
			if (activeEditorJavaInput.getElementType() == IJavaElement.COMPILATION_UNIT) {
				try {
					ICompilationUnit iCompilationUnit = (ICompilationUnit) activeEditorJavaInput;
					String elementName = iCompilationUnit.getElementName();
					elementName = elementName.substring(0,
							elementName.lastIndexOf('.'));
					IType[] types = iCompilationUnit.getTypes();
					for (IType type : types) {
						String fullyQualifiedName = type
								.getFullyQualifiedName();
						if (fullyQualifiedName.contains(elementName)) {
							return type;
						}
					}
				} catch (JavaModelException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			System.err.println("Java Editor is not open");
		}
		return null;
	}

}
