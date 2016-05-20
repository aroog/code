package oog.ui.tree;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IElement;
import oog.itf.IObject;
import oog.ui.content.wrappers.OGraphInfoWrapper;
import oog.ui.content.wrappers.OGraphInfoWrapper.TopLevel;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import ast.Type;

import edu.wayne.ograph.ODomain;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OObject;


public class ContentProviderObjectGraph implements ITreeContentProvider, Observer {
	private static Object[] EMPTY_ARRAY = new Object[0];
	private static ArrayList<ODomain> addedDomains = new ArrayList<ODomain>();

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

		if (parentElement instanceof OGraph) {
			OGraph treeElement = (OGraph) parentElement;
			array = new OGraphInfoWrapper(treeElement).toArray();
		}else if(parentElement instanceof TopLevel){
			array = ((TopLevel) parentElement).getChildren();
		}else if(parentElement instanceof IObject){
			IObject treeElement = (IObject) parentElement;
			
			createDomain(treeElement, "PARAM");
			
			ArrayList<Object> o = new ArrayList<Object>();
			o.addAll(treeElement.getChildren());
			o.add(new TopLevel(treeElement.getTraceability().toArray(), "Expressions"));
			array = o.toArray();
		}else if(parentElement instanceof IDomain){
			IDomain treeElement = (IDomain) parentElement;
			array = treeElement.getChildren().toArray();
		}else if (parentElement instanceof Set) {
			System.err.println(parentElement.toString());
			Set<IElement> set = (Set<IElement>) parentElement;
			array = set.toArray(new IElement[0]);
		}
		else{
			//System.err.println(parentElement.getClass().getSimpleName());
		}
		return array;
	}

	/**
	 * Use to create a placeholder domain (called "PARAM") to add to the object's sub-tree, to support drag-and-drop into a PARAM
	 * 
	 * @param param XXX. Move this static method to a Utils class.
	 * 
	 * @return Retrue true if the domain did not exist and got created
	 */
	public static boolean createDomain(IObject parent, String param) {

		boolean create = shouldCreateDomain(parent, param);
		if (create) {
			String Did = parent.getO_id() + param;
			Type C = parent.getC();
			IDomain oDomain = new ODomain(Did, C, param);
			((ODomain)oDomain).setPublic(true);
			
			// XXX. Hackish cast
			((OObject) parent).addDomain((ODomain) oDomain);
			addedDomains.add((ODomain) oDomain);
			return true;
			
		}
		return false;
	}
	
	public static boolean shouldCreateDomain(IObject parent, String param)
	{
		IDomain oDomain = null;
		
		Set<IDomain> children = parent.getChildren();
		for (IDomain child : children) {
			if (child.getD().equals(param)) {
				oDomain = child;
				break;
			}
		}
		
		if (oDomain == null)
		{
			return true;
		}
		else
			return false;
	}
	
	public static ArrayList<ODomain> getAddedDomains()
	{
		return addedDomains;
	}

	public Object getParent(Object element) {
		Object parent = null;
		if (element instanceof IObject) {
			IObject treeElement = (IObject) element;
			parent = treeElement.getParent();
		}
		return parent;
	}

	// Remark: hasChildren is not implemented in terms of getChildren()
	// since it is probably more efficient this way
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;
		if (element instanceof IObject) {
			IObject treeElement = (IObject) element;
			hasChildren = treeElement.hasChildren();
		}else if(element instanceof IDomain){
			IDomain treeElement = (IDomain) element;
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
