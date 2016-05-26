package oogre.refinements.tac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ast.AstNode;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;

import oog.itf.IElement;
import oog.itf.IObject;
import util.TraceabilityEntry;
import util.TraceabilityList;
import util.TraceabilityListSet;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;
import edu.wayne.ograph.OObject;
import edu.wayne.ograph.ORootObject;

/**
 * Builds the PC map: O_C -> {O} means that O_C is analyzed in the context of O. 
 * 
 * NOTE: Requires looking up Oid -> IObject
 * TOMAR: TODO: HIGH. XXX. Clean up the data: includes "dummy" and "main" for *every* object!
 * 
 */
public class MappingBuilder extends OGraphVisitorBase {

	private Map<IObject, HashSet<IObject>> PC = new HashMap<IObject, HashSet<IObject>>();
	private MappingHelper helper;


	public MappingBuilder(OGraph graph) {
		helper = new MappingHelper(graph);
	}
	
	@Override
    public boolean visit(IObject oO_C) {
	    // return super.visit(node);
	    
		
		// if (oO_C instanceof edu.wayne.ograph.ORootObject ) {
		// return true;
		// }
		//
		// if (oO_C.isMainObject()) {
		// return true;
		// }

		// O_C represents new C(). O_C was created in the context of O.
	    TraceabilityListSet path = oO_C.getPath();
	    Set<TraceabilityList> setOfLists = path.getSetOfLists();
	    for(TraceabilityList list : setOfLists) {
	    	List<TraceabilityEntry> rawList = list.getRawList();
	    	for(TraceabilityEntry entry : rawList ) {
	    		// Must convert this Oid to an IObject later
	    		String Oid = entry.getFirst();
	    		
	    		BaseTraceability second = entry.getSecond();
	    		AstNode expression = second.getExpression();
	    		if(expression instanceof ClassInstanceCreation) {
	    			
	    			IElement iElement = helper.getElement(Oid);
	    			if (iElement instanceof IObject ) {
	    				IObject oO = (IObject)iElement;
	    				
	    				// Filter out the MainObject
	    				if (excludeDummy(oO) ) {
	    					continue;
	    				}
	    				
	    				HashSet<IObject> hashSet = PC.get(oO_C);
	    				if (hashSet == null ) {
	    					hashSet = new HashSet<IObject>();
	    					PC.put(oO_C, hashSet);
	    				}
	    				hashSet.add(oO);
	    			}
	    		}
	    	}
	    }
	    
	    return true;
    }

	private boolean exclude(IObject oObject) {
	    return oObject.isMainObject();
    }
	
	// XXX. We may no longer need to worry about RootObject since we took it out from the path
	private boolean excludeDummy(IObject oObject) {
	    return oObject instanceof ORootObject;
    }

	public Map<IObject, HashSet<IObject>> getPC() {
		return PC;
	}
	
}
