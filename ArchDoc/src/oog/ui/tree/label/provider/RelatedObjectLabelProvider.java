package oog.ui.tree.label.provider;

import oog.itf.IEdge;
import oog.itf.IObject;
import oog.ui.tree.displaygraph.ArchitecturalLabelDecoratorBase;
import oog.ui.utils.IconUtils;
import oog.ui.utils.LabelUtil;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;


public class RelatedObjectLabelProvider extends ArchitecturalLabelDecoratorBase implements ILabelProvider, IColorProvider {
	

	@Override
	public String getText(Object element) {
		return LabelUtil.getRelatedObjectsTreeLabel(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IEdge) {
			return IconUtils.getImageDescriptor(IconUtils.IMG_EDGE_BASE)
					.createImage();
		} else if (element instanceof IObject) {
			return IconUtils.getImageDescriptor(IconUtils.IMG_OBJECT_BASE)
					.createImage();
		}
		return null;
	}

}
