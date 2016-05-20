package edu.cmu.cs.viewer.objectgraphs.views;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import oog.itf.IObject;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayDomain;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.cmu.cs.viewer.ui.ITreeElement;

public class ShowInternalsAction extends ShowAction {

	public ShowInternalsAction() {
		super("Show Internals");
	}

	@Override
	public void run() {

		// TODO: HIGH. Cannot use ShowAction method anymore...
		//for(ITreeElement element : ShowAction.getSelectedElement(treeViewer) ) {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredselection = (IStructuredSelection) selection;

			Iterator iterator = structuredselection.iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof DisplayObject) {
					DisplayObject displayObject = (DisplayObject) element;
					toggleShowInternals(displayObject);
				}
				else if (element instanceof DisplayDomain) {
					DisplayDomain displayDomain = (DisplayDomain) element;
					toggleShowInternals(displayDomain);
				}
				else if (element instanceof IObject) {
					if (displayModel != null) {
						Set<DisplayObject> set = displayModel.getDisplayObject((IObject) element);
						if (set != null && set.size() > 0)
							for (DisplayObject displayObject : set) {
								toggleShowInternals(displayObject);
							}
					}
				}
			}
		}

		if (imageRefresh != null ) {
			imageRefresh.updateDisplay();
		}
	}

	private void toggleShowInternals(DisplayDomain displayDomain) {
	    displayDomain.setShowInternals(!displayDomain.isShowInternals());
    }

	private void toggleShowInternals(DisplayObject displayObject) {
	    displayObject.setShowInternals(!displayObject.isShowInternals());
    }

}
