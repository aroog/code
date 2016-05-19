package edu.wayne.summary.strategies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IObject;
import secoog.EdgeType;
import ast.AstNode;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import ast.FieldDeclaration;
import ast.MethodDeclaration;
import ast.MiniAstUtils;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OObject;
import edu.wayne.ograph.OPTEdge;
import edu.wayne.summary.traceability.ReverseTraceabilityMap;

public abstract class EdgeSummary extends SummaryBase<IElement> {

	// Careful: "shared" vs. "SHARED"
	private static final String D_SHARED = "SHARED";
	
	protected abstract boolean includeSelfEdges();

	/*
	 * o:C -> o1:C1 [outgoing edge] o2:C2 -> o:C [incoming edge] then MIRC(C) ->
	 * {C1, C2}
	 * 
	 * 
	 * Strategy1: Disregard hierarchy; - Chase any object that is reachable,
	 * regardless of the ownership depth of the object
	 * 
	 * Ranking Based number of incoming and outgoing edges that the related
	 * classes have
	 */
	@Override
	public Set<Info<IElement>> computeClassesRelatedToClass(String javaClass) {

		Set<EdgeType> edgeTypes = null;
		Set<Info<IElement>> classes = new TreeSet<Info<IElement>>();

		Set<IEdge> summaryEdges = getSummaryEdges(javaClass, edgeTypes);
		Set<IObject> summaryObjects = getClassesFromEdges(summaryEdges,
				javaClass);

		for (IObject obj : summaryObjects) {

			String fullyQualifiedName = obj.getC().getFullyQualifiedName();
			if (fullyQualifiedName.compareTo(javaClass) != 0) {
				InfoIElement e = new InfoIElement(fullyQualifiedName, InfoType.CLASS);
				e.addAll(getSummaryEdges(fullyQualifiedName, edgeTypes));
				classes.add(e);
			}

		}

		return classes;
	}

	/*
	 * MIMs(C): related to dataflow edges.
	 * 
	 * Most Important Methods of C.
	 * 
	 * MIMs(C) = { C::mc } NOT {C'::m}
	 * 
	 * 
	 * given a class, look for each method mc in C look for all expressions in
	 * mc that are of the following types:
	 * 
	 * FieldRead FieldWrites MethodInvocation ClassInstanceCreation: (creates
	 * objects, not edges)
	 * 
	 * Count the number of elements created by an expression. Add them all.
	 * 
	 * DONE: Find a solution to use the options singleton to either
	 * include/exclude static methods and constructors
	 */

	@Override
	public Set<Info<IElement>> computeMethodsOfClass(String javaClass) {

		Map<String, Info<IElement>> map = new HashMap<String, Info<IElement>>();

		Set<EdgeType> edgeTypes = new HashSet<EdgeType>();
		edgeTypes.add(EdgeType.DataFlow);
		Set<IEdge> edges = this.getEdges(edgeTypes);
		for (IEdge edge : edges) {
			for (BaseTraceability traceability : edge.getTraceability()) {

				TypeDeclaration enclosingType = MiniAstUtils.getEnclosingTypeDeclaration(traceability.getExpression());
				if (enclosingType != null) {
					String classType = enclosingType.getFullyQualifiedName();
					if (classType.compareTo(javaClass) == 0) {

						ast.BodyDeclaration declaration = traceability.getExpression().enclosingDeclaration;
						if (declaration instanceof MethodDeclaration) {

							MethodDeclaration methodDeclaration = (MethodDeclaration) declaration;
							if (Utils.isMethodCompatible(methodDeclaration)) {

								String methodName = methodDeclaration.methodName;

								if (!map.containsKey(methodName)) {
									map.put(methodName, new InfoIElement(
											methodName,InfoType.METHOD));
								}

								Info<IElement> infoIElement = map
										.get(methodName);

								addMethodEdges(edge, traceability, infoIElement);
							}
						}
					}
				}

			}
		}
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();

		for (IObject obj : instance.getAllObjects()) {
			for (BaseTraceability trace : obj.getTraceability()) {
				TypeDeclaration enclosingType = MiniAstUtils.getEnclosingTypeDeclaration(trace.getExpression());
				if (enclosingType != null) {

					AstNode expression = trace.getExpression();
					if (expression instanceof ClassInstanceCreation) {
						String classType = enclosingType
								.getFullyQualifiedName();
						if (classType.compareTo(javaClass) == 0) {
							ast.BodyDeclaration declaration = trace.getExpression().enclosingDeclaration;
							if (declaration instanceof MethodDeclaration) {

								MethodDeclaration methodDeclaration = (MethodDeclaration) declaration;
								if (Utils.isMethodCompatible(methodDeclaration)) {
									String methodName = methodDeclaration.methodName;
									if (!map.containsKey(methodName)) {
										map.put(methodName, new InfoIElement(
												methodName, InfoType.METHOD));
									}

									Info<IElement> infoIElement = map
											.get(methodName);

									addMethodCreatedObj(obj, trace,
											infoIElement);
								}
							}
						}
					}
				}
			}
		}

		Set<Info<IElement>> methods = new TreeSet<Info<IElement>>(map.values());

		return methods;
	}

	protected abstract void addMethodCreatedObj(IObject obj,
			BaseTraceability trace, Info<IElement> infoIElement);

	protected abstract void addMethodEdges(IEdge edge,
			BaseTraceability traceability, Info<IElement> infoIElement);

	protected abstract Set<IObject> getClassesFromEdges(Set<IEdge> edges,
			String javaClass);

	protected abstract Set<IEdge> getSummaryEdges(String javaClass,
			Set<EdgeType> edgeTypes);

	/**
	 * One implementation of: getMostImportantClasses
	 * - Get the top-level objects in the top-level domains. 
	 * -- Get the classes associated with
	 * ---Reach into traceability
	 * 
	 * Order is based upon number of edges retrieved from method
	 * {@link EdgeSummary#getSummaryEdges(String, Set)}
	 */
	@Override
	// TODO: Add types in public domains of top-level objects
	// TODO: Add class of mainObject to MICs?
	// TODO: Rename: computeClasses -> computeMICs
	public Set<Info<IElement>> computeClasses() {
		Set<Info<IElement>> classes = new TreeSet<Info<IElement>>();
		OGraph oGraph = getGraph();
		IObject dummyObject = null;
		if (oGraph != null ) {
			dummyObject = oGraph.getRoot();
		}
		
		boolean includeObjectsInPublicDomains = Options.getInstance().includeObjectsInPublicDomains();
		
		if (dummyObject != null) {
			// dummy has 3 domains: D_SHARED, D_LENT, D_UNIQUE
			// D_SHARED contains the main object
			for (IDomain rootDomain : dummyObject.getChildren()) {

				if (!rootDomain.getD().equals(D_SHARED)) {
					continue;
				}
				for (IObject mainObject : rootDomain.getChildren()) {

					// TODO: Add option to skip objects in the shared domain that are not the main object
					if (!mainObject.isMainObject()) {
						continue;
					}

					for (IDomain topLevelDomain : mainObject.getChildren()) {
						for (IObject topLevelObject : topLevelDomain.getChildren()) {
							Type type = topLevelObject.getC();
							if (type != null) {
								Info<IElement> clazz = addObjectType(type);
								classes.add(clazz);
							}
							
							// Check option for including objects in public domains, before doing so.
							if ( includeObjectsInPublicDomains ) {
								for(IDomain domain : topLevelObject.getChildren() ) {
									// Skip private domains
									if (!domain.isPublic() ) {
										continue;
									}

									for (IObject object : domain.getChildren()) {
										Type objectType = object.getC();
										if (objectType != null) {
											Info<IElement> clazz = addObjectType(objectType);
											classes.add(clazz);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return classes;
	}

	private Info<IElement> addObjectType(Type type) {
	    Info<IElement> clazz = new InfoIElement(type.getFullyQualifiedName(), InfoType.CLASS);
	    Set<IElement> elemsSet = new HashSet<IElement>();

	    elemsSet.addAll(getSummaryEdges(type.getFullyQualifiedName(), null));
	    clazz.addAll(elemsSet);
	    return clazz;
    }

	@Override
	public Set<Info<IElement>> computeClassesBehindInterface(String theEnclosingTypeName, String fieldType, String theFieldName) {
		Set<Info<IElement>> classes = new TreeSet<Info<IElement>>();
		Map<FieldDeclaration, Set<IElement>> fieldDeclMap = ReverseTraceabilityMap.getInstance().getFieldDeclMap();

		for (Entry<FieldDeclaration, Set<IElement>> entry : fieldDeclMap.entrySet()) {
			FieldDeclaration key = entry.getKey();
			Set<IElement> value = entry.getValue();
		
			Type toType = key.fieldType;
			if (toType.getFullyQualifiedName().contentEquals(fieldType)) {
				// DONE: Check for nulls
				TypeDeclaration enclosingTypeDecl = key.enclosingType;
				if (enclosingTypeDecl != null) {
					Type enclosingType = enclosingTypeDecl.type;
					String fieldName = key.fieldName;
					
					if (enclosingType != null && enclosingType.getFullyQualifiedName().equals(theEnclosingTypeName)
					        && fieldName != null && fieldName.equals(theFieldName)) {
						classes = new TreeSet<Info<IElement>>();
						Set<String> concreteClasses = new HashSet<String>();

						// Compute the real, concrete classes
						getConcreteClasses(key, value, concreteClasses);

						for (String clazz : concreteClasses) {
							InfoIElement e = new InfoIElement(clazz, InfoType.CLASS);
							e.addAll(getSummaryEdges(clazz, null));
							classes.add(e);
						}
					}
				} else {
					System.err
							.println("Warning: EdgePrecision: cannot find enclosing type decl for "
									+ key);
				}

			}
			
		}
		return classes;
	}

	private void getConcreteClasses(FieldDeclaration fieldRef, Set<IElement> elements,
			Set<String> concreteClasses) {
		// DO NOT add the field type, which could be an interface
		Type toType = fieldRef.fieldType;
		
		for (IElement element : elements) {
			if (element instanceof OPTEdge) {
				OPTEdge edge = (OPTEdge) element;

				OObject runtimeObject = edge.getOdst();
				Type declaredType = runtimeObject.getC();

				String fullyQualifiedName = toType.getFullyQualifiedName();
				if ( fullyQualifiedName.endsWith("[]") ) {
					int debug = 0; debug++;
				}
				// Check that the types are compatible!
				// TODO: Is this check needed? Shouldn't the OOG construction already take care of that?
				// Or is our reverse traceability map broken, in that it associates unrelated things?
				if (Utils.isSubtypeCompatible(declaredType, toType)) {
					concreteClasses.add(declaredType.toString());
				}
			}
		}
		
	}
}
