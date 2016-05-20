package oog.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class HideAllAction extends PartialOOGViewActions implements IViewActionDelegate {

	@Override
	public void run(IAction action) {
		view.hideAll();
		super.run(action);

	}

}
