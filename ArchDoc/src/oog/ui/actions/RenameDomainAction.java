package oog.ui.actions;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oog.common.OGraphFacade;
import oog.itf.IObject;
import oog.re.CreateDomain;
import oog.re.IOtherRefinement;
import oog.re.RefinementModel;
import oog.re.RenameDomain;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.widgets.TreeItem;

import edu.wayne.ograph.ODomain;

public class RenameDomainAction extends Action {
	
	// XXX. Maybe clean up this field...Not used.
	private ODomain domain;
	
	private TreeViewer treeViewer;

	private TableViewer tableViewer;

	public RenameDomainAction(TreeViewer viewer, TableViewer tableViewer, ODomain domain)
	{
		// Are we updating Refinements based on name changes?
		// As per the Refinement window, we are updating the tree label but the underlying domain remains PD2
		
		this.domain = domain;
		this.treeViewer = viewer;
		this.tableViewer = tableViewer;
		
		TreeViewerEditor.create(treeViewer, new ColumnViewerEditorActivationStrategy(treeViewer){
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {  
				return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}  
			
		}, TreeViewerEditor.DEFAULT);
		
		treeViewer.setColumnProperties(new String[] {"col1"});
		treeViewer.setCellEditors(new CellEditor[]{new TextCellEditor(treeViewer.getTree())});
		treeViewer.setCellModifier(new ICellModifier() {

			@Override
			public boolean canModify(Object element, String property) {
				return true;
			}

			@Override
			public Object getValue(Object element, String property) {
				String value = "";
				if (element instanceof ODomain)
				{
					ODomain domain = (ODomain) element;
					value = domain.getD();
				}
				else
					value = element.toString();
				return value;
			}

			@Override
			public void modify(Object element, String property, Object value) {
				if (element instanceof TreeItem)
				{
					TreeItem treeItem = (TreeItem) element;
	                element = treeItem.getData();
				}
				
				if(element instanceof ODomain) {
					ODomain oDomain = (ODomain)element;
					oDomain.setD(value.toString());
					updateRefinement(oDomain);
				}
				
				treeViewer.refresh(element,  true);
			}
			
		});
	}

	private RefinementModel getRefModel() {
		RefinementModel refModel = null;

	    OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
		if(facade != null ) {
			refModel = facade.getRefinementModel();
		}
		
		return refModel;
    }
	
	private void updateRefinement(ODomain oDomain) {
		RefinementModel refModel = getRefModel();
		if (refModel != null ) {
			List<IOtherRefinement> otherRefinements = refModel.getOtherRefinements();
			boolean createdAndRenamed = false;
			for(IOtherRefinement ref : otherRefinements ) {
				if (ref instanceof CreateDomain ) {
					CreateDomain createDomain = (CreateDomain)ref;
					// XXX. HACK: parent of an oDomain is not unique
					if (oDomain.getParents().contains( createDomain.getSrcIObject()) ) { 
						createDomain.setDstDomain(oDomain.getD());
						tableViewer.refresh(createDomain, true);
						createdAndRenamed = true;
					}
				}
			}
			if (createdAndRenamed == false) // if the refinement was not just created and then renamed (it was pre-existing, e.g. PD)
			{
				// create a new Rename Domain refinement
				Set<IObject> domainParents = oDomain.getParents();
				Iterator<IObject> iterator = domainParents.iterator();
				IObject parent = null;
				while(iterator.hasNext())
				{
					parent = iterator.next();
				}
				RenameDomain renamed = new RenameDomain(parent, oDomain.getD());
				
				// add it to the refinement model's other list
				refModel.addOther(renamed);
				
				// refresh the tableViewer
				tableViewer.refresh();
			}
		}
	}

	@Override
	public String getText() {
		return "Rename Domain";
	}

	@Override
	public void run() {
		treeViewer.editElement(((IStructuredSelection) treeViewer.getSelection()).getFirstElement(), 0);
		super.run();
	}
}
