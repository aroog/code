package oog.ui.views;

import java.util.Set;

import oog.Refreshable;
import oog.itf.IObject;
import oog.ui.RuntimeModel;
import oog.ui.SimpleLoggedTreeViewListener;
import oog.ui.actions.GoToElementAction;
import oog.ui.actions.GoToStackAction;
import oog.ui.actions.LinkAction;
import oog.ui.actions.LoggedAction;
import oog.ui.actions.OpenTypeAction;
import oog.ui.actions.RefreshAction;
import oog.ui.tree.content.provider.ContentProviderDisplayGraph;
import oog.ui.tree.displaygraph.ArchitecturalDecoratingLabelProvider;
import oog.ui.tree.displaygraph.ArchitecturalViewerSorter;
import oog.ui.tree.displaygraph.ObjectGraphPatternFilter;
import oog.ui.tree.displaygraph.ViewerFilterObjectGraph;
import oog.ui.utils.ASTUtils;
import oog.ui.utils.LabelUtil;
import oog.ui.utils.LogUtils;
import oog.ui.utils.LogUtils.Mode;
import oog.ui.utils.MenuUtils;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
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
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayEdge;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayElement;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.cmu.cs.viewer.objectgraphs.views.MakeVisibleAction;
import edu.cmu.cs.viewer.objectgraphs.views.ShowInternalsAction;

public class AbstractObjectTreeView extends ViewPart implements Refreshable {

	public static final String ID = "oog.ui.views.AbstractObjectTreeView"; //$NON-NLS-1$
	private ViewerFilterObjectGraph viewerFilterDisplay;
	private ArchitecturalViewerSorter viewerSorterDisplay;
	private FilteredTree ftDisplay;
	private TreeViewer viewer;
	private boolean fLinkingEnabled = false;

	private DrillDownAdapter drillDownAdapter;
	private DisplayModel loadModel;
	private Action stackAction;
	private LinkAction linkStackAction;
	private RefreshAction refreshAction;
	private MakeVisibleAction visibleAction;
	private ShowInternalsAction showInternalAction;
	private Logger log = Logger.getLogger(AbstractObjectTreeView.class
			.getSimpleName());

	public TreeViewer getViewer() {
		return viewer;
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		// create the desired layout for this wizard page
		GridLayout glLeft = new GridLayout();
		glLeft.numColumns = 3;
		parent.setLayout(glLeft);
		// Create the sorters and filters
		viewerFilterDisplay = new ViewerFilterObjectGraph();
		viewerSorterDisplay = new ArchitecturalViewerSorter();

		GridData gdVisualFilteredViewer = new org.eclipse.swt.layout.GridData();
		gdVisualFilteredViewer.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdVisualFilteredViewer.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdVisualFilteredViewer.grabExcessHorizontalSpace = true;
		gdVisualFilteredViewer.grabExcessVerticalSpace = true;
		gdVisualFilteredViewer.horizontalSpan = 3;

		ftDisplay = new FilteredTree(parent, SWT.BORDER | SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL, new ObjectGraphPatternFilter()); // Support
		// single
		ftDisplay.setLayoutData(gdVisualFilteredViewer);
		viewer = ftDisplay.getViewer();
		drillDownAdapter = new DrillDownAdapter(viewer);

		viewer.setContentProvider(new ContentProviderDisplayGraph());
		viewer.setLabelProvider(new ArchitecturalDecoratingLabelProvider(
				ftDisplay));
		// By default, no sorting
		viewer.setSorter(viewerSorterDisplay);
		viewer.addFilter(viewerFilterDisplay);

		viewer.setUseHashlookup(true);

		createActions();
		initializeToolBar();
		initializeMenu();
		hookLogListeners();
		refresh();
	}

	private void hookLogListeners() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (linkStackAction.isLinked()) {
					stackAction.run();
					LogUtils.info(log, "Linked Abtract Stack",
							Mode.SELECTION_CHANGED, event.getSelection()
									.toString());
				}

			}
		});
		viewer.addTreeListener(new SimpleLoggedTreeViewListener(log,
				"Abstract Object Tree"){

					@Override
					public String getLabel(Object obj) {
						return LabelUtil.getDisplayElementLabel(obj);
					}
			
		});

	}

	/**
	 * Create the actions.
	 */
	private void createActions() {

		stackAction = new GoToStackAction(viewer);

		refreshAction = new RefreshAction(this);

		linkStackAction = new LinkAction("Link to Abstract Stack",
				fLinkingEnabled);

		// TODO: Encapsulate some of this logic inside MakeVisibleAction
		visibleAction = new MakeVisibleAction();
		visibleAction.setTreeViewer(viewer);
		showInternalAction = new ShowInternalsAction();
		showInternalAction.setTreeViewer(viewer);
		PartialOOG showView = null;
		try {

			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				IWorkbenchWindow activeWorkbenchWindow = workbench
						.getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null) {
					IWorkbenchPage activePage = activeWorkbenchWindow
							.getActivePage();
					if (activePage != null) {
						showView = (PartialOOG) activePage
								.showView(PartialOOG.ID);
					}
				}

			}

		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showInternalAction.setImageRefresh(showView);
		visibleAction.setImageRefresh(showView);
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();

		toolbarManager.add(new LoggedAction(refreshAction, log).setLogInfo(
				"Refresh Tree", Mode.CLICK_TOOLBAR_MENU, "NA"));

		toolbarManager.add(new LoggedAction(linkStackAction, log).setLogInfo(
				"Toggle Link With Stack", Mode.CLICK_TOOLBAR_MENU, "NA"));

		toolbarManager
				.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		drillDownAdapter.addNavigationActions(toolbarManager);
	}

	DisplayObject getDisplayObject(Object element) {
		DisplayObject displayObject = null;
		if (element instanceof IObject) {
			DisplayModel displayModel = RuntimeModel.getInstance()
					.getDisplayModel();
			if (displayModel != null) {
				Set<DisplayObject> objs = displayModel
						.getDisplayObject((IObject) element);
				if (objs != null && objs.size() > 0)
					for (DisplayObject obj : objs) {
						displayObject = obj;
						break;
					}
			}
		}
		return displayObject;
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		setupMenuListener(menuMgr);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	public void setupMenuListener(MenuManager menuMgr) {
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = viewer.getSelection();
				final Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				LogUtils.info(log, "Show Popup Menu", Mode.RIGHT_CLICK,
						"Tree Element " + LabelUtil.getDisplayElementLabel(obj));
				if (obj instanceof DisplayElement) {
					Set<BaseTraceability> setOfLists = ((DisplayElement) obj).getElement().getTraceability();
					MenuUtils.setTraceToCodeMenu(manager, setOfLists, log);

					if (obj instanceof DisplayObject) {
						final IObject element = (IObject) ((DisplayObject) obj)
								.getElement();
						final String qualifiedName = element.getC()
								.getFullyQualifiedName();
						if (ASTUtils.isFromSource(qualifiedName)) {
							OpenTypeAction openTypeAction = new OpenTypeAction(
									qualifiedName);
							manager.add(new LoggedAction(openTypeAction, log)
									.setLogInfo("Open Type",
											Mode.CLICK_POPUP_MENU,
											qualifiedName));
						}

						manager.add(new LoggedAction(visibleAction, log)
								.setLogInfo("Toggle Visible",
										Mode.CLICK_POPUP_MENU,
										LabelUtil.getIObjectLabel(element)));

						manager.add(new LoggedAction(showInternalAction, log)
								.setLogInfo("Show Internals",
										Mode.CLICK_POPUP_MENU,
										LabelUtil.getIObjectLabel(element)));

						visibleAction.setChecked(((DisplayObject) obj)
								.isVisible());
						// TODO: Avoid doing this here; or move to
						// getDisplayObject
						// Why not set the activeDisplayObject on the command
						// itself...so we don't have to do another lookup!
						visibleAction.setDisplayModel(RuntimeModel
								.getInstance().getDisplayModel());
						visibleAction.setEnabled(true);

						showInternalAction.setChecked(((DisplayObject) obj)
								.isShowInternals());
						// TODO: Avoid doing this here; or move to
						// getDisplayObject
						// Why not set the activeDisplayObject on the command
						// itself...so we don't have to do another lookup!
						showInternalAction.setDisplayModel(RuntimeModel
								.getInstance().getDisplayModel());
						showInternalAction.setEnabled(true);

					}
					if (obj instanceof DisplayEdge) {
						DisplayObject dstObj = ((DisplayEdge) obj)
								.getToObject();
						GoToElementAction goToDst = new GoToElementAction(
								viewer, dstObj);
						goToDst.setText("Go To Destination");
						final DisplayObject srcObj = ((DisplayEdge) obj)
								.getFromObject();
						GoToElementAction goToSrc = new GoToElementAction(
								viewer, srcObj);
						goToSrc.setText("Go To Source");
						manager.add(new LoggedAction(goToSrc, log).setLogInfo(
								"Go To Source Obj", Mode.CLICK_POPUP_MENU,
								LabelUtil.getDisplayObjectLabel(srcObj)));

						manager.add(new LoggedAction(goToDst, log).setLogInfo(
								"Go to Destination Obj", Mode.CLICK_POPUP_MENU,
								LabelUtil.getDisplayObjectLabel(dstObj)));

					}
					if (!linkStackAction.isLinked())
						manager.add(new LoggedAction(stackAction, log)
								.setLogInfo("Open Abstract Stack",
										Mode.CLICK_POPUP_MENU,
										LabelUtil.getDisplayElementLabel(obj)));

				}

				AbstractObjectTreeView.this.fillContextMenu(manager);
			}

		});
	}

	@Override
	public void setFocus() {
		// ftDisplay.setFocus();
	}

	private void fillContextMenu(IMenuManager manager) {

		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Deprecated
	public void loadOOG() {
		refresh();
	}

	@Override
	public void refresh() {
		// RuntimeModel.invalidate();
		loadModel = RuntimeModel.getInstance().getDisplayModel();

		if (loadModel != null && viewer != null) {
			// Update the treeviewer to display the Ownership Tree
			viewer.setInput(loadModel);
		}

	}

	public static void selectElement(DisplayElement element) {
		AbstractObjectTreeView showView = null;
		try {

			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				IWorkbenchWindow activeWorkbenchWindow = workbench
						.getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null) {
					IWorkbenchPage activePage = activeWorkbenchWindow
							.getActivePage();
					if (activePage != null) {
						showView = (AbstractObjectTreeView) activePage
								.showView(ID);
					}
				}

			}

		} catch (PartInitException e) {
			e.printStackTrace();
		}
		new GoToElementAction(showView.getViewer(), element).run();

	}

}