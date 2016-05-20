package oog.ui.tree.content.provider;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;


import oog.itf.IElement;

import oog.ui.content.wrappers.DisplayGraphInfoWrapper;

import oog.ui.content.wrappers.DisplayGraphInfoWrapper.TopLevel;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayDomain;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayGraph;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.wayne.ograph.OGraph;


public class ContentProviderDisplayGraph implements ITreeContentProvider, Observer {
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
		Object[] array = ContentProviderDisplayGraph.EMPTY_ARRAY;

		if (parentElement instanceof DisplayModel) {
			
			DisplayModel treeElement = (DisplayModel) parentElement;
			
			array = new DisplayGraphInfoWrapper(treeElement).toArray();
		}else if(parentElement instanceof TopLevel){
			array = ((TopLevel) parentElement).getChildren();
		}else if(parentElement instanceof DisplayObject){
			DisplayObject treeElement = (DisplayObject) parentElement;
			array = treeElement.getChildren();
		}else if(parentElement instanceof DisplayDomain){
			DisplayDomain treeElement = (DisplayDomain) parentElement;
			array = treeElement.getChildren();
		}else if (parentElement instanceof Set) {
			System.err.println(parentElement.toString());
			Set<IElement> set = (Set<IElement>) parentElement;
			array = set.toArray(new IElement[0]);
		}else{
			//System.err.println(parentElement.getClass().getSimpleName());
		}
		return array;
	}

	public Object getParent(Object element) {
		Object parent = null;
		if (element instanceof DisplayObject) {
			DisplayObject treeElement = (DisplayObject) element;
			parent = treeElement.getParent();
		}
		return parent;
	}

	// Remark: hasChildren is not implemented in terms of getChildren()
	// since it is probably more efficient this way
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;
		if (element instanceof DisplayObject) {
			DisplayObject treeElement = (DisplayObject) element;
			hasChildren = treeElement.hasChildren();
		}else if(element instanceof DisplayDomain){
			DisplayDomain treeElement = (DisplayDomain) element;
			hasChildren = treeElement.hasChildren();
		}else if(element instanceof TopLevel){
			hasChildren = true;
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
