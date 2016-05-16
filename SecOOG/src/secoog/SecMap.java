package secoog;

import java.util.HashMap;
import java.util.Map;

import oog.itf.IEdge;
import oog.itf.IObject;

// TODO: Looks like this has to be an inner class of SecAnalysis
public class SecMap {

	// A SecObject wraps an OObject
	// Map: OObject->SecObject; 
	private Map<IObject, SecObject> oObjectToSecObject = new HashMap<IObject, SecObject>();
	
	// Map: SecObject->OObject
	private Map<SecObject, IObject> secObjectToOObject = new HashMap<SecObject, IObject>();
	
	// A SecEdge wraps an OEdge
	// Map: OEdge->SecEdge;
	private Map<IEdge, SecEdge> oEdgeToSecEdge = new HashMap<IEdge, SecEdge>();
	
	// Map SecEdge->OEdge
	private Map<SecEdge, IEdge> secEdgeToOEdge = new HashMap<SecEdge, IEdge>();
	
	private SecMap() {
	}

	private static SecMap s_instance = null;
	
	public static SecMap getInstance() {
		if (s_instance == null) {
			s_instance = new SecMap();
		}
		
		return s_instance;
	}
	
	
	// Create the mapping of objects
	public void mapObjects(IObject oObject, SecObject secObject) {
		this.oObjectToSecObject.put(oObject, secObject);
		this.secObjectToOObject.put(secObject, oObject);
	}
	
	public void mapEdges(IEdge oEdge, SecEdge secEdge) {
		this.oEdgeToSecEdge.put(oEdge, secEdge);
		this.secEdgeToOEdge.put(secEdge, oEdge);
	}

	public void clear() {
		this.oObjectToSecObject.clear();
		this.oEdgeToSecEdge.clear();
		this.secObjectToOObject.clear();
		this.secEdgeToOEdge.clear();
		// HACK:
//		s_instance = null;
	}
	
	/**
	 * returns the component corresponding to the OOject o
	 * 
	 * This is using a map: OObject->SecObject. Instead of using wrappers.
	 * 
	 * DONE. Rename. No longer using "Component" -> getSecObject
	 * */
	public SecObject getSecObject(IObject o){
		return oObjectToSecObject.get(o);
	}


	/**
	 * returns the OObject corresponding to the SecOject o
	 * 
	 * This is using a map: SecObject->OObject. Instead of using wrappers.
	 * */
	public IObject getOObject(SecObject o) {
		return secObjectToOObject.get(o);
	}

	/**
	 * returns the SecEdge corresponding to the OEdge e
	 * 
	 * DONE: Rename. No longer using "Connector" -> getSecEdge

	 * This is using a map: OEdge->SecEdge. Instead of using wrappers.
	 * TODO: Maybe CUT: do we really need this?	Keep for symmetry
	 * */
	public SecEdge getSecEdge(IEdge e) {
		return oEdgeToSecEdge.get(e);
	}


	/**
	 * returns the OEdge corresponding to the SecEdge e
	 * 
	 * This is using a map: SecEdge->OEdge. Instead of using wrappers.
	 * */
	public IEdge getOEdge(SecEdge e) {
		return secEdgeToOEdge.get(e);
	}
	
}
