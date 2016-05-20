package oog.ui.actions;

import oog.itf.IElement;
import oog.ui.views.AbstractStackView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayElement;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;

public class GoToStackAction extends Action {
	
	private TreeViewer viewer;
	public GoToStackAction(TreeViewer viewer) {
		this.viewer = viewer;
		this.setText("Abstract Stack Trace");
	}

	@Override
	public void run() {
		try {
			AbstractStackView showView = (AbstractStackView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(AbstractStackView.ID);
			ISelection selection = viewer.getSelection();
			Object obj = ((IStructuredSelection) selection)
					.getFirstElement();
			if (obj instanceof IElement) {
				showView.setTraceObject(((IElement)obj).getPath());
			}
			if(obj instanceof DisplayElement){
				
				IElement element = ((DisplayElement) obj).getElement();
				if(element!=null)
				showView.setTraceObject(element.getPath());
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		super.run();
	}
	

}
