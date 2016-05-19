package edu.wayne.summary.strategies;

import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;

import edu.wayne.ograph.OObject;

import oog.itf.IEdge;
import oog.itf.IObject;
import secoog.EdgeType;

//TODO: HIGH. Rewrite in terms of more type safe nodes: miniAst nodes instead of Strings.
public interface SummaryStrategy<T> {
	void compute() throws JavaModelException;

	Set<Info<T>> getMostImportantClasses();

	Set<Info<T>> getMostImportantRelatedClass(String javaClass);

	Set<Info<T>> getMostImportantMethods(String javaClass);

	// Lookup field by (class, name)
	// XXX. What about other cases, e.g. method parameters?
	Set<Info<T>> getClassesBehindInterface(String enclosingType, String fieldType, String fieldName);

	
	//TODO: Move below methods somewhere else? 
	Set<IEdge> getEdges(Set<EdgeType> type);
	Set<IObject> getObjects();

	Set<String> getAllClasses();

	Set<String> getConcreteClasses(String itf);

	Set<String> getAllInterfaces();
}
