package secoog.itf;

import java.util.Set;

import secoog.Property;

/**
 * define a named set of elements that share the same properties
 * */
public interface IElemSet {

	String getName();

	Set<Property> getProperties();
}
