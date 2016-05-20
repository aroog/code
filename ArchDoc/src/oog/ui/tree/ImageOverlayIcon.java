package oog.ui.tree;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public class ImageOverlayIcon extends AbstractOverlayIcon {
	private Image base;

	public ImageOverlayIcon(Image base, ImageDescriptor[][] overlays) {
		this(base, overlays, null);
	}

	public ImageOverlayIcon(Image base, ImageDescriptor[][] overlays, Point size) {
		super(overlays, size);
		this.base = base;

		if (base == null) {
			throw new IllegalArgumentException("Cannot accept null base image");
		}
	}

	protected ImageData getBaseImageData() {
		return base.getImageData();
	}
}