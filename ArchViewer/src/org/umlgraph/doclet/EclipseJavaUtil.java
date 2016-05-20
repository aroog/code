/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.umlgraph.doclet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class EclipseJavaUtil {

	public static String getMemberTypeAsString(IMember member) {
		if(member instanceof IField) return getMemberTypeAsString((IField)member);
		if(member instanceof IMethod) return getMemberTypeAsString((IMethod)member);
		return null;
	}

	public static String getMemberTypeAsString(IField f) {
		if(f == null) return null;
		try	{
			String typeName = new String(Signature.toCharArray(f.getTypeSignature().toCharArray()));
			return resolveType(f.getDeclaringType(), typeName);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getMemberTypeAsString(IMethod m) {
		if(m == null) return null;
		try	{
			return resolveTypeAsString(m.getDeclaringType(), m.getReturnType());
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String resolveTypeAsString(IType type, String typeName) {
		if(type == null || typeName == null) return null;
		typeName = new String(Signature.toCharArray(typeName.toCharArray()));
		return resolveType(type, typeName);
	}

	static String NULL = ";;;";

	static class Resolved {
		IType type;
		Map<String, String> types = new HashMap<String, String>();
		Resolved(IType type) {
			this.type = type;
		}
		
		void setType(IType type) {
			this.type = type;
			types.clear();
		}
	}
	
	private static Map<String,Resolved> resolved = new HashMap<String, Resolved>();
	
	static String getKey(IType type) {
		String n = type.getFullyQualifiedName();
		IJavaProject jp = type.getJavaProject();
		if(jp == null) return n;
		IProject p = jp.getProject();
		if(p == null || !p.isAccessible()) return n;
		return p.getName() + ":" + n;
	}
	public static String resolveType(IType type, String typeName) {
		if(type == null) return null;
		if(type.isBinary() || typeName == null) return typeName;
		
		String n = getKey(type);
		Resolved r = resolved.get(n);
		if(r == null) {
			r = new Resolved(type);
			resolved.put(n, r);
			if(resolved.size() % 100 == 0) {
				System.out.println("-->" + resolved.size() + " " + n);
			}
		}
		if(r.type != type) {
			r.setType(type);
		}
		
		String result = r.types.get(typeName);
		
		if(result != null) {
			return (result == NULL) ? null : result;
		}
		
		result = __resolveType(type, typeName);
		
		String nresult = result == null ? NULL : result;
		
		r.types.put(typeName, nresult);
		
//		System.out.println(n + " " + typeName);
		
		return result;

	}
	
	private static String __resolveType(IType type, String typeName) {
		if(type == null || typeName == null) return null;
		try	{
			String resolvedArray[][] = type.resolveType(typeName);
//			resolvedArray == null for primitive types
			if(resolvedArray == null) return typeName;
			typeName = "";
			for (int i = 0; i < resolvedArray[0].length; i++) 
				typeName += (!"".equals(typeName) ? "." : "") + resolvedArray[0][i]; 
			return typeName;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static IType findType(IJavaProject javaProject, String qualifiedName) throws JavaModelException {
		if(qualifiedName == null || qualifiedName.length() == 0) return null;
		IType type = javaProject.findType(qualifiedName);
		if(type != null) return type;
		int dot = qualifiedName.lastIndexOf('.');
		String packageName = (dot < 0) ? "" : qualifiedName.substring(0, dot);
		String shortName = qualifiedName.substring(dot + 1);
		IPackageFragmentRoot[] rs = javaProject.getPackageFragmentRoots();
		for (int i = 0; i < rs.length; i++) {
			IPackageFragment f = rs[i].getPackageFragment(packageName);
			if(f == null || !f.exists()) continue;
			ICompilationUnit[] us = f.getCompilationUnits();

			for (int j = 0; j < us.length; j++) {
				IType t = us[j].getType(shortName);
				if(t != null && t.exists()) return t;
			}
		}
		return null;
	}

	// Cleanup by clearing out static Hashtable
	public static void reset() {
		resolved.clear();
	}
}

