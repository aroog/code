package oog.ui.tree.label.provider;

import oog.ui.content.wrappers.Info;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class ColumnInfoLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		String label = "";
		if(element instanceof Info){
			label = ((Info) element).getLabel();
		}
		return label;
	}
}
