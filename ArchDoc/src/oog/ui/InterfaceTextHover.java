package oog.ui;

import java.util.Set;

import oog.itf.IElement;
import oog.ui.utils.ASTUtils;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

import edu.wayne.summary.internal.WorkspaceUtilities;
import edu.wayne.summary.strategies.EdgeSummary;
import edu.wayne.summary.strategies.EdgeSummaryAll;
import edu.wayne.summary.strategies.Info;

public class InterfaceTextHover implements IJavaEditorTextHover {

	private IEditorPart editor;
	private static EdgeSummary summary;

	@Override
	// XXX. Refactor logic to avoid code duplication with SummaryView
	// XXX. This is triggering way too early, before the UI is ready!
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		
		RuntimeModel instance = RuntimeModel.getInstance();
		if(instance != null ) {
			summary = instance.getSummaryInfo();
		}
		
		if(summary!=null){
			ASTNode node = ASTUtils.getASTNode(hoverRegion.getOffset(),
					hoverRegion.getLength(), this.editor);
			if (node != null) {
				if (node.getNodeType() == ASTNode.SIMPLE_NAME) {
					
					SimpleName simpleName = (SimpleName) node;
					ITypeBinding typeBinding = simpleName.resolveTypeBinding();
					if(typeBinding != null)
						if (typeBinding.isInterface() || Modifier.isAbstract(typeBinding.getModifiers())) {
	
						String fieldType = typeBinding.getQualifiedName();
						
						// XXX. Fill in the rest of the arguments
						Set<Info<IElement>> classesBehindInterface = summary.getClassesBehindInterface("", fieldType, "");
	
						StringBuffer buffer = new StringBuffer();
	
						
						if (classesBehindInterface != null) {
	
							HTMLPrinter.addSmallHeader(buffer, fieldType);
							HTMLPrinter.startBulletList(buffer);
							for (Info i : classesBehindInterface) {
								HTMLPrinter.addBullet(buffer, i.getKey());
							}
							HTMLPrinter.endBulletList(buffer);
						}else{
							return null;
						}
	
						return buffer.toString();
					}
	
				}
			}
		}

		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {

		return null;
	}

	@Override
	public void setEditor(IEditorPart editor) {
		this.editor = editor;

	}

}
