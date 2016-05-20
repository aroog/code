package oog.ui.actions;

import oog.common.OGraphFacade;
import oog.itf.IDomain;
import oog.itf.IObject;
import oog.re.CreateDomain;
import oog.re.RefinementModel;
import oog.ui.tree.ContentProviderObjectGraph;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import edu.wayne.ograph.ODomain;

public class CreateDomainAction extends  Action {
	
	private IObject oObject = null;
	
	private RefinementModel refModel;

	private TreeViewer treeViewer;

	private TableViewer tableViewer;
	
	private boolean isPublic;
	

	public CreateDomainAction(TreeViewer viewer, TableViewer tableViewer, IObject iObject, boolean isPublic) {
		this.treeViewer = viewer;
		this.tableViewer = tableViewer;
		this.oObject = iObject;
		this.isPublic = isPublic;
		
		// Get the refinement model once
		OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
		if(facade != null ) {
			refModel = facade.getRefinementModel();
		}
    }

	@Override
    public String getText() {
		String labelText = "";
		if(isPublic)
			labelText = "Create Public Domain";
		else
			labelText = "Create Private Domain";
		return labelText;
    }

	@Override
    public void run() {
		System.out.println("Create domain");
		
		if (refModel != null) {
			// XXX. Remove hard-coded domain name. How to specify the domain.
			String dstDomain = "PD2";
			
			boolean create = ContentProviderObjectGraph.createDomain(oObject, dstDomain);
			if (create) {
				// Make the new domain show up in the tree
				treeViewer.refresh(oObject, true);

				CreateDomain cdRef = new CreateDomain(oObject, dstDomain);
				refModel.addOther(cdRef);
				tableViewer.refresh(true);
			}
		}
		
	    super.run();
    }

	public boolean shouldDisplay() {
		return ContentProviderObjectGraph.shouldCreateDomain(oObject, "PD2");
	}

}
