package oog.logging;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class LogStopAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window = null;

	@Override
	public void run(IAction action) {
		if (LogWriter.LogState == 1) {
			LogWriter.LogState = 0;
			LogWriter.OrderNum = 1;
			LogWriter.newTypes.clear();
			LogWriter.Reason = "";
			LogWriter.CheckedTypes.clear();
			LogWriter.CheckedInterfaces.clear();
			
			LogWriter.writeEmptyLines();
		}
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}
