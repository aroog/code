package oog.ui.actions;
import oog.ui.utils.LogUtils;
import oog.ui.utils.LogUtils.Mode;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.internal.ole.win32.FUNCDESC;
public class LoggedAction extends Action{

	private Action action;
	private Logger log;
	private String functionality;
	private Mode mode;
	private String arg;
	public LoggedAction(Action action, Logger log) {
		super(action.getText(), action.getStyle());
		setImageDescriptor(action.getImageDescriptor());
		setChecked(action.isChecked());
		this.action = action;
		this.log = log;
	}
	@Override
	public void run() {
		LogUtils.info(log, functionality, mode, arg);
		action.run();
		super.run();
	}

	public Action setLogInfo(String functionality, Mode mode, String arg){
		this.functionality = functionality;
		this.mode = mode;
		this.arg = arg;
		return this;
	}
	

	

}
