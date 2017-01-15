package oogre.analysis;

import java.util.Hashtable;
import java.util.Map.Entry;

import oog.common.OGraphFacade;
import oog.itf.IElement;
import oog.itf.IObject;
import oog.re.IRefinement;
import oog.re.RefinementModel;
import oog.re.RefinementState;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;

// TODO: Why not drop down to traceability info, look at new expr.
public class SanityCheckRefs {
	
	private Hashtable<String, RefinementState> mapTypeToRef = new Hashtable<String, RefinementState>();
	
	private RefinementModel refinementModel;

	private OGraph oGraph;
	
	class MyVisitor extends OGraphVisitorBase {
		
		public MyVisitor() {
	        super();
        }

		@Override
        public boolean visit(IObject oObject) {
			addToMap(oObject, oObject);
	        return super.visit(oObject);
        }

		public void reset() {
			mapTypeToRef.clear();
		}
	}
	

	public void addToMap(IObject oObject, IElement element) {
		String typeName = getTypeName(oObject.getObjectKey());
		
		if(oObject.isMainObject()) {
			typeName = "MainObject";
		}
		if(oGraph.getRoot() == oObject) {
			typeName = "DUMMY";
		}
		mapTypeToRef.put(typeName, RefinementState.Pending);
	}

	private String getTypeName(String objectKey) {
		int indexType = objectKey.indexOf('<');
		if (indexType != -1) {
			objectKey = objectKey.substring(0, indexType);
		}
	    return objectKey;
    }
	
	public RefinementState getElement(String key) {
	    return mapTypeToRef.get(key);
    }
	
	public SanityCheckRefs() {
		super();

		initFromFacade();
	}
	
	void doit() {
		
		pullAllObjects();
		
		updateRefinementState();
		
		checkMissingRefs();
	}
	
	private void checkMissingRefs() {
		
		System.err.println("*******************************************************");

		for(Entry<String, RefinementState> entry : mapTypeToRef.entrySet()) {
			RefinementState value = entry.getValue();
			if(value != RefinementState.Completed) {
				String typeName = entry.getKey();
				if (typeName.equals("MainObject") || typeName.equals("DUMMY")) {
					continue;
				}
				System.err.println("ATTEMPT A REFINEMENT WITH: " + typeName);
			}
		}
		
		System.err.println("*******************************************************");
    }

	private void updateRefinementState() {

		for(IRefinement facadeRef: refinementModel.getRefinements() ) {
			if (facadeRef.getState() == RefinementState.Completed ) {
				String objectKey = facadeRef.getSrcObject();

				// Match the first part of the object key (the fully qualified type)
				// against the type of the object
				String typeName = getTypeName(objectKey);

				RefinementState state = getElement(typeName);
				if (state != null) {
					mapTypeToRef.put(typeName, RefinementState.Completed);
				}
			}
		}
    }

	// Init objects from facade lazily
	private void initFromFacade() {
		OGraphFacade facade = oogre.plugin.Activator.getDefault().getMotherFacade();
		refinementModel = facade.getRefinementModel();
		oGraph = facade.getGraph();
	}
	
	private void pullAllObjects() {
		MyVisitor visitor = new MyVisitor();
		if (oGraph != null) {
			this.oGraph.accept(visitor);
		}
	}
}
