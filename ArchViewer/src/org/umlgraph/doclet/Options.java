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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Represent the program options
 */ 
public class Options implements Cloneable {
    // dot's font platform dependence workaround
    private static String defaultFont;
    private static String defaultItalicFont;
    // reused often, especially in UmlGraphDoc, worth creating just once and reusing
    private static final Pattern allPattern = Pattern.compile(".*");
    protected static final String DEFAULT_EXTERNAL_APIDOC = "http://java.sun.com/j2se/1.4.2/docs/api/";
    
    static {
//	// use an appropriate font depending on the current operating system
//	// (on windows graphviz is unable to locate "Helvetica-Oblique"
//	if(System.getProperty("os.name").toLowerCase().contains("windows")) {
//	    defaultFont = "arial";
//	    defaultItalicFont = "ariali";
//	} else {
	    defaultFont = "Helvetica-Bold";
	    //defaultItalicFont = "Helvetica-Oblique";
	    defaultItalicFont = "Helvetica";
//	}
    }
    
    // instance fields
    Vector<Pattern> hidePatterns;
    boolean showQualified;
    boolean showAttributes;
    boolean showEnumerations;
    boolean showEnumConstants;
    boolean showOperations;
    boolean showConstructors;
    boolean showVisibility;
    boolean horizontal;
    boolean showType;
    boolean showComment;
    String edgeFontName;
    String edgeFontColor;
    String edgeColor;
    double edgeFontSize;
    String nodeFontName;
    String nodeFontAbstractName;
    String nodeFontColor;
    double nodeFontSize;
    String nodeFillColor;
    double nodeFontClassSize;
    String nodeFontClassName;
    String nodeFontClassAbstractName;
    double nodeFontTagSize;
    String nodeFontTagName;
    double nodeFontPackageSize;
    String nodeFontPackageName;
    Shape shape;
    String bgColor;
    public String outputFileName;
    String outputEncoding;
    Map<Pattern, String> apiDocMap;
    String apiDocRoot;
    boolean postfixPackage;
    boolean useGuillemot;
    boolean findViews;
    String viewName;
    public String outputDirectory;
    /** Guillemot left (open) */
    String guilOpen = "&laquo;"; // "\u00ab";
    /** Guillemot right (close) */
    String guilClose = "&raquo;"; // "\u00bb";
    boolean inferRelationships;
    boolean inferDependencies;
    boolean useImports;
    boolean inferDepInPackage;
    boolean compact;
    // internal option, used by UMLDoc to generate relative links between classes
    boolean relativeLinksForSourcePackages;
    // internal option, used by UMLDoc to force strict matching on the class names
    // and avoid problems with packages in the template declaration making UmlGraph hide 
    // classes outside of them (for example, class gr.spinellis.Foo<T extends java.io.Serializable>
    // would have been hidden by the hide pattern "java.*"
    // TODO: consider making this standard behaviour
    boolean strictMatching;    

    Options() {
	showQualified = false;
	showAttributes = false;
	showEnumConstants = false;
	showOperations = false;
	showVisibility = false;
	showEnumerations = false;
	showConstructors = false;
	showType = false;
	showComment = false;
	edgeFontName = defaultFont;
	edgeFontColor = "black";
	edgeColor = "black";
	edgeFontSize = 10;
	nodeFontColor = "black";
	nodeFontName = defaultFont;
	nodeFontAbstractName = defaultItalicFont;
	nodeFontSize = 10;
	nodeFontClassSize = -1;
	nodeFontClassName = null;
	nodeFontClassAbstractName = null;
	nodeFontTagSize = -1;
	nodeFontTagName = null;
	nodeFontPackageSize = -1;
	nodeFontPackageName = null;
	nodeFillColor = "lightyellow";
	bgColor = null;
	shape = new Shape();
	outputFileName = "umlgraph.dot";
	outputDirectory= null;
	outputEncoding = "ISO-8859-1";
	hidePatterns = new Vector<Pattern>();
	apiDocMap = new HashMap<Pattern, String>();
	apiDocRoot = null;
	postfixPackage = false;
	useGuillemot = true;
	findViews = false;
	viewName = null;
	inferRelationships = false;
	inferDependencies = false;
	inferDepInPackage = false;
	useImports = false;
	compact = false;
	relativeLinksForSourcePackages = false;
    }

    /** Most complete output */
    public void setAll() {
	showAttributes = true;
	showEnumerations = true;
	showEnumConstants = true;
	showOperations = true;
	showConstructors = true;
	showVisibility = true;
	showType = true;
    }


}
