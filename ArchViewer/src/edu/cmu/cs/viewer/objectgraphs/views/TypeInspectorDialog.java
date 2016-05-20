package edu.cmu.cs.viewer.objectgraphs.views;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.umlgraph.doclet.UmlGraph;

import edu.cmu.cs.viewer.objectgraphs.VisualReportOptions;
import edu.cmu.cs.viewer.ui.ImageAnalyzer;
import edu.cmu.cs.viewer.ui.ImageAnalyzerSelectionEvent;
import edu.cmu.cs.viewer.ui.ImageAnalyzerSelectionListener;

/**
 * TODO: Add ability to zoom-in on view (useful for screenshots)
 * TODO: Add buttons to removeall!
 *
 */
public class TypeInspectorDialog extends Dialog  implements IImageRefresh {
	
    protected Set<String> selectedTypes = new LinkedHashSet<String>();
    
	private ImageAnalyzer imageCanvas;
	
	
    public TypeInspectorDialog (Shell shell) {
        super (shell);
    }

	@Override
    public int getShellStyle() {
		return (SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX);
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Type Inspector");
	}

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
     */
    protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout glRight = new GridLayout();
		glRight.numColumns = 1;
		
		GridData gdCanvas = new org.eclipse.swt.layout.GridData();
		gdCanvas.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdCanvas.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdCanvas.grabExcessHorizontalSpace = true;
		gdCanvas.grabExcessVerticalSpace = true;

		imageCanvas = new ImageAnalyzer(composite, SWT.NONE);
		imageCanvas.setLayoutData(gdCanvas);
		imageCanvas.addImageAnalyzerSelectionListener(new ImageAnalyzerSelectionListener() {
			public void select(ImageAnalyzerSelectionEvent event) {
				Object data = event.data;
				if (data instanceof String) {
					String url = UmlGraph.urlToClassName((String) data).intern();
				}
			}
		});
		
        updateDisplay();
        
        return parent;
	}
    
	private void buildImage() {
		// TODO: Fix hard-coded paths
		String gifImageFileName = "C:\\temp\\umlgraph.gif";
		String dotFullFilename = "C:\\temp\\umlgraph.dot";
		try {

			generateImageFile(dotFullFilename, "-Tgif", gifImageFileName);

			String cmapImageFileName = "C:\\temp\\umlgraph.cmap";
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
		command.add(instance.getExecutable(getShell()));
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

	public void updateDisplay() {
	    buildImage();
    }
	
    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        // Note: Call the superclass version after you do the processin, to avoid "Widget Disposed" exceptions!
		super.okPressed();
    }

    @Override
    protected void cancelPressed() {
    	// Do nothing
        super.cancelPressed ();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		return c;
	}

	public void setMergedTypes(Set<String> set, IJavaProject javaProject) {

		UmlGraph umlGraph = new UmlGraph();
		try {
			umlGraph.buildGraph(set, new HashSet<String>(), javaProject);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	


}
