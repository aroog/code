package edu.cmu.cs.viewer.objectgraphs.views;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.TreeViewer;

import edu.cmu.cs.viewer.objectgraphs.ResourceLineKey;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.cmu.cs.viewer.ui.ITreeElement;

public class TypeInspector {

	// Convert static method to instance method
	public static Set<String> getTypesToDisplay(TreeViewer tv) {
		Set<String> set = new HashSet<String>();
		ITreeElement[] selectedElements = ShowAction.getSelectedElement(tv);
		if (selectedElements != null && selectedElements.length > 0) {
			for (ITreeElement element : selectedElements) {
				Set<String> mergedClasses = getClass(element);
				set.addAll(mergedClasses);
			}
		}
		return set;
	}
	
	/**
	 * IMPORTANT: must return a list of fully qualified type names. Otherwise, IJavaProject.findType does not work! 
	 */
	public static Set<String> getClass(ITreeElement element) {
		Set<String> mergedClasses = new HashSet<String>();
		if (element instanceof DisplayObject) {
			DisplayObject displayObject = (DisplayObject) element;
			for (ResourceLineKey key : displayObject.getTraceability()) {
				mergedClasses.add(key.getFullyQualifiedTypeName());
			}
		}

		return mergedClasses;
	}	
}
