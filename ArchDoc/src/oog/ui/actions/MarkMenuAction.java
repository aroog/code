package oog.ui.actions;

import java.awt.MenuItem;

import oog.ui.utils.LogUtils.Mode;
import oog.ui.views.SummaryView;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

// NOT USED. DELETE
@Deprecated
public class MarkMenuAction implements IMenuCreator, IViewActionDelegate {
	
	private MenuManager menuMgr = new MenuManager();
	private Menu menu;

	@Override
	public void run(IAction action) {

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void init(IViewPart view) {

	}

	@Override
	public void dispose() {
		if (menu != null)
	    {
	      menu.dispose();
	    }
	}

	@Override
	public Menu getMenu(Control parent) {
		menu = menuMgr.createContextMenu(parent);
	    return menu;
	}

	@Override
	public Menu getMenu(Menu parent) {
		// Not use
		return null;
	}
	
	private Logger log = Logger.getLogger(SummaryView.class.getSimpleName());
	public void addActionToMenu(Action action) {
		menuMgr.add(new LoggedAction(action, log).setLogInfo(
				"Clear all marks", Mode.CLICK_POPUP_MENU, "Clear All Marks"));
	}

}
