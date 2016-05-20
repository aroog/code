package edu.wayne.metrics.adb;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents a cluster of K to a Collection<V>
 *
 * @param <K>
 * @param <V>
 */
public interface ClusterInfo<K, V> {

	Map<K, Collection<V>> asMap();

	Set<K> keySet();
	
	Collection<V> get(K arg0);

	Collection<V> values();

	int size();

// TODO: Add back if needed
//	Collection<V> replaceValues(K key, Iterable<? extends V> vals);
//
//	Collection<V> removeAll(Object arg0);
//
//	boolean remove(Object arg0, Object arg1);

	boolean putAll(K key, Iterable<? extends V> vals);

	boolean put(K key, V val);

	boolean isEmpty();

	boolean containsValue(V val);

	boolean containsKey(K key);

	void clear();


}
