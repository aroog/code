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
 */

package org.umlgraph.doclet;


/**
 * Class's dot-comaptible alias name (for fully qualified class names)
 * and printed information
 * @version $Revision: 1.62 $
 * @author <a href="http://www.spinellis.gr">Diomidis Spinellis</a>
 */
class ClassInfo {
    /** True if the class class node has been printed */
    boolean nodePrinted;
    /** True if the class class node is hidden */
    boolean hidden;

    ClassInfo(boolean p) {
	nodePrinted = p;
    }
    
}

