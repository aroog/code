package oog.ui.actions;

import oog.ui.utils.ASTUtils;
import oog.ui.views.ObjectSearchView;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class GoToInstanceAction extends Action {
	String type;
	public GoToInstanceAction(String type) {
		super("Find Instances");
		this.type = type;
	}
	
	@Override
	public void run() {
		ObjectSearchView view;
		try {
			IWorkbenchPage activePage = ASTUtils.getActivePage();
			if(activePage!=null){
				view = (ObjectSearchView) activePage.showView(ObjectSearchView.ID);
				view.setType(this.type);
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		
		super.run();
	}
}
