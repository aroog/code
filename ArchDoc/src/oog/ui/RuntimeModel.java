package oog.ui;

import org.eclipse.jdt.core.JavaModelException;

import oog.itf.IElement;
import oog.ui.utils.LoadUtils;
import oog.ui.viewer.RuntimeModelHelper;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.cmu.cs.viewer.objectgraphs.runtimegraph.RuntimeReport;
import edu.wayne.ograph.OGraph;
import edu.wayne.summary.Crystal;
import edu.wayne.summary.strategies.EdgeSummary;
import edu.wayne.summary.strategies.EdgeSummaryAll;
import edu.wayne.summary.strategies.OGraphSingleton;

// XXX. Move this class into common JAR?
public class RuntimeModel {

	private static RuntimeModel instance;

	// Store the display model here.
	private DisplayModel displayModel = null;
	
	private OGraph graph;
	
	private boolean isSummaryComputed = false;

	private EdgeSummary edgeSummary;
	
	public OGraph getGraph() {
		return graph;
	}
	
	private RuntimeModelHelper helper = null;

	private RuntimeModel(){
		
	}
	
	public static RuntimeModel getInstance(){
		if(instance == null || instance.graph ==null){
			instance = new RuntimeModel();
			instance.graph = LoadUtils.loadModel();
			// If no graph loaded (does not exist yet)
			if (instance.graph != null) {
				// XXX. Make this optional				
				instance.helper = new RuntimeModelHelper(instance.graph);
			}
			instance.edgeSummary = new EdgeSummaryAll();
		}
		
		return instance;
	}
	
	public EdgeSummary getSummaryInfo(){
		if (this.graph != null && !isSummaryComputed) {
			try {
				if (edgeSummary != null )
					edgeSummary.compute();
				isSummaryComputed = true;
			} catch (JavaModelException exception) {
				exception.printStackTrace();
				isSummaryComputed=false;
			}
		}
		return edgeSummary;
	}
	
	/**
	 * Reset the state on the singleton.
	 * 
	 * Call this when the graph changes.
	 * 	
	 * XXX. Shouldn't we just re-set the graph?!
	 * - Do we really need to reset summary.Crystal? Probably not! 
	 */
	public static void invalidate(){

//		if(instance.graph != null ) {
//			instance.graph = null;
//		}
		instance = null;
		
		Crystal.getInstance().reset();
		
		// Reset the ArchSummary singleton too!
		OGraphSingleton.getInstance().reset();
	}
	
	public DisplayModel getDisplayModel() {
		if (displayModel == null && graph != null) {
			displayModel = new DisplayModel();
			RuntimeReport visualReport = new RuntimeReport(graph, displayModel);
			visualReport.generateGraph();
			
			// Update the hashtables
			displayModel.finish();
		}
    	return displayModel;
    }

	public void setDisplayModel(DisplayModel displayModel) {
    	this.displayModel = displayModel;
    }

	
	public IElement getElement(String url) {
	    return this.helper.getElement(url);
    }	
}
