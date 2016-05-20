package oog.ui.content.wrappers;

public interface Info {
	/**
	 * 
	 * @return String label for info object
	 */
	String getLabel();

	/**
	 * 
	 * @return String title for this info object may return a empty String if
	 *         there is no title
	 */
	String getTitle();
}
