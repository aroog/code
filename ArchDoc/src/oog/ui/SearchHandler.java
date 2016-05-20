package oog.ui;

import oog.ui.utils.ASTUtils;
import oog.ui.utils.LogUtils;
import oog.ui.utils.LogUtils.Mode;
import oog.ui.views.ObjectSearchView;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.views.AbstractView;

public class SearchHandler extends AbstractHandler {
	private Logger log = Logger.getLogger(SearchHandler.class.getSimpleName());
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ObjectSearchView view = (ObjectSearchView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().showView(ObjectSearchView.ID);
			
			String qualifedName = ASTUtils.getTypeOfOpenJavaEditor().getFullyQualifiedName();
			LogUtils.info(log, "Find Instances of Open Type", Mode.CLICK_POPUP_MENU, qualifedName);
			view.setType(qualifedName);
			
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}



}
