package edu.wayne.summary.traceability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IElement;
import oog.itf.IGraph;
import oog.itf.IObject;

import org.eclipse.jdt.core.dom.ASTNode;

import ast.AstNode;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import ast.FieldDeclaration;
import ast.MiniAstUtils;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;
import edu.wayne.ograph.ORootObject;
import edu.wayne.tracing.actions.DeclarationVisitor;
import edu.wayne.tracing.actions.TraceToCodeUIAction;

/**
 * Build a reverse map, associating various elements to a set of OElements
 * 
 * XXX. Why is this using the awful TraceToCodeUIAction?! No wonder this is super-slow!
 * It scans the entire workspace MANY times...
 */
public class ReverseTraceabilityMap {

	private static boolean enableMappingASTNodes = true;
	private static ReverseTraceabilityMap s_instance = null;

	private ReverseTraceabilityMap() {
	}

	public static ReverseTraceabilityMap getInstance() {

		if (s_instance == null) {
			s_instance = new ReverseTraceabilityMap();
		}
		return s_instance;
	}

	private TraceToCodeUIAction action = null;

	/**
	 * Map a (fully qualified) class name to a set of IElements that are
	 * associated with it
	 */
	private Map<String, Set<IElement>> map = new HashMap<String, Set<IElement>>();

	/**
	 * Map a FieldDeclaration to a set of IElements that are associated with it
	 */
	private Map<FieldDeclaration, Set<IElement>> fieldDeclMap = new HashMap<FieldDeclaration, Set<IElement>>();


	/**
	 * Set of all IObjects
	 */
	private Set<IObject> allObjects = new HashSet<IObject>();

	//Change this map from ASTNode -> Set<IElement>
	// XXX. Eeek! Holding on to ASTNode's here! Major memory leak!
	// Multiple ASTNode's could be generated... 
	private Map<ASTNode, Set<IElement>> mapAST = new HashMap<ASTNode, Set<IElement>>();

	public void init(OGraph graph) {
		// HACK: If null, something seriously bad, like cannot load OOG.xml
		if (graph != null) {
			TraceabilityVisitor visitor = new TraceabilityVisitor();
			graph.accept(visitor);
		}
	}
	/**
	 * Set whether or not to enable mapping IElements to ASTNodes 
	 * 
	 * this flag is used to map IElements to ASTNode used in the 
	 * Related Objects and Edge View for Runtime Perspective
	 * XXX This may cause large systems to load extremely slow  
	 */
	public static void enableMappingASTNodes(boolean val){
		enableMappingASTNodes = val;
	}

	public boolean addMap(String declaredName, IElement element) {
		Set<IElement> set = map.get(declaredName);
		if (set == null) {
			set = new HashSet<IElement>();
		}
		map.put(declaredName, set);

		return set.add(element);
	}

	public boolean addMap(FieldDeclaration fieldDeclaration, IElement element) {
		Set<IElement> set = fieldDeclMap.get(fieldDeclaration);
		if (set == null) {
			set = new HashSet<IElement>();
		}
		fieldDeclMap.put(fieldDeclaration, set);

		return set.add(element);
	}


	
	public boolean addASTMap(IElement element, ASTNode node){
		Set<IElement> set = mapAST.get(node);
		if (set == null) {
			set = new HashSet<IElement>();
		}
		mapAST.put(node, set);

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

	public Set<IObject> getAllObjects() {
		return allObjects;
	}
	
	public Set<IElement> getElements(ASTNode node){
		
		Set<IElement> set = this.mapAST.get(node);
		if(set == null){
			for(ASTNode node1: this.mapAST.keySet()){
				if(node1.toString().compareTo(node.toString()) == 0){
					System.out.println(node1.toString()+ " Found but for some reason not returning set of iElements");
					return this.mapAST.get(node1);
				}
			}
		}
		return set;
	}

	// Extract the FieldDeclaration from a PT Edge traceability
	// XXX. This logic is repeated in several places. Use a Util class.
	public static FieldDeclaration getFieldDeclaration(BaseTraceability trace) {
		FieldDeclaration fieldDeclaration = null;
		// Handle other kinds of edges...
		// if ( trace.getExpression() instanceof FieldAccess ) {
		// FieldAccess fieldAccess = (FieldAccess)trace.getExpression();
		// fieldDeclaration = fieldAccess.fieldDeclaration;
		// }
		// else if ( trace.getExpression() instanceof FieldWrite ) {
		// FieldAccess fieldAccess = (FieldAccess)trace.getExpression();
		// fieldDeclaration = fieldAccess.fieldDeclaration;
		// }

		// PT case
		// TODO: Check that dealing with EdgePTTraceability
		AstNode astNode = trace.getExpression();
		if (astNode instanceof FieldDeclaration) {
			fieldDeclaration = (FieldDeclaration) astNode;
		}
		return fieldDeclaration;
	}

	// Extract the TypeDeclaration that is being created?
	// Should this not acount for generics?
	public static TypeDeclaration getObjectCreationType(BaseTraceability trace) {
		TypeDeclaration typeDecl = null;
		if (trace.getExpression() instanceof ClassInstanceCreation) {
			ClassInstanceCreation objectCreation = (ClassInstanceCreation) trace.getExpression();
			typeDecl = objectCreation.typeDeclaration;
		}
		return typeDecl;
	}

	// Extract the TypeDeclaration that is being created?
	public static String getObjectCreationExpression(BaseTraceability trace) {
		String typeDecl = null;
		if (trace.getExpression() instanceof ClassInstanceCreation) {
			ClassInstanceCreation objectCreation = (ClassInstanceCreation) trace.getExpression();
			typeDecl = objectCreation.complexExpression;
		}
		return typeDecl;
	}

	// Extract the TypeDeclaration that is being created?
	public static ClassInstanceCreation getObjectCreation(BaseTraceability trace) {
		ClassInstanceCreation typeDecl = null;
		if (trace.getExpression() instanceof ClassInstanceCreation) {
			ClassInstanceCreation objectCreation = (ClassInstanceCreation) trace.getExpression();
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

		@Override
		public boolean visit(IEdge oEdge) {
			// System.out.println("Visiting OEdge " + oEdge);

			// Map FieldDeclarations to OEdges
			Set<BaseTraceability> traceability = oEdge.getTraceability();
			if (traceability != null)
				for (BaseTraceability trace : traceability) {
					mapFieldDeclaration(oEdge, trace);
					if(enableMappingASTNodes)
						mapAST(oEdge,trace);
					//TOAND:TODO:Enable later when we figure out objects first
					//TOAND: Maybe filter out by edge type
				}

			return super.visit(oEdge);
		}


		@Override
		public boolean visit(IObject oObject) {
			if (!(oObject instanceof ORootObject)) {
				// DO NOT include the root object.
				// I don't want to deal with getParent() returning null! Not
				// relevant here
				// Every object is in domain
				// TODO: HIGH. When Radu fixes SecOOG to change "root" ...we
				// should get rid of this hack
				allObjects.add(oObject);
			}
			// Map class names to Objects
			Set<BaseTraceability> traceability = oObject.getTraceability();
			if (traceability != null)
				for (BaseTraceability trace : traceability) {
					mapClass(oObject, trace);
					if(enableMappingASTNodes)
						mapAST(oObject,trace);

				}

			return super.visit(oObject);
		}

		private void mapFieldDeclaration(IElement element,
				BaseTraceability trace) {
			FieldDeclaration fieldDeclaration = getFieldDeclaration(trace);
			// Add to the map
			if (fieldDeclaration != null) {
				ReverseTraceabilityMap.this.addMap(fieldDeclaration, element);
			}
		}

		private void mapClass(IObject oObject, BaseTraceability trace) {
			// TODO: HIGH. Read everything from the traceability object.
			TypeDeclaration enclosingType = MiniAstUtils.getEnclosingTypeDeclaration(trace.getExpression());
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
		this.fieldDeclMap.clear();
		this.map.clear();
		this.allObjects.clear();
		this.mapAST.clear();

	}

	public void mapAST(IElement element, BaseTraceability trace) {
		TraceToCodeUIAction.setHighlightCode(false);
		// Create lazily
		if (action == null ) {
			action = new TraceToCodeUIAction();
		}
		action.setTraceability(trace);
		action.run(null);
		//Get the ArchTrace Enclosing Declaration visitor 
		//Finds an enclosing declaration of a BaseTraceability 
		DeclarationVisitor visitor = action.getVisitor();

		if (visitor != null) {
			ASTNode enclosingDeclaration = visitor.getEnclosingDeclaration();
			if (enclosingDeclaration != null) {

				//mapAST.put(enclosingDeclaration, trace.enclosingDeclaration);
				//TOAND: Do we really need to remember this?
			//TOAND:TODO we only filter out only the expressions MethodInvokations, FieldWrite, FieldRead, ClassInstanceCreation
			//TOAND: Can create sub strategies for dealing with different type of expressions
			Set<ASTNode> astNodes = visitor.getExpressions();
			
			if (astNodes != null) {
				for (ASTNode node : astNodes) {
					addASTMap(element, node);

				}
			}
			visitor.getExpressions().clear();
			visitor = null;
			}
		}

	}

}