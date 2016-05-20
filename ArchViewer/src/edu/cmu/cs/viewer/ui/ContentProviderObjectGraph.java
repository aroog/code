package edu.cmu.cs.viewer.ui;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class ContentProviderObjectGraph implements ITreeContentProvider, Observer {
	private static Object[] EMPTY_ARRAY = new Object[0];

	protected TreeViewer viewer;

	public void dispose() {
	}

	/**
	 * Notifies this content provider that the given viewer's input has been switched to a different element.
	 * <p>
	 * A typical use for this method is registering the content provider as a listener to changes on the new input
	 * (using model-specific means), and deregistering the viewer from the old input. In response to these change
	 * notifications, the content provider propagates the changes to the viewer.
	 * </p>
	 * 
	 * @param viewer the viewer
	 * @param oldInput the old input element, or <code>null</code> if the viewer did not previously have an input
	 * @param newInput the new input element, or <code>null</code> if the viewer does not have an input
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Note: We assume that it is the responsibility of each domain object to recursively
		// call addObserver(...) or deleteObserver(...) on any of its owned children
		this.viewer = (TreeViewer) viewer;

		if (oldInput instanceof Observable) {
			Observable observableOldInput = (Observable) oldInput;
			observableOldInput.deleteObserver(this);
		}
		if (newInput instanceof Observable) {
			Observable observableNewInput = (Observable) newInput;
			observableNewInput.addObserver(this);
		}
	}

	public Object[] getChildren(Object parentElement) {
		Object[] array = ContentProviderObjectGraph.EMPTY_ARRAY;

		if (parentElement instanceof ITreeElement) {
			ITreeElement treeElement = (ITreeElement) parentElement;
			array = treeElement.getChildren();
		}
		else if ( parentElement instanceof Set) {
			Set<ITreeElement> set = (Set<ITreeElement>)parentElement;
			array = set.toArray(new ITreeElement[0]);
		}
		return array;
	}

	public Object getParent(Object element) {
		Object parent = null;
		if (element instanceof ITreeElement) {
			ITreeElement treeElement = (ITreeElement) element;
			parent = treeElement.getParent();
		}

		return parent;
	}

	// Remark: hasChildren is not implemented in terms of getChildren()
	// since it is probably more efficient this way
	public boolean hasChildren(Object element) {
		boolean hasChildren = true;
		if (element instanceof ITreeElement) {
			ITreeElement treeElement = (ITreeElement) element;
			hasChildren = treeElement.hasChildren();
		}
		return hasChildren;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void update(Observable arg0, Object arg1) {
		if (viewer != null) {
			viewer.refresh(arg0);
		}
	}
}
