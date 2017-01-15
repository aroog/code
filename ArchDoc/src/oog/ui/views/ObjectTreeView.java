package oog.ui.views;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import oog.Refreshable;
import oog.common.OGraphFacade;
import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IObject;
import oog.re.Persist;
import oog.re.RankedTypings;
import oog.re.Refinement;
import oog.re.RefinementModel;
import oog.re.RefinementState;
import oog.ui.RuntimeModel;
import oog.ui.actions.CreateDomainAction;
import oog.ui.actions.DeleteDomainAction;
import oog.ui.actions.GoToElementAction;
import oog.ui.actions.GoToStackAction;
import oog.ui.actions.LinkAction;
import oog.ui.actions.OpenTypeAction;
import oog.ui.actions.RefreshAction;
import oog.ui.actions.RenameDomainAction;
import oog.ui.actions.TraceAction;
import oog.ui.tree.ArchitecturalDecoratingLabelProvider;
import oog.ui.tree.ArchitecturalViewerSorter;
import oog.ui.tree.ContentProviderObjectGraph;
import oog.ui.tree.ContentProviderRefinements;
import oog.ui.tree.ObjectGraphPatternFilter;
import oog.ui.tree.ViewerFilterObjectGraph;
import oog.ui.tree.label.provider.RefinementInfoLabelProvider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import ast.BaseTraceability;
import edu.wayne.ograph.ODomain;
import edu.wayne.ograph.OGraph;
import edu.wayne.summary.internal.WorkspaceUtilities;

/**
 * Expose OGraph in tree form. Does not deal at all with hiding nodes, expanding/collapsing.
 * TODO: Rename: OOGRETreeView
 */
public class ObjectTreeView extends ViewPart implements Refreshable {

	private static final String OOGRE_XML = "oogre.xml";

	public static final String ID = "oog.ui.views.ObjectTreeView"; //$NON-NLS-1$

	private ViewerFilterObjectGraph viewerFilterDisplay;

	private ArchitecturalViewerSorter viewerSorterDisplay;

	private FilteredTree ftDisplay;

	private TreeViewer viewer;

	private boolean fLinkingEnabled = false;

	public TreeViewer getViewer() {
		return viewer;
	}

	private DrillDownAdapter drillDownAdapter;

	private OGraph loadModel;

	private Action stackAction;

	private LinkAction linkStackAction;

	private RefreshAction refreshAction;
	
	private ObjectTreeViewDropListener dropListener = null;
	
	private TableViewer refinementViewer;

	private Menu headerMenu;

    private oog.re.IOperation selectedRef;

	private IObject parentObject;

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		

		// create the desired layout for this wizard page
		GridLayout glLeft = new GridLayout();
		glLeft.numColumns = 3;
		sashForm.setLayout(glLeft);
		// Create the sorters and filters
		viewerFilterDisplay = new ViewerFilterObjectGraph();
		viewerSorterDisplay = new ArchitecturalViewerSorter();

		GridData gdVisualFilteredViewer = new org.eclipse.swt.layout.GridData();
		gdVisualFilteredViewer.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdVisualFilteredViewer.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdVisualFilteredViewer.grabExcessHorizontalSpace = true;
		gdVisualFilteredViewer.grabExcessVerticalSpace = true;
		gdVisualFilteredViewer.horizontalSpan = 3;

		ftDisplay = new FilteredTree(sashForm,
		        SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL,
		        new ObjectGraphPatternFilter()); // Support
		// single
		ftDisplay.setLayoutData(gdVisualFilteredViewer);
		viewer = ftDisplay.getViewer();
		drillDownAdapter = new DrillDownAdapter(viewer);

		viewer.setContentProvider(new ContentProviderObjectGraph());
		viewer.setLabelProvider(new ArchitecturalDecoratingLabelProvider(ftDisplay));
		// By default, no sorting
		viewer.setSorter(viewerSorterDisplay);
		viewer.addFilter(viewerFilterDisplay);
		viewer.setUseHashlookup(true);
		
		Table table = new Table(sashForm, SWT.BORDER|SWT.FULL_SELECTION);
		table.setHeaderVisible(true); 
		table.setLinesVisible(true);

		createColumns(table);
		createMenu(table);
		
		refinementViewer = new TableViewer(table);
		refinementViewer.setLabelProvider(new RefinementInfoLabelProvider());
		refinementViewer.setContentProvider(new ContentProviderRefinements());
		refinementViewer.setComparator(new ViewerComparator() {
			// Sort by Ref ID
			@Override
            public int compare(Viewer viewer, Object e1, Object e2) {
				Refinement r1 = null;
				Refinement r2 = null;
				if (e1 instanceof Refinement ) {
					r1 = (Refinement)e1;
				}
				if (e2 instanceof Refinement ) {
					r2 = (Refinement)e2;
				}
				if (r1 != null && r2 != null ) {
					return r1.getRefID().compareTo(r2.getRefID());
				}
	            return super.compare(viewer, e1, e2);
            }
			
		}
		);
		refinementViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(final SelectionChangedEvent event) {
		        IStructuredSelection selection = (IStructuredSelection)event.getSelection();
		        Object firstElement = selection.getFirstElement();
		        if (firstElement instanceof Refinement) {
		        	selectedRef = (Refinement) firstElement;
		        			
		        }
		    }
		});
		
		refinementViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			private void setMessage(StringBuilder builder ) {
				
				RankedTypings rankedTypings = selectedRef.getRankedTypings();
				Map<String, ArrayList<String>> rankedTyping = rankedTypings.getRankedTypings();
				for(Entry<String, ArrayList<String>> entry : rankedTyping.entrySet() ) {
					builder.append("AU := ");
					builder.append(entry.getKey());
					builder.append(";\nTypings := \n");
					
					for(String typing: entry.getValue()) {
						builder.append(typing);
						builder.append("\n");
					}
					builder.append("");
				}
			}
			
		    @Override
		    public void doubleClick(DoubleClickEvent event) {
		        IStructuredSelection selection = (IStructuredSelection)event.getSelection();
		        Object firstElement = selection.getFirstElement();
		        if (firstElement instanceof oog.re.IOperation) {
		        	selectedRef = (oog.re.IOperation) firstElement;

		        	// XXX. What about if RefinementUnsupported?
		        	if(selectedRef.getState() == RefinementState.MoreInfoNeeded ) {
		        		//Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		        		
		        		Shell activeShell = getSite().getWorkbenchWindow().getShell();
		        		//MessageBox messageBox = new MessageBox(activeShell, SWT.ICON_WARNING | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
		        		//messageBox.setText("OOGRE");
//		        		StringBuilder builder = new StringBuilder();
//		        		builder.append("More information needed.\n");
//		        		setMessage(builder);
//		        		messageBox.setMessage(builder.toString());
		        		
		        		SetTypingsDialog messageBox = new SetTypingsDialog(activeShell);
		        		messageBox.populate(selectedRef.getRankedTypings());
		        		
		        		int buttonID = messageBox.open();
		        		switch(buttonID) {
		        		case IDialogConstants.OK_ID:
		        			// saves changes ...
		        		case IDialogConstants.CANCEL_ID:
		        			// does nothing ...
		        		}
		        		System.out.println(buttonID);
		        	}
		        }
		    }
		});
		
		sashForm.setWeights(new int[] { 75, 25 });
		
		setupDragNDrop();
		createActions();
		initializeToolBar();
		initializeMenu();
		refresh();
	}

	private void createMenu(Table table) {
		headerMenu = new Menu(table);
		table.setMenu(headerMenu);
		
		//  TODO: Convert Auto to toggle?
		final MenuItem enableAuto = new MenuItem(headerMenu, SWT.NORMAL);
		enableAuto.setText("Enable Auto");
		enableAuto.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// Get the refinement model once
				OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
				facade.setAuto(true);
			}
		});
		
		final MenuItem disableAuto = new MenuItem(headerMenu, SWT.NORMAL);
		disableAuto.setText("Disable Auto");
		disableAuto.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// Get the refinement model once
				OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
				facade.setAuto(false);
			}
		});
		
		
		final MenuItem sep1 = new MenuItem(headerMenu, SWT.SEPARATOR);
		
		final MenuItem resetStatus = new MenuItem(headerMenu, SWT.NORMAL);
		resetStatus.setText("Reset status");
		resetStatus.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (selectedRef != null ) {
					selectedRef.setState(RefinementState.Pending);
					refinementViewer.refresh(selectedRef, true);
				}
			}
		});
		
		final MenuItem deleteRef = new MenuItem(headerMenu, SWT.NORMAL);
		deleteRef.setText("Delete");
		deleteRef.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (selectedRef != null ) {
					selectedRef.setState(RefinementState.Deleted);
					refinementViewer.refresh(selectedRef, true);
				}
			}
		});		
		
		final MenuItem clearAll = new MenuItem(headerMenu, SWT.NORMAL);
		clearAll.setText("Clear Refinements");
		clearAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// Get the refinement model once
				OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
				RefinementModel refinementModel = facade.getRefinementModel();
				refinementModel.clear();
				
				refinementViewer.refresh(true);
			}
		});	
		
		final MenuItem exportRefs = new MenuItem(headerMenu, SWT.NORMAL);
		exportRefs.setText("Export Refinements");
		exportRefs.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// Get the refinement model once
				OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
				RefinementModel refinementModel = facade.getRefinementModel();
				
				Persist.save(refinementModel, getRefinementPath(OOGRE_XML));
			}
		});		

		final MenuItem importRefs = new MenuItem(headerMenu, SWT.NORMAL);
		importRefs.setText("Import Refinements");
		importRefs.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// Get the refinement model once
				OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
				RefinementModel refinementModel = Persist.load(getRefinementPath(OOGRE_XML));
				if (refinementModel != null ) {
					facade.setRefinementModel(refinementModel);
				}
				refinementViewer.setInput(refinementModel);
				refinementViewer.refresh(true);
			}
		});	

	}
	private void createColumns(Table table) {
		
		TableColumn column0 = new TableColumn(table, SWT.LEFT);
		column0.setText("Refinement #");
		column0.setWidth(80);
		
	    TableColumn column1 = new TableColumn(table, SWT.LEFT); 
		column1.setText("Refinement"); 
		column1.setWidth(80);
		
//		TableColumn column2 = new TableColumn(table, SWT.LEFT);
//		column2.setText("Source Object");
//		column2.setWidth(80);
		
		TableColumn column3 = new TableColumn(table, SWT.LEFT);
		column3.setText("Source Type");
		column3.setWidth(80);
		
//		TableColumn column4 = new TableColumn(table, SWT.LEFT);
//		column4.setText("Destination Object");
//		column4.setWidth(80);
		
		TableColumn column5 = new TableColumn(table, SWT.LEFT);
		column5.setText("Destination Type");
		column5.setWidth(80);
		
		TableColumn column6 = new TableColumn(table, SWT.LEFT);
		column6.setText("Destination Domain");
		column6.setWidth(80);
		
	    TableColumn column7 = new TableColumn(table, SWT.LEFT); 
		column7.setText("Status"); 
		column7.setWidth(80); 
		
    }
	
	private void createMenuItem(Menu parent, final TableColumn column) {
		final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
		itemName.setText(column.getText());
		itemName.setSelection(column.getResizable());
		itemName.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (itemName.getSelection()) {
					column.setWidth(150);
					column.setResizable(true);
				} else {
					column.setWidth(0);
					column.setResizable(false);
				}
			}
		});

	}
	private void setupDragNDrop() {
		dropListener = new ObjectTreeViewDropListener(viewer, refinementViewer, this );
		int operations = DND.DROP_MOVE;
		// TOWES: Next iteration: write a custom Transfer object
		Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
		viewer.addDragSupport(operations, transferTypes, new ObjectTreeViewDragListener(viewer, this));
		viewer.addDropSupport(operations, transferTypes, dropListener);
    }
	
	/**
	 * Create the actions.
	 */
	private void createActions() {
		stackAction = new GoToStackAction(viewer);

		refreshAction = new RefreshAction(this);

		linkStackAction = new LinkAction("Link to Abstract Stack", fLinkingEnabled);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (linkStackAction.isLinked()) {
					stackAction.run();
				}

			}
		});
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

		toolbarManager.add(refreshAction);
		toolbarManager.add(linkStackAction);
		toolbarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		drillDownAdapter.addNavigationActions(toolbarManager);
		
//		toolbarManager.add(exportAction);
//		toolbarManager.add(importAction);
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof IElement) {
					MenuManager subMenu = new MenuManager("Trace to Code", null);
					for (BaseTraceability last : ((IElement) obj).getTraceability()) {
						TraceAction action = new TraceAction(last);
						subMenu.add(action);
					}
					manager.add(subMenu);

					if (obj instanceof IObject) {
						manager.add(new OpenTypeAction(((IObject) obj).getC().getFullyQualifiedName()));

						manager.add(stackAction);
						
						manager.add(new Separator());
						
						MenuManager domainSubMenu = new MenuManager("Add New Domains", null);
						CreateDomainAction publicDomainAction = new CreateDomainAction(viewer, refinementViewer, (IObject) obj, true);
						boolean enabled = publicDomainAction.shouldDisplay();
						domainSubMenu.add(publicDomainAction);
						publicDomainAction.setEnabled(enabled);	
						
						CreateDomainAction privateDomainAction = new CreateDomainAction(viewer, refinementViewer, (IObject) obj, false);
						domainSubMenu.add(privateDomainAction);
						privateDomainAction.setEnabled(false);
						
						manager.add(domainSubMenu);
					}
					if (obj instanceof IEdge) {
						GoToElementAction goToDst = new GoToElementAction(viewer, ((IEdge) obj).getOdst());
						goToDst.setText("Go To Destination");
						GoToElementAction goToSrc = new GoToElementAction(viewer, ((IEdge) obj).getOsrc());
						goToSrc.setText("Go To Source");
						manager.add(goToSrc);
						manager.add(goToDst);
						manager.add(stackAction);
					}
					if (obj instanceof ODomain)
					{
						ODomain domain = (ODomain) obj;
						String domainName = domain.getD();
						boolean enabled = (!(domainName.equals("owner") || domainName.equals("owned") || domainName.equals("DLENT") || domainName.equals("SHARED") || domainName.equals("DUNIQUE")));
						RenameDomainAction action = new RenameDomainAction(viewer, refinementViewer, domain);
						manager.add(action);
						action.setEnabled(enabled);
						
						DeleteDomainAction deleteAction = new DeleteDomainAction(refinementViewer, domain);
						boolean deleteEnabled = deleteAction.shouldDisplayDeleteOption(domain);
						manager.add(deleteAction);
						deleteAction.setEnabled(deleteEnabled);
					}
				}

				ObjectTreeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	@Override
	public void setFocus() {
		ftDisplay.setFocus();
	}

	private void fillContextMenu(IMenuManager manager) {

		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	public void refresh() {
		// XXX. Is there a better way to do this? 
		RuntimeModel.invalidate();
		
		// Use the real graph
		loadModel = RuntimeModel.getInstance().getGraph();

		// Get the refinement model once
		OGraphFacade facade = oog.ui.Activator.getDefault().getMotherFacade();
		RefinementModel refinementModel = facade.getRefinementModel();
		
		if (loadModel != null && viewer != null) {
			// Update the treeviewer to display the Ownership Tree
			viewer.setInput(loadModel);
			viewer.refresh(true);
			
			// Update the list of refinements
			refinementViewer.setInput(refinementModel);
			refinementViewer.refresh(true);
		}

	}
	
	public static void selectElement(IElement element) {
		ObjectTreeView showView = null;
		try {

			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null) {
					IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
					if (activePage != null) {
						showView = (ObjectTreeView) activePage.showView(ID);
					}
				}

			}

		}
		catch (PartInitException e) {
			e.printStackTrace();
		}
		new GoToElementAction(showView.getViewer(), element).run();	    
    }


	// HACK: why not pass the BaseTraceability and the IObject as a pair on the clipboard...
	// Then, no need for this
	public void setParentObject(IObject object) {
		this.parentObject = object;
    }

	public IObject getParentObject() {
    	return parentObject;
    }

	// TODO: Move this helper method elsewhere.
	// XXX. Extract constant for "oogre.xml"
	private static String getRefinementPath(String fileName) {
		String path = "C:\\temp\\oogre.xml";
		IJavaProject javaProject = WorkspaceUtilities.getJavaProject();
		if (javaProject != null) {
			IPath location = null;
	            location = javaProject.getResource().getLocation();
			if (location != null) {
				IPath append = location.append(fileName);
				path = append.toOSString();
			}
		}
	    return path;
    }
}
