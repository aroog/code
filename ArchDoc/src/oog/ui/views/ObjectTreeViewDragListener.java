package oog.ui.views;

import java.util.Iterator;
import java.util.Set;

import oog.itf.IObject;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

import ast.ObjectTraceability;
import edu.wayne.ograph.ODomain;
import edu.wayne.ograph.OObject;

public class ObjectTreeViewDragListener implements DragSourceListener {

	private final TreeViewer viewer;
	
	private ObjectTreeView objectTreeView;
	
	public ObjectTreeViewDragListener(TreeViewer viewer, ObjectTreeView objectTreeView) {
		this.viewer = viewer;
		this.objectTreeView = objectTreeView;
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		System.out.println("Finished Drag");
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		// Here you do the conversion to the type which is expected.
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

		//IElement firstElement = (IElement) selection.getFirstElement();

		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			Object firstElement = selection.getFirstElement();
			event.data = firstElement.toString();
		}
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection instanceof TreeSelection)
		{
			TreeSelection treeSelection = (TreeSelection) selection;
			Object selectionType = treeSelection.getFirstElement();
			if (selectionType instanceof ObjectTraceability) {
				TreePath[] pathsFor = treeSelection.getPaths();
				if (pathsFor != null && pathsFor.length > 0) {
					TreePath treePath = pathsFor[0];
					int segmentCount = treePath.getSegmentCount();
					if (segmentCount > 2) {
						TreePath expressions = treePath.getParentPath();
						TreePath objects = expressions.getParentPath();
						Object lastSegment = objects.getLastSegment();
						if (lastSegment instanceof IObject) {
							objectTreeView.setParentObject((IObject) lastSegment);
						}
					}
				}
				System.out.println("Start Drag");
			}
			else if(selectionType instanceof ODomain)
			{
				event.doit = false;
			}
			else if (selectionType instanceof OObject)
			{
				Object object = selectionType;
				
				boolean isDomain = false;
				boolean isUniqueOrLent = false;
				while(object != null)
				{
					if (isDomain)
					{
						Set<IObject> parents = ((ODomain) object).getParents();
						Iterator<IObject> iterator = parents.iterator();
						OObject current = null;
						while(iterator.hasNext())
							current = (OObject) iterator.next();
						object = current;
						isDomain = false;
					}
					else
					{
						ODomain parent = (ODomain) ((OObject) object).getParent();
						isDomain = true;
						if(parent != null && (parent.getD().contains("DLENT") || parent.getD().contains("DUNIQUE")))
						{
							isUniqueOrLent = true;
							break;
						}
						object = parent;
					}
				}
				if(isUniqueOrLent) // cannot drag out of unique or lent
					event.doit = false;
				System.out.println("Start Drag");
			}
		}
	}

}