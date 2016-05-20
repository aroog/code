package oog.ui.actions;

import javax.swing.Icon;

import oog.Refreshable;
import oog.ui.utils.IconUtils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class RefreshAction extends Action {
	private Refreshable refreshable;
	public RefreshAction(Refreshable refreshableView) {
		this.refreshable = refreshableView;
		this.setText("Refresh");
		
		ImageDescriptor imageDescriptor = IconUtils.getImageDescriptor(IconUtils.IMG_REFRESH);
		
	    if(imageDescriptor != null){
	    	this.setImageDescriptor(imageDescriptor);
	    }
	}
	
	@Override
	public void run() {
		refreshable.refresh();
		super.run();
	}

}
