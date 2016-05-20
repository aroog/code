package oog.ui.actions;

import java.util.ArrayList;
import java.util.Set;

import oog.common.OGraphFacade;
import oog.itf.IDomain;
import oog.itf.IObject;
import oog.re.CreateDomain;
import oog.re.DeleteDomain;
import oog.re.RefinementModel;
import oog.ui.tree.ContentProviderObjectGraph;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;

import edu.wayne.ograph.ODomain;
import edu.wayne.ograph.OObject;

public class DeleteDomainAction extends Action{
	
	private TableViewer tableViewer;
	
	private IDomain oDomain;
	
	private RefinementModel refModel;
	
	public DeleteDomainAction(TableViewer tableViewer, IDomain iDomain) {
		this.tableViewer = tableViewer;
		this.oDomain = iDomain;
		
		// Get the refinement model once
		OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
		if(facade != null ) {
			refModel = facade.getRefinementModel();
		}
    }

	@Override
	public String getText() {
		return "Delete Domain";
	}

	@Override
	public void run() {
		System.out.println("Delete domain");
		
		if (refModel != null) {
			OObject parent;
			Set<IObject> parentSet = oDomain.getParents();
			parent = (OObject) parentSet.iterator().next();
			String oObjectName = parent.getInstanceDisplayName();
			String oDomainName = oDomain.getD();
			DeleteDomain cdRef = new DeleteDomain(parent, oDomainName);
			refModel.addOther(cdRef);
			tableViewer.refresh(true);
		}
		
	    super.run();
	}
	
	public boolean shouldDisplayDeleteOption(ODomain domain)
	{
		ArrayList<ODomain> domainsAdded = ContentProviderObjectGraph.getAddedDomains();
		if (domainsAdded.contains(domain))
			return true;
		else
			return false;
	}

}
