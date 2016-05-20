package oog.ui.actions;

import oog.itf.IElement;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;

public class StackAction extends Action {
	private TreeViewer treeViewer;
	private IElement element;

	public StackAction(IElement obj,
			TreeViewer viewer) {
		super("Stack Trace");
		this.treeViewer = viewer;
		this.element = obj;
	}

	public void run() {

		if(element!=null){
			treeViewer.setInput(element.getPath());
		}

	}
}
