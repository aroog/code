package oog.ui.views;

import oog.itf.IEdge;
import oog.ui.RuntimeModel;
import oog.ui.actions.DoubleClickTraceAction;
import oog.ui.tree.content.provider.ContentProviderRelatedElements;
import oog.ui.tree.content.provider.ContentProviderTraceInfo;
import oog.ui.tree.label.provider.ColumnInfoLabelProvider;
import oog.ui.tree.label.provider.ColumnInfoTitleProvider;
import oog.ui.tree.label.provider.RelatedObjectLabelProvider;
import oog.ui.utils.ASTUtils;
import oog.ui.utils.LabelUtil;
import oog.ui.utils.LogUtils;
import oog.ui.utils.LogUtils.Mode;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ast.BaseTraceability;

public class RelatedObjectsEdges extends ViewPart {
	// TODO: Add link with editor option
	public static final String ID = "oog.ui.views.RelatedObjectsEdges"; //$NON-NLS-1$
	private SelectionListener textListener = null;
	private TreeViewer fTreeViewer;
	// TODO: Performance: only check for ASTNode Selection Change when this view
	// is visible
	private Action traceAction;
	private TableViewer tableViewer;
	private Logger log = Logger.getLogger(RelatedObjectsEdges.class.getSimpleName());

	private class SelectionListener implements ISelectionListener,
			ISelectionChangedListener {

		public SelectionListener() {

		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
		}

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof ITextSelection) {
				ASTNode node = ASTUtils.getASTNode((ITextSelection) selection);
				if (node != null) {
					String string = node.toString();
					CharSequence subSequence = string.subSequence(0,
							string.length() > 100 ? 100 : string.length());
					RelatedObjectsEdges.this.setContentDescription(subSequence.toString()
							.trim());
					RelatedObjectsEdges.this.fTreeViewer.setInput(node);
					RelatedObjectsEdges.this.fTreeViewer.expandAll();
					RelatedObjectsEdges.this.tableViewer.setInput(null);
				}
			}

		}

	}

	public RelatedObjectsEdges() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */

	@Override
	public void createPartControl(Composite parent) {
		setContentDescription("No Expression Selected");
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

		fTreeViewer = new TreeViewer(sashForm);

		tableViewer = new TableViewer(sashForm, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(new ContentProviderTraceInfo());

		createColumns();

		fTreeViewer.setContentProvider(new ContentProviderRelatedElements());
		fTreeViewer.setLabelProvider(new DecoratingLabelProvider(
				new RelatedObjectLabelProvider(), null));

		sashForm.setWeights(new int[] { 50, 50 });
		fTreeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						ISelection selection = event.getSelection();
						if (selection instanceof IStructuredSelection) {
							Object firstElement = ((IStructuredSelection) selection)
									.getFirstElement();

							if (firstElement != null
									&& (firstElement instanceof BaseTraceability || firstElement instanceof IEdge)) {
								LogUtils.info(log,
										"Related Objects Tree Selection",
										Mode.SELECTION_CHANGED,
										LabelUtil.getRelatedObjectsTreeLabel(firstElement));
								tableViewer.setInput(firstElement);
							}
						}

					}
				});
		createActions();
		initializeToolBar();
		initializeMenu();
		hookDoubleClickAction();

	}

	protected void createColumns() {
		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn headerColumn = column.getColumn();
		headerColumn.setWidth(150);
		column.setLabelProvider(new ColumnInfoTitleProvider());

		TableViewerColumn infoColumn = new TableViewerColumn(tableViewer,
				SWT.NONE);
		infoColumn.setLabelProvider(new ColumnInfoLabelProvider());

		TableColumn column2 = infoColumn.getColumn();
		column2.setWidth(400);

	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		RuntimeModel.getInstance();
		if (textListener == null) {
			textListener = new SelectionListener();
			ISelectionService service = site.getWorkbenchWindow()
					.getSelectionService();
			service.addPostSelectionListener(textListener);
		}
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		traceAction = new DoubleClickTraceAction(fTreeViewer);
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
		if (fTreeViewer != null && fTreeViewer.getTree() != null)
			fTreeViewer.getTree().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (textListener != null) {
			ISelectionService service = getSite().getWorkbenchWindow()
					.getSelectionService();
			service.removePostSelectionListener(textListener);
			textListener = null;
		}
	}

	private void hookDoubleClickAction() {
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {

				ISelection selection = fTreeViewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				LogUtils.info(log, "Trace-to-Code", Mode.DOUBLE_CLICK,
						obj.toString());
				traceAction.run();

			}
		});

	}

}
