package oog.ui.tree.content.provider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IElement;
import oog.itf.IObject;
import oog.ui.RuntimeModel;
import oog.ui.content.wrappers.DisplayGraphInfoWrapper;
import oog.ui.content.wrappers.DisplayGraphInfoWrapper.TopLevel;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayEdge;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.wayne.summary.strategies.Utils;

public class ContentProviderOOGSearch implements ITreeContentProvider {

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	private Set<DisplayEdge> getEdgesWithType(
			String inputElement) {
		Set<DisplayEdge> edges = RuntimeModel.getInstance().getDisplayModel().getEdges();
		Set<DisplayEdge> retEdges = new HashSet<DisplayEdge>();
		for(DisplayEdge edge: edges){
			DisplayObject toObject = edge.getToObject();
			DisplayObject fromObject = edge.getFromObject();
			String toTypeDisplayName = toObject.getTypeDisplayName();
			String fromTypeDisplayName = fromObject.getTypeDisplayName();
			String simpleName = Signature.getSimpleName(inputElement);
			if(toTypeDisplayName.contentEquals(simpleName)||
					fromTypeDisplayName.contentEquals(simpleName)){
				retEdges.add(edge);
			}
		}
		return retEdges;
	}

	private Set<DisplayObject> getInstancesOfType(String inputElement) {
		Set<DisplayObject> retObj = new HashSet<DisplayObject>();
		Collection<DisplayObject> objects = RuntimeModel.getInstance()
				.getDisplayModel().getObjects();
		for (DisplayObject obj : objects) {
			IElement element = obj.getElement();
			if (element instanceof IObject) {
				String fullyQualifiedName = ((IObject) element).getC()
						.getFullyQualifiedName();

				if (fullyQualifiedName.contentEquals(inputElement)
						|| Utils.isSubtypeCompatible(fullyQualifiedName,
								inputElement)) {

					retObj.add(obj);
				}
			}
		}
		return retObj;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] array = new Object[0];
		if (parentElement instanceof String) {
			Set<DisplayEdge> edges = getEdgesWithType((String)parentElement);
			Set<DisplayObject> objects = getInstancesOfType((String) parentElement);

			array = new DisplayGraphInfoWrapper(objects, edges).toArray();
		}else if (parentElement instanceof TopLevel){
			array = ((TopLevel) parentElement).getChildren();
		}
		return array;
	}

	@Override
	public Object getParent(Object element) {
		Object parent = null;
		if (element instanceof DisplayObject) {
			DisplayObject treeElement = (DisplayObject) element;
			parent = treeElement.getParent();
		}
		return parent;
	}

	@Override
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;
		if(element instanceof TopLevel){
			hasChildren = true;
		}
		return hasChildren;
	}

}
