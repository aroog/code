package edu.wayne.ograph;

import static edu.wayne.ograph.OGraphStateMgr.GRAPH_STATE;
import static edu.wayne.ograph.OGraphStateMgr.TRUE;

/**
 * Class to store some global options to control the display (of the graph, etc.) 
 * 
 */
public class DisplayState {

	private boolean showEdgeLabels = false;

	// XXX. Extract constants for defaults for showPT and showDF
	private boolean showPtEdges = true;
	
	private boolean showDfEdges = true;
	
	private boolean showCrEdges = false;

	public DisplayState(OGraphStateMgr stateMgr) {
	    super();
	    
	    // XXX. Extract constants
	    String showPtStr = stateMgr.getGlobalPropertyValue(GRAPH_STATE, "showPtEdges");
	    // If state is not set, at least show PT edges. Otherwise, we end up with no edges at all in DisplayGraph
	    boolean showPt = (showPtStr == null || TRUE.equals(showPtStr)) ? true : false; // If showPtStr not set, assume true (default)!
	    setShowPtEdges(showPt);

	    String showDfStr = stateMgr.getGlobalPropertyValue(GRAPH_STATE, "showDfEdges");
	    boolean showDf = (showDfStr == null || TRUE.equals(showDfStr) )? true : false; // If showDfStr not set, assume true (default)!
	    setShowDfEdges(showDf);

	    String showCrStr = stateMgr.getGlobalPropertyValue(GRAPH_STATE, "showCrEdges");
	    boolean showCr = TRUE.equals(showCrStr) ? true : false;
	    setShowCrEdges(showCr);

	    String showEdgeLabelsStr = stateMgr.getGlobalPropertyValue(GRAPH_STATE, "showEdgeLabels");
	    boolean showEdgeLabels = TRUE.equals(showEdgeLabelsStr) ? true : false;
	    setShowEdgeLabels(showEdgeLabels);
    }

	public boolean isShowDfEdges() {
    	return showDfEdges;
    }

	public void setShowDfEdges(boolean showDfEdges) {
    	this.showDfEdges = showDfEdges;
    }

	public boolean isShowEdgeLabels() {
    	return showEdgeLabels;
    }

	public void setShowEdgeLabels(boolean showEdgeLabels) {
    	this.showEdgeLabels = showEdgeLabels;
    }

	public boolean isShowPtEdges() {
    	return showPtEdges;
    }

	public void setShowPtEdges(boolean showPtEdges) {
    	this.showPtEdges = showPtEdges;
    }

	public boolean isShowCrEdges() {
    	return showCrEdges;
    }

	public void setShowCrEdges(boolean showCrEdges) {
    	this.showCrEdges = showCrEdges;
    }
	

		
}
