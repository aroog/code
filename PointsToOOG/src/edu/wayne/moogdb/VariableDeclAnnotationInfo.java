package edu.wayne.moogdb;

import java.util.Hashtable;

import org.eclipse.jdt.core.dom.IBinding;

import edu.cmu.cs.aliasjava.AnnotationInfo;

// XXX. Gotta find a way to deal with return values!
// Use IMethodBinding for those!
public class VariableDeclAnnotationInfo  {

	// TODO: Store binding key instead of binding object
	private static Hashtable<IBinding, AnnotationInfo> hash = new Hashtable<IBinding, AnnotationInfo>();

	public VariableDeclAnnotationInfo() {
	}

	public static AnnotationInfo getInfo(IBinding binding) {
		if (binding != null) {
			return hash.get(binding);
		}
		return null;
	}
	
	public static void reset() {
		VariableDeclAnnotationInfo.hash.clear();
	}

	public static void putInfo(IBinding binding, AnnotationInfo info) {
		if(binding != null) {
			hash.put(binding, info);
		}
	}
}
