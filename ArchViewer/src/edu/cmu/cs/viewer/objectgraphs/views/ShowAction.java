package edu.cmu.cs.viewer.objectgraphs.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.cmu.cs.viewer.ui.ITreeElement;

public abstract class ShowAction extends Action {

	protected TreeViewer treeViewer;

	protected DisplayModel displayModel;

	protected IImageRefresh imageRefresh;

	public ShowAction() {
	}

	public ShowAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public ShowAction(String text, int style) {
		super(text, style);
	}

	public ShowAction(String text) {
		super(text);
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void setTreeViewer(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	public static ITreeElement[] getSelectedElement(TreeViewer tv) {
		List<ITreeElement> list = new ArrayList<ITreeElement>();

		ISelection selection = tv.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredselection = (IStructuredSelection) selection;

			Iterator iterator = structuredselection.iterator();
			while(iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof ITreeElement) {
					list.add((ITreeElement)object);
				}
			}
		}

		return list.toArray( new ITreeElement[0]);
	}

	public IImageRefresh getImageRefresh() {
		return imageRefresh;
	}

	public void setImageRefresh(IImageRefresh imageRefresh) {
		this.imageRefresh = imageRefresh;
	}

	public DisplayModel getDisplayModel() {
    	return displayModel;
    }

	public void setDisplayModel(DisplayModel displayModel) {
    	this.displayModel = displayModel;
    }

}
