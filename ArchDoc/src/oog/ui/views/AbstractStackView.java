package oog.ui.views;

import oog.ui.actions.DoubleClickTraceAction;
import oog.ui.tree.content.provider.ContentProviderTraceability;
import oog.ui.utils.LabelUtil;
import oog.ui.utils.LogUtils;
import oog.ui.utils.LogUtils.Mode;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;

import util.TraceabilityListSet;

public class AbstractStackView extends ViewPart {

	public static final String ID = "oog.ui.views.AbstractStackView"; //$NON-NLS-1$
	private TreeViewer treeViewerTraceability;
	private Tree stackTree;
	private Action doubleClickTraceabilityAction;
	private Logger log = Logger.getLogger(AbstractStackView.class.getSimpleName());

	public AbstractStackView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		treeViewerTraceability = new TreeViewer(parent, SWT.BORDER
				| SWT.FULL_SELECTION);

		stackTree = treeViewerTraceability.getTree();

		setLabelProvider(treeViewerTraceability);

		treeViewerTraceability.setContentProvider(new ContentProviderTraceability());

		createActions();
		initializeToolBar();
		hookDoubleClickAction();
		initializeMenu();
	}

	private void hookDoubleClickAction() {
		treeViewerTraceability
				.addDoubleClickListener(new IDoubleClickListener() {

					@Override
					public void doubleClick(DoubleClickEvent event) {
						doubleClickTraceabilityAction.run();
						LogUtils.info(log, "Trace-to-Code", Mode.DOUBLE_CLICK,
								LabelUtil.getAbstractStackLabel(event.getSelection()));
					}
				});

	}
	private void setLabelProvider(TreeViewer viewer) {
		viewer.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return LabelUtil.getAbstractStackLabel(element);

			}


		});

	}

	/**
	 * Create the actions.
	 */
	private void createActions() {

		// Create the actions
		doubleClickTraceabilityAction = new DoubleClickTraceAction(
				treeViewerTraceability);

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
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public void setTraceObject(TraceabilityListSet element) {
		if (element != null) {
			treeViewerTraceability.setInput(element);
		}
	}

}
