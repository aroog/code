package edu.cmu.cs.viewer.objectgraphs.views;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import oog.itf.IObject;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.cmu.cs.viewer.ui.ITreeElement;

public class MakeVisibleAction extends ShowAction {

	public MakeVisibleAction() {
		super("Visible");
	}

	@Override
	public void run() {

		// TODO: HIGH. Need to handle selected IElement
		// Also, need to lookup DisplayElement from RuntimeElement
		// TODO: HIGH. Cannot use ShowAction method anymore...
		//for(ITreeElement element : ShowAction.getSelectedElement(treeViewer) ) {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredselection = (IStructuredSelection) selection;

			Iterator iterator = structuredselection.iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();

				if (element instanceof DisplayObject) {
					toggleVisible((DisplayObject) element);
				}
				else if (element instanceof IObject) {
					if (displayModel != null) {
						Set<DisplayObject> set = displayModel.getDisplayObject((IObject) element);
						if (set != null && set.size() > 0)
							for (DisplayObject displayObject : set) {
								toggleVisible(displayObject);
							}
					}
				}
			}
		}
		
		imageRefresh.updateDisplay();
	}

	private void toggleVisible( DisplayObject displayObject) {
	    displayObject.setVisible(!displayObject.isVisible());
    }

}
