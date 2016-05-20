package oog.ui.tree;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ViewerFilterObjectGraph extends ViewerFilter {

	public ViewerFilterObjectGraph() {
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return true;
	}
}
