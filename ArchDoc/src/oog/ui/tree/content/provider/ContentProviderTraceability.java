package oog.ui.tree.content.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import oog.ui.content.wrappers.TraceabilityNode;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import util.TraceabilityEntry;
import util.TraceabilityList;
import util.TraceabilityListSet;


public class ContentProviderTraceability implements ITreeContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];

	protected TreeViewer viewer;
	private TraceabilityListSet model;

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
		
		model = (TraceabilityListSet) newInput;

	}

	public Object[] getChildren(Object parentElement) {
		Object[] array = ContentProviderTraceability.EMPTY_ARRAY;
		if(parentElement instanceof TraceabilityListSet){
			return ((TraceabilityListSet) parentElement).getSetOfLists().toArray();
		}else if(parentElement instanceof TraceabilityList){
			
			List<TraceabilityEntry> rawList = new ArrayList<TraceabilityEntry>( ((TraceabilityList) parentElement).getRawList());

			//TraceabilityStack createStack = TraceabilityStack.createStack(rawList);
			Collections.reverse(rawList);
			TraceabilityNode head = TraceabilityNode.createNodeList(rawList);
			
			return head.getNext().toArray();
		}else if(parentElement instanceof TraceabilityNode){
			TraceabilityNode next = ((TraceabilityNode) parentElement).getNext();
			if(next!= null){
				return next.toArray(); 
			}
		}

	
		return array;
	}

	public Object getParent(Object element) {
		return null;
	}

	// Remark: hasChildren is not implemented in terms of getChildren()
	// since it is probably more efficient this way
	public boolean hasChildren(Object element) {
		boolean hasChildren = true;
	 if (element instanceof TraceabilityList) {
			TraceabilityList treeElement = (TraceabilityList) element;
			hasChildren = !treeElement.getRawList().isEmpty();
	 }
	
		return hasChildren;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}


}
