package moogrex.testing;

import java.util.Hashtable;

import oog.itf.IDomain;
import oog.itf.IObject;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;


/**
 * TODO: Rename: -> IdToElementMapper
 *
 * TODO: Move this into another Jar. Reusable. With RuntimeModel.
 * 
 * Map: OObjectKey to OObject
 */
public class OObjectKeyMapper {
	
	private OGraph graph;
	
	private Hashtable<String,IObject> hashElems = new Hashtable<String, IObject>();

	private TypeResolver resolver = new TypeResolver();
	
	class MyVisitor extends OGraphVisitorBase {
		
		public MyVisitor() {
	        super();
        }

		@Override
        public boolean visit(IObject oObject) {
			addToMap(oObject.getObjectKey(), oObject);
			resolver.addObject(oObject);
	        return super.visit(oObject);
        }

		@Override
        public boolean visit(IDomain oDomain) {
	        return super.visit(oDomain);
        }
	}
	
	public OObjectKeyMapper(OGraph graph) {
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
	
	public IObject getElement(String oObjectKey) {
	    IObject iObject = hashElems.get(oObjectKey);
	    // Dangling ObjectKey; try looking up by type
	    if(iObject == null ) {
	    	iObject = resolver.getObject(oObjectKey);
	    }
		return iObject;
    }

	public void reset() {
		hashElems.clear();
	}
}

