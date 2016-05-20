/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002-2005 Diomidis Spinellis
 *
 * Permission to use, copy, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted,
 * provided that the above copyright notice appear in all copies and that
 * both that copyright notice and this permission notice appear in
 * supporting documentation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Adapted from UMLgraph 5.0
 *
 */

package org.umlgraph.doclet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

public class UmlGraph {

	private Options opt = new Options();

    public Options getOptions() {
    	return opt;
    }

    public UmlGraph() {
    }

    /**
     * Builds and outputs a single graph according to the view overrides
     */
    public void buildGraph(Set<String> drawTypes, Set<String> highlightSet, IJavaProject javaProject) throws IOException {
		opt.outputDirectory = "C:\\temp";
		opt.outputFileName = "umlgraph.dot";

		ClassGraph c = new ClassGraph(opt);
		c.setJavaProject(javaProject);
		c.prologue();
		
		// Compute the super-type set
		Set<String> sTypes = new HashSet<String>();
		for (String typeBinding : drawTypes) {
			c.getSuperTypeSet(sTypes, typeBinding);
		}

		// Set the types
		c.setTypesToDraw(sTypes);
		c.setTypesToHighlight(highlightSet);
		
		for (String typeName : sTypes) {
			IType drawType = c.findType(typeName);
			if ( drawType != null ) {
				c.printClass(drawType, true);
			}
		}
		for (String typeName : sTypes) {
			IType drawType = c.findType(typeName);
			if ( drawType != null ) {
				c.printRelations(drawType);
			}
		}
		c.epilogue();
		
		// Clear any temporary hashtables
		ClassGraph.reset();
	}

    /**
     * Replace <> in generic types with different characters since they are not allowed in URLs.
     * Luckily, \/ are! 
     */
	public static String urlToClassName(String url) {
	   url = url.replace('\\', '<');
	   url = url.replace('/', '>');
	   return url;
	}
}
