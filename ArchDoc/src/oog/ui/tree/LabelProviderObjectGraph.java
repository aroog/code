package oog.ui.tree;

import oog.itf.IEdge;
import oog.itf.IObject;
import oog.ui.content.wrappers.OGraphInfoWrapper.TopLevel;
import oog.ui.utils.IconUtils;
import oog.ui.utils.LabelUtil;
import oog.ui.views.MiniAstToEclipseAST;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.FilteredTree;

import ast.BaseTraceability;

import edu.wayne.ograph.ODomain;



// TODO: Consolidate all the image registries!!!
public class LabelProviderObjectGraph extends PreferenceBoldLabelProvider {
	// ImageRegistry takes care of disposing any image objects
	private static ImageRegistry imageCache = new ImageRegistry();

	public LabelProviderObjectGraph(FilteredTree filterTree) {
		super(filterTree);
	}

	public void dispose() {
		super.dispose();
	}

	/*
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		String key = element.getClass().getSimpleName();

		// obtain the cached image corresponding to the descriptor
		Image image = LabelProviderObjectGraph.imageCache.get(key);
		if (image == null) {
			ImageDescriptor imageDescriptor = IconUtils.getImageDescriptor("icons/" + key+".gif");
			

		    if(imageDescriptor == null) return null;
		    else{
				image = imageDescriptor.createImage();
				LabelProviderObjectGraph.imageCache.put(key, image);
			}
		}
		return image;
		

	}

	public String getText(Object element) {
		String label = "";

		if(element instanceof ODomain){
			ODomain treeElement = (ODomain)element;
			String d = treeElement.getD();
			int lastIndexOf = d.lastIndexOf("::");
			if(lastIndexOf > -1){
				label = d.substring(lastIndexOf+2);
			}else{
				label= d;
			}
		}else if (element instanceof IObject) {
			IObject treeElement = (IObject) element;

			String fullyQualifiedName = treeElement.getC().getFullyQualifiedName();
			label = treeElement.getInstanceDisplayName() + " : "+Signature.getSimpleName(fullyQualifiedName);
		}else if(element instanceof IEdge){
			label =LabelUtil.getEdgeLabel((IEdge)element);
		}else if(element instanceof TopLevel){
			label = ((TopLevel) element).getTitle();
		}else if (element instanceof BaseTraceability) {
			// label = ((BaseTraceability)element).toString();
			label = MiniAstToEclipseAST.toString((BaseTraceability)element);
		}
		

		return label;
	}

	
	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}
}
