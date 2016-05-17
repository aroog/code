package oog.common;

import java.util.ArrayList;
import java.util.List;

import oog.heuristics.HeuristicsModel;
import oog.re.Persist;
import oog.re.RefinementModel;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphState;


/**
 *
 * Move to this class any dependencies on ScoriaX.
 * This will be the interface to the MotherAnalysis.
 * 
 * TODO: HIGH. For now, just clean up Facade.
 * 
 * Expose other options too.
 *
 */
public class MotherFacade implements OGraphFacade {
	
	private List<GraphChangeListener> graphListeners = new ArrayList<GraphChangeListener>();
	
//	private List<RefinementsChangedListener> refinementsListeners = new ArrayList<RefinementsChangedListener>();

	private static MotherFacade instance;
	
	// Project path
	private String path;
	
	// Fully qualified class name of the root class, read from the Config file
	private String rootClass;
	
	private OGraph graph = null;
	
	private OGraphState graphState = null;

	private boolean isExtractionSuccess = true;
	
	private boolean isInferenceSuccess = true;
	
	private RefinementModel model;
	
	private HeuristicsModel heuristicsModel;

	/**
	 * Private constructor to enforce singleton
	 */
	private MotherFacade() {
	}
	
	public static OGraphFacade getInstance() {
		if(instance == null ) {
			instance = new MotherFacade();
			// XXX. What if we are reading this from file?
			instance.model = new RefinementModel();
		}
		
		return instance;
	}
	
	@Override
    public OGraph getGraph() {
	    return graph;
    }

	@Override
    public void sayHello(String arg0) {
		System.err.println(arg0 + " says hello!");
    }

	@Override
    public void setGraph(OGraph graph) {
		this.graph = graph;
		
		// Notify observers
//		if (graph != this.graph) {
//			for(GraphChangeListener l : graphListeners ) {
//				l.graphChanged();
//			}
//		}
    }

	public void notifyGraphChanged() {
		for(GraphChangeListener listener : graphListeners) {
			listener.graphChanged();
		}
	}
	
	@Override
    public void addGraphChangeListener(GraphChangeListener listener) {
	    this.graphListeners.add(listener);
    }

	@Override
    public RefinementModel getRefinementModel() {
	    return model;
    }

	// XXX. What do we need a public setter? Use loadRefinementModel instead
    public void setRefinementModel(RefinementModel model) {
    	this.model = model;
    }

    public void loadRefinementModel(String refinementFile) {
		this.model = Persist.load(refinementFile);
    }
    
	public HeuristicsModel getHeuristicsModel() {
    	return heuristicsModel;
    }

	// XXX. We need a public setter to load data from file. Use instead loadHeuristicsModel
	public void setHeuristicsModel(HeuristicsModel heuristicsModel) {
    	this.heuristicsModel = heuristicsModel;
    }

	@Override
    public void loadHeuristicsModel(String heuristicsFile) {
		this.heuristicsModel = HeuristicsModel.load(heuristicsFile);
	    
    }

	public String getPath() {
    	return path;
    }

	public void setPath(String path) {
    	this.path = path;
    }

	public String getRootClass() {
    	return rootClass;
    }

	public void setRootClass(String rootClass) {
    	this.rootClass = rootClass;
    }

	@Override
    public boolean isExtractionSuccess() {
	    return isExtractionSuccess;
    }

	@Override
    public void setExtractionSuccess(boolean status) {
	    isExtractionSuccess = status;
    }

	@Override
    public boolean isInferenceSuccess() {
	    return isInferenceSuccess;
    }

	@Override
    public void setInferenceSuccess(boolean status) {
		isInferenceSuccess = status;
    }

	@Override
    public OGraphState getGraphState() {
	    return graphState;
    }

	@Override
    public void setGraphState(OGraphState state) {
		graphState = state;
    }
	
//	public static void setInstance(OGraphFacade service) {
//		instance = ((MotherFacade)service);
//		instance.model = ((MotherFacade)service).model; 
//    }
}
