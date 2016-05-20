/***********************************************************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************/
package oog.ui.tree;

import oog.itf.IElement;
import oog.itf.IObject;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

import edu.wayne.ograph.ODomain;



/**
 * A class that handles filtering preference node items based on a supplied matching string.
 * 
 * @since 3.2
 */
public class ObjectGraphPatternFilter extends PatternFilter {

	/**
	 * Create a new instance of a PreferencePatternFilter
	 * 
	 * @param isMatchItem
	 */
	public ObjectGraphPatternFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	public boolean isElementSelectable(Object element) {
		if (element instanceof IElement) {
			IElement node = (IElement) element;
			return node !=null;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.PatternFilter#isElementVisible(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	public boolean isElementVisible(Viewer viewer, Object element) {
		// Preference nodes are not differentiated based on category since
		// categories are selectable nodes.
		if (isLeafMatch(viewer, element)) {
			return true;
		}

		ITreeContentProvider contentProvider = (ITreeContentProvider) ((TreeViewer) viewer).getContentProvider();
		Object[] children = contentProvider.getChildren(element);
		// Will return true if any subnode of the element matches the search
		if (filter(viewer, element, children).length > 0) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.PatternFilter#isLeafMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		String text = "";
		if(element instanceof ODomain){
			ODomain treeElement = (ODomain)element;
			text = treeElement.getD();
		}else if (element instanceof IObject) {
			IObject treeElement = (IObject) element;
			text = treeElement.getInstanceDisplayName();
		}
		if (wordMatches(text)) {
			return true;
		}
		return false;
	}

}
