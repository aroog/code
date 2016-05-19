package edu.wayne.summary.strategies;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IObject;
import secoog.EdgeType;
import util.TraceabilityEntry;
import util.TraceabilityList;
import util.TraceabilityListSet;
import ast.AstNode;
import ast.BaseTraceability;
import ast.MethodDeclaration;
import ast.MethodInvocation;
import ast.MiniAstUtils;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.ograph.OObject;
import edu.wayne.summary.traceability.ReverseTraceabilityMap;

/**
 * Converted current code to use new strategy design
 */
public class SummaryImpl1 extends SummaryBase<String> {

	/**
	 * Get the most important classes associated with a OObject. Returns a set
	 * of fully qualified type names
	 */
	private Set<Info<String>> getMostImportantClasses(IObject object) {
		Set<Info<String>> importantClasses = new TreeSet<Info<String>>();

		// Reach into traceability
		for (BaseTraceability trace : object.getTraceability()) {
			TypeDeclaration enclosingType = MiniAstUtils.getEnclosingTypeDeclaration(trace.getExpression());
			if (enclosingType != null) {
				Type type = enclosingType.type;

				if (type != null) {
					importantClasses.add(new InfoUnranked(type
							.getFullyQualifiedName()));
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
		return importantClasses;
	}

	/*
	 * One implementation of: getMostImportantClasses - Get the top-level
	 * objects in the top-level domains. -- Get the classes associated with ---
	 * Either reach into traceability --- Or just get the concrete type
	 * associated with the OObject
	 */
	@Override
	public Set<Info<String>> computeClasses() {
		Set<Info<String>> classes = new TreeSet<Info<String>>();

		OObject rootObject = getGraph().getRoot();

		for (IDomain domain : rootObject.getChildren()) {
			for (IObject object : domain.getChildren()) {
				classes.addAll(getMostImportantClasses(object));
				// NOTE: Cannot just get the concrete types
				// TODO: HIGH. Revisit this with the new infrastructure; why not
				// use IObject.getC()?
				// NOTE: object.getTypeDisplayName() does not return fully
				// qualified type name.
			}
		}

		return classes;
	}

	/**
	 * 
	 * Alt. Impl.: - Find all MethodInvocations of any Method declared on this
	 * class - In all traceability info? - The Methods that are ever invoked are
	 * important - Hold on to this info in the ReverseTraceability Map
	 */
	@Override
	public Set<Info<String>> computeMethodsOfClass(String javaClass) {
		Set<Info<String>> methods = new TreeSet<Info<String>>();

		for (IEdge edge : getGraph().getEdges()) {
			TraceabilityListSet path = edge.getPath();
			for (TraceabilityList list : path.getSetOfLists()) {
				for (TraceabilityEntry trace : list.getRawList()) {
					BaseTraceability second = trace.getSecond();
					AstNode expression = second.getExpression();

					if (expression instanceof MethodInvocation) {
						MethodDeclaration methodDeclaration = ((MethodInvocation) expression).methodDeclaration;
						if (methodDeclaration != null) {
							TypeDeclaration type = ((MethodDeclaration) methodDeclaration).enclosingType;
							if (type != null) {
								if (type.getFullyQualifiedName().compareTo(
										javaClass) == 0) {
									String methName = methodDeclaration.methodName;
									methods.add(new InfoUnranked(methName));

								}
							}
						}
					}

				}

			}
		}

		/*
		 * ReverseTraceabilityMap instance =
		 * ReverseTraceabilityMap.getInstance(); Set<IElement> elements =
		 * instance.getElements(javaClass); if (elements != null) { for
		 * (IElement element : elements) { if (element instanceof OObject) {
		 * OObject object = (OObject) element;
		 * 
		 * TraceabilityListSet path = object.getPath(); for (TraceabilityList
		 * traceabilities : path.getSetOfLists()) { for (TraceabilityEntry entry
		 * : traceabilities.getRawList()) {
		 * 
		 * BaseTraceability second = entry.getSecond();
		 * 
		 * AstNode expression = second.expression;
		 * 
		 * if (expression instanceof ast.MethodInvocation) {
		 * ast.MethodInvocation invk = (ast.MethodInvocation) expression; if
		 * (invk != null) { MethodDeclaration methDecl = invk.methodDeclaration;
		 * if (methDecl != null) { String methName = methDecl.methodName;
		 * methods.add( new InfoUnranked(methName)); } } } } } } } }
		 */
		return methods;
	}

	/**
	 * From the javaClass, pull up the associated RuntimeObjects. - Find the
	 * edges associated with the OObject. - Find the immediate
	 * successor/predecessor objects. NOTE: NOT doing any transitive closure
	 * here.
	 */
	@Override
	public Set<Info<String>> computeClassesRelatedToClass(String javaClass) {
		Set<Info<String>> classes = new TreeSet<Info<String>>();
		
		Set<EdgeType> relationshipTypes = null;
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();

		Set<IElement> elements = instance.getElements(javaClass);
		if (elements != null) {
			for (IElement element : elements) {
				if (element instanceof OObject) {
					OObject object = (OObject) element;
					// Find immediate predecessors
					Set<IObject> preds = getPredecessors(object,
							relationshipTypes);
					for (IObject pred : preds) {
						classes.addAll(getMostImportantClasses(pred));
					}

					// Find immediate successors
					Set<IObject> succs = getSuccessors(object,
							relationshipTypes);
					for (IObject succ : succs) {
						classes.addAll(getMostImportantClasses(succ));
					}
				}
			}
		}
		return classes;
	}

	/*
	 * Return the predecessor OObjects associated with the OObject by following
	 * the edges. NOTE: NOT doing any transitive closure here.
	 */
	// TODO: Move this elsewhere. Could push this method to IObject
	private Set<IObject> getPredecessors(IObject object, Set<EdgeType> edgeTypes) {
		Set<IObject> preds = new HashSet<IObject>();
		Set<IEdge> allEdges = this.getEdges(edgeTypes);
		for (IEdge edge : allEdges) {
			if (edge.getOdst() == object) {
				preds.add(edge.getOsrc());
			}
		}
		return preds;
	}

	/*
	 * Return the successor OObjects associated with the OObject by following
	 * the edges. NOTE: NOT doing any transitive closure here.
	 */
	// TODO: Move this elsewhere. Could push this method to IObject
	private Set<IObject> getSuccessors(IObject object, Set<EdgeType> edgeTypes) {
		Set<IObject> succs = new HashSet<IObject>();
		Set<IEdge> allEdges = this.getEdges(edgeTypes);
		for (IEdge edge : allEdges) {
			if (edge.getOsrc() == object) {
				succs.add(edge.getOdst());
			}
		}
		return succs;
	}

	@Override
	// XXX. This is broken.
	public Set<Info<String>> computeClassesBehindInterface(String enclosingType, String fieldType, String fieldName) {
		Set<Info<String>> classes = new TreeSet<Info<String>>();
		for(String clazz: getConcreteClasses(fieldType)){
			InfoUnranked infoUnranked = new InfoUnranked(clazz);
			classes.add(infoUnranked);
		}
		return classes;
	}



}
