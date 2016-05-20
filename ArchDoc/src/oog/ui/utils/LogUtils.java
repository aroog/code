package oog.ui.utils;

import org.apache.log4j.Logger;

public class LogUtils {
	public static enum Mode{
		CLICK_POPUP_MENU("Clicked Popup Menu Item"),
		CLICK_TOOLBAR_MENU("Clicked Toolbar Menu Item"),
		DOUBLE_CLICK ("Double-Clicked"), 
		RIGHT_CLICK("Right Clicked"), 
		SELECTION_CHANGED("Selection Changed"), 
		COLLAPSE_TREE_ELEMENT("Collapsed Tree Element"),
		EXPAND_TREE_ELEMENT("Expanded Tree Element");
		
		
		private String display;
		Mode(String display){
			this.display = display;
		}
		public String getDisplay() {
			return display;
		}

	}

	public static void info(Logger log, String funcionality, Mode mode, String argument){
		log.info(funcionality+ " | " + mode.getDisplay() + " : " + argument);
	}
	
	

}
