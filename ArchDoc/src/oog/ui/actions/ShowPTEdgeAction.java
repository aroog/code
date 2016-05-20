package oog.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewActionDelegate;

public class ShowPTEdgeAction extends PartialOOGViewActions implements
		IViewActionDelegate {

	@Override
	public void run(IAction action) {
		options.setShowReferenceEdges(action.isChecked());
		super.run(action);

	}

}
