package oog.ui.tree.displaygraph;

import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IObject;
import oog.ui.utils.LabelUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.wayne.ograph.ODomain;



public class ArchitecturalLabelProvider extends LabelProvider {
	// ImageRegistry takes care of disposing any image objects
	private static ImageRegistry imageCache = new ImageRegistry();

	public ArchitecturalLabelProvider() {
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
		Image image = imageCache.get(key);
		if (image == null) {
			ImageDescriptor descriptor = null;
			if (descriptor != null) {
				image = descriptor.createImage();
				imageCache.put(key, image);
			}
		}
		return image;
	}

	public String getText(Object element) {
		return LabelUtil.getDisplayElementLabel(element);
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}
}
