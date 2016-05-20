package edu.wayne.metrics.adb;

import java.util.Set;

import edu.wayne.metrics.utils.ObjectsUtils;

import oog.itf.IDomain;
import oog.itf.IObject;
import ast.BaseTraceability;

// PulledObjects: measure effect of "pulling" of objects from formal domain parameters, to actual domains
// 1. Get EnclosingType(A) --> C
// (may need to reach into traceability info to get this; or OOG_DM may need to expose additional info)
// 2. Get domain of instance of type A domainof(A)--> D_formal;
//    D_formal \in params(C) (params returns list of formal parameters; D_formal must be declared on C) 
// 3. D in domains(B) (domains(...) returns list of locally declared (private and public) domains, excluding parameters)
// 4. D_formal is bound to D
// Rationale: Locate "pulled" objects of type A. A field could be declared in a formal domain, D_formal.
// where the domain D is locally declared on B;
// 
// NOTE: Take into account inheritance; could separately measure effect of inheritance;
// Since inheritance is also a form of de-localized information.
// Could split inheritance into a separate metric.

// <A,D,B> in the OOG ==> D is a domain declared on B
// In the code/annotations, the domain of A can be a formal domain parameter that is bound to B::D 

// NOTE: The current implementation is hackish since the traceability information does not have the formal domain parameter.
// Instead, we're just relying on the enclosing type in the traceability information being different from the declaring type of the domain.

// TODO: HIGH. This is hackish because the types could be different, not because of formal/actual bindings
// but because of inheritance (the domain is introduced at declaration point).
/**
 * @deprecated  Just use PulledObjects.
 */
@Deprecated
public class PulledObjectsAlt extends ObjectMetricBase<String, String> {
	private static final String HEADER = "IsOutlier,ObjectType,EnclosingType";

	private static final String D_SHARED = "SHARED";
	private static final String NULL_TYPE = "null";

	@Override
    public String getHeader() {
	    return HEADER;
    }
	
	@Override
    public String getHeaderShort() {
	    return HEADER;
    }	

	@Override
	public void compute(Set<IObject> allObjects) {
		this.mmap = ClusterInfoFactory.create();
		
		if (allObjects != null) {
			for (IObject tt1 : allObjects) {
				String objectType = tt1.getC().toString();
				String parentType =  getParentType(tt1);
				if (parentType != null ) {
					Set<BaseTraceability> set = tt1.getTraceability();
					if (set != null) {
						for (BaseTraceability traceability : set) {
							String declaringType = ObjectsUtils.getEnclosingType(traceability).toString();

							if (declaringType != null && parentType != null
							        && declaringType.compareTo(parentType) != 0) {
								
								// Put in the same cluster the original declaring type and the parentType
								mmap.put(objectType, parentType);
								mmap.put(objectType, declaringType);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * NOTE: getParentType can return the constant D_SHARED if the object is in the 'SHARED' domain, which has a 'null'
	 * parent.
	 */
	private String getParentType(IObject tt1) {
		String domainDeclaringType = NULL_TYPE;
		IDomain actualDomain = tt1.getParent();
		// NOTE: actualDomainName could be SHARED!
		// TODO: HIGH. XXX. What if actualDomain == null? This can happen for root object!
		// But right now, root object is excluded from 'allObjects'.
		if (actualDomain != null) {
			// TODO: HIGH. Check this.
			domainDeclaringType = actualDomain.getC().getFullyQualifiedName();
			// If actualDomainName == "SHARED", the parent type is "null"
			// Re-generate a "declaringType" that is SHARED.
			// NOTE: This is a bit bogus, but better than displaying "null" in the output
			if (domainDeclaringType.compareTo(NULL_TYPE) == 0) {
				domainDeclaringType = D_SHARED;
			}
		}
		
		return domainDeclaringType;
	}
}
