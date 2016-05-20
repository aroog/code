package edu.wayne.metrics.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ast.BaseTraceability;
import ast.Expression;
import ast.TypeDeclaration;
import edu.wayne.metrics.adb.Util;

public class ObjectsUtils {
	private final static String[] primitiveTypes = {"boolean", "byte", "char", "short", "int", "long", "float", "double"};
	private final static String[] lowLevelPackagesRegEx = {"java.util.*","java.lang.*","java.awt.*"};
	
	public static boolean isLowLevelObject(String fullyQualifiedName) {
		//TODO: check if type is java.util... maybe use an external list provided by developer.
		for (int i=0;i<lowLevelPackagesRegEx.length;i++){
			Pattern patternJava = Pattern.compile(lowLevelPackagesRegEx[i]);
			Matcher matcherJava = patternJava.matcher(fullyQualifiedName);
			if (matcherJava.matches()) return true;
		}
		return false;
    }
	
//TODO make it work with non-qualified names too.
	public static boolean isLowLevelFlatObject(String label){
		//TODO: check if type is java.util... maybe use an external list provided by developer.
		String[] classnameGeneric = label.split("<");
		String[] classnameArray = classnameGeneric[0].split("\\[");
		String classname = classnameArray[0];
		if (isPrimitiveType(classname)) return true;
		try{
			Class.forName(classname);
		}
		catch(ClassNotFoundException ex){
			return false;
		}		
		return true;
	}

	private static boolean isPrimitiveType(String classname) {
		for (int i = 0; i < primitiveTypes.length; i++) {
	        if (primitiveTypes[i].equals(classname))
	        	return true;
        }	
		return false;
    }
	
	// TOMAR: TODO: HIGH. FIX ME. Was is das?
	public static String extractQualifiedName(Set<BaseTraceability> set){
		//HACK: For multiple Resources it returns the first name only. They should all be of the same name. 
		String fullyQualifiedTypeName = "";
		for(BaseTraceability traceability : set) {
			// XXX. Why using toString() instead of getFullyQualifiedName()
			TypeDeclaration enclosingType = getEnclosingType(traceability);
			if (enclosingType != null ) {
				fullyQualifiedTypeName = enclosingType.toString();
			}
			// Return the first one...
			break;
		}
		if (fullyQualifiedTypeName == null) {
			int debug = 0;
			debug++; // this should not happen.
		}
		return fullyQualifiedTypeName;
	}
	
	// TOMAR: TODO: HIGH. FIX ME.	
	public static int getScattering(Set<BaseTraceability> traceability) {
	    Set<String> declaringTypes = new HashSet<String>();
	    for (BaseTraceability trace : traceability) {
			TypeDeclaration enclosingType = getEnclosingType(trace);
			if (enclosingType != null ) {
				declaringTypes.add(enclosingType.toString());
			}
		}
	    return declaringTypes.size();
    }
	
//	public static NodeType getNodeType(Object object) {
//		
//		if(object instanceof OObject){
//			OObject runtimeObject = (OObject)object;
//		if (runtimeObject.equals(runtimeModel.getRootObject()))
//			return NodeType.ORoot;
//		
//		//TODO: why are you doing this?
//		String qualfiedName = ObjectsUtils.extractQualfiedName(runtimeObject.getTraceability());
//		if (ObjectsUtils.isLowLevelObject(qualfiedName))
//			return NodeType.LLO;
//		}
//		
//		if(object instanceof ODomain){
//			ODomain runtimeDomain = (ODomain)object;
//		if (runtimeDomain.equals(runtimeModel.getRootDomain()))
//			return NodeType.DRoot;
//		if (ObjectsUtils.isTopLevelDomain(runtimeDomain))
//			return NodeType.TLD;
//		}
//		
//		return NodeType.O;
//	}
//	

	
	//TODO: move method getParentObjects here
	
	public static TypeDeclaration getEnclosingType(BaseTraceability trace) {
		return Util.getEnclosingTypeDeclaration(trace);
		
//		TypeDeclaration typeDeclaration = null;
//		
//		AstNode expression = trace.expression;
//		if(expression != null ) {
//			typeDeclaration = expression.enclosingType;
//		}
//		else {
//			int debug = 0; debug++;
//		}
//        return typeDeclaration;
    }

	public static TypeDeclaration getEnclosingType(Expression expr) {
		return Util.getEnclosingTypeDeclaration(expr);
    }
}
