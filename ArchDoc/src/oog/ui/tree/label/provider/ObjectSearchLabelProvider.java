package oog.ui.tree.label.provider;

import oog.itf.IEdge;
import oog.ui.content.wrappers.DisplayGraphInfoWrapper.TopLevel;
import oog.ui.tree.displaygraph.ArchitecturalLabelDecoratorBase;
import oog.ui.utils.IconUtils;
import oog.ui.utils.LabelUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayEdge;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;

public class ObjectSearchLabelProvider extends ArchitecturalLabelDecoratorBase implements ILabelProvider, IColorProvider{

	@Override
	public Image getImage(Object element) {
		String name = element.getClass().getSimpleName();
		ImageDescriptor imageDescriptor = IconUtils.getImageDescriptor("icons/" + name+".gif");
		if(imageDescriptor!=null){
			return imageDescriptor.createImage();
		}
		return null;
	}
	@Override
	public String getText(Object element) {
		String label = "";
		if(element instanceof TopLevel){
			label = ((TopLevel) element).getTitle();
		}else if(element instanceof DisplayObject){
			label = LabelUtil.getDisplayObjectLabel((DisplayObject) element);
			
		}else if( element instanceof DisplayEdge){
			label = LabelUtil.getEdgeLabel((IEdge)((DisplayEdge) element).getElement());
		}
		return label;
	}

}
