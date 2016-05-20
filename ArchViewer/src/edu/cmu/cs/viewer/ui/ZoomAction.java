package edu.cmu.cs.viewer.ui;

import org.eclipse.jface.action.Action;

public class ZoomAction extends Action {

	private float factor = 1.0f;

	private IImageZoom imageZoom = null;

	public float getFactor() {
		return factor;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}

	@Override
	public void run() {
		imageZoom.scaleXY(factor, factor);
	}

	public IImageZoom getImageZoom() {
		return imageZoom;
	}

	public void setImageZoom(IImageZoom imageZoom) {
		this.imageZoom = imageZoom;
	}

}
