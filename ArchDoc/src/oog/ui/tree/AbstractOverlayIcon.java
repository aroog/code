package oog.ui.tree;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public abstract class AbstractOverlayIcon extends CompositeImageDescriptor {

	static final int DEFAULT_WIDTH = 16;

	static final int DEFAULT_HEIGHT = 16;

	private Point fSize = null;

	private ImageDescriptor fOverlays[][];

	public AbstractOverlayIcon(ImageDescriptor[][] overlays) {
		this(overlays, null);
	}

	public AbstractOverlayIcon(ImageDescriptor[][] overlays, Point size) {
		fOverlays = overlays;
		if (size != null) {
			fSize = size;
		}
		else {
			fSize = new Point(AbstractOverlayIcon.DEFAULT_WIDTH, AbstractOverlayIcon.DEFAULT_HEIGHT);
		}
	}

	protected void drawBottomLeft(ImageDescriptor[] overlays) {
		if (overlays == null) {
			return;
		}
		int length = overlays.length;
		int x = 0;
		for (int i = 0; i < 3; i++) {
			if ((i < length) && (overlays[i] != null)) {
				ImageData id = overlays[i].getImageData();
				drawImage(id, x, getSize().y - id.height);
				x += id.width;
			}
		}
	}

	protected void drawBottomRight(ImageDescriptor[] overlays) {
		if (overlays == null) {
			return;
		}
		int length = overlays.length;
		int x = getSize().x;
		for (int i = 2; i >= 0; i--) {
			if ((i < length) && (overlays[i] != null)) {
				ImageData id = overlays[i].getImageData();
				x -= id.width;
				drawImage(id, x, getSize().y - id.height);
			}
		}
	}

	protected abstract ImageData getBaseImageData();

	protected void drawCompositeImage(int width, int height) {
		ImageData base = getBaseImageData();
		drawImage(base, 0, 0);
		if (fOverlays != null) {
			if (fOverlays.length > 0) {
				drawTopRight(fOverlays[0]);
			}

			if (fOverlays.length > 1) {
				drawBottomRight(fOverlays[1]);
			}

			if (fOverlays.length > 2) {
				drawBottomLeft(fOverlays[2]);
			}

			if (fOverlays.length > 3) {
				drawTopLeft(fOverlays[3]);
			}
		}
	}

	protected void drawTopLeft(ImageDescriptor[] overlays) {
		if (overlays == null) {
			return;
		}
		int length = overlays.length;
		int x = 0;
		for (int i = 0; i < 3; i++) {
			if ((i < length) && (overlays[i] != null)) {
				ImageData id = overlays[i].getImageData();
				drawImage(id, x, 0);
				x += id.width;
			}
		}
	}

	protected void drawTopRight(ImageDescriptor[] overlays) {
		if (overlays == null) {
			return;
		}
		int length = overlays.length;
		int x = getSize().x;
		for (int i = 2; i >= 0; i--) {
			if ((i < length) && (overlays[i] != null)) {
				ImageData id = overlays[i].getImageData();
				x -= id.width;
				drawImage(id, x, 0);
			}
		}
	}

	protected Point getSize() {
		return fSize;
	}
}
