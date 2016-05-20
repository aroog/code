package oog.ui.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import oog.itf.IElement;
import oog.ui.RuntimeModel;
import oog.ui.viewer.ImageAnalyzer;
import oog.ui.viewer.ImageAnalyzerSelectionEvent;
import oog.ui.viewer.ImageAnalyzerSelectionListener;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.cmu.cs.viewer.objectgraphs.VisualReportOptions;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.wayne.summary.internal.WorkspaceUtilities;

/**
 * Lightweight display of the OOG or OGraph, using ImageViewer
 */
public class OOGREView extends ViewPart {

	public enum ViewType {
		OOG, OGraph
	};

	public static final String ID = "oog.ui.views.OOGREView"; //$NON-NLS-1$

	private ImageAnalyzer imageCanvas;

	private String dotFullFilename;

	private String reportFullPath;

	// Constants for the file name that we stored in each project folder
	private static final String DOT_FILE_NAME = "OOG.dot";

	private static final String DOT_FILE_NAME_NO_EXT = "OOG";

	private static final String OGRAPH__DOTFILE_NAME = "dfOGraph.dot";

	private static final String OGRAPH__DOTFILE_NAME_NO_EXT = "dfOGraph";

	public OOGREView() {
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

		final Display display = parent.getDisplay();
		final RuntimeModel runtimeModel = RuntimeModel.getInstance();

		imageCanvas = new ImageAnalyzer(parent, SWT.NONE);
		imageCanvas.addImageAnalyzerSelectionListener(new ImageAnalyzerSelectionListener() {

			public void select(ImageAnalyzerSelectionEvent event) {
				Object data = event.data;
				if (data instanceof String) {
					String url = (String) data;

					final IElement element = runtimeModel.getElement(url);
					if (element != null) {
						// Marshal back onto the SWT thread...
						display.asyncExec(new Runnable() {
							public void run() {
								ObjectTreeView.selectElement(element);
							}
						});
					}
				}
			}
		});
		imageCanvas.setLayoutData(gdCanvas);

		createActions();
		initializeToolBar();
		initializeMenu();

		setupFiles();
		buildImage();
	}

	private void setupFiles() {

		// Gets the model
		RuntimeModel singleton = RuntimeModel.getInstance();
		DisplayModel displayModel = singleton.getDisplayModel();

		// Clear the summary edges each time, before re-generating the DOT
		if (displayModel != null) {
			displayModel.clearSummaryEdges();

			// XXX. Remove hard-coded path
			String path = "C:\\temp";
			IJavaProject javaProject = WorkspaceUtilities.getJavaProject();
			if (javaProject != null) {
				path = javaProject.getResource().getLocation().toOSString();
			}

			ViewType selection = ViewType.OGraph;
			switch (selection) {
			case OOG:
				dotFullFilename = path + File.separator + DOT_FILE_NAME;
				// Gets the model
				reportFullPath = path + File.separator + DOT_FILE_NAME_NO_EXT;
				break;
			case OGraph:
				dotFullFilename = path + File.separator + OGRAPH__DOTFILE_NAME;
				// Gets the model
				reportFullPath = path + File.separator + OGRAPH__DOTFILE_NAME_NO_EXT;
				break;
			}
		}
	}

	public void selectElement(IElement element) {
		ObjectTreeView objectTreeView;
		try {
			objectTreeView = (ObjectTreeView) PlatformUI.getWorkbench()
			        .getActiveWorkbenchWindow()
			        .getActivePage()
			        .showView(ObjectTreeView.ID);
			TreeViewer viewer = objectTreeView.getViewer();
			viewer.setSelection(new StructuredSelection(element), true);
		}
		catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void buildImage() {

		String gifImageFileName = reportFullPath + ".gif";
		String cmapImageFileName = reportFullPath + ".cmap";

		// If PointsTo has not run yet, no graph, no dot
		if (dotFullFilename == null) {
			return;
		}
		
		try {
			generateImageFile(dotFullFilename, "-Tgif", gifImageFileName);
			generateImageFile(dotFullFilename, "-Tcmap", cmapImageFileName);

			imageCanvas.load(cmapImageFileName);
			imageCanvas.menuOpenFile(gifImageFileName);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void generateImageFile(String dotFileName, String imageType, String imageFileName) throws IOException,
	        InterruptedException {
		VisualReportOptions instance = VisualReportOptions.getInstance();

		// Always use DOT for this
		boolean backup = instance.isUseFDP();
		instance.setUseFDP(false);
		java.util.List<String> command = new ArrayList<String>();
		command.add(instance.getExecutable(Display.getDefault().getActiveShell()));
		command.add(imageType);
		command.add(instance.getEngine());
		command.add(dotFileName);
		command.add("-o");
		command.add(imageFileName);
		instance.setUseFDP(backup);

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(instance.getTempDirectory());

		final Process process = builder.start();
		if (process.waitFor() == 0) {
			// Display image
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
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

}
