package edu.wayne.summary.actions;

import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OOGUtils;
import edu.wayne.ograph.OObject;
import edu.wayne.summary.Crystal;
import edu.wayne.summary.facade.Facade;
import edu.wayne.summary.facade.FacadeImpl;
import edu.wayne.summary.facade.TypeStringConverter;
import edu.wayne.summary.facade.TypeStringConverterImpl;
import edu.wayne.summary.internal.WorkspaceUtilities;
import edu.wayne.summary.strategies.OGraphSingleton;
import edu.wayne.summary.strategies.Utils;
import edu.wayne.summary.test.MD;
import edu.wayne.summary.traceability.ReverseTraceabilityMap;
import edu.wayne.summary.utils.JavaElementUtils;

public class OGraphSummary implements IWorkbenchWindowActionDelegate {

	private static final String SRC_FOLDER = "src"; // by default

	private static final String OGRAPH_XML_GZIP = "OOG.xml.gz";
	// by default parse the following xml file, use constructor to set a
	// different one.
	private static String SRC_OGRAPH_XML = "src.ograph.xml";

	private IPath currentProjectPath;

	private String summaryFile;
	// TODO: Clean up the fields related to output if not in use.
	private PrintStream summaryStream;

	private IWorkbenchWindow window = null;

	public OGraphSummary() {
	}

	@Override
	public void run(IAction action) {
		OGraph graph = null;

		// Set the project
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);
		if (currentProject != null) {
			// Get Java Project
			// TODO: This requires generating a warning when multiple Java projects are open.
			IJavaProject javaProject = WorkspaceUtilities.getJavaProject(currentProject.getName());
			if (javaProject != null) {
				JavaElementUtils.setJavaProject(javaProject);
			}
			else  {
				System.err.println("Error: null Java project");
			}

			// XXX. If not running standalone, no need to run Crystal again
			// Run Crystal
			Crystal crystal = Crystal.getInstance();
			crystal.runAnalyses();
			crystal.finish();

			currentProjectPath = currentProject.getLocation();

			String projectName = currentProject.getName();
			String summaryFileName = projectName + "_DEBUG.txt";
			summaryFile = currentProjectPath.append(summaryFileName)
					.toOSString();
			// First, look for the Zip file;
			String filePath = getFilePath(currentProject, OGRAPH_XML_GZIP);
			if(filePath != null){
				graph = OOGUtils.loadGZIP(filePath);
			}else{
				// If not, look for the non-zip file
				filePath = getFilePath(currentProject, SRC_OGRAPH_XML);
				if (filePath != null ) {
					graph = OOGUtils.load(filePath);
				}
			}


			if (graph != null) {


				try {
					summaryStream = new PrintStream(summaryFile);

					oOGObjectsReport(graph);
					summaryStream.close();

					MessageBox msgbox = new MessageBox(window.getShell(),
							SWT.ICON_INFORMATION);
					msgbox.setText("OOG Summary");
					msgbox.setMessage("The summaries were generated in the project directory in "
							+ summaryFileName);
					msgbox.open();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				MessageBox msgbox = new MessageBox(window.getShell(),
						SWT.ICON_INFORMATION);
				msgbox.setText("OOG source file");
				msgbox.setMessage("Cannot load an Ograph. Missing or bad file in: \n" + currentProject.getLocation().toOSString() + filePath);
				msgbox.open();
			}
		} else {
			MessageBox box = new MessageBox(window.getShell(),
					SWT.ICON_INFORMATION);
			box.setMessage("Please open a Java file in the project to analyze");
			box.open();
			return;
		}
		
		reset();
	}

	public void oOGObjectsReport(OGraph graph) throws IOException {

		// Just set the root object on the facade;
		// The FacadeImpl must traverse the OOG to figure out what to do with
		// the information
		/*
		 * Facade facade = FacadeImpl.getInstance();
		 * facade.setRuntimeModel(this.runtimeModel);
		 */

		Utils.loadSummary(graph, JavaElementUtils.getJavaProject());

// XXX. For testing only
/*		// 1. Load MiniDraw in the Eclipse child window
		// 2. Invoke the test
		MD mdTest = new MD();
		// mdTest.testMostImportantStuff();
		mdTest.testStrategies(currentProjectPath);
		mdTest.runTest();
*/
	}


	private String getFilePath(IProject currentProject, String filename) {
		String path = null;
		
		IPath relativePath = new Path(filename);
		IResource findMember = currentProject.findMember(relativePath);
		if (findMember instanceof IFile) {
			IFile file = (IFile)findMember;
			if (file.exists() ) {
				path = file.getLocation().toOSString();
			}
		}
		return path;
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
	private void reset(){
		// Reset the Crystal state
		Crystal.getInstance().reset();
		// Reset all the hashtables
		OGraphSingleton.getInstance().reset();
		ReverseTraceabilityMap.getInstance().reset();
	}
}