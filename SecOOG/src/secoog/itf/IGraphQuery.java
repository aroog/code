package secoog.itf;

import java.util.Set;

import conditions.Condition;
import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IObject;
import secoog.EdgeType;
import secoog.Property;

//TODO: make this a generic interface
public interface IGraphQuery {

	//TODO:XXX: add back isREachableFrom(...)
	
	/**
	 * 
	 * @param cond
	 * @return all objects that satisfy a given condition
	 */
	<T extends IObject> Set<T> getObjectsByCond(Condition<IObject> cond);
	//Set<? extends IObject> getObjectsByCond(Condition<IObject> cond);
	
	/**
	 *  Check if an element has an expected property value
	 * @param elem
	 * @param p
	 * @return
	 */
	boolean hasPropertyValue(IElement elem, Property p);
	
	/**
	 * 
	 * @param elem
	 * @param propertyName
	 * @return given a property return the value. The architect does not know what value to expect 
	 */
	Property getProperty(IElement elem, String propertyName);
	
	/***
	 * assign all objects/edges/domain that satisfy one or multiple condition a given property value 
	 * @param p
	 * @param c
	 * @return true if such objects were found
	 */
	boolean setObjectProperty(Property p, Condition<IObject> c);	
	boolean setObjectProperty(Property p, Condition<IObject>[] c);
	
	boolean setEdgeProperty(Property p, Condition<IEdge> c);
	boolean setEdgeProperty(Property p, Condition<IEdge>[] c);
	
	boolean setDomainProperty(Property p, Condition<IDomain> c);
	boolean setDomainProperty(Property p, Condition<IDomain>[] c);

	/***
	 * 
	 * calls connected(src,dst,eType=EdgeType.DataFlow,isDirected=false,useAncs=false,usedDescs=true,useTrans=false)
	 * */
	<T extends IEdge> Set<T> connected(IObject a, IObject b);
	
	/***
	 * 
	 * returns the set of edges that connect two objects. 
	 * */
	//TODO: should eType be an array?
	//TODO: no hardcoded edge type - look everywhere
	<T extends IEdge> Set<T> connected(IObject src, IObject dst, EdgeType[] eTypes, boolean isDirected, boolean useAncs,
			boolean useDescs, boolean useTrans, boolean useReach);
	//add defaults for booleans
		

	Set<IObject> getReachableObjects(IObject o1, EdgeType eType);

	//default implementations of connected 
	<T extends IEdge> Set<T> connectedByEdgeType(IObject src, IObject dst, EdgeType[] eType);
	<T extends IEdge> Set<T> connectedByUndirected(IObject src, IObject dst, EdgeType[] eTypes);
	<T extends IEdge> Set<T> connectedByAncs(IObject src, IObject dst);
	<T extends IEdge> Set<T> connectedByDescs(IObject src, IObject dst);
	<T extends IEdge> Set<T> connectedByObjectReachability(IObject src, IObject dst, EdgeType[] eType);
	<T extends IEdge> Set<T> connectedByEdgeTransitiviy(IObject src, IObject dst, EdgeType[] eType);
	
	/***
	 * @param src
	 * @param dst
	 * @param flow
	 * @return set of dataflow edges through which the objec flow can go from src to dst
	 */
	Set<? extends IEdge> connectedByObjectTransitivity(IObject src, IObject dst, IObject flow);

	<T extends IEdge> Set<T> connectedByDF(IObject a, IObject b);
	
	<T extends IEdge> Set<T> connectedByPT(IObject a, IObject b);

}