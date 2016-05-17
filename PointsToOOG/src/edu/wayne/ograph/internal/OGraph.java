package edu.wayne.ograph.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IGraph;
import oog.itf.IObject;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ast.Type;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.wayne.ograph.OGraphVisitor;

public class OGraph implements IGraph {
	private Map<OObjectKey, OObject> DO;
	private Map<DomainMapKey, ODomain> DD;
	
	// XXX. Could split up this map into several to optimize lookup.
	// Since many of these edges have the same Osrc, Odst, but the rest is different...
	// E.g., keep map of DF edges separate
	// But since we have keys of different types, we are already doing well.
	// The equals() impl. of OCFEdgeKey, ODFEdgeKey, ...  returns early if not the right type.
	
	private Map<OEdgeKey, OEdge> DE;
	private Map<Type, ITypeBinding> typeMap;
	private static OGraph s_instance = null;
	
	/**
	 * Optimization: to avoid iterating over the entire DO Map.
	 * Eliminate entries where the owning domain is not the one we are looking for.
	 * We use only the owning domain, because that's the only domain guaranteed to be there
	 * e.g., java.lang.Object takes owner.
	 */
	private Multimap<ODomain, OObject> cache = HashMultimap.create();

	// Encapsulate this method
	public Multimap<ODomain, OObject> getCache() {
    	return cache;
    }
	
	public Collection<OObject> getSameOwnerObjects(ODomain D1) {
		return cache.get(D1);
	}

	private OGraph() {
		DO = new Hashtable<OObjectKey, OObject>();
		DD = new Hashtable<DomainMapKey, ODomain>();
		// HashSet semantics
		DE = new Hashtable<OEdgeKey, OEdge>();
		typeMap = new Hashtable<Type, ITypeBinding>();
	}

	public static OGraph getInstance() {
		if (s_instance == null) {
			s_instance = new OGraph();
		}
		return s_instance;
	}

	// XXX. Alt. Could get getDOMap() then use DO.putAll()? 
	// putAll is equivalent to what is below. But currently, we are also computing the set of values() of DO
	// in order to get the argument to this method. We should just get the full DO map.
	// Also, with putAll, it will be harder to do the caching.
	// XXX. Make this method private; use clone
	/**
	 * @deprecated 	Stop using addAllOObjects. Requires a copy of the set of values.
	 */	
	@Deprecated
	public void addAllOObjects(Collection<OObject> newOObjects) {
		for (OObject object : newOObjects) {
			OObjectKey key = object.getKey();
			DO.put(key, object);
			cacheObject(object, key);
		}
	}

	
	public void addAllOObjects(Map<OObjectKey, OObject> newOObjects) {
		for (Entry<OObjectKey, OObject> entry: newOObjects.entrySet()) {
			OObjectKey key = entry.getKey();
			OObject value = entry.getValue();
			DO.put(key, value);
			cacheObject(value, key);
		}
	}
	
	private void cacheObject(OObject object, OObjectKey key) {
	    List<ODomain> domains = key.getDomains();
	    // Extract out the owning domain
	    ODomain oDomain = domains != null && !domains.isEmpty() ? domains.get(0) : null;
	    cache.put(oDomain, object);
    }

	/**
	 * @deprecated 	Stop using addAllOEdges. Requires a copy of the set of values 
	 */
	@Deprecated
	public void addAllOEdges(Collection<OEdge> newOEdges) {
		for (OEdge edge : newOEdges) {
			OEdgeKey key = edge.getKey();
			DE.put(key, edge);
		}
	}
	

	public void addAllOEdges2(Map<OEdgeKey, OEdge> newOEdges) {
		DE.putAll(newOEdges);
	}

	public void addAllDD(Map<DomainMapKey, ODomain> newDD) {
		DD.putAll(newDD);
	}

	/**
	 * Returns an unmodifiable view of the set of OObjects, i.e., just the values of the DO map.
	 * The values will not have duplicates, because of how we construct the DO map, where the key of the map ensures that a 
	 * unique representative OObject is created.
	 * If there are duplicates, then, we have a big problem somewhere.
	 * To get hold of the full map, use getDOMap();
	 * 
	 * @deprecated Creates a new collection of the values 
	 */
	@Deprecated
	// XXX. Avoid creating a copy of the values!
	public Collection<OObject> getDO() {
		return Collections.unmodifiableCollection(DO.values());
	}

	/**
	 * Use this to get hold of an unmodified view of the DO map
	 * @return
	 */
	public Map<OObjectKey, OObject> getDOMap() {
		return Collections.unmodifiableMap(DO);
	}
	
	// TODO: avoid exposure
	/**
	 * Returns an unmodifiable view of the map
	 */
	public Map<DomainMapKey, ODomain> getDD() {
		return Collections.unmodifiableMap(DD);
	}

	// TODO: avoid exposure
	/**
	 * Returns an unmodifiable view of the set
	 * XXX. Avoid constructing another set!
	 */
	@Deprecated
	// DONE. Avoid creating a copy!
	public Collection<OEdge> getDE() {
		return Collections.unmodifiableCollection(DE.values());
	}
	
	public Map<OEdgeKey, OEdge> getDEMap() {
		return Collections.unmodifiableMap(DE);
	}


	// Use only to add a new root object
	// Package protected on purpose
	void addRoot(OObject oC) {
		OObjectKey key = oC.getKey();
		OObject oObject = DO.get(key);
		if (oObject == null) {
			DO.put(key, oC);
		}
	}

	void addShared(ODomain dShared) {
		// DD' = DD U {(Oworld, ::SHARED)->DSHARED}
		DD.put(new DomainMapKey(OOGContext.getOWorld(), OOGContext.getDShared().getDomainDecl()), dShared);
	}

	public OObject getOObject(QualifiedClassName aQCN, List<ODomain> listD) {
		OObjectKey key = new OObjectKey(aQCN.getFullyQualifiedName(), listD);
		OObject O_C = DO.get(key);
		if (O_C == null) {
			O_C = new OObject(key);
			// Important: set the QCN, since it is not part of the OObjectKey
			O_C.setQCN(aQCN);
			DO.put(key, O_C); // DO' = DO U {O_C}
			
			cacheObject(O_C, key);
		}
		return O_C;
	}

	/**
	 * We might be trying to add an OEdge that is value-equal to an existing edge.
	 * It will not get added. But we need to merge the traceability information.
	 * @param oEdge
	 */
	public void addOEdge(OEdge oEdge) {
		// Return the previous value associated with key
		DE.put(oEdge.getKey(), oEdge);
//		if (oldValue != null) {
//			oEdge.unionLink(oldValue.getPath());
	}
	
	
	public OEdge getOEdge(OEdgeKey oEdgeKey) {
		return DE.get(oEdgeKey);
	}

	/**
	 * 
	 */
	void clearGraph() {
		DO.clear();
		DD.clear();
		DE.clear();
		typeMap.clear();
		cache.clear();
		
		realGraph = null;
		
		//nullify the s_instance
	}

	public static void reset() {
		if (s_instance != null ) {
			// Scrub singleton. May have been called already.
			s_instance.clearGraph();
		}
		s_instance = null;
	}
	
	public OGraph clone() {
		OGraph clone = new OGraph();
		// The cache gets updated as part of the addAllObjects
		clone.addAllOObjects(getDOMap());
		clone.addAllDD(this.DD);
		// XXX. Do not use. Creates a copy of the set of Values first.
		//clone.addAllOEdges(getDE());
		clone.addAllOEdges2(getDEMap());
		// XXX. Should we avoid cloning the typemap? That can be shared across graphs!
		clone.typeMap.putAll(this.typeMap);
		return clone;
	}

	/**
	 * Not implemented
	 * */
	@Override
	public boolean accept(OGraphVisitor visitor) {
		return false;
	}

	@Override
	// XXX. Why this extra copy?!?! Fix this!
	// Just make it return a Collection. Return an unmodifiable view
	public Set<IEdge> getEdges() {
		Set<IEdge> returnEdges = new HashSet<IEdge>();
		for (OEdge edge : DE.values())
			returnEdges.add(edge);
		return returnEdges;
	}

	public Set<IEdge> getRealEdges() {
		Set<IEdge> returnEdges = new HashSet<IEdge>();
		for (OEdge edge : DE.values())
			returnEdges.add(edge.getReal());
		return returnEdges;
	}

	
	@Override
	public IObject getRoot() {
		return OOGContext.getOWorld();
	}
	
	public edu.wayne.ograph.OObject getRealRoot() {
		return OOGContext.getOWorld().getReal();
	}

	public ODomain getODomain(DomainMapKey dmkey) {
		return DD.get(dmkey);
	}

	public boolean containsODomainKey(DomainMapKey dmkey) {
		return DD.containsKey(dmkey);
	}

	public void putODomain(DomainMapKey domainMapKey, ODomain dj) {
		DD.put(domainMapKey, dj);
	}

	public ODomain removeODomain(DomainMapKey dmkey) {
		return DD.remove(dmkey);
	}

	public ITypeBinding getBinding(Type type) {
		return typeMap.get(type);
	}

	public void addType(ITypeBinding typeBinding) {

		Type type = Type.createFrom(typeBinding);
		if (!typeMap.containsKey(type)) {
			typeMap.put(type, typeBinding);
			if (typeBinding.getSuperclass() != null)
				addType(typeBinding.getSuperclass());
		}
	}
	
	private edu.wayne.ograph.OGraph realGraph = null;
	
	public edu.wayne.ograph.OGraph getReal() {
		if (realGraph == null ) {
			realGraph = new edu.wayne.ograph.OGraph(getRealRoot(), getRealEdges());
		}
		return realGraph;
	}
}
