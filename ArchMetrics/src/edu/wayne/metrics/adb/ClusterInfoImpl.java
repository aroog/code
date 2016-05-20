package edu.wayne.metrics.adb;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/**
 * Implementation of ClusterInfo that uses a HashMultiMap as a backing. 
 * 
 * NOTE: But the backing Multimap implementation
 * probably doesn't actually store anything for that key, and if a key isn't mapped to a nonempty collection, it won't
 * e.g. appear in the keySet(). Multimap is not a Map<K, Collection<V>>.
 * 
 * This could be a problem if we want to store empty/trivial clusters
 * 
 * 	TODO: Rename: --> ClusterInfoMultiMap
 */
public class ClusterInfoImpl<K, V> implements Multimap<K, V>, ClusterInfo<K, V> {

	// Use HashMultiMap, a Multimap that cannot hold duplicate key-value pairs. 
	private Multimap<K, V> mmap = HashMultimap.create();

    @Override
    public Map<K, Collection<V>> asMap() {
	    return this.mmap.asMap();
    }

    @Override
    public void clear() {
	    this.mmap.clear();
    }

	public boolean containsEntry(Object arg0, Object arg1) {
	    return this.mmap.containsEntry(arg0, arg1);
    }

    @Override
    public boolean containsKey(Object key) {
	    return this.mmap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
	    return this.mmap.containsValue(value);
    }

	@Override
    public Collection<Entry<K, V>> entries() {
	    return this.mmap.entries();
    }

	// Implement value equality
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof ClusterInfoImpl)) {
			return false;
		}

		ClusterInfoImpl<K,V> key = (ClusterInfoImpl<K,V>)o;
		return this.mmap.equals(key.mmap);
	}

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (mmap == null ? 0 : mmap.hashCode());
		return result;
	}
	@Override
	public Collection<V> get(K key) {
		return this.mmap.get(key);
	}

    @Override
    public boolean isEmpty() {
	    return this.mmap.isEmpty();
    }

    @Override
    public Set<K> keySet() {
	    return this.mmap.keySet();
    }

    public Multiset<K> keys() {
	    return this.mmap.keys();
    }

    @Override
    public boolean put(K key, V value) {
	    return this.mmap.put(key, value);
    }

	@Override
    public boolean putAll(K key, Iterable<? extends V> values) {
	    return this.mmap.putAll(key, values);
    }

	@Override
    public boolean putAll(Multimap<? extends K, ? extends V> mmap) {
	    return this.mmap.putAll(mmap);
    }

    @Override
    public boolean remove(Object arg0, Object arg1) {
	    return this.mmap.remove(arg0, arg1);
    }

    @Override
    public Collection<V> removeAll(Object arg0) {
	    return this.mmap.removeAll(arg0);
    }

    @Override
    public Collection<V> replaceValues(K key, Iterable<? extends V> arg1) {
	    return this.mmap.replaceValues(key, arg1);
    }

    @Override
    public int size() {
	    return this.mmap.size();
    }

    @Override
    public Collection<V> values() {
	    return this.mmap.values();
    }
}
