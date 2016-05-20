package oog.ui.actions;

import oog.ui.utils.ASTUtils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;

import edu.wayne.summary.Crystal;

public class OpenTypeAction extends Action {
	
	private String fullyQualifiedName;
	public OpenTypeAction(String fullyQualifiedName){
		this.fullyQualifiedName = fullyQualifiedName;
		this.setText("Open Type");
	}
	
	
	@Override
	public void run() {
		ITypeBinding typeBinding = Crystal.getInstance().getTypeBindingFromName(fullyQualifiedName);
		if(typeBinding!=null){
			//get all types & names of fields & methods & class/interface
			IJavaElement javaElement = typeBinding.getJavaElement();
			if (javaElement != null && ASTUtils.isFromSource(typeBinding)) {
				try {
//					EditorUtility.openInEditor(javaElement, true);
					/*
					 * code above causes a bug that if several classes are in
					 * the same java file, always open the first one no matter
					 * which one the user chooses
					 */
					JavaUI.openInEditor(javaElement);
//					IEditorPart javaEditor = JavaUI.openInEditor(javaElement);
//					JavaUI.revealInEditor(javaEditor, javaElement);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		super.run();
	}

}
