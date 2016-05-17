package edu.wayne.moogdb;

import java.util.Hashtable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethDeclAnnotationInfo  {
	// TODO: Store binding key instead of binding object
	private static Hashtable<IMethodBinding, MethDeclAnnotationInfo> hash = new Hashtable<IMethodBinding, MethDeclAnnotationInfo>();

	public MethDeclAnnotationInfo() {
	}

	public static void putAnnotation(IMethodBinding node, MethDeclAnnotationInfo info) {
		MethDeclAnnotationInfo.hash.put(node, info);
	}
	
	public static void reset() {
		MethDeclAnnotationInfo.hash.clear();
	}

}
