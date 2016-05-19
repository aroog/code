package edu.wayne.tracing;

/**
 * A contract to have a retrievable name. This contract is used for classes that are intended to be subclasses by the
 * user of the framework. The framework can then use this name to provide more readable feedback.
 * 
 * @author David Dickey
 */
public interface INamed {

	/**
	 * This method is intended to be used to simply return an arbitrary name that can be used to help identify the
	 * "named" objects.
	 * 
	 * @return a name
	 */
	public String getName();
}
