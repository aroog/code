package oog.ui.views;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oog.Refreshable;
import oog.itf.IElement;
import oog.logging.LogComment;
import oog.logging.LogWriter;
import oog.ui.RuntimeModel;
import oog.ui.actions.GoToInstanceAction;
import oog.ui.actions.LoggedAction;
import oog.ui.actions.OpenTypeAction;
import oog.ui.actions.RefreshAction;
import oog.ui.tree.content.provider.ContentProviderSummary;
import oog.ui.utils.ASTUtils;
import oog.ui.utils.LabelUtil;
import oog.ui.utils.LogUtils;
import oog.ui.utils.LogUtils.Mode;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.wayne.summary.Crystal;
import edu.wayne.summary.strategies.EdgeSummary;
import edu.wayne.summary.strategies.Info;
import edu.wayne.summary.strategies.InfoIElement;
import edu.wayne.summary.strategies.InfoType;
import edu.wayne.summary.strategies.MarkManager;
import edu.wayne.summary.strategies.MarkStatus;

// XXX. Gotta unregister...at some point. Right now, keep listening after project has been closed!
public class SummaryView extends ViewPart implements IPartListener2,
		Refreshable {
	private Logger log = Logger.getLogger(SummaryView.class.getSimpleName());

	public enum Tab {
		MIC, MIRC, MIM, MCBI, UNK;

	}

	public class InterfaceListener implements ISelectionListener,
			ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {

		}

		// XXX. Make this work on parameters too and receivers.
		// XXX. Refactor this method: extract helper methods
		// - one to find the type of selection
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			TabItem[] tabItems = SummaryView.this.tabFolder.getSelection();
			if(tabItems != null && tabItems.length > 1 ) {
			TabItem tabItem = tabItems[0];
			if (tabItem.getText().compareTo("MCBI") == 0) {
				if (selection instanceof ITextSelection) {
					ASTNode node = ASTUtils.getASTNode((ITextSelection) selection);
					// XXX. Why convert everything into SimpleName? There are more interesting nodes that can be considered.
					//if (node.getNodeType() == ASTNode.SIMPLE_NAME) {
					if (node != null && node instanceof org.eclipse.jdt.core.dom.FieldDeclaration ) {
						org.eclipse.jdt.core.dom.FieldDeclaration  eclipseFieldDecl = (org.eclipse.jdt.core.dom.FieldDeclaration)node;
						ASTNode parent = eclipseFieldDecl.getParent();
						if (parent instanceof org.eclipse.jdt.core.dom.TypeDeclaration ) {
							org.eclipse.jdt.core.dom.TypeDeclaration typedecl = (org.eclipse.jdt.core.dom.TypeDeclaration)parent;
						
						String fieldName = getFieldName(eclipseFieldDecl);
						String enclosingType = typedecl.resolveBinding().getQualifiedName();
						// System.out.println("node: " + node.toString());
						ITypeBinding typeBinding = eclipseFieldDecl.getType().resolveBinding();
						if (typeBinding.isInterface() || Modifier.isAbstract(typeBinding.getModifiers())) {

							String fieldType = typeBinding.getQualifiedName();
							Set<Info<IElement>> classesBehindInterface = edgeSummaryAll.getClassesBehindInterface(enclosingType, fieldType, fieldName);
							RankedTableViewer.resetRank();
							tableViewer.setInput(classesBehindInterface);
							setContentDescription(fieldType);
							
							ITypeBinding binding = Crystal.getInstance().getTypeBindingFromName(enclosingType);
							//Set opened type as Visited
							updateVisitedType(binding.getQualifiedName());

							if (LogWriter.LogState == 1)
								if (LogWriter.CheckedInterfaces.add(fieldType)) {									
									if(binding != null){
										LogComment lc = new LogComment();
										synchronized (lc) {
											while (!LogWriter.CmmLock) {
												try {
													lc.wait();
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
										}
										LogWriter.CmmLock = false;
										LogWriter.setLogForDelaration(
												binding,
												fieldName,
												fieldType,
												classesBehindInterface);
										LogWriter.OrderNum++;
									}
								}
						}
						} else {
							tableViewer.setInput(null);
						}
					}
				}
			}
			}
		}

		// XXX. Move to Util class
		// NOTE: In case there are multiple fragments, they all have the same type and the same domain;
		// So, it is OK to get the first one
		private String getFieldName(org.eclipse.jdt.core.dom.FieldDeclaration fieldDeclaration) {
			String name = null;

			List fragments = fieldDeclaration.fragments();
			Iterator iterator = fragments.iterator();
			while (iterator.hasNext()) {
				Object next = iterator.next();
				if (next instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration svdecl = (SingleVariableDeclaration) next;
					name = svdecl.getName().getIdentifier();
					break;
				}
				else if (next instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) next;
					name = vdf.getName().getIdentifier();
					break;
				}
			}
			
			return name;
		}
		

	}

	private InterfaceListener interfaceListener = null;
	public static final String ID = "oog.ui.views.SummaryView"; //$NON-NLS-1$
	private TabFolder tabFolder;
	private EdgeSummary edgeSummaryAll;
	private RankedTableViewer tableViewer;
	private TabItem micTab;
	private TabItem mircTab;
	private TabItem mimTab;
	private TabItem mcbiTab;

	public SummaryView() {

	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		setContentDescription("");

		tabFolder = new TabFolder(parent, SWT.NONE);
		
		final IWorkbenchWindow workbenchWindow = getSite().getWorkbenchWindow();
		if(workbenchWindow != null ) {
			IPartService partService = workbenchWindow.getPartService();
			if (partService != null )
				partService.addPartListener(this);
		}
		edgeSummaryAll = RuntimeModel.getInstance().getSummaryInfo();

		micTab = new TabItem(tabFolder, SWT.NONE);
		micTab.setText("MIC");
		mircTab = new TabItem(tabFolder, SWT.NONE);
		mircTab.setText("MIRC");
		mimTab = new TabItem(tabFolder, SWT.NONE);
		mimTab.setText("MIM");

		mcbiTab = new TabItem(tabFolder, SWT.NONE);
		mcbiTab.setText("MCBI");

		tableViewer = new RankedTableViewer(tabFolder, SWT.BORDER
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		setControlTab(micTab);
		setControlTab(mircTab);
		setControlTab(mimTab);
		setControlTab(mcbiTab);

		createActions();
		initializeToolBar();
		initializeMenu();
		hookListeners();
		
		if(LogWriter.isEmptyProjectTypes())
			LogWriter.initProjectTypes();
	}

	public void computeSummaryInfo() {
		RuntimeModel instance = RuntimeModel.getInstance();
		if (instance.getGraph() != null) {
			edgeSummaryAll = instance.getSummaryInfo();
		}
	}

	private void hookListeners() {
		tabFolder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				LogUtils.info(log, "Switch Summary Tabs",
						Mode.SELECTION_CHANGED, getCurrentTab().toString());
				tableViewer.setInput(null);
				updateTabs();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);

			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof Info) {

					Info i = (Info) firstElement;
					System.out.println("Doubleclick: " + i.getKey());
					Tab currentTab = getCurrentTab();

					if (currentTab.equals(Tab.MIM)) {
						LogUtils.info(log, "Select Method In Editor",
								Mode.DOUBLE_CLICK,
								LabelUtil.getSummaryViewLabel(i));
						selectMethodInEditor(i);

					} else {
						LogUtils.info(log, "Select Type in Editor",
								Mode.DOUBLE_CLICK,
								LabelUtil.getSummaryViewLabel(i));
						selectTypeInEditor(i);
					}
				}
			}

			private void selectTypeInEditor(Info i) {
				new OpenTypeAction(i.getKey()).run();
				//change the mark
				if(i.getMark() != MarkStatus.Impacted &&
						i.getMark() != MarkStatus.Unchanged &&
								i.getMark() != MarkStatus.Visited)
					i.setMark(MarkStatus.Visited);
				tableViewer.refresh();
				//add logs if it is not a library type
				if (LogWriter.LogState == 1){
					String fullName = i.getKey();
					ITypeBinding binding = Crystal.getInstance().getTypeBindingFromName(fullName);
					if (binding != null)
						if (LogWriter.CheckedTypes.add(fullName)) {
							LogWriter.Reason = "";
							if (!LogWriter.isFromLibrary((InfoIElement)i)){
								LogWriter.setLogs(binding, edgeSummaryAll
									.getMostImportantRelatedClass(fullName), i.getMark().toString());
								}
							else 
								//LogWriter.setLogs(binding, null, i.getMark().toString());
								/*cancel comment below if also need to record library types in log*/;
							LogWriter.OrderNum++;
						}
				}
			}

			private void selectMethodInEditor(Info i) {
				MethodDeclaration methodDeclaration = ASTUtils
						.getMethodDeclaration(i.getKey());
				if (methodDeclaration != null) {
					IEditorPart part = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage()
							.getActiveEditor();
					if (part instanceof ITextEditor) {
						((ITextEditor) part).selectAndReveal(methodDeclaration
								.getName().getStartPosition(),
								methodDeclaration.getName().getLength());
					}
				}
			}
		});
	}

	private void setControlTab(TabItem tab) {
		tableViewer.setContentProvider(new ContentProviderSummary());
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		tab.setControl(table);

	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		RuntimeModel.getInstance();
		super.init(site);
		if (interfaceListener == null) {
			interfaceListener = new InterfaceListener();
			ISelectionService service = site.getWorkbenchWindow()
					.getSelectionService();
			service.addPostSelectionListener(interfaceListener);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (interfaceListener != null) {
			ISelectionService service = getSite().getWorkbenchWindow()
					.getSelectionService();
			service.removePostSelectionListener(interfaceListener);
			interfaceListener = null;
		}
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		Action refresh = new RefreshAction(this);
		toolbarManager.add(refresh);
		
		/*TODO: add mark menu to tool-bar
		ClearMarkAction clearAction = new ClearMarkAction("Clear all marks");
		markMenu.addActionToMenu(clearAction);
		ImageDescriptor image = IconUtils.getImageDescriptor(IconUtils.IMG_MARK);
		Action marksAction = new Action("", image) {
		};
		marksAction.setMenuCreator(markMenu);
		marksAction.setToolTipText("Mark class");
		toolbarManager.add(marksAction);
		*/
	}
	
	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		setupMenuListener(menuMgr);
		Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}

	public void setupMenuListener(final MenuManager menuMgr) {
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = tableViewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				if (obj instanceof Info) {
					Info infoObject = (Info) obj;
					InfoType type = infoObject.getType();
					if (type == InfoType.CLASS || type == InfoType.INTERFACE) {
						LogUtils.info(log, "Show Popup Menu", Mode.RIGHT_CLICK,
								"Table Element " + LabelUtil.getSummaryViewLabel(obj));
						String key = infoObject.getKey();
						//DONE. Add right-click here!!
						MarkAction impactAction = new MarkAction(MarkStatus.Impacted, infoObject);
						menuMgr.add(new LoggedAction(impactAction, log).setLogInfo(
								"Impacted", Mode.CLICK_POPUP_MENU, "Impacted"));
						MarkAction visitAction = new MarkAction(MarkStatus.Unchanged, infoObject);
						menuMgr.add(new LoggedAction(visitAction, log).setLogInfo(
								"Unchanged", Mode.CLICK_POPUP_MENU, "Unchanged"));
						ClearMarkAction clearAction = new ClearMarkAction("Clear all marks");
						menuMgr.add(new LoggedAction(clearAction, log).setLogInfo(
								"Clear all marks", Mode.CLICK_POPUP_MENU, "Clear All Marks"));
						menuMgr.add(new Separator());
						
						GoToInstanceAction action = new GoToInstanceAction(key);
						menuMgr.add(new LoggedAction(action, log).setLogInfo(
								"Find Instances ", Mode.CLICK_POPUP_MENU, key));
					}
				}
			}
		});
	}
	
	private class MarkAction extends Action{
		public MarkAction(MarkStatus mark, Info infoObject) {
			super(mark.toString());
			this.infoObject = infoObject;
			this.mark = mark;
		}
		private Info infoObject;
		private MarkStatus mark;
		
		@Override
		public void run() {
//			if(infoObject instanceof InfoIElement)
//				((InfoIElement)infoObject).setMark(mark);
//			else
//				((InfoUnranked)infoObject).setMark(mark);
			infoObject.setMark(mark);
			tableViewer.refresh();
			
			//add logs if it is not a library type
			if (LogWriter.LogState == 1 && !LogWriter.isFromLibrary((InfoIElement)infoObject)){
				String fullName = infoObject.getKey();
				ITypeBinding binding = Crystal.getInstance().getTypeBindingFromName(fullName);
				if (binding != null) {
						LogComment lc = new LogComment();
						synchronized (lc) {
							while (!LogWriter.CmmLock) {
								try {
									lc.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						LogWriter.CmmLock = false;
						LogWriter.CheckedTypes.add(fullName);
						LogWriter.setLogs(binding, null, mark.toString());
						//Do not increase OrderNum here
//						LogWriter.OrderNum++;
					}
			}
		}
	};
	
	private class ClearMarkAction extends Action{
		private ClearMarkAction(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			MarkManager.getInstance().reset();
//			RankedTableViewer.resetRank();
			tableViewer.refresh();
		}
	};

	@Override
	public void setFocus() {
		tabFolder.setFocus();
	}

	@Override
	// XXX. Make sure still OK to listen in?
	// XXX. Check the argument! What is being activated?!
	public void partActivated(IWorkbenchPartReference partRef) {
		updateTabs();

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
		// TODO Auto-generated method stub

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

	public void updateTabs() {
		if(LogWriter.isEmptyProjectTypes())
			LogWriter.initProjectTypes();
		String fullName = "";
		RankedTableViewer.resetRank();
		
		
		if (edgeSummaryAll != null) {
			IJavaElement activeEditorJavaInput = EditorUtility
					.getActiveEditorJavaInput();

			Tab currentTab = getCurrentTab();
			if (activeEditorJavaInput != null) {
				if (currentTab.equals(Tab.MIC)) {
					tableViewer.setInput(edgeSummaryAll
							.getMostImportantClasses());
					setContentDescription("Not Class Specific");
				}
				if (!currentTab.equals(Tab.MCBI)) {
					IType currentType = ASTUtils.getTypeOfOpenJavaEditor();
					if(currentType != null){
						fullName = ASTUtils.getTypeOfOpenJavaEditor().getFullyQualifiedName();
						//Set opened type as Visited
						updateVisitedType(fullName);
						
						setContentDescription(fullName);
						ITypeBinding binding = Crystal.getInstance().getTypeBindingFromName(fullName);
						
						if (currentTab.equals(Tab.MIRC)) {
							tableViewer.setInput(edgeSummaryAll
									.getMostImportantRelatedClass(fullName));
							// add logs
							if (LogWriter.LogState == 1)
							if (LogWriter.CheckedTypes.add(fullName)) {
									// System.out.println(currentType.getElementName());
								//no need to leave comments when open a new type
									/*LogComment lc = new LogComment();
									synchronized (lc) {
										while (!LogWriter.CmmLock) {
											try {
												lc.wait();
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
									}
									LogWriter.CmmLock = false;*/
									LogWriter.Reason = "";
									LogWriter.setLogs(binding, edgeSummaryAll
															.getMostImportantRelatedClass(fullName), "Visited");
									LogWriter.OrderNum++;
								}
						}
						if (currentTab.equals(Tab.MIM)) {
							tableViewer.setInput(edgeSummaryAll.getMostImportantMethods(fullName));
						}
					}
				}
			}
		}
	}
	
	//Mark opened type as Visited
	public void updateVisitedType(String fullName){
		MarkStatus newMark = MarkManager.getInstance().getMark(fullName);
		//Below is wrong because each node didn't have a mark when created
//		if(element.getMark() == MarkStatus.NotVisited)
		
		//only refresh table if the mark should be changed
		if(newMark != MarkStatus.Impacted &&
				newMark != MarkStatus.Unchanged &&
						newMark != MarkStatus.Visited) {
			MarkManager.getInstance().setMark(fullName, MarkStatus.Visited);
			System.out.println("Visit " + fullName);
			tableViewer.refresh();
		}
	}

	public void setTab(Tab tab) {
		if (tab.equals(Tab.MCBI)) {
			tabFolder.setSelection(mcbiTab);
		}
	}

	public Tab getCurrentTab() {
		if (tabFolder != null && !this.tabFolder.isDisposed()) {
			TabItem tabItem = SummaryView.this.tabFolder.getSelection()[0];
			if (tabItem.getText().compareTo("MIC") == 0) {
				return Tab.MIC;
			} else if (tabItem.getText().compareTo("MIRC") == 0) {
				return Tab.MIRC;
			} else if (tabItem.getText().compareTo("MIM") == 0) {
				return Tab.MIM;
			} else if (tabItem.getText().compareTo("MCBI") == 0) {
				return Tab.MCBI;
			}
		}
		return Tab.UNK;

	}

	@Override
	public void refresh() {
		computeSummaryInfo();
		updateTabs();

	}
}
