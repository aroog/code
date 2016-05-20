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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

class ClassGraph {
	private IJavaProject javaProject;
	
	private Options opt = null;
    protected Map<String, ClassInfo> classnames = new HashMap<String, ClassInfo>();

	protected static final char FILE_SEPARATOR = '/';

	enum Font {
		NORMAL, ABSTRACT, CLASS, CLASS_ABSTRACT, TAG, PACKAGE
	}

	enum Align {
		LEFT, CENTER, RIGHT
	};

	protected Set<String> typesToDraw = new LinkedHashSet<String>();
	
	protected Set<String> typesToHighlight = new LinkedHashSet<String>();

	protected PrintWriter w;

	protected String linePostfix;

	protected String linePrefix;

	public ClassGraph(Options opt) {
		this.opt = opt;

		if (opt.compact) {
			linePrefix = "";
			linePostfix = "";
		}
		else {
			linePrefix = "\t";
			linePostfix = "\r\n";
		}
	}
	
	public void setTypesToDraw(Set<String> typeNames) {
		for (String typeName : typeNames) {
			IType typeBinding = findType(typeName);
			if ( typeBinding != null ) {
				typesToDraw.add(typeBinding.getFullyQualifiedName());
			}
		}
	}
	
	public void setTypesToHighlight(Set<String> highlightSet) {
		this.typesToHighlight.addAll(highlightSet);

	}

	/** Return the class's name, possibly by stripping the leading path */
	private String qualifiedName(Options opt, String r) {
		if (!opt.showQualified) {
			// Create readable string by stripping leading path
			for (;;) {
				int dotpos = r.lastIndexOf('.');
				if (dotpos == -1)
					break; // Work done!
				/*
				 * Change all occurences of "p1.p2.myClass<S extends dummy.Otherclass>" into "myClass<S extends
				 * Otherclass>"
				 */
				int start = dotpos;
				while (start > 0 && Character.isJavaIdentifierPart(r.charAt(start - 1)))
					start--;
				r = r.substring(0, start) + r.substring(dotpos + 1);
			}
		}
		return r;
	}

	/**
	 * Escape &lt;, &gt;, and &amp; characters in the string with the corresponding HTML entity code.
	 */
	private String escape(String s) {
		final Pattern toEscape = Pattern.compile("[&<>]");

		if (toEscape.matcher(s).find()) {
			StringBuffer sb = new StringBuffer(s);
			for (int i = 0; i < sb.length();) {
				switch (sb.charAt(i)) {
				case '&':
					sb.replace(i, i + 1, "&amp;");
					i += "&amp;".length();
					break;
				case '<':
					sb.replace(i, i + 1, "&lt;");
					i += "&lt;".length();
					break;
				case '>':
					sb.replace(i, i + 1, "&gt;");
					i += "&gt;".length();
					break;
				default:
					i++;
				}
			}
			return sb.toString();
		}
		else
			return s;
	}

	/**
	 * Convert embedded newlines into HTML line breaks
	 */
	private String htmlNewline(String s) {
		if (s.indexOf('\n') == -1)
			return (s);

		StringBuffer sb = new StringBuffer(s);
		for (int i = 0; i < sb.length();) {
			if (sb.charAt(i) == '\n') {
				sb.replace(i, i + 1, "<br/>");
				i += "<br/>".length();
			}
			else
				i++;
		}
		return sb.toString();
	}

	/**
	 * Wraps a string in Guillemot (or an ASCII substitute) characters.
	 * 
	 * @param str the <code>String</code> to be wrapped.
	 * @return the wrapped <code>String</code>.
	 */
	private String guilWrap(Options opt, String str) {
		return opt.guilOpen + str + opt.guilClose;
	}

	/** Print a a basic type t */
	private String type(Options opt, IType t) {
		String type = "";
		if (opt.showQualified)
			type = t.getFullyQualifiedName();
		else
			type = t.getElementName();
		// type += typeParameters(opt, t.asParameterizedType());
		return type;
	}

	// /** Print the parameters of the parameterized type t */
	// private String typeParameters(Options opt, ParameterizedType t) {
	// String tp = "";
	// if (t == null)
	// return tp;
	// Type args[] = t.typeArguments();
	// tp += "&lt;";
	// for (int i = 0; i < args.length; i++) {
	// tp += type(opt, args[i]);
	// if (i != args.length - 1)
	// tp += ", ";
	// }
	// tp += "&gt;";
	// return tp;
	// }

	/** Print the common class node's properties */
	private void nodeProperties(Options opt) {
		w.print(", fontname=\"" + opt.nodeFontName + "\"");
		w.print(", fontcolor=\"" + opt.nodeFontColor + "\"");
		w.print(", fontsize=" + opt.nodeFontSize);
		w.print(opt.shape.graphvizAttribute());
		w.println("];");
	}

    protected ClassInfo getClassInfo(String className) {
    	return classnames.get(removeTemplate(className));
        }
        
        private ClassInfo newClassInfo(String className, boolean printed) {
    	ClassInfo ci = new ClassInfo(printed);
            classnames.put(removeTemplate(className), ci);
            return ci;
        }

    	/**
    	 * Characters that are not allowed in DOT IDs, etc.
    	 */
    	private static final char[] stripChars = { '<', '>', '[', ']'};

    	public static String removeTemplate(String str) {

    		String ret = str;
    		for (int i = 0; i < stripChars.length; i++) {
    			if (ret.indexOf(stripChars[i]) > -1) {
    				ret = ret.replace(stripChars[i], '_');
    			}
    		}
    		return ret;
    	}
       private static String classToUrl(IType c) {
    	   String url = c.getFullyQualifiedName();
    	   url = url.replace('<', '\\');
    	   url = url.replace('>', '/');
    	   return url;
       }
       
	public void printClass(IType c, boolean rootClass) {
		ClassInfo ci;
		boolean toPrint;

		String className = removeTemplate(c.getElementName());
		if ((ci = getClassInfo(className)) != null)
		    toPrint = !ci.nodePrinted;
		else {
		    toPrint = true;
		    ci = newClassInfo(className, true);
		}
		try {
	        if (toPrint&&(!c.isEnum() || opt.showEnumerations)) {
	        	 // Add comment (use qualified name)
	        	w.println("\t// " + c.getFullyQualifiedName());
	        	// Create dot label (use sanitized name)
	        	w.print("\t" + className + " [label=");
	        	externalTableStart(opt, c.getFullyQualifiedName(), classToUrl(c));
	        	innerTableStart();
	        	if (c.isInterface())
	        		tableLine(Align.CENTER, guilWrap(opt, "interface"));
	        	if (c.isEnum())
	        		tableLine(Align.CENTER, guilWrap(opt, "enumeration"));
	        	Font font = isAbstract(c) && !c.isInterface() ? Font.CLASS_ABSTRACT : Font.CLASS;
	        	String qualifiedName = qualifiedName(opt, c.getElementName());
	        	int startTemplate = qualifiedName.indexOf('<');
	        	int idx = 0;
	        	if (startTemplate < 0)
	        		idx = qualifiedName.lastIndexOf('.');
	        	else
	        		idx = qualifiedName.lastIndexOf('.', startTemplate);
	        	if (opt.showComment)
	        		tableLine(Align.LEFT, htmlNewline(escape("")), opt, Font.CLASS);
	        	else if (opt.postfixPackage && idx > 0 && idx < (qualifiedName.length() - 1)) {
	        		String packageName = qualifiedName.substring(0, idx);
	        		String cn = className.substring(idx + 1);
	        		tableLine(Align.CENTER, escape(cn), opt, font);
	        		tableLine(Align.CENTER, packageName, opt, Font.PACKAGE);
	        	}
	        	else {
	        		tableLine(Align.CENTER, escape(qualifiedName), opt, font);
	        	}
	        	innerTableEnd();

	        	externalTableEnd();
	        	nodeProperties(opt);
	        }
        }
        catch (JavaModelException e) {
	        e.printStackTrace();
        }
	}

	private boolean isAbstract(IType c) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Return the full name of a relation's node. This may involve appending the port :p for the standard nodes whose
	 * outline is rendered through an inner table.
	 */
	private String relationNode(IType c) {
		return removeTemplate(c.getElementName());
	}

	/** Print a class's relations */
	public void printRelations(IType c) {

		try {
		IType s = getSuperclass(c);
		if (s != null && !s.getFullyQualifiedName().equals("java.lang.Object")) {
			w.println("\t" + relationNode(s) + " -> " + relationNode(c) + " [dir=back,arrowtail=empty];");
		}

		// Print realizations (Java interfaces)
		for (IType iface : getSuperInterfaces(c)) {
			if (iface != null ) {
				w.println("\t" + relationNode(iface) + " -> " + relationNode(c) + " [dir=back,arrowtail=empty,style=dashed];");
			}
		}
        }
        catch (JavaModelException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}

	/***
	 * Hashtable to cache the resolution from fully qualified type names to IType objects
	 * XXX: Consolidate with hashtable maintained inside EclipseJavaUtil
	 */
	private static HashMap<String, IType> types = new HashMap<String, IType>();

	public static void reset() {
		types.clear();
		EclipseJavaUtil.reset();
	}
	
	/**
	 * Important: This takes fully qualified type name
	 */
	public IType findType(String qualifiedName) {
		IType type = null;
		if (qualifiedName != null) {
			type = types.get(qualifiedName);
			if (type == null) {
				try {
					if (javaProject != null) {
						type = javaProject.findType(qualifiedName);
					}
					if (type != null) {
						// Add to hashtable
						types.put(qualifiedName, type);
					}
				}
				catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		}
		return type;
	}

	private static void addToCache(String qualifiedName, IType type) {
		if (type != null) {
			// Add to hashtable
			types.put(qualifiedName, type);
		}
	}
	
	public void getSuperTypeSet(Set<String> types, String qualifiedName) {
		IType type = findType(qualifiedName);
		if ( type != null ) {
			types.add(type.getFullyQualifiedName());
			
			try{
			IType superClass = getSuperclass(type);
			if ( superClass != null ) {
				String superFullyQualified = superClass.getFullyQualifiedName();
//				types.add(superFullyQualified);
				getSuperTypeSet(types, superFullyQualified);
			}
			for(IType superInterface : getSuperInterfaces(type) ) {
				String superFullyQualified = superInterface.getFullyQualifiedName();
					// types.add(superFullyQualified);
				getSuperTypeSet(types, superFullyQualified);
			}
			}
            catch (JavaModelException e) {
	            e.printStackTrace();
            }
		}
    }
	
	/**
	 * Helper method to lookup the super-class of an IType. Important: Avoid using type.getSuperclassName() since that
	 * may not return a fully qualified type name
	 */
	private IType getSuperclass(IType type) throws JavaModelException {
	    String superclassName = type.getSuperclassName();
	    if(superclassName!=null) {
	        String fullySuperclassName = EclipseJavaUtil.resolveType(type, superclassName);
	        if(fullySuperclassName!=null&&!fullySuperclassName.equals("java.lang.Object")) { //$NON-NLS-1$
	            if(fullySuperclassName.equals(type.getFullyQualifiedName())) {
	                return null;
	            }
	            IType superType = type.getJavaProject().findType(fullySuperclassName);
	            addToCache(fullySuperclassName, superType);
	            return superType;
	        }
	    }
	    return null;
	}
	
	
	
	/**
	 * Helper method to lookup the super-class of an IType. Important: Avoid using type.getSuperInterfaceNames() since that
	 * may not return a fully qualified type name
	 */
	private static IType[] getSuperInterfaces(IType type) throws JavaModelException {
		List<IType> types = new ArrayList<IType>();
	    for(String superclassName : type.getSuperInterfaceNames() ) {
	    if(superclassName!=null) {
	        String fullySuperclassName = EclipseJavaUtil.resolveType(type, superclassName);
	        if(fullySuperclassName!=null&&!fullySuperclassName.equals("java.lang.Object")) { //$NON-NLS-1$
	            if(fullySuperclassName.equals(type.getFullyQualifiedName())) {
	                //FIX JBIDE-1642
	                return null;
	            }
	            IType superType = type.getJavaProject().findType(fullySuperclassName);
	            addToCache(fullySuperclassName, superType);	            
	            types.add(superType);
	        }
	    }
	    }
	    return types.toArray(new IType[0]);
	}
	/**
	 * Dot prologue
	 * 
	 * @throws IOException
	 */
	public void prologue() throws IOException {
		OutputStream os = null;

		if (opt.outputFileName.equals("-"))
			os = System.out;
		else {
			// prepare output file. Use the output file name as a full path unless the output
			// directory is specified
			File file = null;
			if (opt.outputDirectory != null)
				file = new File(opt.outputDirectory, opt.outputFileName);
			else
				file = new File(opt.outputFileName);
			// make sure the output directory are there, otherwise create them
			if (file.getParentFile() != null && !file.getParentFile().exists())
				file.getParentFile().mkdirs();
			os = new FileOutputStream(file);
		}

		// print plologue
		w = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os), opt.outputEncoding));
		w.println("digraph G {\r\n" + "\tedge [fontname=\"" + opt.edgeFontName
		        + "\",fontsize=10,labelfontname=\"" + opt.edgeFontName + "\",labelfontsize=10];\r\n"
		        + "\tnode [fontname=\"" + opt.nodeFontName + "\",fontsize=10,shape=plaintext];");
		if (opt.horizontal)
			w.println("\trankdir=LR;\r\n\tranksep=1;");
		if (opt.bgColor != null)
			w.println("\tbgcolor=\"" + opt.bgColor + "\";\r\n");
	}

	/** Dot epilogue */
	public void epilogue() {
		w.println("}\r\n");
		w.flush();
		w.close();
	}

	private void externalTableStart(Options opt, String name, String url) {
		String bgcolor = "";
		if (opt.nodeFillColor != null)
			bgcolor = " bgcolor=\"" + opt.nodeFillColor + "\"";
		// If the type is already listed, highlight it differently...
		if (typesToHighlight.contains(name)) {
			bgcolor = " bgcolor=\"tomato\"";
		}
		String href = "";
		if (url != null)
			href = " href=\"" + url + "\"";

		w.print("<<table border=\"" + opt.shape.border() + "\" cellborder=\"" + opt.shape.cellBorder()
		        + "\" cellspacing=\"0\" " + "cellpadding=\"2\" port=\"p\"" + bgcolor + href + ">" + linePostfix);
	}

	private void externalTableEnd() {
		w.print(linePrefix + linePrefix + "</table>>");
	}

	private void innerTableStart() {
		w.print(linePrefix + linePrefix + "<tr><td><table border=\"0\" cellspacing=\"0\" " + "cellpadding=\"1\">" + linePostfix);
	}

	private void innerTableEnd() {
		w.print(linePrefix + linePrefix + "</table></td></tr>" + linePostfix);
	}

	private void tableLine(Align align, String text) {
		tableLine(align, text, null, Font.NORMAL);
	}

	private void tableLine(Align align, String text, Options opt, Font font) {
		String open;
		String close = "</td></tr>";
		String prefix = linePrefix + linePrefix + linePrefix;
		String alignText;

		if (align == Align.CENTER)
			alignText = "center";
		else if (align == Align.LEFT)
			alignText = "left";
		else if (align == Align.RIGHT)
			alignText = "right";
		else
			throw new RuntimeException("Unknown alignement type " + align);

		text = fontWrap(" " + text + " ", opt, font);
		open = "<tr><td align=\"" + alignText + "\" balign=\"" + alignText + "\">";
		w.print(prefix + open + text + close + linePostfix);
	}

	/**
	 * Wraps the text with the appropriate font according to the specified font type
	 * 
	 * @param opt
	 * @param text
	 * @param font
	 * @return
	 */
	private String fontWrap(String text, Options opt, Font font) {
		if (font == Font.ABSTRACT) {
			return fontWrap(text, opt.nodeFontAbstractName, opt.nodeFontSize);
		}
		else if (font == Font.CLASS) {
			return fontWrap(text, opt.nodeFontClassName, opt.nodeFontClassSize);
		}
		else if (font == Font.CLASS_ABSTRACT) {
			String name;
			if (opt.nodeFontClassAbstractName == null)
				name = opt.nodeFontAbstractName;
			else
				name = opt.nodeFontClassAbstractName;
			return fontWrap(text, name, opt.nodeFontClassSize);
		}
		else if (font == Font.PACKAGE) {
			return fontWrap(text, opt.nodeFontPackageName, opt.nodeFontPackageSize);
		}
		else if (font == Font.TAG) {
			return fontWrap(text, opt.nodeFontTagName, opt.nodeFontTagSize);
		}
		else {
			return text;
		}
	}

	/**
	 * Wraps the text with the appropriate font tags when the font name and size are not void
	 * 
	 * @param text the text to be wrapped
	 * @param fontName considered void when it's null
	 * @param fontSize considered void when it's <= 0
	 */
	private String fontWrap(String text, String fontName, double fontSize) {
		if (fontName == null && fontSize == -1)
			return text;
		else if (fontName == null)
			return "<font point-size=\"" + fontSize + "\">" + text + "</font>";
		else if (fontSize <= 0)
			return "<font face=\"" + fontName + "\">" + text + "</font>";
		else
			return "<font face=\"" + fontName + "\" point-size=\"" + fontSize + "\">" + text + "</font>";
	}

	public IJavaProject getJavaProject() {
    	return javaProject;
    }

	public void setJavaProject(IJavaProject javaProject) {
    	this.javaProject = javaProject;
    }
	
	
}
