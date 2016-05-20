package edu.wayne.metrics.adb;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Implementation of ClusterInfo that uses a Map<K, Set<V>> as a backing.
 * Allows storing empty collections/sets for the trivial clusters.
 * A trivial cluster is one of size zero, not one of a size 1.
 * Size 1 means there is at least one entry that satisfies some criteria.
 * 
 * TODO: Rename: ClusterInfoMapKeyToSet
 */
public class ClusterInfoImpl2<K, V> implements ClusterInfo<K, V> {

	// Use HashMultiMap, a Multimap that cannot hold duplicate key-value pairs. 
	private HashMap<K, Set<V>> map = new HashMap<K, Set<V>>();

    @Override
    public Map<K, Collection<V>> asMap() {
    	Map<K, Collection<V>> mapKeyVals = new HashMap<K, Collection<V>>();
    	for(Entry<K, Set<V>> entry :  this.map.entrySet() ) {
    		mapKeyVals.put(entry.getKey(), entry.getValue());
    	}
    	
    	return mapKeyVals;
    }

    @Override
    public void clear() {
	    this.map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
	    return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
	    return this.map.containsValue(value);
    }

	// Implement value equality
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof ClusterInfoImpl2)) {
			return false;
		}

		ClusterInfoImpl2<K,V> key = (ClusterInfoImpl2<K,V>)o;
		return this.map.equals(key.map);
	}

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (map == null ? 0 : map.hashCode());
		return result;
	}

	@Override
    public Collection<V> get(K key) {
	    return this.map.get(key);
    }


    @Override
    public boolean isEmpty() {
	    return this.map.isEmpty();
    }

    @Override
    public Set<K> keySet() {
	    return this.map.keySet();
    }

    public Set<K> keys() {
	    return this.map.keySet();
    }

    @Override
    /**
     * Important: Allow storing null. Just create an empty set.
     */
    public boolean put(K key, V val) {
		Set<V> set = this.map.get(key);
		if ( set == null ) {
			set = new HashSet<V>();
			this.map.put(key, set);
		}
		
		if (val != null ) {
			return set.add(val);
		}
		return false;
    }

	@Override
    public boolean putAll(K key, Iterable<? extends V> values) {
		Collection<V> collection = getOrCreateCollection(key);

		if (values instanceof Collection) {
			collection.addAll((Collection<? extends V>) values);
		}
		else {
			for (V value : values) {
				collection.add(value);
			}
		}

		return false;
    }

	private Collection<V> getOrCreateCollection(K key) {
	    Set<V> collection = map.get(key);
	    if (collection == null) {
	      collection = new HashSet<V>();
	      map.put(key, collection);
	    }
	    return collection;
	  }

// TOAND: TODO: Add back if needed
//    @Override
//    public boolean remove(Object arg0, Object arg1) {
//	    // return this.map.remove(arg0, arg1);
//    	return false;    	
//    }
//
//    @Override
//    public Collection<V> removeAll(Object arg0) {
//	    // return this.map.removeAll(arg0);
//    	return null;
//    }
//
//    @Override
//    public Collection<V> replaceValues(K arg0, Iterable<? extends V> vals) {
//	    // return this.map.replaceValues(arg0, vals);
//    	return null;
//    }

    @Override
    public int size() {
	    return this.map.size();
    }

    @Override
    public Collection<V> values() {
    	Set<V> allVals = new HashSet<V>();
    	for( Set<V> values : this.map.values()  ) {
    		allVals.addAll(values);
    	}
    	return allVals;
    }
}
