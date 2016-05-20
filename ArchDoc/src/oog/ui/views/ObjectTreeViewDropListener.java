package oog.ui.views;

import java.util.Iterator;
import java.util.Set;

import oog.common.OGraphFacade;
import oog.itf.IDomain;
import oog.itf.IObject;
import oog.re.PushIntoOwned;
import oog.re.PushIntoPD;
import oog.re.PushIntoParam;
import oog.re.Refinement;
import oog.re.RefinementModel;
import oog.re.SplitUp;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

import ast.BaseTraceability;
import ast.ObjectTraceability;
import edu.wayne.ograph.ODomain;
import edu.wayne.ograph.OObject;

public class ObjectTreeViewDropListener extends ViewerDropAdapter {

	private static final String OWNED = "owned";
	private static final String PARAM = "PARAM";
	private static final String PD = "PD";
	
	private final TreeViewer viewer;
	private Object source;
	private Object target;
	
	public RefinementModel refinementModel;
	
	private TableViewer tableViewer;
	private ObjectTreeView objectTreeView;

	public ObjectTreeViewDropListener(TreeViewer viewer, TableViewer tableViewer, ObjectTreeView objectTreeView) {
		super(viewer);
		this.viewer = viewer;
		this.tableViewer = tableViewer;
		this.objectTreeView = objectTreeView;
		
		// Get the refinement model once
		OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
		refinementModel = facade.getRefinementModel();
	}
	
	@Override
	public void drop(DropTargetEvent event) {
		int location = this.determineLocation(event);
		
		source = getSelectedObject();
		target = determineTarget(event);
		
		String translatedLocation = "";
		switch (location) {
		case ViewerDropAdapter.LOCATION_BEFORE:
			translatedLocation = "Dropped before the target ";
			break;
		case ViewerDropAdapter.LOCATION_AFTER:
			translatedLocation = "Dropped after the target ";
			break;
		case ViewerDropAdapter.LOCATION_ON:
			translatedLocation = "Dropped on the target ";
			break;
		case ViewerDropAdapter.LOCATION_NONE:
			translatedLocation = "Dropped into nothing ";
			break;
		}
		System.out.println(translatedLocation);
		System.out.println("The drop was done on the element: " + target);
		super.drop(event);
	}


	// This method performs the actual drop
	// We simply add the object we receive to the model and trigger a refresh 
	// (if using a viewer, have to calling its setInput method)
	@Override
	public boolean performDrop(Object data) {
		// DONE. Update the refinement model
		// DONE. Create the right object here...
		// DONE. Read data being dragged.
		
		Refinement ref = null;
		
		if (!(target instanceof ODomain)) // can't drag into ObjectTraceabililty or OObject 
		{
			return false;
		}
		else if (source instanceof ObjectTraceability)
		{
			ODomain tgtDomain = (ODomain) target;
			String domainName = tgtDomain.getD();
			
			if(domainName.equals(PARAM))
				return false;
			else if (domainName.equals(OWNED))
				domainName = OWNED;
			else
				domainName = PD;
			
			IObject destObject = getParent(tgtDomain);
			
			// Obtain the parent IObject of the BaseTraceability source object
			IObject srcObject = objectTreeView.getParentObject();
			ref = new SplitUp(srcObject, destObject, domainName);
			MiniAstToEclipseAST.populateSplitUp((BaseTraceability) source, (SplitUp) ref);
			
		}
		else if(source instanceof OObject) 
		{ 
			OObject srcObject = (OObject) source;
			ODomain tgtDomain = (ODomain) target;
			String domainName = tgtDomain.getD();
			// XXX. TOMAR: investigate:
			// 
			if (srcObject.getChildren().contains(target)) 
			{
				ref = null;		
			}
			else if(domainName.equals(OWNED))
			{
				ref = new PushIntoOwned(srcObject, getParent(tgtDomain), domainName);
			}
			else if(domainName.equals(PARAM))
			{
				ref = new PushIntoParam(srcObject, getParent(tgtDomain), domainName);
			}
			else if(domainName.contains("PD"))
			{
				ref = new PushIntoPD(srcObject, getParent(tgtDomain), "this.PD");
			}
			else if(domainName.contains("SHARED")) {				
				ref = new PushIntoPD(srcObject, getParent(tgtDomain), "shared");
			}
		}
		
		if (ref != null)
		{
			refinementModel.add(ref);
			tableViewer.refresh(true);
			
			// At this point, need to run OOGRE, re-gen the graph, then re-load the tree from the updated graph
			return true;
		}
		return false;
	}

	// XXX. Getting the first parent
	private IObject getParent(ODomain tgtDomain) {
		IObject firstParent = null;
		Set<IObject> parents = tgtDomain.getParents();
		Iterator<IObject> it  = parents.iterator();
		while(it.hasNext()) {
			 firstParent = it.next();
			 break;
		}
		return firstParent;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return true;
	}
}
