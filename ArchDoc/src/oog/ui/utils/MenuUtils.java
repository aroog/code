package oog.ui.utils;

import java.util.Set;

import oog.ui.actions.LoggedAction;
import oog.ui.actions.TraceAction;
import oog.ui.utils.LogUtils.Mode;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import ast.BaseTraceability;

import util.TraceabilityList;

public class MenuUtils {

	public static void setTraceToCodeMenu(IMenuManager manager, Set<BaseTraceability> set, Logger log) {
		if(set!=null){
		MenuManager subMenu = new MenuManager("Trace to Code", null);
		for(BaseTraceability traceEntry: set){
			final TraceAction action = new TraceAction(traceEntry);
			subMenu.add(new LoggedAction(action, log).setLogInfo
					("Trace-to-Code", Mode.CLICK_POPUP_MENU, action.getText()));
		}
		manager.add(subMenu);
		}
	}

}
