package oogre.adapter;

import java.util.Hashtable;

import oog.itf.IObject;
import secoog.SecGraph;
import secoog.SecObject;
import secoog.SecVisitorBase;


/**
 * we need to make sure we are passing secObjects to secGraph 
 * secGraph is relying a lot of equals using object reference, not by Oid or Did
 */
public class SecModelHelper {
	
	private SecGraph graph;
	
	private Hashtable<String,IObject> hashElems = new Hashtable<String, IObject>();
	
	class MyVisitor extends SecVisitorBase {
		
		public MyVisitor() {
	        super();
        }

		@Override
		public boolean visit(SecObject secObject) {
			addToMap(secObject.getO_id(), secObject);
			return super.visit(secObject);
		}

	}
	
	public SecModelHelper(SecGraph graph) {
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

