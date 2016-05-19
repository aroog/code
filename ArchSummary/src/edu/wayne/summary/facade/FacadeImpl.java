package edu.wayne.summary.facade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IObject;
import util.TraceabilityEntry;
import util.TraceabilityList;
import util.TraceabilityListSet;
import ast.AstNode;
import ast.BaseTraceability;
import ast.MethodDeclaration;
import ast.MiniAstUtils;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OObject;
import edu.wayne.summary.traceability.ReverseTraceabilityMap;

// DONE. Fix singleton; no public constructor
// TOMAR: TODO: HIGH. Have an init method to ensure that this plugin is initialized.
// TOMAR: TODO: HIGH. Must also ensure that Crystal stuff is initialized..
@Deprecated
public class FacadeImpl implements Facade {

	// Use lazy instantiation
	private static Facade sFacade = null;
	
	// Private constructor to enforce singleton
	private FacadeImpl() {
	}
	
	public static Facade getInstance() {
		if (sFacade == null ) {
			sFacade = new FacadeImpl();
		}
		
		return sFacade;
	}
	
	private OGraph runtimeModel;
	
	@Override
    public OGraph getRuntimeModel() {
	    return runtimeModel;
    }

	@Override
    public void setRuntimeModel(OGraph runtimeModel) {
	    this.runtimeModel = runtimeModel;
    }
	
	@Override
    public Set<String> getClassesBehindInterface(String itf) {
		// TODO: Implement me!		
	    return null;
    }

	@Override
	/* One implementation of: getMostImportantClasses
	 * - Get the top-level objects in the top-level domains.
	 * -- Get the classes associated with
	 * --- Either reach into traceability
	 * --- Or just get the concrete type associated with the OObject
	 */
    public Set<String> getMostImportantClasses() {
		Set<String> importantClasses = new HashSet<String>();
		OObject rootObject = this.runtimeModel.getRoot();

		for(IDomain domain : rootObject.getChildren() ) {
			for (IObject object: domain.getChildren() ) {
				importantClasses.addAll( getMostImportantClasses(object) );
				// NOTE: Cannot just get the concrete types
				// TODO: HIGH. Revisit this with the new infrastructure; why not use IObject.getC()?
				// NOTE: object.getTypeDisplayName() does not return fully qualified type name.
			}
		}
		
		return importantClasses;
	}

	/**
	* Get the most important classes associated with a OObject.
	* Returns a set of fully qualified type names
	*/
	private Set<String> getMostImportantClasses(IObject object) {
		Set<String> importantClasses = new HashSet<String>();
		
	    // Reach into traceability
		for (BaseTraceability trace : object.getTraceability()) {
			TypeDeclaration enclosingType = MiniAstUtils.getEnclosingTypeDeclaration(trace.getExpression());
			if (enclosingType != null) {
				Type type = enclosingType.type;
				if (type != null) {
					importantClasses.add(type.toString());
				}
				else {
					// TODO: Do something better here.
					System.err.println("Error: TypeDeclaration.enclosingType: unexpected null type");
				}
			}
			else {
				// TODO: Do something better here.				
				System.err.println("Error: TypeDeclaration: unexpected null enclosingType");
			}
		}
	    return importantClasses;
    }

	@Override
	/**
	 * From the javaClass, pull up the associated RuntimeObjects.
	 * - Find the edges associated with the OObject.
	 * - Find the immediate successor/predecessor objects.
	 * NOTE: NOT doing any transitive closure here.
	 * 
	 */
    public Set<String> getMostImportantClassesRelatedToClass(String javaClass, RelationshipType type) {
		Set<String> importantClasses = new HashSet<String>();

		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();

		Set<IElement> elements = instance.getElements(javaClass);
		if (elements != null) {
			for (IElement element : elements) {
				if (element instanceof OObject) {
					OObject object = (OObject) element;

					// Find immediate predecessors
					Set<IObject> preds = getPredecessors(object);
					for (IObject pred : preds) {
						importantClasses.addAll(getMostImportantClasses(pred));
					}

					// Find immediate successors
					Set<IObject> succs = getSuccessors(object);
					for (IObject succ : succs) {
						importantClasses.addAll(getMostImportantClasses(succ));
					}
				}
			}
		}

		return importantClasses;
    }
	
	/*
	 * Return the predecessor OObjects associated with the OObject by following the edges.
	 * NOTE: NOT doing any transitive closure here.
	 */
	// TODO: Move this elsewhere. Could push this method to IObject
	private Set<IObject> getPredecessors(IObject object) {
		Set<IObject> preds = new HashSet<IObject>();
		Set<IEdge> allEdges = this.runtimeModel.getEdges();
		for(IEdge edge : allEdges ) {
			if ( edge.getOdst() == object ) {
				preds.add(edge.getOsrc());
			}
		}
		return preds;
	}
	
	/*
	 * Return the successor OObjects associated with the OObject by following the edges.
	 * NOTE: NOT doing any transitive closure here.
	 */
	// TODO: Move this elsewhere. Could push this method to IObject
	private Set<IObject> getSuccessors(IObject object) {
		Set<IObject> succs = new HashSet<IObject>();
		Set<IEdge> allEdges = this.runtimeModel.getEdges();
		for (IEdge edge : allEdges) {
			if ( edge.getOsrc() == object ) {
				succs.add(edge.getOdst());
			}
		}
		return succs;
	}	

	@Override
	/**
	 * One implementation of: getMostImportantMethodsOfClass (MIMs(C)):
	 * - Find instances of this class
	 * - Find all the OObjects associated with this class
	 * - Find all the method invocations that are alongside the path that creates this OObject?
	 * 
	 *  Alt. Impl.:
	 *  - Find all MethodInvocations of any Method declared on this class
	 *  - In all traceability info?
	 *  - The Methods that are ever invoked are important
	 *  - Hold on to this info in the ReverseTraceability Map
	 */
    public Set<String> getMostImportantMethodsOfClass(String javaClass) {
		Set<String> importantMethods = new HashSet<String>();

		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();

		Set<IElement> elements = instance.getElements(javaClass);
		if (elements != null) {
			for (IElement element : elements) {
				if (element instanceof OObject) {
					OObject object = (OObject) element;
						for (BaseTraceability second: object.getTraceability()) {
							AstNode expression = second.getExpression();
							if (expression instanceof ast.MethodInvocation) {
								ast.MethodInvocation invk = (ast.MethodInvocation) expression;
								if (invk != null) {
									MethodDeclaration methDecl = invk.methodDeclaration;
									if (methDecl != null) {
										String methName = methDecl.methodName;
										importantMethods.add(methName);
									}
								}
							}
						}
				}
			}
		}

		return importantMethods;
    }
}
