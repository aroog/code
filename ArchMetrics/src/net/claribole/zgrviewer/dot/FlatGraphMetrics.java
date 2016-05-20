package net.claribole.zgrviewer.dot;


import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.wayne.dot.DotParser;
import edu.wayne.metrics.datamodel.FlatGraphMetricItem;
import edu.wayne.metrics.datamodel.GraphMetricItem;
import edu.wayne.metrics.datamodel.NodeType;
import edu.wayne.metrics.internal.WorkspaceUtilities;

// TODO: LOW. Refactor. Create constants for magic string:  src.womble.dot
/**
 * HACK: This class is being placed in this package in order to access some fields (label) that are 'package protected'
 */
public class FlatGraphMetrics implements IWorkbenchWindowActionDelegate {

	private static final String DOT_FOLDER = "src";
	private static String DOTFILE = "src.womble.dot";

	private String currentProjectPath;

	private IWorkbenchWindow window = null;
	
	private Hashtable<String,FlatGraphMetricItem> listOfObjects; 

	public FlatGraphMetrics(String inputFileName){
		DOTFILE = inputFileName;
	}
	@Override
	public void run(IAction action) {

		listOfObjects = new Hashtable<String, FlatGraphMetricItem>();
		listOfObjects.clear();

		String fileName;
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);

		currentProjectPath = currentProject.getLocation().toOSString();
		
		String projectName = currentProject.getName();
		
		IPath currentProjectPath = currentProject.getLocation();
		IPath relativePath = new Path(DOT_FOLDER).append(DOTFILE);
		if (currentProject != null) {
			IResource findMember = currentProject.findMember(relativePath);
			if (findMember != null){
			fileName = currentProjectPath.append(relativePath).toOSString();			
			
			Graph g = DotParser.parse(fileName);
					
			processGraph(g);
			
			MessageBox msgbox = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
			msgbox.setText("FlatGraph Metrics");
			msgbox.setMessage("The FlatGraph metrics were generated successfully into a spreadsheet in the project directory.");
			msgbox.open();
			
			}
			else {
				// The file "\src\src.womble.dot" does not exist
				MessageBox msgbox = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
				msgbox.setText("Flat Object Graph file");
				msgbox.setMessage("The following file does not exist: \n"+currentProjectPath + DOTFILE);
				msgbox.open();
			}
		}
		else {
			MessageBox box = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
			box.setMessage("Please open a Java file in the project to analyze");
			box.open();
			return;
		}

	}

	private void processGraph(Graph g) {
		Node[] nodes = g.getNodes();
		if(nodes != null ) {
			System.out.println("There are " + nodes.length + " nodes.");
			for(Node n:nodes){
				if (n instanceof BasicNode){
					 BasicNode basicNode = (BasicNode)n;
					 int inDegree = 0; 
					 if (basicNode.getIn()!=null)
						 inDegree = basicNode.getIn().length;
					 int outDegree = 0; 
					 if (basicNode.getOut()!=null)
						 outDegree = basicNode.getOut().length;			 
					 FlatGraphMetricItem fgmi = new FlatGraphMetricItem(basicNode.id,
							 basicNode.label,
							 basicNode.height,
							 inDegree,
							 outDegree
							 );
					 listOfObjects.put(basicNode.id,fgmi);
					 //how to get the label value without hacking toString()?
				}
			}
		}
    }
	
	/**
     * @return the listOfObjects
     */
    public Hashtable<String, FlatGraphMetricItem> getListOfObjects() {
    	return listOfObjects;
    }

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}