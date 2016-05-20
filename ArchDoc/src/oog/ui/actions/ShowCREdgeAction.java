package oog.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewActionDelegate;

public class ShowCREdgeAction extends PartialOOGViewActions implements	IViewActionDelegate {


	@Override
	public void run(IAction action) {
		options.setShowCreationEdges(action.isChecked());
		super.run(action);
	}


}
