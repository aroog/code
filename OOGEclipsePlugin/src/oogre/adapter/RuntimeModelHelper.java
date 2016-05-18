package oogre.adapter;

import java.util.Hashtable;

import oog.itf.IDomain;
import oog.itf.IElement;
import oog.itf.IObject;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;


/**
 * TODO: Rename: -> IdToElementMapper
 *
 * TODO: Move this into another Jar. Reusable. With RuntimeModel.
 */
public class RuntimeModelHelper {
	
	private OGraph graph;
	
	private Hashtable<String,IObject> hashElems = new Hashtable<String, IObject>();
	
	class MyVisitor extends OGraphVisitorBase {
		
		public MyVisitor() {
	        super();
        }

		@Override
        public boolean visit(IObject oObject) {
			addToMap(oObject.getO_id(), oObject);
	        return super.visit(oObject);
        }


		@Override
        public boolean visit(IDomain oDomain) {
	        return super.visit(oDomain);
        }
	}
	
	public RuntimeModelHelper(OGraph graph) {
		super();

		this.graph = graph;
		
		updateMap();
	}
	
	private void updateMap() {
		MyVisitor visitor = new MyVisitor();
		this.graph.accept(visitor);
	}
	
	private void addToMap(String key, IObject element) {
		this.hashElems.put(key, element);
	}
	
	public IObject getElement(String key) {
	    return hashElems.get(key);
    }

	public void reset() {
		hashElems.clear();
	}
}

