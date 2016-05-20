package edu.wayne.metrics.traceability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IGraph;
import oog.itf.IObject;
import ast.AstNode;
import ast.BaseTraceability;
import ast.BodyDeclaration;
import ast.ClassInstanceCreation;
import ast.FieldAccess;
import ast.FieldDeclaration;
import ast.FieldWrite;
import ast.MethodInvocation;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.metrics.utils.ObjectsUtils;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;


/**
 * Build a reverse map, associating various elements to a set of OElements
 * 
 */
public class ReverseTraceabilityMap{
	private static ReverseTraceabilityMap s_instance = null;

	private ReverseTraceabilityMap() {
	}

	public static ReverseTraceabilityMap getInstance() {
		if (s_instance == null) {
			s_instance = new ReverseTraceabilityMap();
		}

		return s_instance;
	}

	/**
	 * Map a (fully qualified) class name to a set of IElements that are
	 * associated with it
	 */
	private Map<String, Set<IElement>> map = new HashMap<String, Set<IElement>>();

	/**
	 * Traverse field Accesses, field Writes, Method Invocations - from the underlying Expression Node
	 * 
	 */
	
	private Map<FieldDeclaration, Set<IElement>> fieldDeclMap = new HashMap<FieldDeclaration, Set<IElement>>();
	private Map<MethodInvocation, Set<IElement>> methodInvok = new HashMap<MethodInvocation, Set<IElement>>();
	private Map<FieldAccess, Set<IElement>> fieldAccessMap = new HashMap<FieldAccess, Set<IElement>>();
	private Map<FieldWrite, Set<IElement>> fieldWriteMap = new HashMap<FieldWrite, Set<IElement>>();
	
	
	
	public void init(OGraph runtimeModel) {
		TraceabilityVisitor visitor = new TraceabilityVisitor();
		runtimeModel.accept(visitor);
	}

	public boolean addMap(String declaredName, IElement element) {
		Set<IElement> set = map.get(declaredName);
		if (set == null) {
			set = new HashSet<IElement>();
			map.put(declaredName, set);
		}

		return set.add(element);
	}

	public boolean addMap(FieldDeclaration fieldDeclaration, IElement element) {
		Set<IElement> set = fieldDeclMap.get(fieldDeclaration);
		if (set == null) {
			set = new HashSet<IElement>();
			fieldDeclMap.put(fieldDeclaration, set);
		}

		return set.add(element);
	}

	
	// Maps Method Invocation
	public boolean addMap(MethodInvocation methodInc, IElement element) {
		Set<IElement> set = methodInvok.get(methodInc);
		if (set == null) {
			set = new HashSet<IElement>();
			methodInvok.put(methodInc, set);
		}

		return set.add(element);
	}
	
	
	// Maps Field Access
	public boolean addMap(FieldAccess fieldAccess, IElement element) {
		Set<IElement> set = fieldAccessMap.get(fieldAccess);
		if (set == null) {
			set = new HashSet<IElement>();
			fieldAccessMap.put(fieldAccess, set);
		}
		return set.add(element);
	}
	
	
	// Maps Field Write
	public boolean addMap(FieldWrite fieldWrite, IElement element) {
		Set<IElement> set = fieldWriteMap.get(fieldWrite);
		if (set == null) {
			set = new HashSet<IElement>();
			fieldWriteMap.put(fieldWrite, set);
		}
		return set.add(element);
	}
	
	
	public Set<IElement> getElements(String declaredName) {
		return this.map.get(declaredName);
	}

	public Map<String, Set<IElement>> getMap() {
		return map;
	}

	public Map<FieldDeclaration, Set<IElement>> getFieldDeclMap() {
		return fieldDeclMap;
	}
	

	/*
	 * Getters from Maps - MethodInv, FieldAccess, FieldWrite 
	 */
	public Map<MethodInvocation, Set<IElement>> getMethodInvokMap() {
		return methodInvok;
	}
	
	public Map<FieldAccess, Set<IElement>> getFieldReadMap() {
		return fieldAccessMap;
	}
	
	public Map<FieldWrite, Set<IElement>> getFieldWriteMap() {
		return fieldWriteMap;
	}
	
	// Extract the FieldDeclaration from a PT Edge traceability
	// DONE. Use instanceof check also that it is a case of ast.EdgePTTraceability
	public static FieldDeclaration getFieldDeclaration(BaseTraceability trace) {
		FieldDeclaration fieldDeclaration = null;
		// PT case
		if (trace instanceof ast.EdgePTTraceability) {
			AstNode expession = trace.getExpression();
			if (expession instanceof FieldDeclaration) {
				fieldDeclaration = (FieldDeclaration) expession;
			}
		}
		return fieldDeclaration;
	}

	// Extract the MethodInvocation from a DF Edge
	public static MethodInvocation getMethodInvoc(BaseTraceability trace) {
		MethodInvocation methdInk = null;
		AstNode expression = trace.getExpression();
		if(expression instanceof MethodInvocation)
		{
			methdInk =(MethodInvocation) expression;			
		}
		return methdInk;
	}
	
	
	// Extract the FieldAccess from a DF Edge
	public static FieldAccess getFieldRead(BaseTraceability trace) {
		FieldAccess fieldAccess = null;
		
		AstNode expression = trace.getExpression();
		if(expression instanceof FieldAccess && !(expression instanceof ClassInstanceCreation)
				&& !(expression instanceof FieldDeclaration))
		{
			fieldAccess =(FieldAccess) expression;		
		}
		return fieldAccess;
	}
	
	// Extract the FieldWrite from a DF Edge
	public static FieldWrite getFieldWrite(BaseTraceability trace) {
		FieldWrite fieldWrite = null;		
		AstNode expression = trace.getExpression();
		if(expression instanceof FieldWrite && !(expression instanceof ClassInstanceCreation)
				&& !(expression instanceof FieldDeclaration))		{
			fieldWrite =(FieldWrite) expression;			
		}
		return fieldWrite;
	}
	
	// Extract the TypeDeclaration that is being created?
	// Should this not acount for generics?
	public static TypeDeclaration getObjectCreationType(BaseTraceability trace) {
		TypeDeclaration typeDecl = null;
		AstNode expression = trace.getExpression();
		if (expression instanceof ClassInstanceCreation) {
			ClassInstanceCreation objectCreation = (ClassInstanceCreation) expression;
			typeDecl = objectCreation.typeDeclaration;
		}
		return typeDecl;
	}

	// Extract the TypeDeclaration that is being created?
	public static String getObjectCreationExpression(BaseTraceability trace) {
		String typeDecl = null;
		AstNode expression = trace.getExpression();
		if (expression instanceof ClassInstanceCreation) {
			ClassInstanceCreation objectCreation = (ClassInstanceCreation) expression;
			typeDecl = objectCreation.complexExpression;
		}
		return typeDecl;
	}

	// Extract the TypeDeclaration that is being created?
	public static ClassInstanceCreation getObjectCreation(BaseTraceability trace) {
		ClassInstanceCreation typeDecl = null;
		AstNode expression = trace.getExpression();
		if (expression instanceof ClassInstanceCreation) {
			ClassInstanceCreation objectCreation = (ClassInstanceCreation) expression;
			typeDecl = objectCreation;
		}
		return typeDecl;
	}

	/**
	 * Populate the map, while traversing the OGraph
	 */
	public class TraceabilityVisitor extends OGraphVisitorBase {

		public TraceabilityVisitor() {
		}

		// Visit all the Edges from the OGraph
		@Override
		public boolean visit(IEdge oEdge) {
			// System.out.println("Visiting OEdge " + oEdge);

			// Map FieldDeclarations to OEdges
			Set<BaseTraceability> traceability = oEdge.getTraceability();
			if (traceability != null)
				for (BaseTraceability trace : traceability) {
					mapFieldDeclaration(oEdge, trace);
					mapMethodInvoc(oEdge, trace);
					mapFieldRead(oEdge, trace);
					mapFieldWrite(oEdge, trace);				
				}
			return super.visit(oEdge);
		}

	
		@Override
		public boolean visit(IObject oObject) {
			// Map class names to Objects
			Set<BaseTraceability> traceability = oObject.getTraceability();
			if (traceability != null)
				for (BaseTraceability trace : traceability) {
					mapClass(oObject, trace);
				}

			return super.visit(oObject);
		}

		private void mapFieldDeclaration(IElement element, BaseTraceability trace) {
			FieldDeclaration fieldDeclaration = getFieldDeclaration(trace);
			// Add to the map
			if (fieldDeclaration != null) {
				ReverseTraceabilityMap.this.addMap(fieldDeclaration, element);
			}
		}


		/*
		 * Maps methodInvo, FieldAccess, FieldWrites with corresponding IEdges
		 */
		private void mapMethodInvoc(IElement element, BaseTraceability trace) {
			MethodInvocation methodInvc = getMethodInvoc(trace);
			// Add to the map
			if (methodInvc != null) {
				ReverseTraceabilityMap.this.addMap(methodInvc, element);				
			}
		}

		private void mapFieldRead(IElement element, BaseTraceability trace) {
			FieldAccess fieldAccess = getFieldRead(trace);
			// Add to the map
			if (fieldAccess != null) {
				ReverseTraceabilityMap.this.addMap(fieldAccess, element);				
			}
		}
		

		private void mapFieldWrite(IElement element, BaseTraceability trace) {
			FieldWrite fieldWrite = getFieldWrite(trace);
			// Add to the map
			if (fieldWrite != null) {
				ReverseTraceabilityMap.this.addMap(fieldWrite, element);				
			}
			
		}
		
		private void mapClass(IObject oObject, BaseTraceability trace) {
			// TODO: HIGH. Read everything from the traceability object.
			// XXX. This looks fishy here...
			TypeDeclaration enclosingType = ObjectsUtils.getEnclosingType(trace);
			if (enclosingType != null) {
				Type type = enclosingType.type;
				if (type != null) {
					String declaredType = type.toString();

					// String objectName = lineKey.getObjectName();
					// String declaringType =
					// lineKey.getFullyQualifiedDeclaringTypeName();

					// Add to the map
					ReverseTraceabilityMap.this.addMap(declaredType, oObject);
				} else {
					// TODO: Do something better here.
					System.err
							.println("Error: TypeDeclaration.enclosingType: unexpected null type");
				}
			} else {
				// TODO: Do something better here.
				System.err
						.println("Error: TypeDeclaration: unexpected null enclosingType");
			}
		}

		@Override
		public boolean visit(IGraph node) {
			// System.out.println("Visiting the OGraph");
			return super.visit(node);
		}
	}

	public void reset() {
		this.map.clear();
		this.methodInvok.clear();
		this.fieldDeclMap.clear();
		this.fieldAccessMap.clear();
		this.fieldWriteMap.clear();
		
	}
}
