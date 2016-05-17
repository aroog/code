package edu.wayne.ograph.internal;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import util.TraceabilityEntry;
import ast.Type;
import edu.cmu.cs.aliasjava.Constants;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.util.TypeHierarchy;
import edu.wayne.auxiliary.Config;
import edu.wayne.auxiliary.Utils;
import edu.wayne.flowgraph.FlowGraph;
import edu.wayne.flowgraph.FlowGraphEdge;
import edu.wayne.flowgraph.FlowGraphNode;

public class OOGContext implements Cloneable {
	// OWORLD does not have type
	private static ORootObject OWORLD = null;
	private static ODomain DSHARED = null;
	private static ODomain DUNIQUE = null;
	private static ODomain DLENT = null;
	private OObject O;
	private OGraph G;
	private Set<OObject> Upsilon;
	// this is just x: \ob{p}, because we can get C from
	// Variable.getTypeBinding()
	private Map<Variable, List<DomainP>> Gamma;
	private Stack<TraceabilityEntry> expressionStack;
	private FlowGraph fg;

	// TODO: Gamma we need to get hold of formal domain parameters.
	
	protected TypeHierarchy types;


	public OOGContext(TypeHierarchy types) {
		super();
		init();
	}
	// Extract method that we can use to reset the Graph
	private void init() {
	    O = getOWorld();
		G = OGraph.getInstance();
		G.addRoot(getOWorld());
		G.addShared(getDShared());
		Upsilon = new LinkedHashSet<OObject>();
		Gamma = new Hashtable<Variable, List<DomainP>>();
		expressionStack = new Stack<TraceabilityEntry>();
		
		// XXX. Do not allocate this when NOT using the flow analysis
		if(Config.HANDLE_LENT_UNIQUE) {
			fg = FlowGraph.getInstance();
		}
    }

	public OOGContext(TypeHierarchy types, OGraph G, FlowGraph fg) {
		super();
		O = getOWorld();
		this.G = G;
		Upsilon = new LinkedHashSet<OObject>();
		Gamma = new Hashtable<Variable, List<DomainP>>();
		expressionStack = new Stack<TraceabilityEntry>();
		this.fg = fg;
	}

	public OOGContext(OGraph gPrime, OObject oC, Set<OObject> upsilonPrime, TypeHierarchy types,
			Map<Variable, List<DomainP>> gamma, Stack<TraceabilityEntry> stack, FlowGraph fgPrime) {
		super();
		O = oC;
		G = gPrime;
		Upsilon = upsilonPrime;
		Gamma = gamma;
		expressionStack = stack;
		fg = fgPrime;
	}

	@Override
	public OOGContext clone() {
		Set<OObject> newUpsilon = new LinkedHashSet<OObject>();
		newUpsilon.addAll(Upsilon);
		Stack<TraceabilityEntry> stack = new Stack<TraceabilityEntry>();
		stack.addAll(expressionStack);
		OOGContext clone = new OOGContext(G, O, newUpsilon, types, Gamma, stack, fg);
		return clone;
	}

	public OObject getO() {
		return O;
	}

	public OGraph getG() {
		return G;
	}

	public FlowGraph getFG() {
		return fg;
	}
	
	public Set<OObject> getUpsilon() {
		return Upsilon;
	}

	public Stack<TraceabilityEntry> getExpressionStack() {
		return expressionStack;
	}

	/***
	 * handles shared, lent and unique, and
	 * 
	 * XXX. Mostly commented out code
	 * TORAD: cleanup commented out code. might be early iteration of value-flow 
	 * XXX. Inline these calls.
	 * 
	 * XXX. Does this resolve 'lent' and 'unique' being passed as actual domains, not owning domain?
	 * 
	 * @param o
	 * @param c
	 * @param pi
	 * @return
	 */
	public Set<ODomain> lookupDD(OObject o, QualifiedClassName c, String pi) {
		Set<ODomain> domSet = new HashSet<ODomain>();
		// XXX. This is checked again in lookupDomain
		if (pi.equals(Constants.SHARED)){
			domSet.add(getDShared());			
		} else if (pi.equals(Constants.LENT)){
			System.err.println("Object being created in LENT inside class " + c.toString());
			// XXX. Should we keep resolving, i.e., call lookupDomainDD(o, c, pi)
			// remove hack for objects created in lent to be placed in SHARED
			// these objects should be ignored with a warning. We will need to change the annotations.
			// domSet.add(getDLent());
			// domSet.add(getDShared());
//			Set<ObjectDomainPair> solveLent = solveLent(o);			
//			for (ObjectDomainPair pair:solveLent){
//				domSet.add(lookupDomainDD(pair.getKey(), pair.getValue().getTypeBinding(), pair.getValue().getShortName()));
//			}
//		} else if (pi.equals(Constants.UNIQUE)){
//			Set<ObjectDomainPair> solveUnique = solveUnique(o, c);
//			for (ObjectDomainPair pair:solveUnique){
//				domSet.add(lookupDomainDD(pair.getKey(), pair.getValue().getTypeBinding(), pair.getValue().getShortName()));
//			}
		} else 
			domSet.add(lookupDomainDD(o, c, pi));
		return domSet;
	}

	public Set<ODomain> lookupDD(OObject o, DomainP pi) {
		return lookupDD(o, pi.getTypeBinding(), pi.getShortName());
	}

	// DD[(O, C::pi)], if not found lookup DD[(O, C'::pi)] where C<:C'
	private ODomain lookupDomainDD(OObject o, QualifiedClassName c, String pi) {
		// HACK: even java.lang.Object has a domain parameter, which is owner.
		if (c == null || c.getActualName().equals(Utils.JAVA_LANG_OBJECT)) {
			// throw new IllegalStateException("Cannot find " + o + " " +
			// c.getTypeBinding().getQualifiedName()+ "::"+ pi + " in DD");
			//System.out.println("Cannot find " + o + " " + pi + " in DD");
			return null;
		}
		if (pi == Constants.SHARED)
			return getDShared();
		ODomain foundDomain = findD(o, c, pi);
		if (foundDomain != null)
			return foundDomain;
		else {
			ODomain oDom = lookupDomainDD(o, c.getSuperclass(), pi);
			if (oDom!=null)
				return oDom;
			else{
				QualifiedClassName[] interfaces = c.getInterfaces();
				for (QualifiedClassName itf : interfaces) {
					foundDomain = findD(o, itf, pi);
					if (foundDomain!=null)
						return foundDomain;
				}
				//System.out.println("Cannot find " + o + " " + c.getFullyQualifiedName()+" " + pi + " in DD");
				return null;
			}
		}
	}

	/**
	 * @param o
	 * @param c
	 * @param pi
	 * @return
	 */
	// Performance: O(1) lookup in DD
	private ODomain findD(OObject o, QualifiedClassName c, String pi) {
		DomainP dd = new DomainP(c, pi);
		Map<DomainMapKey, ODomain> DD = G.getDD();
		ODomain foundDomain = DD.get(new DomainMapKey(o, dd));
		return foundDomain;
	}

	public void setO(OObject oC) {
		O = oC;
	}

	public Map<Variable, List<DomainP>> getGamma() {
		return Gamma;
	}

	public static OObject getOWorld() {
		if (OWORLD == null) {
			OWORLD = new ORootObject();
		}
		return OWORLD;
	}

	public static ODomain getDShared() {
		if (DSHARED == null)
			DSHARED = new ODomain("DS", new DomainP(null, "SHARED"));
		return DSHARED;
	}

	public static ODomain getDUnique() {
		if (DUNIQUE == null)
			DUNIQUE = new ODomain("DUNIQUE", new DomainP(null, "DUNIQUE"));
		return DUNIQUE;
	}
	
	public static ODomain getDLent() {
		if (DLENT == null)
			DLENT = new ODomain("DLENT", new DomainP(null, "DLENT"));
		return DLENT;
	}
	
	public void clear() {
		G.clearGraph();
		G = null;
		O = null;
		
		// Reset the singleton; also calls clearGraph()
		OGraph.reset();
		
		if (fg != null ) {
		fg.clear();
		fg = null;
		}
		
		DSHARED = null;
		OWORLD = null;
		DLENT = null;
		DUNIQUE = null;
		Upsilon.clear();
		Upsilon = null;
		
		Gamma.clear();
		Gamma = null;
		
		expressionStack.clear();
		expressionStack = null;
		


		
		// XXX. Do not reset types; type hierarchy not likely to change!
				
		// NOTE: We are reusing the type hierarchy. Is that ok?
		init();
	}

	/**
	 * @param O_C
	 * @param upsilonP
	 * @param gammaP
	 * @return
	 */
	public OOGContext createNewContext(OObject O_C, Set<OObject> upsilonP, Map<Variable, List<DomainP>> gammaP,
			Stack<TraceabilityEntry> stack) {
		OOGContext context = clone();
		context.O = O_C;
		context.Upsilon = upsilonP;
		context.Gamma = gammaP;
		context.expressionStack = stack;
		return context;
	}

	public Set<FlowGraphNode> solveUnique(OObject o, QualifiedClassName c){
		Set<FlowGraphEdge> edges = fg.findEdgesWithUniqueSource(o);
		Set<FlowGraphNode> result = new HashSet<FlowGraphNode>();
		for (FlowGraphEdge flowGraphEdge : edges) {			
			FlowGraphNode dst = flowGraphEdge.getDst();
			ast.Type tDst = Type.createFrom(dst.getX().resolveType());
			boolean isB = tDst.isSubtypeCompatible(c.getType());
			if (isB)
				result.add(dst);
		}
		return result;
	}

	// XXX. Iterating over all the flow edges...
	// XXX. Maybe need a different representation of the graph.
	public Set<FlowGraphNode> solveLent(OObject o, QualifiedClassName c){
		Set<FlowGraphEdge> edges = fg.findEdgesWithLentDest(o);
		Set<FlowGraphNode> result = new HashSet<FlowGraphNode>();
		for (FlowGraphEdge flowGraphEdge : edges) {
			FlowGraphNode src = flowGraphEdge.getSrc();
			ast.Type tSRC = Type.createFrom(src.getX().resolveType());
			boolean isB = tSRC.isSubtypeCompatible(c.getType());			
			if (isB)
				result.add(src);
		}
		Set<FlowGraphNode> moreFromUnique = new HashSet<FlowGraphNode>(); 
		for (FlowGraphNode node:result){
			moreFromUnique.add(node);
			if (node.getB().isUnique())
				moreFromUnique.addAll(solveUnique(node.getO(), c));				
		}
		return moreFromUnique;
	}
	
	
}
