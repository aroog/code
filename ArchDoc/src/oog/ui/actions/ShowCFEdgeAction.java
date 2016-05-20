package oog.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;

public class ShowCFEdgeAction extends PartialOOGViewActions implements
		IViewActionDelegate {

	@Override
	public void run(IAction action) {
		options.setShowControlFlowEdges(action.isChecked());
		super.run(action);

	}

}
