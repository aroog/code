package oog.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewActionDelegate;

public class ShowDFEdgeAction extends PartialOOGViewActions implements
		IViewActionDelegate {

	@Override
	public void run(IAction action) {
		options.setShowUsageEdges(action.isChecked());
		super.run(action);
	};
}
