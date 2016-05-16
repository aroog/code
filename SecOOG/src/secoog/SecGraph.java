package secoog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import ast.AstNode;
import ast.BaseTraceability;
import ast.MethodInvocation;
import conditions.Condition;
import edu.wayne.ograph.OGraphVisitor;
import graph.TransitiveClosure;
import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IGraph;
import oog.itf.IObject;
import secoog.itf.IConstraints;

//TODO: No common superclass as SecObject, SecEdge, etc.
//TODO: Maybe have SecGraph implement ISecVisitable
public class SecGraph extends AbstractGraphQuery implements IGraph,IConstraints{
	private static SecGraph s_instance = null;
	
	private SecGraph() {
	}
	
	public static SecGraph getInstance() {
		if (s_instance == null) {
			s_instance = new SecGraph();
		}
		
		return s_instance;
	}
	
	// TODO: Hold on to root object separately?
	private SecObject root;
	
	private Set<SecEdge> edges = new HashSet<SecEdge>();

	private Set<DataFlowEdge> dataflows =  new HashSet<DataFlowEdge>();

	private Set<PointsToEdge> pointsto = new HashSet<PointsToEdge>();

	private Set<CreationEdge> creations = new HashSet<CreationEdge>();

	private Set<ControlFlowEdge> controlflows = new HashSet<ControlFlowEdge>();
	
	void addEdge(SecEdge secEdge) {
		// Cache into into subsets 
		this.edges.add(secEdge);
		
		switch (secEdge.edgeType) {
		case PointsTo:
			pointsto.add((PointsToEdge) secEdge);
			break;
		case DataFlow:
			dataflows.add((DataFlowEdge) secEdge);
			break;
		case Creation:
			creations.add((CreationEdge) secEdge);
			break;
		case ControlFlow:
			controlflows.add((ControlFlowEdge) secEdge);
			break;
		default:
			// do nothing
		}
	}

	/**
	 * return set of edges given an EdgeType
	 * can use Set<DataFlowEdge> dfdges = getEdgesByType(EdgeType.Dataflow)
	 * be careful not to mix up the type with the argument,
	 * if you are not sure return Set<SecEdge> 
	 * */
	public <T extends SecEdge> Set<T> getEdgesByType(EdgeType eType) {
		switch (eType) {
		case PointsTo:
			return (Set<T>) pointsto;
		case DataFlow:
			return (Set<T>) dataflows;
		case Creation:
			return (Set<T>) creations;
		case ControlFlow:
			return (Set<T>) controlflows;

		default:
			return new HashSet<T>();
		}
	}

	
	/**
	 * returns reachable objects starting from o1. for o1->o2->o3, returns o2 and o3 
	 * */
	@Override
	public Set<IObject> getReachableObjects(IObject o1, EdgeType eType) {
		Set<IObject> reachableObjects = new HashSet<IObject>();
		List<SecEdge> edgesAsList = getEdgesAsList(eType);
		List<IObject> verticesAsList = getObjectsAsList();		
		TransitiveClosure<SecEdge, IObject> closure = new TransitiveClosure<SecEdge, IObject>(verticesAsList, edgesAsList);
		for (IObject o: verticesAsList){
			//HACK: translate closure in terms of interfaces. 
			if (closure.hasPath(o1, o))
				reachableObjects.add(o);
		}
		return reachableObjects;
		
	}

	/****
	 * returns incoming edges into a given object
	 * the user can specify the type of edges 
	 * calls getInEdgesByDesc with useDescs = false;
	 * */
	public <T extends SecEdge> Set<T> getInEdges(IObject secObject, EdgeType eType) {
		return getInEdgesByDescs(secObject, eType, false);
	}

	/****
	 * returns incoming edges into a given object
	 * the user can specify the type of edges 
	 * useDescs = false does not consider incoming edges through descendants ;
	 * */
	public <T extends SecEdge> Set<T> getInEdgesByDescs(IObject obj, EdgeType eType, boolean useDescs) {
		Set<T> inEdges = new HashSet<T>();
		Set<T> allEdges = getEdgesByType(eType);
		for(T edge :allEdges )
			if (edge.dst.equals(obj)){
				inEdges.add(edge);
			}
		if (useDescs){
			for (IObject o:obj.getDescendants()) {
				Set<T> inEdgesByDescs = getInEdgesByDescs(o,eType,false);
				inEdges.addAll(inEdgesByDescs);
			}			
		}
		return inEdges;	
	}
	
	/**
	 * returns outgoing edges into a given object
	 * the user can specify the type of edges 
	 * */
	public <T extends SecEdge> Set<T> getOutEdges(IObject secObject, EdgeType eType) {
		Set<T> outEdges = new HashSet<T>();
		Set<T> allEdges = getEdgesByType(eType);
		for(T edge :allEdges )
			if (edge.src.equals(secObject))
				outEdges.add(edge);
		return outEdges;
	}	
	

	/**
	 * return the set of all the edges in all the paths from source to sink where every dataflow in the path has the same flow
	 *
	 * given: source, sink and o	
	 * source--o:O-->a:A--o:O-->b:B--o:O-->sink
	 * source--o:O-->c:C--o:O-->sink
	 * returns {source->a, a->b, b->sink, sounce->c, c->sink}  
	 * 
	 * if no such path exists, returns empty set.
	 * */
	public Set<DataFlowEdge> getTransitiveCommunication(IObject source, IObject sink, IObject flow){
		Set<DataFlowEdge> dataflows = new HashSet<DataFlowEdge>();
		//consider only the edges that have the given flow
		List<DataFlowEdge> edgesAsList = getEdgesByFlow(flow);
		List<IObject> verticesAsList = getObjectsAsList();		
		TransitiveClosure<DataFlowEdge, IObject> closure = new TransitiveClosure<DataFlowEdge, IObject>(verticesAsList, edgesAsList);
		//return all the dataflows such that there is a path from edge.dst to sink.
		for (DataFlowEdge edge: edgesAsList){
			if (closure.hasPath(edge.src, sink)
					&& closure.hasPath(source, edge.dst))
				dataflows.add(edge);
		}
		return dataflows;
	}

	/**
	 * get all dataflow edges where the label is a given flow
	 * */
	// TORAD: TODO: HIGH. Why is this a List instead of a Set?
	@Deprecated
	public List<DataFlowEdge> getEdgesByFlow(IObject flow){
		List<DataFlowEdge> list = new ArrayList<DataFlowEdge>();
		Set<DataFlowEdge> edgesByType = getEdgesByType(EdgeType.DataFlow);
		for (DataFlowEdge edge : edgesByType){
			if (edge.getFlow().equals(flow))
				list.add(edge);
		}
		return list;
	}
	

	/**
	 * get all edges that refer to a given object flow
	 * */
	public <T extends SecEdge> Set<T>  getEdgesByFlow(IObject flow, EdgeType eType){
		Set<T> set = new HashSet<T>();
		Set<? extends SecEdge> edgesByType = getEdgesByType(eType);
		if (eType.equals(EdgeType.DataFlow)) {
			for (SecEdge edge : edgesByType) {
				if (((DataFlowEdge) edge).getFlow().equals(flow))
					set.add((T)edge);
			}
		}
		if (eType.equals(EdgeType.Creation)) {
			for (SecEdge edge : edgesByType) {
				if (((CreationEdge) edge).getFlow().equals(flow))
					set.add((T)edge);
			}
		}
		return set;
	}
	

	public boolean accept(SecVisitor visitor) {
		visitor.visit(this);
		
		root.accept(visitor);
		
		// Visit edges
		for (SecEdge edge :edges){
			edge.accept(visitor);
		}
		
		return true;
	}

	public void setRoot(SecObject root) {
		this.root = root;
    }

	public void clear(){
		edges.clear();
		if (pointsto!=null)
			pointsto.clear();
		if (dataflows!=null)
			dataflows.clear();
		if (creations!=null)
			creations.clear();
		if (controlflows!=null)
			controlflows.clear();
		root = null;
		// HACK:XXX ensure that everything is cleared.
		//s_instance = null;
	}

	// TORAD: TODO: HIGH. Why is this a List instead of a Set?
	public List<IObject> getObjectsAsList() {
		List<IObject> secObjectsAsList = new ArrayList<IObject>();
		Set<SecObject> visited = getSecObjects();
		// XXX. Why more copying?!
		secObjectsAsList.addAll(visited);
		return secObjectsAsList;
	}

	/**
	 * @return the set of all SecObjects starting from root
	 */
	public Set<SecObject> getSecObjects() {
		SecVisitorBase visitor = new SecVisitorBase();
		root.accept(visitor);
		Set<SecObject> visited = visitor.getVisitedObjects();
		return visited;
	}

	/**
	 * @return
	 */
	public List<SecEdge> getEdgesAsList(EdgeType eType) {
		Set<? extends SecEdge> edges = getEdgesByType(eType);
		List<SecEdge> edgesAsList = new ArrayList<SecEdge>();
		// XXX. Why more copying?!
		edgesAsList.addAll(edges);
		return edgesAsList;
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		return root.getOObject().accept(visitor);
	}

	@Override
	public Set<? extends IEdge> getEdges() {
		return edges;
	}

	@Override
	public IObject getRoot() {
		return root;
	}

	//TODO: rename = setProperty
	@Override
	public boolean setObjectProperty(Property prop, Condition<IObject> condition) {
		SecVisitorBase visitor = new SecObjectConditionVisitor(prop, condition);
		return root.accept(visitor);
		
	}

	@Override
	public boolean setObjectProperty(Property prop, Condition<IObject>[] conditions) {
		boolean result = true;
		for (Condition<IObject> cond : conditions) {					
			SecVisitorBase visitor = new SecObjectConditionVisitor(prop, cond);
			result &= root.accept(visitor);
		}	
		return result;
	}
	
	public Set<SecObject> getObjects(Property p) {
		Set<SecObject> result = new HashSet<SecObject>();
		Set<SecObject> l = getSecObjects();
		for (SecObject secObject : l) {
			if (secObject.hasPropertyValue(p,false))
				result.add(secObject);
		}
		return result;
	}

	@Override
	public boolean hasPropertyValue(IElement rObject, Property p) {
		if (rObject instanceof SecObject){
			SecObject sObject = (SecObject)rObject;
			return sObject.hasPropertyValue(p);
		}
		//TODO: implement for SecEdge, ...
		return false;
	}

	public Set<DataFlowEdge> getDFEdgesByMethodInvk(SecObject obj, String methodName) {
		Set<DataFlowEdge> dfEdges = new HashSet<DataFlowEdge>();
		Set<DataFlowEdge> inEdges = getInEdges(obj, EdgeType.DataFlow);
		for (DataFlowEdge dataFlowEdge : inEdges) {
			Set<BaseTraceability> traceability = dataFlowEdge.getTraceability();
			for (BaseTraceability tlink : traceability) {
				AstNode tLinkExpression = tlink.getExpression();
				if (tLinkExpression !=null && tLinkExpression instanceof MethodInvocation)
					if (((MethodInvocation)tLinkExpression).methodDeclaration.methodName.equals(methodName))
						dfEdges.add(dataFlowEdge);
			}
		}
		return dfEdges;
	}
	
	public Set<SecObject> getObjects(Property[] props, boolean withReachability, boolean withDescendants) {
		Set<SecObject> result = new HashSet<SecObject>();
		for (SecObject secObject : getSecObjects()) {
			Set<SecObject> descendants = secObject.getDescendants();
			Set<IObject> reachables = getReachableObjects(secObject, EdgeType.PointsTo);
			if (withDescendants)
				for (SecObject desc : descendants) {
					if (checkAllProps(desc, props))
						result.add(secObject);
				}
			if (withReachability)
				for (IObject reach : reachables) {
					if (checkAllProps(reach, props))
						result.add(secObject);
				}
			if (checkAllProps(secObject, props))
				result.add(secObject);
		}
		return result;
	}
	
	public Set<SecObject> getObjects(Property[] props){
		return getObjects(props, false, false);
	}
	
	/**
	 * returns true if Dataflow or Creation edges have as destination an object with snkProps properties
	 * and these edges refer to an object with flowProps
	 * **/
	public boolean checkFlowIntoSink(Property[] snkProps, Property[] flwProps) {
		EdgeType[] reachETypes = { EdgeType.PointsTo };
		EdgeType[] flowETypes = { EdgeType.DataFlow, EdgeType.Creation };
		return checkFlowIntoSink(snkProps,flwProps,reachETypes, flowETypes);
	}
	
	public boolean checkFlowIntoSink(Property[] snkProps, Property[] flwProps, EdgeType[] reachETypes,EdgeType[] flowETypes){
		return getFlowIntoSink(snkProps, flwProps, reachETypes, flowETypes).size()>0;
	}
	
	/**
	 * *returns set of Dataflow and Creation edges have as destination an object with snkProps properties
	 * and these edges refer to an object with flowProps
	 * uses reasonable defaults for reachability - only EdgeType.PointsTo
	 */
	@Override
	public Set<SecEdge> getFlowIntoSink(Property[] snkProps, Property[] flwProps) {
		EdgeType[] reachETypes = { EdgeType.PointsTo };
		EdgeType[] flowETypes = { EdgeType.DataFlow, EdgeType.Creation };
		return getFlowIntoSink(snkProps, flwProps, reachETypes, flowETypes);
	}
	
	/**
	 * returns set of Dataflow and Creation edges have as destination an object with snkProps properties
	 * and these edges refer to an object with flowProps
	 * allows specifying the edge type to be considered
	 */
	@Override
	public Set<SecEdge> getFlowIntoSink(Property[] snkProps, Property[] flwProps, EdgeType[] reachETypes, EdgeType[] flowETypes) {
		Set<SecEdge> suspiciousEdges = new HashSet<SecEdge>();
		Set<SecObject> sinkObjects = getObjects(snkProps);  // .., false, false); XXX. Should this be hard-coded here??
		// XXX. Extract and use option here.
		Set<SecObject> flowObjects = getObjects(flwProps, true, true); // XXX. Should this be hard-coded here??
		//EdgeType[] eTypes = {EdgeType.PointsTo}; //were are only interested in persistent relations
		for (SecObject sink : sinkObjects)
			for (SecObject flow : flowObjects) {
				suspiciousEdges.addAll(getFlowIntoSink(sink, snkProps, flow, flwProps, reachETypes, flowETypes));
			}
		return suspiciousEdges;
	}
	
	/**
	 * given sinks and flows as set of IObjects
	 * return set of Dataflow and Creation edges have as destination an object with snkProps properties
	 * and these edges refer to an object with flowProps
	 * uses reasonable defaults for reachability - only EdgeType.PointsTo
	 */
	public Set<SecEdge> getFlowIntoSink(Set<IObject> sinkObjects, Set<IObject> flowObjects){
		EdgeType[] reachETypes = { EdgeType.PointsTo };
		EdgeType[] flowETypes = { EdgeType.DataFlow, EdgeType.Creation };
		return getFlowIntoSink(sinkObjects, flowObjects, reachETypes, flowETypes);
	}

	/**
	 * given sinks and flows as set of IObjects
	 * return set of Dataflow and Creation edges have as destination an object with snkProps properties
	 * and these edges refer to an object with flowProps
	 * user needs to specify what types of edges are to be considered
	 */
	public Set<SecEdge> getFlowIntoSink(Set<IObject> sinkObjects, Set<IObject> flowObjects, EdgeType[] reachETypes,EdgeType[] flowETypes) {
		Set<SecEdge> suspiciousEdges = new HashSet<SecEdge>();
		//EdgeType[] eTypes = {EdgeType.PointsTo}; //were are only interested in persistent relations
		Property[] props = {};
		for (IObject sink : sinkObjects)
			for (IObject flow : flowObjects) {
				suspiciousEdges.addAll(getFlowIntoSink(sink, props, flow, props, reachETypes, flowETypes));
			}
		return suspiciousEdges;
	}

	
	public boolean checkFlow(IObject flow, Property[] flwProps, EdgeType[] eTypes) {
		Assert.assertTrue(flow instanceof SecObject);
		SecObject sflow = (SecObject)flow;
		boolean isFlowByDesc = checkFlowByDescendants(flow, flwProps);
		// WHy just PointsTo		
		boolean isFlowByReach = checkFlowByReachability(flow, flwProps, eTypes);

		return (isFlowByDesc || isFlowByReach);
	}

	
	public Set<SecEdge> getFlowIntoSink(IObject sink, Property[] snkProps, IObject flow, Property[] flwProps,
			EdgeType[] reachETypes, EdgeType[] flowETypes) {
		Set<SecEdge> result = new HashSet<SecEdge>();
		if (checkFlow(flow, flwProps, reachETypes)) {
			result.addAll(getInSinkByDescendants(sink, snkProps, flow, flowETypes));
			result.addAll(getInSinkByReachability(sink, snkProps, flow, reachETypes, flowETypes));
		}
		return result;
	}
	


	private boolean checkFlowByDescendants(IObject flow, Property[] flwProps) {		
		Set<? extends IObject> descendants = flow.getDescendants();
		for (IObject o : descendants) {			
			if (checkAllProps(o, flwProps)) 
				return true;
		}
		return false;
	}


	// Done: generalize to take a set of edgeTypes
	private boolean checkFlowByReachability(IObject flow, Property[] flwProps, EdgeType[] eTypes) {
		for (EdgeType eType:eTypes){
			Set<IObject> reachedObjects = getReachableObjects(flow, eType);
			for (IObject o : reachedObjects) {			
				if (checkAllProps(o, flwProps)) return true;
			}
		}
		return false;
	}

	/**
	 * @param flwProps
	 * @param o
	 */
	public boolean checkAllProps(IObject o, Property[] flwProps) {		
		boolean found = true;
		if (!(o instanceof SecObject)) return false;
		if (flwProps.length == 0) return true;
		SecObject secObject = (SecObject)o;
		for (Property p : flwProps){			
			found = found && secObject.hasPropertyValue(p, false); 
		}
		return found;
	}

	/**
	 * return incomming dataflow edges into sink that refers to flow
	 * the source of the edge is not important
	 * TODO: maybe filter self edges.  
	 * @param flow
	 * @param sink
	 */
	// XXX. Do we need to make the distinction by edgetype? It's the same code in both cases...
	private Set<? extends SecEdge> getFlowsInto(IObject flow, IObject sink, EdgeType[] eTypes) {
		Set<SecEdge> result = new HashSet<SecEdge>();
		for (EdgeType eType : eTypes) {
			if (eType.equals(EdgeType.DataFlow)) {
				Set<DataFlowEdge> edges = getInEdges(sink, eType);
				for (DataFlowEdge dfEdge : edges) {
					if (dfEdge.getFlow().equals(flow))
						result.add(dfEdge);
				}
			}
			if (eType.equals(EdgeType.Creation)) {
				Set<CreationEdge> edges = getInEdges(sink, eType);
				for (CreationEdge crEdge : edges) {
					if (crEdge.getFlow().equals(flow))
						result.add(crEdge);
				}
			}
		}
		return result;
	}

	private Set<SecEdge> getInSinkByReachability(IObject sink, Property[] snkProps, IObject flow,
			EdgeType[] reachETypes, EdgeType[] flowETypes) {
		Set<SecEdge> result = new HashSet<SecEdge>();
		if (checkAllProps(sink, snkProps)) {
			for (EdgeType eType : reachETypes) {
				Set<IObject> reachedObjects = getReachableObjects(sink, eType);
				for (IObject o : reachedObjects) {
					result.addAll(getFlowsInto(flow, o, flowETypes));
				}
			}
		}
		return result;
	}
	
	private Set<SecEdge> getInSinkByDescendants(IObject sink, Property[] snkProps,
			IObject flow, EdgeType[] eTypes) {
		Set<SecEdge> result = new HashSet<SecEdge>();
		if (sink == null) return result;
		if (snkProps!=null && checkAllProps(sink, snkProps)) {
			result.addAll(getFlowsInto(flow,sink, eTypes));
			Set<? extends IObject> descendants = sink.getDescendants();
				for (IObject o : descendants) {
					result.addAll(getFlowsInto(flow,o, eTypes));
				}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<DataFlowEdge> checkObjectProvenance(IObject a, IObject b, IObject c, IObject d) {
		//not exist o in flow(connected(a, b)) and o in flow(connected(c, d))
		Set<DataFlowEdge> abEdges = connectedByDF(a,b);
		Set<SecObject> abFlows = flow(abEdges);
		Set<DataFlowEdge> cdEdges = connectedByDF(c,d); 
		Set<SecObject> cdFlows = flow(cdEdges);
		abFlows.retainAll(cdFlows); // abFlows = abFlows \cup cdFlows
		Set<DataFlowEdge> suspiciousEdges = new HashSet<DataFlowEdge>();
		for (SecObject flow:abFlows)
			for (DataFlowEdge edge:cdEdges)
				if (edge.getFlow().equals(flow))
					suspiciousEdges.add(edge); 
		return suspiciousEdges;
	}

	/**
	 * helper method
	 * returns all dataflow objects from a given set of edges
	 * */
	public Set<SecObject> flow(Set<DataFlowEdge> abEdges) {
		Set<SecObject> flows = new HashSet<SecObject>();
		for (DataFlowEdge dfEdge : abEdges) {
			flows.add(dfEdge.getFlow());
		}
		return flows;
	}

	@Override
	public <T extends IEdge> Set<T> connected(IObject a, IObject b, EdgeType[] eTypes, boolean isDirected, boolean useAncs,
			boolean useDescs, boolean useTrans, boolean useReach) {
		Set<T> dfEdges = new HashSet<T>();
		for (EdgeType eType : eTypes) {
			Set<SecEdge> inbEdges = getInEdgesByDescs(b, eType, useDescs);
			for (SecEdge secEdge : inbEdges) {
				if (secEdge.src.equals(a) || (useDescs && a.getDescendants().contains(secEdge.src)))
					dfEdges.add((T) secEdge);
			}
		}
		return dfEdges;
	}

		
	public <T extends IObject> Set<T> getObjectsByCond(Condition<IObject> cond) {
		FindObjectCondVisitor<T> visitor = new FindObjectCondVisitor<T>(cond);
		root.accept(visitor);
		return visitor.getFoundObjects();
	}

	@Override
	public boolean setDomainProperty(Property p, Condition<IDomain> c) {
		// TODO implement me
		return false;
	}

	@Override
	public boolean setDomainProperty(Property p, Condition<IDomain>[] c) {
		// TODO implement me
		return false;
	}

	@Override
	public boolean setEdgeProperty(Property p, Condition<IEdge> c) {
		SecVisitorBase visitor = new SecEdgeConditionVisitor(p, c);
		return accept(visitor);
	}

	@Override
	public boolean setEdgeProperty(Property p, Condition<IEdge>[] c) {
		// TODO implement me
		return false;
	}

	@Override
	public Property getProperty(IElement elem, String propName) {
		if (elem instanceof SecElement){
			SecElement secElem = (SecElement)elem;
			return secElem.getPropertyValue(propName);
		}
		return null;
	}

	@Override
	public Set<? extends IEdge> connectedByObjectTransitivity(IObject src, IObject dst, IObject flow) {
		Set<DataFlowEdge> transitiveCommunication = getTransitiveCommunication(src, dst, flow);
		return transitiveCommunication;
	}



}
