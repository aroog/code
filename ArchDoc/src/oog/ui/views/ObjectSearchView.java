package oog.ui.views;

import java.util.Set;

import oog.itf.IElement;
import oog.ui.SimpleLoggedTreeViewListener;
import oog.ui.tree.content.provider.ContentProviderOOGSearch;
import oog.ui.tree.label.provider.ObjectSearchLabelProvider;
import oog.ui.utils.LabelUtil;
import oog.ui.utils.LogUtils;
import oog.ui.utils.MenuUtils;
import oog.ui.utils.LogUtils.Mode;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import ast.BaseTraceability;

import util.TraceabilityListSet;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayElement;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;

public class ObjectSearchView extends ViewPart {
	public static final String ID = "oog.ui.views.ObjectSearchView"; //$NON-NLS-1$
	private TreeViewer viewer;
	private Logger log = Logger.getLogger(ObjectSearchView.class.getSimpleName());

	public ObjectSearchView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new ContentProviderOOGSearch());
		viewer.setLabelProvider(new DecoratingLabelProvider(
				new ObjectSearchLabelProvider(), null));

		createActions();
		initializeToolBar();
		initializeMenu();
		hookDoubleClickAction();
		hookLogListeners();
	}

	private void hookLogListeners() {
		viewer.addTreeListener(new SimpleLoggedTreeViewListener(log, 
				"Object Search Tree"){

					@Override
					public String getLabel(Object obj) {
						
						return LabelUtil.getDisplayElementLabel(obj);
					}
			
		});
		
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object firstElement = ((IStructuredSelection) selection)
							.getFirstElement();
					if (firstElement instanceof DisplayElement) {
						AbstractObjectTreeView
								.selectElement((DisplayElement) firstElement);
						LogUtils.info(log,
								"Select Element in Abstract Object Tree",
								Mode.DOUBLE_CLICK, LabelUtil.getDisplayElementLabel(firstElement));
					}
				}
			}
		});

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
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();

				if (obj instanceof DisplayObject) {

					IElement element = ((DisplayObject) obj).getElement();
					if (element != null) {
						Set<BaseTraceability> path = element.getTraceability();
						if (path != null) {
							MenuUtils.setTraceToCodeMenu(manager, path, log);
						}
					}
				} else {
					System.out.println(obj.getClass().getSimpleName());
				}

			}
		});
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public void setType(String fullyQualifiedNameOfOpenJavaEditor) {
		setContentDescription(fullyQualifiedNameOfOpenJavaEditor);
		viewer.setInput(fullyQualifiedNameOfOpenJavaEditor);
	}

}
