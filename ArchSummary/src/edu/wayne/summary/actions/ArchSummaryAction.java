package edu.wayne.summary.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ArchSummaryAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window = null;

	@Override
	public void run(IAction action) {
		OGraphSummary graphSummary = new OGraphSummary();
		graphSummary.init(this.window);
		graphSummary.run(action);
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}