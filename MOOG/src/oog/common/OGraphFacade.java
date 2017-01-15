package oog.common;

import oog.heuristics.HeuristicsModel;
import oog.re.RefinementModel;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphState;


/**
 *
 * Move to this class any dependencies on ScoriaX.
 * This will be the interface to the MotherAnalysis.
 * 
 * WishList:
 * - reloadGraph()
 * - graphChanged() 
 * 
 * - refinementsChanged?
 * 
 * XXX. Expose projectName too.
 * 
 */
public interface OGraphFacade {
	
	OGraph getGraph();
	
	void setGraph(OGraph graph);
	
	void notifyGraphChanged();
	
	void addGraphChangeListener(GraphChangeListener listener);
	
	/**
	 * For testing purposes only
	 */
	void sayHello(String plugin);

	RefinementModel getRefinementModel();

	// XXX. What does this setter need to be on Facade? Instead use loadRefinementModel.
	void setRefinementModel(RefinementModel model);
	
	HeuristicsModel getHeuristicsModel();

	void loadHeuristicsModel(String heuristicsFile);
	
	void loadRefinementModel(String refinementFile);

	String getPath();
	
	void setPath(String path);

	String getRootClass();

	void setRootClass(String rootClass);
	
	/**
	 * @return true if last OGraph extraction was successful 
	 */
	boolean isExtractionSuccess();
	
	void setExtractionSuccess(boolean status);

	OGraphState getGraphState();
	
	void setGraphState(OGraphState state);

	/**
	 * @return true if last refinement was successful; false, if it was unsupported 
	 */
	boolean isInferenceSuccess();
	
	void setInferenceSuccess(boolean status);

	boolean isAuto();
	
	void setAuto(boolean auto);
	
}

