package edu.wayne.metrics.adb;

public class ClusterInfoFactory {

	/**
	 * Use this factory. Which hides behind the ClusterInfo interface which concrete implementation class is being used.
	 */
	public static <K,V> ClusterInfo<K,V> create() {
		// return new ClusterInfoImpl<K, V>();
		return new ClusterInfoImpl2<K, V>();
	}
}
