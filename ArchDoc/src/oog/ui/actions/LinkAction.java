package oog.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class LinkAction extends Action {

	private boolean fLinkingEnabled;
	private List<Action> enabledList = new ArrayList<Action>(); 
	private List<Action> disabledList = new ArrayList<Action>(); 
public LinkAction(String text, boolean defaultCheked) {
		super(text, IAction.AS_CHECK_BOX);
		this.setChecked(defaultCheked);
		this.fLinkingEnabled = defaultCheked;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
	}
	@Override
	public void run() {
		fLinkingEnabled = !fLinkingEnabled;
		if(fLinkingEnabled){
			for (Action action : enabledList) {
				action.run();
			}
		}else{
			for (Action action : disabledList) {
				action.run();
			}
		}
		super.run();
	}
	
	public boolean isLinked(){
		return fLinkingEnabled;
	}
	public void addEnabledAction(Action action){
		enabledList.add(action);
	}
	public void addDisabledAction(Action action){
		disabledList.add(action);
	}
}
