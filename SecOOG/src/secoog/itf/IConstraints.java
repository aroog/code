package secoog.itf;

import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;
import secoog.EdgeType;
import secoog.Property;

//rename-> IConstraintTID
public interface IConstraints {

	// information disclosure: sink is untrusted and flow is confidential, unencrypted, etc. 
	// tampering: sink is trusted and flow is unsanitized, untrusted, 
	// denial of service: sink is a hub (is vital for the app), flow is harmfull and crashes the sink 
	// implementation consider all cases - object hierarchy, reachability, etc. 
	
	// return a set - if the set is not empty - that is the boolean.
	// maybe allow the architect to specify edge type
	// add default for PointsTo
	Set<? extends IEdge> getFlowIntoSink(Property[] snkProps, Property[] flwProps, EdgeType[] reachETypes,EdgeType[] flowETypes);
	
	//with defaults
	Set<? extends IEdge> getFlowIntoSink(Property[] snkProps, Property[] flwProps);
	
	/***
	 * not exist o in flow(connected(a, b)) and o in flow(connected(c, d))
	 */
	Set<? extends IEdge> checkObjectProvenance(IObject a, IObject b, IObject c, IObject d);
	
	// TODO: add a version with properties
	// OR a version with specific objects, or set of objects as parameters
}
