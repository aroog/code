package edu.wayne.metrics.adb;

import ast.Type;

/**
 * Callback to figure whether to include a Type.
 * Use this from within Util.
 * 
 * TODO: Add getters/setters to figure out if 
 */
public interface TypeSelector {

	boolean isIncluded(Type type);
	
}
