package oogre.refinements.tac;

import java.util.Hashtable;

import oog.itf.IDomain;
import oog.itf.IElement;
import oog.itf.IObject;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;


public class MappingHelper {
	
	private OGraph graph;
	
	private Hashtable<String,IElement> hashElems = new Hashtable<String, IElement>();
	
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
			// addToMap(oDomain.getD_id(), oDomain);
	        return super.visit(oDomain);
        }
	}
	
	public MappingHelper(OGraph graph) {
		super();

		this.graph = graph;
		
		updateMap();
	}
	
	private void updateMap() {
		MyVisitor visitor = new MyVisitor();
		this.graph.accept(visitor);
	}
	
	public void addToMap(String key, IElement element) {
		this.hashElems.put(key, element);
	}
	
	public IElement getElement(String key) {
	    return hashElems.get(key);
    }

	public void reset() {
		hashElems.clear();
	}
}
