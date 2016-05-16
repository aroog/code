package edu.wayne.ograph;

import static edu.wayne.ograph.OGraphStateMgr.ANALYSIS_STATE;
import static edu.wayne.ograph.OGraphStateMgr.RUN_OOGRE;
import static edu.wayne.ograph.OGraphStateMgr.RUN_POINTS_TO;
import static edu.wayne.ograph.OGraphStateMgr.TRUE;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Class to store some global options to control the tool stack 
 * 
 * XXX. It is in this project/package only because of the JSON library dependencies.
 *
 * TODO: Rename: AnalysisState
 */
public class GlobalState {

	@JsonProperty
	/*
	 * Set this to false to stop running OOGRE
	 * i.e,. just run PointsTo on the existing annotations and query the existing graph.
	 */
	private boolean runOOGRE = true;
	
	/*
	 * Set this to false to stop running PointsTo.
	 * i.e., just load the OGraph from XML, to initialize the facade and query the existing graph.
	 */
	private boolean runPointsTo = true;

	@JsonProperty
	/**
	 * OOGRE directive: load the initial annotations from code instead of overwriting them
	 */
	private boolean loadInitialAnnotations;

	public GlobalState(OGraphStateMgr stateMgr) {
	    super();
	    
		String runOOGRE = stateMgr.getGlobalPropertyValue(ANALYSIS_STATE, RUN_OOGRE);
		boolean boolRunOOGRE = runOOGRE != null ? TRUE.equals(runOOGRE) : true; // If not set, assume true!
		setRunOOGRE(boolRunOOGRE);
		
		String runPointsTo = stateMgr.getGlobalPropertyValue(ANALYSIS_STATE, RUN_POINTS_TO);
		boolean boolRunPointsTo = runPointsTo != null ? TRUE.equals(runPointsTo) : true; // If not set, assume true!
		setRunPointsTo(boolRunPointsTo);	    
    }

	public boolean isRunOOGRE() {
    	return runOOGRE;
    }

	public void setRunOOGRE(boolean runOOGRE) {
    	this.runOOGRE = runOOGRE;
    }
	
	public boolean isLoadInitialAnnotations() {
    	return loadInitialAnnotations;
    }

	public void setLoadInitialAnnotations(boolean loadInitialAnnotations) {
    	this.loadInitialAnnotations = loadInitialAnnotations;
    }
	
	public boolean isRunPointsTo() {
    	return runPointsTo;
    }

	public void setRunPointsTo(boolean runPointsTo) {
    	this.runPointsTo = runPointsTo;
    }
		
}
