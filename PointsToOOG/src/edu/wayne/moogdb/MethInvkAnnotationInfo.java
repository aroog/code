package edu.wayne.moogdb;

import java.util.Hashtable;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethInvkAnnotationInfo  {

	// TODO: avoid storing MethodInvocation ASTNode object
	private static Hashtable<MethodInvocation, MethInvkAnnotationInfo> hash = new Hashtable<MethodInvocation, MethInvkAnnotationInfo>();

	public MethInvkAnnotationInfo() {
	}

	public static MethInvkAnnotationInfo getAnnotation(MethodInvocation node) {
		return MethInvkAnnotationInfo.hash.get(node);
	}

	public static void putAnnotation(MethodInvocation node, MethInvkAnnotationInfo info) {
		MethInvkAnnotationInfo.hash.put(node, info);
	}

	public static void reset() {
		MethInvkAnnotationInfo.hash.clear();
	}
}
