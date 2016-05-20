package oog.ui.viewer;

import org.eclipse.swt.internal.SWTEventListener;

public interface ImageAnalyzerSelectionListener extends SWTEventListener {

	/**
	 * Sent when the user clicks on the close button of an item in the CTabFolder. The item being closed is specified in
	 * the event.item field. Setting the event.doit field to false will stop the CTabItem from closing. When the
	 * CTabItem is closed, it is disposed. The contents of the CTabItem (see CTabItem#setControl) will be made not
	 * visible when the CTabItem is closed.
	 * 
	 * @param event an event indicating the item being closed
	 */
	public void select(ImageAnalyzerSelectionEvent event);
}
