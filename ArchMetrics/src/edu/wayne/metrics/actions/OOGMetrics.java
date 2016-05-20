package edu.wayne.metrics.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ast.ClassTable;
import ast.TypeInfo;
import edu.wayne.dot.DotExport;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.adb.TripletReporter;
import edu.wayne.metrics.adb.TripletVisitor;
import edu.wayne.metrics.qual.ClassifyUtlis;
import edu.wayne.metrics.qual.QualUtils;
import edu.wayne.metrics.datamodel.OOGMetricsReport;
import edu.wayne.metrics.internal.WorkspaceUtilities;
import edu.wayne.metrics.mapping.Model;
import edu.wayne.metrics.mapping.ModelManager;
import edu.wayne.metrics.mapping.Persist;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.MetricsXMLUtils;
import edu.wayne.ograph.OGraph;


public class OOGMetrics implements IWorkbenchWindowActionDelegate {

	
	private IWorkbenchWindow window = null;

	@Override
	public void run(IAction action) {
		
		// TORAD: We still NEED to handle the DGraph because Abstraction by Types is only on the DGraph.
		// (recall, one of the metric about how much compression we get from abstraction by types)
		// TORAD: when computing metrics on the OGraph, must detect cycles. Otherwise, can end up with infinite recursion;
		// TORAD: also, this requires a src.ograph.xml being exported by the latest version of ArchRecJ.
		// TORAD: uncomment this for OGraph metrics
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);
		
		// Get Java Project
		// TODO: This requires generating a warning when multiple Java projects are open.
		IJavaProject javaProject = WorkspaceUtilities.getJavaProject(currentProject.getName());
		if (javaProject != null ) {
//			// Register the TypeHierarchyFactory since we need to use Type.isSubtypeCompatible
//			try {
//	            TypeHierarchyFactory.getInstance().registerProject(javaProject);
//            }
//            catch (JavaModelException e) {
//	            e.printStackTrace();
//            }
		}
		
		// Use jackhammer solution: pull in all the ITypeBindings.
		// TODO: Switch over to using TypeHierarchy
		Crystal crystal = Crystal.getInstance();
		crystal.runAnalyses();
		crystal.finish();

		// For loading the metrics map
		IFile mapFile = currentProject.getFile("metrics_map.xml");
		Model model = null;
		if (mapFile.exists()) {
			String mapping_file_path = mapFile.getLocation().toOSString();
			model = Persist.load(mapping_file_path);
		}
		else {
			// Create a blank model
			model = new Model();
		}

		// Store model in the singleton
		ModelManager.getInstance().setModel(model);
		
		// Store current project to get the location of file.
		QualUtils.getInstance().setProject(currentProject);
		
		
		//ClassifyUtlis classify = new ClassifyUtlis(currentProject);
		
		// For computing the code structure metrics
		IFolder folder = currentProject.getFolder("src");
		IFile file = folder.getFile("src.code.xml");
		MetricsXMLUtils.readXML(file);
		
		// For computing the OGraph metrics
		OOGMetricsOGraph oMetrics = new OOGMetricsOGraph();
		oMetrics.init(this.window);
		oMetrics.run(action);

		OGraph runtimeModel = oMetrics.getRuntimeModel();
		if ( runtimeModel ==   null ) {
			System.err.println("Cannot load OGraph");
			return;
		}
		
		OOGMetricsReport oGraphReport = new OOGMetricsReport(currentProject,
		        oMetrics.getListOfObjects(),
		        oMetrics.getListOfDomains(),
		        oMetrics.getNoOfEdges(),
		        oMetrics.getTotalTCE());
		
		try {
			DotExport dotExport = new DotExport(runtimeModel);
			String dotPath = currentProject.getLocation().append(currentProject.getName() + "_ograph.dot").toOSString();
			dotExport.writeToDotFile(dotPath);

			oGraphReport.oObjectsReport("_OOGMetrics_OGraph.csv", "_OOGMetrics_OGraph_Table.csv");

			// Build reverse traceability map
			ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
			instance.init(runtimeModel);

					
			// Launch the triplets visitor
			TripletVisitor visitor = new TripletVisitor();
			runtimeModel.accept(visitor);

			// Launch the triplets reporter
			TripletReporter tripletReporter = new TripletReporter(currentProject,
			        runtimeModel,
			        visitor.getAllTriplets(),
			        visitor.getAllObjects());
			
			// Compute OObjects as triplets on the OGraph
			// Do not use long name
			// tripletReporter.tripletsReport("_ADBTriplets_OGraph_Table.csv");
			// Use shorter name:
			tripletReporter.tripletsReport(".csv");

			// TOMAR: TODO: Was it das?
			// Compute OObjects as triplets on the OGraph together with their traceability info.
			// tripletReporter.tripletsWithTraceabilityReport("_ADBTripletsWithTraceability_OGraph_Table.csv");
			// Compute OEdges as triplets on the OGraph
			// Do not use long name
			// tripletReporter.edgeTripletsReport("_EdgeTriplets_OGraph_Table.csv");
			// Use shorter name:
			tripletReporter.edgeTripletsReport(".csv");
			tripletReporter.writeSummary(".csv");
			

		}
		catch (IOException ex) {
			System.err.println("could not save file" + ex.getMessage());
		}

		// TODO: Uncomment to display mesage box.
		// MessageBox msgbox = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
		// msgbox.setText("Arch Metrics");
		// msgbox.setMessage("The metrics were generated successfully.");
		// msgbox.open();
		
		System.out.println("*******    ArchMetrics: the metrics were generated successfully    *******");

		// Important: cleanup
		reset();
		 
/*		TOMAR: TODO: HIGH. FIX ME. NO DGraph yet.
		OOGMetricsDGraph dMetrics = new OOGMetricsDGraph("src.xml");
		dMetrics.init(this.window);
		dMetrics.run(action);
		OOGMetricsReport dGraphReport = 
			new OOGMetricsReport(currentProject, dMetrics.listOfObjects, dMetrics.listOfDomains, oMetrics.getAllTriplets(),oMetrics.getAllEdgeTriplets(),
					dMetrics.getNoOfEdges(), dMetrics.getTotalTCE());
		try{
			//HACK: last argument--I'm not handling DGraph yet.
			dGraphReport.oObjectsReport("_OOGMetrics_DGraph.csv", "_OOGMetrics_DGraph_Table.csv");
		}
		catch(IOException ex)
		{
			System.err.println("could not save file"+ex.getMessage());
		}

		OOGMetricsDGraph abtMetrics = new OOGMetricsDGraph("src.abt.xml");
		abtMetrics.init(this.window);
		abtMetrics.run(action);
		OOGMetricsReport abtGraphReport = new OOGMetricsReport(currentProject, abtMetrics.listOfObjects, abtMetrics.listOfDomains,
				abtMetrics.getListOfTriplets(),abtMetrics.getListOfEdgeTriplets(),
				abtMetrics.getNoOfEdges(), abtMetrics.getTotalTCE());
		try{
			//HACK: find a better way to get this result, without using a constant.
			abtGraphReport.abtf = dGraphReport.objectsInDomains.getMean();
			abtGraphReport.tlabtf = dGraphReport.tlo;
			abtGraphReport.abhtf = dMetrics.listOfObjects.size();
			abtGraphReport.oObjectsReport("_OOGMetrics_ABTDGraph.csv", "_OOGMetrics_ABTDGraph_Table.csv");
		}
		catch(IOException ex)
		{
			System.err.println("could not save file"+ex.getMessage());
		}
		

 
		 FlatGraphMetrics fgMetrics = new FlatGraphMetrics("src.womble.dot");
		 fgMetrics.init(this.window);
		 fgMetrics.run(action);
		 
		 FlatGraphMetricReport flatGraphReport = new FlatGraphMetricReport(fgMetrics.getListOfObjects(), abtMetrics.getListOfObjects(), currentProject);
		 try{
			 flatGraphReport.collectAllData();
		 }
		 catch(IOException ex)
		 {
			 System.err.println("could not save file"+ex.getMessage());
		 }

		 String oogtablePath = currentProject.getLocation().append(currentProject.getName() + "_OOGMetrics_FinalTable.csv").toOSString();
		 final String header = abtGraphReport.TABLE_HEADER_DGRAPH+","+flatGraphReport.METRIC_HEADER+"\n";
		 try{
		 CSVOutputUtils.writeTableHeaderToFile(oogtablePath, header);
		 DecimalFormat twoDForm = new DecimalFormat("#.##");
		 final String tableline = oGraphReport.saveTableLine() + twoDForm.format(abtGraphReport.abtf)+","+ twoDForm.format(abtGraphReport.tlabtf)+","+ twoDForm.format(abtGraphReport.abhtf)+","+flatGraphReport.saveLineMetrics(); 					;		 			
		 		
		 CSVOutputUtils.appendRowToFile(oogtablePath,  currentProject.getName(), tableline);
		 }
		 catch(IOException ex)
		 {
			 System.err.println("could not save final file"+ex.getMessage());
		 }
		 */
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

	// Important: clear the hashtables!
	private void reset() {
		
		// Reset the Crystal state
		Crystal.getInstance().reset();

		// Reset all the hashtables
		// TODO: Look for all the singletons... There are too many!
		
		// NOTE: TypeInfo.reset() calls TypeAdapter.reset();
		TypeInfo.getInstance().reset();
		ClassTable.getInstance().reset();
		
		ReverseTraceabilityMap.getInstance().reset();
	}
	
}