package oog.ui.views;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.claribole.zgrviewer.GUIConfig;
import net.claribole.zgrviewer.ZGRApplet;
import net.claribole.zgrviewer.ZGRViewerSelectionEvent;
import net.claribole.zgrviewer.ZGRViewerSelectionListener;
import oog.Refreshable;
import oog.itf.IElement;
import oog.itf.IObject;
import oog.ui.ArchDocOptions;
import oog.ui.RuntimeModel;
import oog.ui.actions.LinkAction;
import oog.ui.actions.LoggedAction;
import oog.ui.actions.RefreshAction;
import oog.ui.utils.ASTUtils;
import oog.ui.utils.IconUtils;
import oog.ui.utils.LogUtils.Mode;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import swingintegration.example.EmbeddedSwingComposite;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayElement;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayGraph;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.cmu.cs.viewer.objectgraphs.views.IImageRefresh;
import edu.wayne.summary.internal.WorkspaceUtilities;
import edu.wayne.summary.strategies.Info;

// TODO: Build map from RuntimeElement to DisplayElement
// TODO: Do we need IImageRefresh
public class PartialOOG extends ViewPart implements IPartListener2,
		IImageRefresh, Refreshable {

	public static final String ID = "oog.ui.views.PartialOOG"; //$NON-NLS-1$
	private ZGRApplet applet;
	private boolean initOnce = false;
	private RefreshAction refreshAction;
	private boolean linkedWithEditorDefault = true;
	private LinkAction linkAction;
	
	// Constant for the file name that we stored in each project folder
	private static final String DOT_FILE_NAME = "OOG.dot";
	
	// The fully qualified dot filename
	private String dotFileName;
	
	private Action collapseAll;
	private Action expandAll;
	private Logger log = Logger.getLogger(PartialOOG.class.getSimpleName());

	public PartialOOG() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		GridData gdCanvas = new org.eclipse.swt.layout.GridData();
		gdCanvas.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdCanvas.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdCanvas.grabExcessHorizontalSpace = true;
		gdCanvas.grabExcessVerticalSpace = true;
		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		workbenchWindow.getPartService().addPartListener(this);
		final Display display = parent.getDisplay();
		EmbeddedSwingComposite embeddedComposite = new EmbeddedSwingComposite(
				parent, SWT.NONE) {
			protected Component createSwingComponent() {
				// TODO: Make the GUIConfig preferences user-settable
				GUIConfig config = new GUIConfig(false, false, false);
				applet = new ZGRApplet(config);
				applet.addImageAnalyzerSelectionListener(new ZGRViewerSelectionListener() {

					public void select(ZGRViewerSelectionEvent event) {
						Object data = event.data;
						// Gets the model

						RuntimeModel singleton = RuntimeModel.getInstance();
						DisplayModel displayModel = singleton.getDisplayModel();

						// This should not happen here!
						if (displayModel == null) {
							return;
						}

						if (data instanceof String) {
							String url = (String) data;

							final DisplayElement element;
							// HACK: This is a hack to trim out the "cluster_";
							// clean this up.
							if (url.length() > 8) {
								url = url.substring(8);
							}

							DisplayElement displayObject = displayModel
									.getObject(url);
							if (displayObject != null) {
								element = displayObject;
							} else {
								// Try to resolve to an edge
								// IMPORTANT: Do not use truncated url
								url = (String) data;
								element = displayModel.getEdge(url);
							}
							if (element != null) {
								// Marshall back onto the SWT thread...
								display.asyncExec(new Runnable() {
									public void run() {
										log.info("Select Element:" + element);
										AbstractObjectTreeView.selectElement(element);
									}
								});
							}
						}
					}
				});
				return applet;

			}
		};
		embeddedComposite.populate();
		embeddedComposite.setLayoutData(gdCanvas);
		createActions();
		initializeToolBar();
	}

	private void createActions() {

		refreshAction = new RefreshAction(this);
		// TODO: add images for collapse all and expand all actions
		linkAction = new LinkAction("Link with editor", linkedWithEditorDefault);
		Action showAll = new Action() {
			@Override
			public void run() {
				RuntimeModel instance = RuntimeModel.getInstance();
				if(instance!=null) {
					DisplayModel displayModel = instance.getDisplayModel();
					if(displayModel!=null){
						for (DisplayObject obj : displayModel.getObjects()) {
							obj.setVisible(true);
						}
						updateDisplay();
					}
				}
				super.run();
			}
		};
		linkAction.addDisabledAction(showAll);
		collapseAll = new Action(
				"Collapse All",
				IconUtils
						.getPlatformImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL)) {

			@Override
			public void run() {
				collapseAllDisplayObjects();
				super.run();
			}
		};
		expandAll = new Action("Expand All",
				IconUtils.getImageDescriptor(IconUtils.IMG_EXPANDALL)) {
			@Override
			public void run() {
				expandAllDisplayObjects();
				super.run();
			}
		};

	}

	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

		toolbarManager.add(new LoggedAction(refreshAction, log).
				setLogInfo("Refresh Partial OOG", Mode.CLICK_TOOLBAR_MENU, "NA"));
		
		toolbarManager.add(new LoggedAction(linkAction, log).
				setLogInfo("Link Partial OOG with Active Editor",
				Mode.CLICK_TOOLBAR_MENU, "NA"));
		
		toolbarManager.add(new LoggedAction(collapseAll, log).
				setLogInfo("Collapse All Display Objects", Mode.CLICK_TOOLBAR_MENU, "NA"));;

		toolbarManager.add(new LoggedAction(expandAll, log).
				setLogInfo("Expand All Display Objects", Mode.CLICK_TOOLBAR_MENU, "NA"));

		// IContributionItem[] items = toolbarManager.getItems();
	}

	// NOTE: init() can get called multiple times
	// TODO: Move computing DisplayElement stuff out of the UI thread: could be
	// the wrong AWT thread, no?
	private void init() {
		if (!initOnce && applet != null) {
			applet.init();
			initOnce = true;
		}

		// TODO: Move this initialization elsewhere...
		RuntimeModel singleton = RuntimeModel.getInstance();
		if (singleton.getGraph() != null) {
			updateDisplay();

			buildImage();
		}
	}

	@Override
	public void setFocus() {
	}

	// XXX. Do not automatically update this...
	// XXX. Do not use MICs
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
// XXX. Comment out bad functionality		
//		if (!ArchDocOptions.getInstance().isEnablePartialOOG()) {
//			return;
//		}
//
//		RuntimeModel instance = RuntimeModel.getInstance();
//		if (linkAction != null && linkAction.isLinked()) {
//
//			EdgeSummary summaryInfo = instance.getSummaryInfo();
//			if (summaryInfo != null) {
//				IType openJavaEditor = ASTUtils.getTypeOfOpenJavaEditor();
//				if (openJavaEditor != null) {
//					String nameOfOpenJavaEditor = openJavaEditor.getFullyQualifiedName();
//					if (nameOfOpenJavaEditor != null) {
//						Set<Info<IElement>> mircs = summaryInfo.getMostImportantRelatedClass(nameOfOpenJavaEditor);
//						displayMircs(mircs);
//					}
//				}
//			}
//		}
//		else {
//
//		}

	}

	// XXX. Make this optional
	private void displayMircs(Set<Info<IElement>> mircs) {
		if (mircs != null) {
			String currentClass = ASTUtils.getTypeOfOpenJavaEditor().getFullyQualifiedName();

			Set<String> strMircs = new HashSet<String>();
			strMircs.add(Signature.getSimpleName(currentClass));// Force display
			                                                    // of currently
			                                                    // opened class;
			for (Info<IElement> mirc : mircs) {
				strMircs.add(Signature.getSimpleName(mirc.getKey()));
			}

			DisplayModel displayModel = RuntimeModel.getInstance().getDisplayModel();

			for (DisplayObject obj : displayModel.getObjects()) {
				IElement element = obj.getElement();

				if (element instanceof IObject && ((IObject) element).isMainObject()) {
					obj.setVisible(true);
					obj.setShowInternals(true);
				}
				else if (strMircs.contains(obj.getTypeDisplayName())) {
					obj.setVisible(true);
					obj.setShowInternals(true);
				}
				else {
					obj.setVisible(false);
				}

			}
			updateDisplay();
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	// This is called by the OOG Viewer component when the visual display should
	// change:
	// - e.g., set Visible = true/false, showInternals = true/false to
	// collapse/expand object
	// - re-create a DisplayGraph; that's the dot file
	public void updateDisplay() {
		if (!ArchDocOptions.getInstance().isEnablePartialOOG()) {
			return;
		}
		
		// Gets the model
		RuntimeModel singleton = RuntimeModel.getInstance();
		DisplayModel displayModel = singleton.getDisplayModel();

		// Clear the summary edges each time, before re-generating the DOT
		if (displayModel != null) {
			displayModel.clearSummaryEdges();
			
			// Encapsulate some of this logic
			String path = "C:\\temp"; // HACK: temp., hard-coded location
			IJavaProject javaProject = WorkspaceUtilities.getJavaProject();
			if(javaProject != null ) {
				path = javaProject.getResource().getLocation().toOSString();
			}
			dotFileName = path + File.separator + DOT_FILE_NAME;
			
			DisplayGraph displayGraph = new DisplayGraph(displayModel, dotFileName);
			displayGraph.generateGraph();

			// Refresh the tree to reflect the summary edges
			// which depend on the current projection depth
			// tvDisplay.refresh(displayModel.getEdgeCollection());
		}

		buildImage();
	}

	// Make the ZGRViewer load and display the DOT file
	public void buildImage() {
		// Can we find a better place to do init()?
		if (!initOnce) {
			init();
		}

		// TODO: Get the dotfilename from somewhere else
		if (initOnce && applet != null && dotFileName != null) {
			File file = new File(dotFileName);
			if (file.exists()) {
				applet.load(dotFileName);
			}
		}
	}

	@Override
	public void refresh() {
		updateDisplay();

	}

	public void showAll(){
		DisplayModel displayModel = RuntimeModel.getInstance().getDisplayModel();

		for (DisplayObject displayObject : displayModel.getObjects()) {
			displayObject.setVisible(true);
		}

	}
	public void hideAll(){
		DisplayModel displayModel = RuntimeModel.getInstance().getDisplayModel();

		for (DisplayObject displayObject : displayModel.getObjects()) {
			displayObject.setVisible(false);
		}
	}
	private void expandAllDisplayObjects() {

		DisplayModel displayModel = RuntimeModel.getInstance().getDisplayModel();

		for (DisplayObject displayObject : displayModel.getObjects()) {
			displayObject.setShowInternals(true);
		}

		updateDisplay();
	}

	private void collapseAllDisplayObjects() {
		DisplayModel displayModel = RuntimeModel.getInstance().getDisplayModel();

		for (DisplayObject displayObject : displayModel.getObjects()) {
			displayObject.setShowInternals(false);
		}

		updateDisplay();
	}

}
