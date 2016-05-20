package oog.ui.actions;

import oog.ui.views.PartialOOG;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import edu.cmu.cs.viewer.objectgraphs.VisualReportOptions;

public abstract class PartialOOGViewActions implements IViewActionDelegate {
	protected PartialOOG view;
	protected VisualReportOptions options = VisualReportOptions.getInstance();
	public void init(IViewPart view) {
		this.view = (PartialOOG) view;
	}

	public void run(IAction action){
		this.view.refresh();
	}

	public void selectionChanged(IAction action, ISelection selection) {}
}
