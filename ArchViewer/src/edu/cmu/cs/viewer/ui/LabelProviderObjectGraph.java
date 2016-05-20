package edu.cmu.cs.viewer.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.FilteredTree;

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
			ImageDescriptor descriptor = null;// ArchJavaTranslatorPlugin.getImageDescriptor(key);
			if (descriptor != null) {
				image = descriptor.createImage();
				LabelProviderObjectGraph.imageCache.put(key, image);
			}
		}
		return image;
	}

	public String getText(Object element) {
		String label = "";

		if (element instanceof ITreeElement) {
			ITreeElement treeElement = (ITreeElement) element;
			label = treeElement.getLabel();
		}

		return label;
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}
}
