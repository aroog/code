package oog.ui;

import oog.ui.utils.LogUtils;
import oog.ui.utils.LogUtils.Mode;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;

public abstract class SimpleLoggedTreeViewListener implements ITreeViewerListener{

	private Logger log;
	private String treeName;

	public SimpleLoggedTreeViewListener(Logger log, String treeName) {
		this.log = log;
		this.treeName = treeName;
	}
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		LogUtils.info(log, treeName , Mode.COLLAPSE_TREE_ELEMENT, getLabel(event.getElement()));
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		LogUtils.info(log, treeName , Mode.EXPAND_TREE_ELEMENT, getLabel(event.getElement()));
	
	}
	
	public abstract String getLabel(Object obj);

}
