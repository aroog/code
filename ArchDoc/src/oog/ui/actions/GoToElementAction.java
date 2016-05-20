package oog.ui.actions;

import oog.itf.IElement;
import oog.itf.IObject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayElement;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;

public class GoToElementAction  extends Action{
	
	private TreeViewer viewer;
	private IElement iobject;
	private DisplayElement dobject;
	
	public GoToElementAction(TreeViewer viewer, IElement object){
		this.viewer = viewer;
		this.iobject = object;
		
	}
	
	public GoToElementAction(TreeViewer viewer, DisplayElement object) {
		this.viewer = viewer;
		this.dobject = object;
	}

	Object getObject(){
		if(iobject!=null)
			return iobject;
		if(dobject!=null)
			return dobject;
		return null;
	}
	@Override
	public void run() {
		//Minor Fix, expands all elements so that we can find it 
		//TODO: hackish find a better way to do this
		//viewer.expandAll();
		viewer.setSelection(new StructuredSelection(getObject()), true);
		
		// TODO: Why do we need all this?
		// TreeItem[] selection = viewer.getTree().getSelection();
		// if(selection!=null && selection.length>0 ){
		// viewer.getTree().showItem(selection[0]);
		// viewer.getTree().setFocus();
		// }
		
		super.run();
	}

}
