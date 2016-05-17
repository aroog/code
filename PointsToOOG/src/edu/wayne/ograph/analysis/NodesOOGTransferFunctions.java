/**
 * 
 */
package edu.wayne.ograph.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import oog.itf.IObject;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.junit.Assert;

import util.TraceabilityEntry;

import com.google.common.collect.Multimap;

import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.aliasjava.Constants;
import edu.cmu.cs.aliasxml.MethodAnnotationInfo;
import edu.cmu.cs.aliasxml.VariableAnnotationInfo;
import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.AnnotationSummary;
import edu.cmu.cs.crystal.annotations.ICrystalAnnotation;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.internal.CrystalRuntimeException;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.tac.eclipse.EclipseTAC;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.AssignmentInstruction;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.InvocationInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.StoreInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.util.TypeHierarchy;
import edu.wayne.alias.FieldVariable;
import edu.wayne.auxiliary.AuxJudgements;
import edu.wayne.auxiliary.CompoundODomain;
import edu.wayne.auxiliary.Config;
import edu.wayne.auxiliary.Utils;
import edu.wayne.flowgraph.FlowAnnot;
import edu.wayne.flowgraph.FlowAnnotType;
import edu.wayne.flowgraph.FlowGraph;
import edu.wayne.flowgraph.FlowGraphEdge;
import edu.wayne.flowgraph.FlowGraphNode;
import edu.wayne.legacy.AliasXMLVariable;
import edu.wayne.moogdb.VariableDeclAnnotationInfo;
import edu.wayne.ograph.internal.DomainMapKey;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.IDDictionary;
import edu.wayne.ograph.internal.ODomain;
import edu.wayne.ograph.internal.OGraph;
import edu.wayne.ograph.internal.OOGContext;
import edu.wayne.ograph.internal.OObject;
import edu.wayne.ograph.internal.OObjectKey;
import edu.wayne.ograph.internal.OwnershipType;
import edu.wayne.ograph.internal.QualifiedClassName;
import edu.wayne.pointsto.PointsToAnalysis;

/**
 * Ideally, create a separate class that does NOT use at all the flow graph. 
 * Right now, using the flag Config.HANDLE_LENT_UNIQUE to not do some work.
 *
 */
public class NodesOOGTransferFunctions extends AbstractingTransferFunction<OOGContext> {

	private static final String CANNOT_FIND_NAME_FOR_OBJECT = "cannot find name for object";
	private static final String CANNOT_DETERMINE_ACTUAL_DOMAINS_FOR_CAST = "cannot determine actual domains for cast";
	private static final String CANNOT_FIND_ANNOTATION = "cannot find annotation: Extract local variable and add annotation";
	public static final String MISSING_DOMAINS = "Expected a list of domains, but was empty";
	public final PointsToAnalysis pointsToAnalysis;
	protected final TraceabilityFactory traceabilityFactory;
	public static final String MISSING_CONSTRUCTOR = "Class has complex field initializers but no constructor. Create a default constructor";
	private static final String MISSING_TYPE_DECL = "Missing type declaration. Add type declaration in current project";
	
	private boolean collectFlowObj = false; //by default do not create flow objects.
	
	private TypeHierarchy hierarchy;
	
	public void collectFlow(boolean flag){
		collectFlowObj = flag;
	}
	
	public NodesOOGTransferFunctions(PointsToAnalysis pointsToAnalysis) {
		this.pointsToAnalysis = pointsToAnalysis;
		this.traceabilityFactory = TraceabilityFactory.getInstance(pointsToAnalysis);
	    this.hierarchy = this.pointsToAnalysis.getHierarchy();
	}

	@Override
	public ILatticeOperations<OOGContext> getLatticeOperations() {
		return new OOGLatticeOps(hierarchy);
	}

	// we need to initialize current context, because we do not analyze each
	// method separately,
	// we have to propagate G, Upsilon, and Gamma
	// HACK: all we need is setEntryValueContext(OOGContext)
	@Override
	public OOGContext createEntryValue(MethodDeclaration method) {
		// use this method to prepare analyzing a method.
		// For example, if there are annotations you need to get from
		// AnnotDB (receiver)
		return this.pointsToAnalysis.getCurrentContext();
	}

	@Override
	// do not mix "OOGContext value" and "currentContext"
	// they are not the same? You don't check! Aliasing :-)
	public OOGContext transfer(NewObjectInstruction instr, OOGContext value) {
		ITypeBinding cBinding = instr.resolveInstantiatedType();
		QualifiedClassName cthis = getC_THIS(value);
		QualifiedClassName aQCN = new QualifiedClassName(cBinding, cthis);
		TypeDeclaration typeDecl = pointsToAnalysis.getTypeDecl(aQCN.getType());

		Set<OObject> upsilon = value.getUpsilon();
		Set<OObject> upsilonP = new LinkedHashSet<OObject>();
		upsilonP.addAll(upsilon);
		
		// D_i = DD[(O,p_i)]
		CompoundODomain compoundListD = createDomainList(cthis, instr, value, aQCN);

		if (compoundListD == null || (!compoundListD.iterator().hasNext())) {
			// push the instr on stack and try again later
			return value.clone();
		}
		
		Stack<TraceabilityEntry> stack = value.getExpressionStack();
		Stack<TraceabilityEntry> stackP = new Stack<TraceabilityEntry>();
		stackP.addAll(stack);
		stackP.push(traceabilityFactory.createTraceability(instr, aQCN.getTypeBinding(), value.getO()));

		
		for (List<ODomain> listD : compoundListD) {

			OObject O_C = value.getG().getOObject(aQCN, listD);
			// NOTE: getOObject performs DO' = DO U {O_C}
			// DO' = DO U {O_C}

			setOObjectInfo(instr, O_C, value.getO(), stackP);

			// DD' = DD U {(O_C,alpha_i)|-> D_i, (O_C,p_i) |-> D_i}
			AnnotationInfo pInfo = getp_i(instr, pointsToAnalysis);
			
			if (pInfo.getAnnotation().isLent()) O_C.setAsLent();
			if (pInfo.getAnnotation().isUnique()) O_C.setAsUnique();
			
			Map<DomainMapKey, ODomain> listDD = auxUnionDD(O_C, aQCN, pInfo, cthis);
			value.getG().addAllDD(listDD);
			auxdDomains(upsilon, O_C, value.getG(), value.getO(), aQCN);

			// Process fields
			doFields(value, O_C, value.getG(), aQCN);
			
			// creation edges
			creationEdges(value, O_C, value.getG(), cthis, instr);

			value.getGamma().put(instr.getTarget(), Utils.getDomainP(cthis, pInfo));
			
			if(Config.HANDLE_LENT_UNIQUE) {
			addValueFlow(value, O_C, cthis, instr, pInfo);
			}
			
			// TODO: add to Gamma this:C_this<p>
			if (!upsilon.contains(O_C)) {
				upsilonP.add(O_C);

				if (typeDecl != null) {
					Set<MethodDeclaration> mBodies = AuxJudgements.mBody(typeDecl,
							this.pointsToAnalysis.getHierarchy(), this.pointsToAnalysis.getTypes(), cthis);
					if (!AuxJudgements.hasConstructor(typeDecl, this.pointsToAnalysis.getTypes(), cthis)) {
						pointsToAnalysis.addWarning(typeDecl,MISSING_CONSTRUCTOR);
					}
					for (MethodDeclaration md : mBodies) {
						if (!Config.isMainMethod(md)) {
							Map<Variable, List<DomainP>> gammaP = new Hashtable<Variable, List<DomainP>>();
							// HACK: gammaP is empty. Add to Gamma: arg:Targ
							// without
							// substitution
							IMethodBinding mb = md.resolveBinding();
							List<Variable> formalParams = getFormalParams(mb, typeDecl);
							int i = 0;
							for (Variable variable : formalParams) {
								if (!variable.resolveType().isPrimitive()) {
									List<DomainP> domainsOfFormalParams = getDomainsOfFormalParams(mb, typeDecl, cthis,
											i);
									if (domainsOfFormalParams != null)
										gammaP.put(variable, domainsOfFormalParams);
									else
										pointsToAnalysis.addWarning(md,
												"cannot find formal domain params for argument " + variable);
								}
								i++;
							}

							this.pointsToAnalysis.doAccept(md, value.createNewContext(O_C, upsilonP, gammaP, stackP));
						}
					}
				} else {
					// TODO: MED: do not report problem if AliasXML exists.
					if (!Utils.isSummaryType(aQCN)) {
						StringBuilder builder = new StringBuilder();
						builder.append(MISSING_TYPE_DECL);
						builder.append(" ");
						builder.append(aQCN.getFullyQualifiedName());
						pointsToAnalysis.addWarning(instr.getNode(), builder.toString());
                    }
				}
			}
		}
		//CHANGE for recursive types: upsilon vs. upsilonP.
		return value.createNewContext(value.getO(), upsilon, value.getGamma(), stackP);
	}

	protected long getInvocation(OObject o, TACInstruction invk) {
		return IDDictionary.generateMethodID(o, invk);
	}
	
	private void addValueFlow(OOGContext value, OObject O_C, QualifiedClassName cthis, NewObjectInstruction instr,
			AnnotationInfo pInfo) {
		Variable l = instr.getTarget();
		DomainP owner = Utils.getOwner(cthis, pInfo);
		OObject o = value.getO();
		TypeDeclaration typeDecl = pointsToAnalysis.getTypeDecl(ast.Type.createFrom(instr.resolveInstantiatedType()));
		IMethodBinding mb = instr.resolveBinding().getMethodDeclaration();
		ThisVariable that = getThatVariable(mb, typeDecl);
		FlowAnnot flowAnnot = new FlowAnnot(getInvocation(o,instr), FlowAnnotType.CALL);
		if (that != null) {
			FlowGraphNode src = new FlowGraphNode(o, l, owner);
			FlowGraphNode dst = new FlowGraphNode(O_C, that, new DomainP(O_C.getQCN(), Constants.OWNER));
			FlowGraphEdge edge = new FlowGraphEdge(src, dst, flowAnnot);
			value.getFG().addInfoFlow(edge);
		}
		List<Variable> argOperands = instr.getArgOperands();
		List<Variable> fparams = getFormalParams(mb, typeDecl);
		for (Variable a : argOperands) {
			List<DomainP> list = value.getGamma().get(a);
			boolean condition = list != null 
								&& list.size() > 0 
								&& fparams!=null && fparams.size()>0 
								&& argOperands.size()==fparams.size()
								&& !(a.resolveType().isPrimitive());
			if (condition) {								
				Variable fParam = fparams.get(argOperands.indexOf(a));
				List<DomainP> declaredParamDomains = getDomainsOfFormalParams(mb, typeDecl, cthis, argOperands.indexOf(a));
				if (declaredParamDomains != null && declaredParamDomains.size() > 0) {
					FlowGraphNode src2 = new FlowGraphNode(o, a, list.get(0));
					FlowGraphNode dst2 = new FlowGraphNode(O_C, fParam, declaredParamDomains.get(0));
					FlowGraphEdge edge2 = new FlowGraphEdge(src2, dst2, flowAnnot);
					value.getFG().addInfoFlow(edge2);
				}
			} else {
				// try again later - argument is a field and was not properly
				// handled yet
			}
		}
	}

	/**
	 * @param mb1
	 * @param typeDecl
	 * @return
	 */
	protected ThisVariable getThatVariable(IMethodBinding mb1, TypeDeclaration typeDecl) {		
		if (typeDecl!=null){		
			EclipseTAC tac = getEclipseTAC(mb1, typeDecl);
			if (tac!=null)
				return tac.thisVariable();
		}
		return null;
	}
	
	/**
	 * @param methodKey
	 * @param typeDecl
	 * @return formal parameters of method given as a binding key
	 */
	public EclipseTAC getEclipseTAC(IMethodBinding mb, TypeDeclaration typeDecl) {
		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration md:methods){
			if (md.resolveBinding().getKey().equals(mb.getKey()) || (Bindings.isSubsignature(md.resolveBinding(), mb))) {
				return this.pointsToAnalysis.getSavedInput().getMethodTAC(md);
			}
		}
		Type superclassType = typeDecl.getSuperclassType();
		if (superclassType != null) {
			ITypeBinding resolveBinding = superclassType.resolveBinding();
			TypeDeclaration typeDecl2 = pointsToAnalysis.getTypeDecl(ast.Type.createFrom(resolveBinding));
			if (typeDecl2 != null)
				return getEclipseTAC(mb, typeDecl2);
		}
		return null;
	}
	
	/**
	 * @param methodKey
	 * @param typeDecl
	 * @return formal parameters of method given as a binding key
	 */
	protected List<Variable> getFormalParams(IMethodBinding mb, TypeDeclaration typeDecl) {
		boolean found = false;
		List<Variable> fparams = new ArrayList<Variable>();
		IMethodBinding methodDeclaration = mb.getMethodDeclaration();
		if (typeDecl == null){			
			ITypeBinding[] parameterTypes = methodDeclaration.getParameterTypes();
			for (final ITypeBinding iTypeBinding : parameterTypes) {
				Variable v  = new AliasXMLVariable(methodDeclaration, iTypeBinding);
				fparams.add(v);
			}
			return fparams;
		}
		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration md:methods){			
			boolean subsignature = Bindings.isSubsignature(md.resolveBinding(), methodDeclaration);
			if (md.resolveBinding().getKey().equals(mb.getKey()) || Bindings.isSubsignature(md.resolveBinding(), methodDeclaration)) {				
				found = true;
				List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>)md.parameters();
				for (SingleVariableDeclaration singleVariableDeclaration : parameters) {
					IVariableBinding paramBinding = singleVariableDeclaration.resolveBinding();
					Variable fParam = getEclipseTAC(methodDeclaration, typeDecl).sourceVariable(paramBinding);
					fparams.add(fParam);
				}
			}
		}
		if (!found) {
			Type superclassType = typeDecl.getSuperclassType();
				if (superclassType!=null){
					ITypeBinding resolveBinding = superclassType.resolveBinding();
					TypeDeclaration typeDecl2 = pointsToAnalysis.getTypeDecl(ast.Type.createFrom(resolveBinding));
					return getFormalParams(mb, typeDecl2);
				}
		}
		 
		return fparams;
	}
	
	/**
	 * @return C_this - class of the receiver
	 */
	public QualifiedClassName getC_THIS(OOGContext context) {
		if (context.getO().getQCN() != null)
			return context.getO().getQCN();
		ITypeBinding declaringClass = getAnalysisContext().getAnalyzedMethod().resolveBinding().getDeclaringClass();
		return new QualifiedClassName(declaringClass, null);
	}

	/**
	 * O_C = C<\ob{D}> {(O_C, p_i) \mapsto D_i} \subseteq DD {(O_C, alpha_i)
	 * \mapsto D_i} \subseteq DD @see unionDDAlpha
	 * */
	protected Map<DomainMapKey, ODomain> auxUnionDD(OObject O_C, QualifiedClassName C, AnnotationInfo pInfo,
			QualifiedClassName cthis) {
		Map<DomainMapKey, ODomain> listDD = new Hashtable<DomainMapKey, ODomain>();
		List<ODomain> listD = O_C.getDomains();

		listDD.put(new DomainMapKey(O_C, Utils.getOwner(cthis, pInfo)), listD.get(0));
		List<String> parameters = Utils.getParameters(pInfo);
		// System.out.println("Sanity Check: " + (parameters.size() + 1 ==
		// listD.size()));
		if (parameters.size() + 1 == listD.size() )
			for (String p1 : parameters) {
				ODomain D_i1 = listD.get(parameters.indexOf(p1) + 1);
				if (D_i1 == null){
					System.err.println("WARNING: ODomain is NULL in the list of " + O_C.getInstanceDisplayName());
				}
				else
					listDD.put(new DomainMapKey(O_C, new DomainP(cthis, p1)), D_i1);
			}
		else {
			int debug = 0;
			debug++;
			System.err.println("WARNING: parameters.size() + 1 != listD.size() for " + O_C.getInstanceDisplayName() + parameters.size() + listD.size() );
		}
		int[] trivialInh = Utils.getDomInhPairs(C, C, pointsToAnalysis.getAnnoDB());
		auxUnionDDAlpha(O_C, C, listDD, trivialInh);
		return listDD;
	}

	/**
	 * O_C = C<\ob{D}> {(O_C, alpha_i) \mapsto D_i} \subseteq DD
	 * */
	private void auxUnionDDAlpha(OObject O_C, QualifiedClassName instType, Map<DomainMapKey, ODomain> listDD, int[] inhParams) {
		List<ODomain> listD = O_C.getDomains();
		AnnotationDatabase annoDB = pointsToAnalysis.getAnnoDB();
		if (hasSuperClass(instType.getTypeBinding()) ){
			int[] domInhPairs = Utils.getDomInhPairs(instType, instType.getSuperclass(), annoDB);
			auxUnionDDAlpha(O_C, instType.getSuperclass(), listDD, domInhPairs);
		}
		
		QualifiedClassName[] interfaces = instType.getInterfaces();
		for (QualifiedClassName itfQCN : interfaces) {
			int[] domInhPairs = Utils.getDomInhPairs(instType, itfQCN, annoDB);
			auxUnionDDAlpha(O_C, itfQCN, listDD, domInhPairs);
		}

		List<DomainP> alphaInfo = getDomainParamsDecls(instType);
		// System.out.println("Sanity Check: " + (alphaInfo.size() ==
		// listD.size()));
		for (DomainP p : alphaInfo) {
			if (p.isOwner())
				listDD.put(new DomainMapKey(O_C, new DomainP(instType, Constants.OWNER)), listD.get(0));
			else {
				if (!Utils.isSummaryType(instType)) {
					int indexOfp = alphaInfo.indexOf(p);
					int indexofD = inhParams[indexOfp];
					if (indexofD >= 0 && indexofD < listD.size()) {
						ODomain D_i = listD.get(indexofD);
						listDD.put(new DomainMapKey(O_C, p), D_i);
					} else {
						// TORAD: TODO: HIGH. Why should this ever happen? Avoid
						// silent failures!
						if (indexofD != -1) {
							StringBuilder builder = new StringBuilder();
							builder.append("WARNING: number of formal domain params greater than number of actual domains for ");
							builder.append(O_C.getInstanceDisplayName());
							builder.append(": ");
							builder.append(O_C.getTypeDisplayName());
							System.err.println(builder.toString());
						}
						else {
							//ignore cases when class does not inherits domain params
						}
					}
				}
				else{
					int indexOfp = alphaInfo.indexOf(p);
					if (indexOfp >= 0 && indexOfp < listD.size()) {
						ODomain D_i = listD.get(indexOfp);
						listDD.put(new DomainMapKey(O_C, p), D_i);
					} else {
						// TORAD: TODO: HIGH. Why should this ever happen? Avoid
						// silent failures!
						StringBuilder builder = new StringBuilder();
						builder.append("WARNING: ALIASXML: number of formal domain params greater than number of actual domains for ");
						builder.append(O_C.getInstanceDisplayName());
						builder.append(": ");
						builder.append(O_C.getTypeDisplayName());
						System.err.println(builder.toString());
					}
				}
			}
		}
	}

	/**
	 * Auxiliary judgment ddomains TODO: LOW. Maybe pass O, from the caller's
	 * value.getO();
	 * CHANGE TO SUPPORT recursive types 
	 * @param upsilon 
	 * @param oObject 
	 * 
	 * */
	private void auxdDomains(Set<OObject> upsilon, OObject O_C, OGraph G, OObject O, QualifiedClassName qualifiedClassName) {
		if (hasSuperClass(qualifiedClassName.getTypeBinding())) {
			auxdDomains(upsilon, O_C, G, O, qualifiedClassName.getSuperclass());
		}
		
		Set<String> domainDecls = new HashSet<String>(); 
			
		domainDecls.addAll(Utils.getDomainDecls(qualifiedClassName.getTypeBinding(), pointsToAnalysis
				.getAnnoDB()));
		
		QualifiedClassName[] interfaces = qualifiedClassName.getInterfaces();
		for (QualifiedClassName itf : interfaces) {
			auxdDomains(upsilon,O_C,G,O,itf);
		}
	   //domainDecls.addAll(Utils.getItfDomainDecls(qualifiedClassName, pointsToAnalysis.getAnnoDB()));
		
		for (String domName : domainDecls) {
			addDomain(upsilon, O_C, G, O, qualifiedClassName, domName);

		}
	}

	/**
	 * @param upsilon 
	 * @param O_C
	 * @param G
	 * @param qualifiedClassName
	 * @param domName
	 */
	private ODomain addDomain(Set<OObject> upsilon, OObject O_C, OGraph G, OObject O, QualifiedClassName qualifiedClassName, String domName) {
		ODomain foundODomain = null;
		for (DomainMapKey dmkey : G.getDD().keySet()) {
			if (dmkey.getO().equals(O_C) && dmkey.getDomDecl().getShortName().equalsIgnoreCase(domName)
			// &&
			// qualifiedClassName.isSubTypeCompatible(dmkey.getDomDecl().getTypeBinding())
					&& isSubtypeofC(qualifiedClassName, dmkey.getDomDecl().getTypeBinding())) {
				foundODomain = G.getODomain(dmkey);
			}

			for (IObject anc : O_C.getAncestors()) {	
				if (anc.getParent()!=null) { // no Oroot
					if (dmkey.getO().equals(anc)
							&& dmkey.getDomDecl().getShortName().equalsIgnoreCase(domName)
							&& qualifiedClassName.equals(((OObject)anc).getQCN())
							&& isSubtypeofC(qualifiedClassName, dmkey.getDomDecl().getTypeBinding())
							) {
						foundODomain = G.getODomain(dmkey);
					}
				}
			}
		}
		DomainP domainP = new DomainP(qualifiedClassName, domName, true);
		DomainMapKey domainMapKey = new DomainMapKey(O_C, domainP);
		if (foundODomain == null) { // do not add C::d if C'::d exits C<:C'
			ODomain dj = new ODomain(/* O_C.getO_id() + "." + */ domName, domainP);
			dj.setPrivate(domName.equals(Constants.OWNED));
			G.putODomain(domainMapKey, dj);
			return dj; //return a fresh ODomain
		} else {
			// when adding (O,pi)->D_i in Df-New, we did not registered that
			// as a domain declaration
			// for recursive types, here we override the key.
			G.removeODomain(domainMapKey);
			G.putODomain(domainMapKey, foundODomain);
		}
		return foundODomain; // returns an existing ODomain
	}

	private boolean isSubtypeofC(QualifiedClassName aQCN, QualifiedClassName c) {		 
		boolean isSubType = false;
		isSubType |= hierarchy.isSubtypeCompatible(aQCN.getActualName(), c.getActualName());
		isSubType |= c.isInterfaceOf(aQCN);
		
		return isSubType;
	}

	/**
	 * @retun true if the type has a non-null supertype, different than OBject
	 * */
	private boolean hasSuperClass(ITypeBinding instantiatedType) {
		if (instantiatedType.getSuperclass() == null)
			return false;
		if (instantiatedType.getSuperclass().getQualifiedName().equals(Utils.JAVA_LANG_OBJECT))
			return false;
		return true;
	}

	protected List<DomainP> getDomainParamsDecls(QualifiedClassName aQCN) {
		List<DomainP> result = new ArrayList<DomainP>();
		List<String> domainParams = Utils.getDomainParamsDecl2(aQCN.getTypeBinding(), pointsToAnalysis.getAnnoDB());
		for (String dparam : domainParams) {
			result.add(new DomainP(aQCN, dparam));
		}
		return result;
	}

	/**
	 * @param upsilon 
	 * @return a list of ODomains extracted from DD or return null if the list
	 *         could not be constructed
	 * */
	private CompoundODomain createDomainList(QualifiedClassName c, AssignmentInstruction instr, OOGContext value, QualifiedClassName aQCN) {
		CompoundODomain listD = new CompoundODomain();
		AnnotationInfo anInfo = getp_i(instr, pointsToAnalysis);
		// Add owner domain D_1
		// IN THEORY: D_1 cannot be a public domain
		// ODomain ownerDomain = currentContext.lookupDD(currentContext.getO(),
		// c.getTypeBinding(), anInfo
		// .getAnnotation().getDomain());
		// IN PRACTICE: Allow D_1 to be a public domain
		Set<ODomain> ownerDomains = new HashSet<ODomain>();
		OObject o = value.getO();
		if (!anInfo.getAnnotation().isUnique())
			ownerDomains =  auxFindD(value, o, c, Utils.getOwner(c, anInfo));
		else if (Config.HANDLE_LENT_UNIQUE ) {
			ownerDomains = getUniqueDomains(value, aQCN, o);
			if (ownerDomains.isEmpty() && collectFlowObj )
				ownerDomains.add(addDomain(value.getUpsilon(), o, value.getG(), value.getO(), aQCN, Constants.UNIQUE));
			else if (!ownerDomains.isEmpty() && collectFlowObj){
				DomainP dp = new DomainP(aQCN, Constants.UNIQUE);
				DomainMapKey domainMapKey = new DomainMapKey(o,dp);
//				ODomain removedODomain = value.getG().removeODomain(domainMapKey);
			}
		}
		if (ownerDomains != null && !ownerDomains.isEmpty())			
			if (!ownerDomains.contains(null))
				listD.add(ownerDomains);// D_i = DD[(O,p_i)]
			else{
				System.err.println("WARNING: cannot create list of domains for the new expression "+instr+" in "+ anInfo.getAnnotation().getDomain());
				return null;
			}
		else {
			// this may happen for an owner public domain n.d if n was not
			// created yet.
			return null;
		}
		List<String> parameters = Utils.getParameters(anInfo);
		for (String p : parameters) {
			Set<ODomain> lookupDD = null;
			if (p != Constants.UNIQUE)
				lookupDD = auxFindD(value, o, c, new DomainP(c, p));
			else
				lookupDD = getUniqueDomains(value, aQCN, o);
			if (lookupDD != null && lookupDD.size() > 0 && !lookupDD.contains(null))
				listD.add(lookupDD);
			else {		
				System.err.println("WARNING: cannot create list of domains for the new expression " + instr + " in " + p);
				return null;
			}
		}
		// ensure that the \ob{D} was correctly constructed, otherwise return
		// null;
		Assert.assertTrue("ob{D} for OObject was not correctly constructed",
				listD.iterator().next().size() == parameters.size() + 1);
		return listD;
	}

	/**
	 * @param value
	 * @param aQCN
	 * @param o
	 * @return 
	 */
	private Set<ODomain> getUniqueDomains(OOGContext value, QualifiedClassName aQCN, OObject o) {
		Set<ODomain> domains = new HashSet<ODomain>();
		Set<FlowGraphNode> solveUnique = value.solveUnique(o, aQCN);
		for (FlowGraphNode odp : solveUnique) {
			if (!odp.getB().isUnique()) {
				OObject oPrime = odp.getO();
				QualifiedClassName cthisPrime = oPrime.getQCN();
				DomainP pPrime = odp.getB();
				try{
				Set<ODomain> auxFindD = auxFindD(value, oPrime, cthisPrime, pPrime);
				domains.addAll(auxFindD);
				}
				catch(IllegalStateException ex){
					//variable for public domain not in scope
					System.out.println("failed attempt to find public domain resolved by unique " + oPrime.getInstanceDisplayName() + " " + pPrime);
				}
			}
		}
		return domains;
	}

	/**
	 * @return declared domains for a new expression. Since we require
	 *         developers to add local vars for new expressions, we can simply
	 *         look for that variable declaration
	 * 
	 * */
	public AnnotationInfo getp_i(TACInstruction instr, PointsToAnalysis ptAnalysis) {
		try {
			return Utils.getp_i(instr, ptAnalysis.getAnnoDB());
		} catch (IllegalStateException ex) {
			pointsToAnalysis.addWarning(instr.getNode(), CANNOT_FIND_ANNOTATION);
			return AnnotationInfo.Shared;
		}
	}

	/**
	 * Implements Aux-Mtype, but returns only T_R
	 * 
	 * @param mb
	 *            method binding (short name m is not enough)
	 * @param receiver
	 * @param C
	 *            , the class we are looking into for method type
	 * @return the actual domains for return type
	 * */
	protected List<DomainP> auxMTypeRet(IMethodBinding mb, Variable recv, QualifiedClassName C,
			List<DomainP> actualDomains) {
		List<DomainP> formalReturnDomains = getReturnDeclaredDomains(C, mb, this.pointsToAnalysis.getAnnoDB());
		List<DomainP> actReturnTypeDoms = substituteActualFormals(recv, C, actualDomains, formalReturnDomains);
		return actReturnTypeDoms;
	}

	/**
	 * Do not substitute actual/formals - simply return formals
	 */
	protected List<DomainP> auxMTypeRetWithoutSubst(IMethodBinding mb, QualifiedClassName C) {
		List<DomainP> formalReturnDomains = getReturnDeclaredDomains(C, mb, this.pointsToAnalysis.getAnnoDB());
		return formalReturnDomains;
	}

	/**
	 * Implements Aux-Mtype, and returns only \ob{T}
	 * 
	 * @param mb
	 *            method binding (short name m is not enough)
	 * @param C
	 *            , the class we are looking into for method type
	 * @return a map where each key is an argument:Variable note that we can get
	 *         C_i by calling Variable.resolveType() value is the list of domain
	 *         params associated with the variable (after substitution).
	 * */

	/**
	 * @return the list of formal domains for return type
	 * */
	// TODO: Move method elsewhere. to TraceUtils?
	public static List<DomainP> getReturnDeclaredDomains(QualifiedClassName C, IMethodBinding mb, AnnotationDatabase annoDB) {
		AnnotationSummary methAnnots = annoDB.getSummaryForMethod(mb);
		List<DomainP> listP = new ArrayList<DomainP>();
		List<ICrystalAnnotation> annots = methAnnots.getReturn();
		// XXX. Why do we have this check here? We might read from cache! There could be another annotation, e.g., @Deprecated
		if (annots.size() > 0) {
			listP = getFormalDomains(C, mb, annots);
		} else {
			String key = mb.getMethodDeclaration().getKey();
			MethodAnnotationInfo methDecl = MethodAnnotationInfo.getBinding(key);
			if (methDecl != null) {
				AnnotationInfo anInfo = methDecl.getReturnAnnotation();
				if (anInfo.getAnnotation().isEmpty()) {
					anInfo = AnnotationInfo.Shared;
				}
				listP = Utils.getDomainP(C, anInfo);
			}
		}
		return listP;
	}

	/**
	 * Implements Aux-Fields key is a string - name of the field value is the
	 * list of actual domain params (after substitution).
	 * 
	 * @param recv
	 * */
	protected Map<String, List<DomainP>> auxFields(Variable recv, QualifiedClassName resolveType,
			List<DomainP> actualDomains) {
		Map<String, List<DomainP>> fieldTypes = new Hashtable<String, List<DomainP>>();
		if (Utils.isSummaryType(resolveType)) {
			Map<String, OwnershipType> declaredFieldsXML = Utils.getDeclaredFieldsXML(resolveType);
			for (String fieldName : declaredFieldsXML.keySet()) {
				OwnershipType ownershipType = declaredFieldsXML.get(fieldName);
				List<DomainP> value = ownershipType.getValue();
				List<DomainP> actFieldTypeDoms = substituteActualFormals(recv, resolveType, actualDomains, value);
				fieldTypes.put(fieldName, actFieldTypeDoms);
			}
			return fieldTypes;
		}
		
		IVariableBinding[] fields = resolveType.getTypeBinding().getDeclaredFields();
		for (IVariableBinding iVariableBinding : fields) {
			List<DomainP> formalDomains = getDeclaredDomains(resolveType, iVariableBinding, this.pointsToAnalysis.getAnnoDB());
			if (formalDomains!=null){
				List<DomainP> actFieldTypeDoms = substituteActualFormals(recv, resolveType, actualDomains, formalDomains);
				fieldTypes.put(iVariableBinding.getName(), actFieldTypeDoms);
			}
		}
	
		if (hasSuperClass(resolveType.getTypeBinding()))
			fieldTypes.putAll(auxFields(recv, resolveType.getSuperclass(), actualDomains));
		return fieldTypes;
	}

	/**
	 * Implements Aux-Fields without substitution key is a string - name of the
	 * field value is the list of actual domain params (after substitution).
	 * */
	protected Map<String, OwnershipType> auxFieldsWithoutSubst(QualifiedClassName resolveType) {		
		if (Utils.isSummaryType(resolveType))
			return Utils.getDeclaredFieldsXML(resolveType);
		
		Map<String, OwnershipType> fieldTypes = new Hashtable<String, OwnershipType>();
		IVariableBinding[] fields = resolveType.getTypeBinding().getDeclaredFields();
		for (IVariableBinding iVariableBinding : fields) {
			List<DomainP> formalDomains = getDeclaredDomains(resolveType, iVariableBinding, this.pointsToAnalysis.getAnnoDB());
			if (formalDomains!=null){
				fieldTypes.put(iVariableBinding.getName(), new OwnershipType(iVariableBinding.getType(), formalDomains));
			}
		}
		if (hasSuperClass(resolveType.getTypeBinding()))
			fieldTypes.putAll(auxFieldsWithoutSubst(resolveType.getSuperclass()));
		return fieldTypes;
	}

	/**
	 * Given a variable, find the domains declared on its type
	 * x : C<\ob{p}>
	 * We lookup the type declaration of C
	 * 
	 * C<\ob{\alpha},\ob{\beta}}>
	 * 
	 * And return:
	 *  <\ob{\alpha},\ob{\beta}}>
	 * 
	 * TODO: Rename: getDeclaredDomains -> getTypeFormalDomainParams
	 * 
	 * r.m(x);
	 * 
	 * Find x : C<\ob{p}>
	 * 
	 * Find the formal param of the method.
	 * And find the domain of that.
	 * 
	 * @param c
	 * @param iVariableBinding
	 * @return
	 */
	// TODO: Move method elsewhere. to TraceUtils?
	public static List<DomainP> getDeclaredDomains(QualifiedClassName c, IVariableBinding iVariableBinding, AnnotationDatabase annoDB) {
		List<ICrystalAnnotation> annots = annoDB.getAnnosForVariable(iVariableBinding);
		// XXX. Move some of these checks into getFormalDomains; or get rid of them because we might read value from cache, without touching annotations!
		// XXX. Check non-primitive too!
		if (annots != null && !annots.isEmpty() && annots.get(0).getName().endsWith(Constants.DOMAIN) ) {		
			List<DomainP> formalDomains = getFormalDomains(c, iVariableBinding, annots);
			return formalDomains;
		}
		else 
		if (!iVariableBinding.getType().isPrimitive()) {
			AnnotationInfo annot = Utils.getAnnotationAliasXML(iVariableBinding);
			if (annot != null && annot.getAnnotation() != null && !annot.getAnnotation().isEmpty())
				return Utils.getDomainP(c, annot);
		}		
		// will return a warning
		return null; 
	}
	
	protected List<DomainP> getDomainsOfFormalParams(IMethodBinding mb, TypeDeclaration typeDecl, QualifiedClassName cthis, int i) {
		IMethodBinding methodDeclaration = mb.getMethodDeclaration();
		boolean found = false;		
		if (typeDecl == null) {
			QualifiedClassName aQCN = new QualifiedClassName(mb.getDeclaringClass(), cthis);			
			ITypeBinding[] parameterTypes = methodDeclaration.getParameterTypes();
			
			if (0<=i && i<parameterTypes.length){
//			for (ITypeBinding iTypeBinding : parameterTypes) {
				VariableAnnotationInfo binding = VariableAnnotationInfo.getBinding(methodDeclaration.getKey() + parameterTypes[i].getKey());
				if (binding != null) {
					AnnotationInfo annot = binding.getAnnotationInfo();
					if (annot != null && annot.getAnnotation() != null && !annot.getAnnotation().isEmpty())
						return Utils.getDomainP(aQCN, annot);
				}
			}
		} else {
			MethodDeclaration[] methods = typeDecl.getMethods();
			QualifiedClassName aQCN = new QualifiedClassName(typeDecl.resolveBinding(), cthis);
			for (MethodDeclaration md : methods) {
				if (md.resolveBinding().getKey().equals(mb.getKey()) || (Bindings.isSubsignature(md.resolveBinding(), methodDeclaration))) {
					found = true;
					List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) md.parameters();
					if (0<=i && i<parameters.size()){
						IVariableBinding paramBinding = parameters.get(i).resolveBinding();
						return getDeclaredDomains(aQCN, paramBinding, this.pointsToAnalysis.getAnnoDB());
					}
				}
			}
			if (!found) {
				Type superclassType = typeDecl.getSuperclassType();
				if (superclassType != null) {
					ITypeBinding resolveBinding = superclassType.resolveBinding();
					QualifiedClassName aQCN2 = new QualifiedClassName(resolveBinding, cthis);
					TypeDeclaration typeDecl2 = pointsToAnalysis.getTypeDecl(ast.Type.createFrom(resolveBinding));
					return getDomainsOfFormalParams(mb, typeDecl2, aQCN2, i);
				}
			}
		}
		return null;
	}
	
	/**
	 * @param recv
	 * @param aQCN
	 *            - type where formals are declared
	 * @param actualTypeDomains
	 *            - actual domains for type as given in the client code
	 * @param formalDomains
	 *            - formal domains to be substituted (unless they are locally
	 *            declared domains, shared, etc)
	 * */
	private List<DomainP> substituteActualFormals(Variable recv, QualifiedClassName aQCN,
			List<DomainP> actualTypeDomains, List<DomainP> formalDomains) {
		// HACK: well if there is nothing to substitute, do not substitute.
		// this may happen for code in libraries
		if (actualTypeDomains == null) {
			return formalDomains;
		}
		List<DomainP> actualSubstitutedDomains = new ArrayList<DomainP>();
		List<DomainP> formalTypeDomains = getDomainParamsDecls(aQCN);
		List<String> domDecls = Utils.getDomainDeclsRecursive(aQCN.getTypeBinding(), pointsToAnalysis.getAnnoDB());

		for (DomainP alpha : formalDomains) {
			int position = -1;
			position = formalTypeDomains.indexOf(alpha);
			if (position >= 0 && position < actualTypeDomains.size())
				actualSubstitutedDomains.add(actualTypeDomains.get(position)); // [p_i/alpha_i]
			else if (domDecls.contains(alpha.getShortName())) {
				if (!Utils.isThis(recv))
					// n.d replace this with n
					actualSubstitutedDomains.add(new DomainP(aQCN, recv.toString() + "." + alpha.getShortName()));
				else
					actualSubstitutedDomains.add(alpha);
			} else
				// probably shared, or unique, leave it as it is,
				// or a formal domain where the actual was not provided:
				// Iterator<iters,V> = new SequenceIterator<iters,V,V>();
				actualSubstitutedDomains.add(alpha);
		}
		return actualSubstitutedDomains;
	}

	/**
	 * @param resolveType
	 * @param annots
	 */
	private static List<DomainP> getFormalDomains(QualifiedClassName resolveType, IBinding varBinding, List<ICrystalAnnotation> annots) {
		List<DomainP> listP = null;
		AnnotationInfo anInfo = null;
		
		anInfo = VariableDeclAnnotationInfo.getInfo(varBinding);
		if ( anInfo == null ) {
			for (ICrystalAnnotation iCrystalAnnotation : annots) {
				if (iCrystalAnnotation.getName().endsWith(Constants.DOMAIN)) {
					Object val = iCrystalAnnotation.getObject("value");
					String annot = "";

					// NOTE: IMemberValuePairBinding.getValue() sometimes returns a
					// String, sometimes an Object[]!
					if (val instanceof String) {
						annot = (String) val;
					} else if (val instanceof Object[]) {
						Object[] arrVal = (Object[]) val;
						annot = (String) arrVal[0];
					}
					anInfo = AnnotationInfo.parseAnnotation(annot);

					// Cache the result
					VariableDeclAnnotationInfo.putInfo(varBinding, anInfo);
				}
			}
		}
		
		// add owner
		if(anInfo != null) {
			listP = Utils.getDomainP(resolveType, anInfo);
		}
		else {
			// Why would it be null?
			listP = new ArrayList<DomainP>();
		}

		return listP;
	}

	/**
	 * @param Gamma
	 * @param varBinding
	 * @param declaredDomains
	 */
	protected List<DomainP> addToGamma(Map<Variable, List<DomainP>> Gamma, IVariableBinding varBinding,
			QualifiedClassName cthis) {
		List<DomainP> declaredDomains = getDeclaredDomains(cthis, varBinding, this.pointsToAnalysis.getAnnoDB());
		if (declaredDomains!=null && declaredDomains.size()>0){
			if (varBinding.isField())
				Gamma.put(new FieldVariable(varBinding), declaredDomains);
			else
				Gamma.put(new SourceVariable(varBinding.getName(), varBinding, true), declaredDomains);
			// TODO: LOW: what if varBinding.isEnumConstant?
		}
		return declaredDomains;
	}

	/**
	 * returns the list of domain params of x in an assignment x = y
	 * 
	 * @param target
	 * @param instr
	 * @param Gamma
	 * @param C
	 * @return
	 */
	protected List<DomainP> getTargetActualDomains(Variable target, AssignmentInstruction instr,
			Map<Variable, List<DomainP>> Gamma, QualifiedClassName C) {
		if (Gamma.containsKey(target))
			return Gamma.get(target);
		if (Utils.isThis(target)) {
			List<DomainP> thisFormalDomains = getDomainParamsDecls(C);
			Gamma.put(target, thisFormalDomains);
			return thisFormalDomains;
		}

		IVariableBinding varBinding = Utils.getTargetVarBinding(instr);
		if (varBinding != null) {
			return addToGamma(Gamma, varBinding, C);
		}
		return null;
	}

/**
		 * implements Df-Lookup
		 * @param context - DO, DD, DE
		 * @param O
		 * @param C - C' from C'<p'>
		 * @param actual Domains - p'
		 * 
		 * 	// TODO: add import edges
		 *	// we may need something more fancy than isSubTypeCompatible for generics
		 */
	protected Set<OObject> auxLookup(OOGContext context, OObject O, QualifiedClassName C, List<DomainP> actualDomains) {
		Set<OObject> lookupResult = new HashSet<OObject>();

		// Sanity checks
		if (actualDomains == null)
			return lookupResult;
		if (actualDomains.contains(null))
			return lookupResult;
		// do not attempt to handle Lent and Unique unless the Config file asks for it 
		if (actDomContains(actualDomains, Constants.UNIQUE)){
			//System.out.println("cannot lookup unique");
			return lookupResult;
		}
		if (actDomContains(actualDomains, Constants.LENT)){
			//System.out.println("cannot lookup lent");
			return lookupResult;
		}

		CompoundODomain cODomain = new CompoundODomain();
		OGraph oGraph = context.getG();
		
		for (DomainP p_i : actualDomains) {
			Set<ODomain> listD_i = new HashSet<ODomain>();
			if (p_i.isLent()) {
				Set<FlowGraphNode> solveLent = context.solveLent(O, C);
				if (solveLent.isEmpty()){
					return lookupResult;
				}				
				for (FlowGraphNode pair : solveLent) {
//					if (isInScope(value, pair.getX(), pair.getO()))
					if (pair.getB().isUnique()){
						ODomain oDomain = oGraph.getODomain(new DomainMapKey(pair.getO(), new DomainP(C,Constants.UNIQUE)));
						if (oDomain!=null)
							listD_i.add(oDomain);
//						else
//							return lookupResult;
					}
					else try
					{
						Set<ODomain> auxFindD = auxFindD(context, pair.getO(), pair.getB().getTypeBinding(), pair.getB());
						listD_i.addAll(auxFindD);
					}	
					catch(IllegalStateException ex){
						//variable for public domain not in scope
						System.out.println("failed attempt to find public domain resolved by lent " + pair.getO().getInstanceDisplayName() + " " + pair.getB());
					}
				}
			} else if (p_i.isUnique()) {
				Set<FlowGraphNode> solveUnique = context.solveUnique(O, C);
				if (solveUnique.isEmpty()){					
					ODomain oDomain = oGraph.getODomain(new DomainMapKey(O, new DomainP(C,Constants.UNIQUE)));
					if (oDomain!=null)
						listD_i.add(oDomain);
//					else												
//						return lookupResult;
				}
				for (FlowGraphNode pair : solveUnique) {
					if (pair.getB().isUnique()) {
						ODomain oDomain = oGraph.getODomain(new DomainMapKey(pair.getO(), new DomainP(C, Constants.UNIQUE)));
						if (oDomain != null)
							listD_i.add(oDomain);
						else{ 
							//find original source of this unique 
							FlowGraph fg = context.getFG();
							//System.out.println(fg.transitiveClosure().print());
							Set<FlowGraphEdge> edges = fg.findEdgesWithUniqueSrcDest(pair.getO(), C.getType());							
							for (FlowGraphEdge flowGraphEdge : edges) {
								FlowGraphNode dst = flowGraphEdge.getDst();
								ast.Type tDst = ast.Type.createFrom(dst.getX().resolveType());
								boolean isB = tDst.isSubtypeCompatible(C.getType());
								if (isB) {
									ODomain oDomain2 = oGraph.getODomain(new DomainMapKey(flowGraphEdge.getSrc().getO(), new DomainP(C, Constants.UNIQUE)));
									if (oDomain2 != null)
										listD_i.add(oDomain2);
								}
							}							
						}
					} else {
						try {
							Set<ODomain> auxFindD = auxFindD(context, pair.getO(), pair.getB().getTypeBinding(),
									pair.getB());
							listD_i.addAll(auxFindD);
						} catch (IllegalStateException ex) {
							// variable for public domain not in scope
							System.out.println("failed attempt to find public domain resolved by unique "
									+ pair.getO().getInstanceDisplayName() + " " + pair.getB());
						}
					}
				}
			} 
			else if (p_i.getShortName() == Constants.SHARED) {
				listD_i.add(context.getDShared());
			}
			// TODO: Move this first as it is the most common case.
			else {
				// ODomain D_i = DD.get(new DomainMapKey(O,p_i));
				listD_i = auxFindD(context, O, C, p_i);
			}
			cODomain.add(listD_i);
		}

		// Here, we find the O_i's of the appropriate sub-types
		OObject oWorld = OOGContext.getOWorld();
		// Get all the objects in DO
        Map<OObjectKey, OObject> DO = oGraph.getDOMap();
        Multimap<ODomain, OObject> cache = oGraph.getCache();
        
        // XXX. Can we avoid iterating over DO
        // This is scanning all the OObjects in the OGraph every single time and calling value equality on each
        // Find a better data structure here...
        // D1 -> {O}
        // Every object has at least owner...
        // XXX. Maybe iterate over DO first.
        // XXX. We are getting a set of lists...in the case of public domains. Double check this cODomain
        for (List<ODomain> listD : cODomain) {
        	ODomain listD0 = listD.isEmpty() ? null : listD.get(0);

        	if(listD0 != null ) {
        		Collection<OObject> collection = cache.get(listD0);
        		for (OObject O_k : collection ) {
        			// If not in the same owning domain, then no point in checking:
        			// either the subtyping or the remaining D_i's
        			if (!O_k.equals(oWorld))
        				if (isSubtype(context, O, C, listD, O_k))
        					lookupResult.add(O_k);
        		}
        	}
        }
		return lookupResult;
	}

	private boolean actDomContains(List<DomainP> actualDomains, String domainB) {
		if (!Config.HANDLE_LENT_UNIQUE)
			for (DomainP domainP : actualDomains) {
				if (domainP.getShortName().equals(domainB))
					return true;
			}
		return false;
	}

	private boolean isInScope(OOGContext value, Variable x, OObject o) {
		if (value.getGamma().containsKey(x))
			return true;
		Map<String, OwnershipType> fieldsInScope = auxFieldsWithoutSubst(o.getQCN());
		if (x instanceof FieldVariable && fieldsInScope.keySet().contains(x.toString()))
			return true;
		return false;
	}

	/**
	 * @param p_i
	 * @return
	 */
	protected boolean isShared(DomainP p_i) {
		return p_i.getShortName().equalsIgnoreCase(Constants.SHARED);
	}

	/**
	 * @param value
	 * @param O
	 * @param C
	 * @param p_i
	 * @return
	 */
	// DONE. No need for AnnotationInfo.parse(...). Just split "n.d"; n is the object name;
	// XXX. Can we cache the lookup results:
	// - Give an (O, C::p) return the Os?
	// -- But have to take into account subtyping, C' <: C
	// -- Also, the number of params could be different.
	// - Avoid stale caching:
	// - If found something, there could be more
	// 
	// WACI. Big optimization:
	// - finalize the lookups:
	// -- once you create all the OObjects...then you're done with the cache.
	// -- right now, as part of creating the OObjects...we keep using lookup and recomputing the same values
	// - then for value flow...you might create new objects and update the cache
	private Set<ODomain> auxFindD(OOGContext value, OObject O, QualifiedClassName C, DomainP p_i) {
		Set<ODomain> resultSet = new HashSet<ODomain>();
		String annot = p_i.getShortName();
		
		boolean isObjectPublicDomain = false;
		String domainName = annot;
		String objName = "";
		if (annot != null) {
			// Just split "n.d"; n is the objName; d is the domainName
			int indexOf = annot.indexOf(".");
			if (indexOf != -1) {
				isObjectPublicDomain = true;
				objName = annot.substring(0, indexOf);
				domainName = annot.substring(indexOf + 1);
				// HACK. Do not get fooled by "Class.this" (when using inner/outer receiver)
				if(domainName.contains("this")) {
					isObjectPublicDomain = false;
				}
			}
		}
		
		// TODO: maybe refactor and move in lookupDD.
		if (isObjectPublicDomain) {
			//NOTE: O is not necessarily the same as value.getO() 
			QualifiedClassName cthis = O.getQCN();
			QualifiedClassName Cn = findClassOfn(value.getGamma(), cthis, objName);
			List<DomainP> actDomainsOfn = findActualDomainsOfn(value.getGamma(), objName, cthis);
			
			// here we have a lookup (o, C::(n.d)) we need to call lookup(n, C_n::d) where n: C_n
			Set<OObject> nSet = auxLookup(value, O, Cn, actDomainsOfn);
			if (nSet.size() > 0) {
				for (OObject n : nSet) {
					DomainP pn = new DomainP(Cn, domainName);
					resultSet.addAll(value.lookupDD(n, pn));
				}
			} else {
				// the OObject might not have been created yet
			}
		} else {
			resultSet.addAll(value.lookupDD(O, p_i));
		}
		return resultSet;
	}

	private List<DomainP> findActualDomainsOfn(Map<Variable, List<DomainP>> gamma, String objectName,
			QualifiedClassName C) {
		for (Variable v : gamma.keySet())
			if (v.toString().equals(objectName))
				return gamma.get(v);
		// if not found let's look in fields
		Map<String, OwnershipType> fields = auxFieldsWithoutSubst(C);
		if (fields.containsKey(objectName))
			return fields.get(objectName).getValue();
		else
			// well it looks like objectName is not in the scope
			throw new IllegalStateException(objectName + " not defined in the scope of " + C.getFullyQualifiedName());

	}

	// XXX. Avoid using exceptions for control flow...
	// Does this get triggered a lot?
	// XXX. Create our own custom exception class...
	private QualifiedClassName findClassOfn(Map<Variable, List<DomainP>> gamma, QualifiedClassName C, String objectName) {
		for (Variable v : gamma.keySet())
			if (v.toString().equals(objectName))
				return new QualifiedClassName(v.resolveType(), C);
		Map<String, OwnershipType> fields = auxFieldsWithoutSubst(C);
		if (fields.containsKey(objectName))
			return new QualifiedClassName(fields.get(objectName).getKey(), C);
		else
			// well it looks like objectName is not in the scope
			throw new IllegalStateException(objectName + " not in defined in the scope of "+C.getFullyQualifiedName());
	}

	/**
	 * @param value
	 * @param C
	 * @param listD
	 * @param O_k
	 * @return true if listD is a sublist of O_k.getDomains() and
	 *         O_k.getClassType() <: C Example: Model<DOCUMENT, DOCUMENT, VIEW>
	 *         <: Listener<VIEW> listD = [VIEW]. O_k.getDomains = [DOCUMENT,
	 *         DOCUMENT, VIEW]
	 */
	private boolean isSubtype(OOGContext value, OObject O, QualifiedClassName C, List<ODomain> listD, OObject O_k) {
		// 1. Check the D's are compatible
		List<ODomain> domains = O_k.getDomains();
		if (listD.isEmpty())
			return false;
		if (listD.get(0) == null)
			return false;
		
		// XXX. Why treating the owner domain as a special case; just as  short-cut
		boolean result = false; // listD.get(0).equals(O_k.getOwnerDomain());// ||
		// (listD.get(0).getDomainDecl().getShortName().equals(Constants.LENT)
		// // && value.getChildrenOObjects(O).contains(O_k)
		// );
		// int[] inhPair = Utils.getDomInhPairs(O_k.getQCN(), C, pointsToAnalysis.getAnnoDB());
		result = /*result &&*/ areCompatible(listD, domains/*, inhPair*/);
		if ( !result ) {
			return false;
		}
		
		// 2. Check the types are compatible
		QualifiedClassName qcn = O_k.getQCN();
		// XXX. Cache the hierarchy.
		// XXX. Check the domains first, then check the subtyping, which is expensive
		// Extract method that combines Subclassing and interfaces. Cache the answers.
		result &= isSubtypeofC(qcn, C); 
		
		return result;
		// if (!O_k.getC().isSubTypeCompatible(C)) return false;
	}

	/**
	 * @param listD
	 * @param domains
	 * @param inhPair 
	 * @return
	 * HACK: assumes the list are in order, which is not always the case
	 * Use DomainInherits here: For testing use SequenceIterator<Mowner,Towner> <: Iterator<Towner>
	 * 
	 * XXX. There should not be a hack. This should not happen. Bad typechecker, bad!
	 * XXX. Get rid of call to List.equals. Just compare the beginnings of two lists in sequence.
	 * 
	 */
	private boolean areCompatible(List<ODomain> listD, List<ODomain> domains/*, int[] inhPair*/) {
		if (domains.equals(listD))
			return true;
		if (domains.size() < listD.size())
			return false;
		int i = 0;
		for (ODomain d : listD) {
			if (i > 0) {
//				if (i<inhPair.length && inhPair[i] > -1 && inhPair[i] < domains.size()) {
//					ODomain oDomainK = domains.get(inhPair[i]);
//					if (!oDomainK.equals(d))
//						return false;
//				} else {
					ODomain oDomainK = domains.get(i);
					if (!oDomainK.equals(d))
						return false;
//				}
			}
			i++;
		}
		return true;
	}

	@Override
	public OOGContext transfer(NewArrayInstruction instr, OOGContext value) {
		// TODO create an OObject even for array of primitive types, array of
		// arrays, etc.
		System.out.println("New Array: " + instr);
		ITypeBinding cBinding = instr.getArrayType().resolveBinding();
		return createOObjectArray(instr, value, cBinding);
	}

	private static int counter = 0;
	// TODO: Use 100 for some systems where the analysis seems to go into a tailspin
	private static int INTERRUPT_AT = 1000;

	@Override
	public OOGContext transfer(ArrayInitInstruction instr, OOGContext value) {
		// HACK: Change counter value....
		counter++;
		if (counter == INTERRUPT_AT) {
			throw new CrystalRuntimeException("INTERRUPT at: " + instr.getNode().toString());
		}
		
		// a = { o1, o2, o3}
		System.out.println("Init Array: " + instr);
		ITypeBinding cBinding = instr.getTarget().resolveType();
		return createOObjectArray(instr, value, cBinding);
	}

	/**
	 * @param instr
	 * @param value
	 * @param cBinding
	 * @return
	 */
	// XXX. Handling new C[] as a special case, because we do not want to go into the declaration of C.
	private OOGContext createOObjectArray(AssignmentInstruction instr, OOGContext value, ITypeBinding cBinding) {
		QualifiedClassName cthis = getC_THIS(value);
		QualifiedClassName aQCN = new QualifiedClassName(cBinding, cthis);
		Set<OObject> upsilon = value.getUpsilon();
		Set<OObject> upsilonP = new LinkedHashSet<OObject>();
		upsilonP.addAll(upsilon);
		
		CompoundODomain compoundListD = createDomainList(cthis, instr, value, aQCN); // D_i
																				// =
		// DD[(O,p_i)]
		if (compoundListD == null) // push the instr on stack and try again
									// later
		{
			return value.clone();
		}

		Stack<TraceabilityEntry> stack = value.getExpressionStack();
		Stack<TraceabilityEntry> stackP = new Stack<TraceabilityEntry>();
		stackP.addAll(stack);
		// stackP.push(traceabilityFactory.createTraceability(instr,
		// value.getO()));

		for (List<ODomain> listD : compoundListD) {
			OObject O_C = value.getG().getOObject(aQCN, listD);
			// DO' = DO U {O_C}
			setOObjectInfo(instr, O_C, value.getO(), stackP);
			
			// DD' = DD U {(O_C,alpha_i)|-> D_i, (O_C,p_i) |-> D_i}
			AnnotationInfo pInfo = getp_i(instr, pointsToAnalysis);
			Map<DomainMapKey, ODomain> listDD = auxUnionDD(O_C, aQCN, pInfo, cthis);
			value.getG().addAllDD(listDD);
			auxdDomains(upsilon, O_C, value.getG(),value.getO(), aQCN);
			upsilonP.add(O_C);
			value.getGamma().put(instr.getTarget(), Utils.getDomainP(cthis, pInfo));
		}
		return value.createNewContext(value.getO(), upsilonP, value.getGamma(), stackP);
	}

	/**
	 * @param instr
	 * @param listD
	 * @param O_C
	 */
	protected void setOObjectInfo(AssignmentInstruction instr, OObject O_C, OObject O,
			Stack<TraceabilityEntry> stack) {
		// HACK: if an object has no local variable associated, let's not crash
		// the analysis. Report an error instead.
		// TODO: group messages
		String simpleName = "";
		try {
			simpleName = Utils.getSimpleAssignName(instr);
		} catch (IllegalStateException ex) {
			simpleName = "NEWWWname";
			pointsToAnalysis.addWarning(instr.getNode(), CANNOT_FIND_NAME_FOR_OBJECT);
		}
		// Set<ASTNode> fullPath = buildFullPath(instr, O);
		O_C.addLink(stack);
		O_C.setInstanceDisplayName(simpleName);
	}

	@Override
	public OOGContext transfer(AssignmentInstruction instr, OOGContext value) {
		// add target of assignment to Gamma if it is a SourceVariable, later it
		// may become a receiver
		QualifiedClassName cthis = getC_THIS(value);
		if (cthis != null) {
			if (instr instanceof LoadLiteralInstruction) {
				LoadLiteralInstruction loadLiteral = (LoadLiteralInstruction) instr;
				if ((loadLiteral.getLiteral() instanceof String) && (!loadLiteral.isPrimitive())) {
					QualifiedClassName aQCN = new QualifiedClassName(instr.getTarget().resolveType(), cthis);
					List<ODomain> listD = new ArrayList<ODomain>();
					listD.add(OOGContext.getDShared());
					OObject O_C = value.getG().getOObject(aQCN, listD);
										
					// DO' = DO U {O_C}
					Stack<TraceabilityEntry> stack = value.getExpressionStack();
					Stack<TraceabilityEntry> stackP = new Stack<TraceabilityEntry>();
					stackP.addAll(stack);
					stackP.push(traceabilityFactory.createTraceability(loadLiteral, value.getO()));

					setOObjectInfo(instr, O_C, value.getO(), stackP);

					// DD' = DD U {(O_C,alpha_i)|-> D_i, (O_C,p_i) |-> D_i}
					AnnotationInfo pInfo = AnnotationInfo.Shared;
					Map<DomainMapKey, ODomain> listDD = auxUnionDD(O_C, aQCN, pInfo, cthis);
					value.getGamma().put(instr.getTarget(), Utils.getDomainP(cthis, pInfo));
					value.getG().addAllDD(listDD);
				}
			}
			// Here we copy the actual domain arguments to a TAC temp variable,
			// then remember that in Gamma for later use
			// 
			if (instr.getTarget() instanceof SourceVariable) {
				// System.out.println("Assignment: " + instr);
				List<DomainP> actDomains = getTargetActualDomains(instr.getTarget(), instr, value.getGamma(), cthis);
				if (actDomains!=null){
					value.getGamma().put(instr.getTarget(), actDomains);
				}
				else{
					returnWarning(instr.getNode(), instr.getTarget());
				}
			}
			if (instr.getNode() instanceof ArrayAccess) {
				ArrayAccess aa = (ArrayAccess) instr.getNode();
				if (Utils.getArrayAccessVarBinding(aa)==null){
					int debug = 0;
					debug++;
					Expression array = aa.getArray();			
					if (instr instanceof LoadArrayInstruction){
						LoadArrayInstruction lai = (LoadArrayInstruction)instr;
						List<DomainP> list = value.getGamma().get(lai.getAccessedArrayOperand());
					}
				}
				else
				{
					List<DomainP> actDomains = getArrayAccessActualDomains(aa, cthis);
					value.getGamma().put(instr.getTarget(), actDomains);
				}
			}

		}
		return super.transfer(instr, value);
	}

	protected void returnWarning(ASTNode node, Variable recv) {
		if (!recv.resolveType().isPrimitive())
			this.pointsToAnalysis.addWarning(node, "cannot determine  domains variable of type "+recv.resolveType().getQualifiedName());
		else{
			//do nothing
		}
	    
    }

	protected List<DomainP> getArrayAccessActualDomains(ArrayAccess aa, QualifiedClassName cthis) {
		List<DomainP> listP = new ArrayList<DomainP>();
		IVariableBinding arrVarBinding = Utils.getArrayAccessVarBinding(aa);
		if (arrVarBinding==null){
			System.err.println("WARNING: could not handle complex array expression");
			return listP;
		}
		AnnotationInfo info = VariableDeclAnnotationInfo.getInfo(arrVarBinding);
		if (info == null) {
		List<ICrystalAnnotation> annots = pointsToAnalysis.getAnnoDB().getAnnosForVariable(arrVarBinding);		
		for (ICrystalAnnotation iCrystalAnnotation : annots) {
			if (iCrystalAnnotation.getName().endsWith(Constants.DOMAIN)) {
				Object val = iCrystalAnnotation.getObject("value");
				String annot = "";

				// NOTE: IMemberValuePairBinding.getValue() sometimes returns a
				// String, sometimes an Object[]!
				if (val instanceof String) {
					annot = (String) val;
				} else if (val instanceof Object[]) {
					Object[] arrVal = (Object[]) val;
					annot = (String) arrVal[0];
				}
				AnnotationInfo anInfo = AnnotationInfo.parseAnnotation(annot);
				VariableDeclAnnotationInfo.putInfo(arrVarBinding, anInfo);
				return Utils.getListDomainP(cthis, anInfo);
			}
		}
		}
		return Utils.getListDomainP(cthis, info);
	}

	@Override
	public OOGContext transfer(CastInstruction instr, OOGContext value) {
		QualifiedClassName cthis = value.getO().getQCN();
		List<DomainP> fromTypeDomains = value.getGamma().get(instr.getOperand());
		List<DomainP> toTypeDomains = new ArrayList<DomainP>();
		ITypeBinding toType = instr.getCastToTypeNode().resolveBinding();
		QualifiedClassName qcnToType = new QualifiedClassName(toType, cthis);
		ITypeBinding fromType = instr.getOperand().resolveType();
		QualifiedClassName qcnFromType = new QualifiedClassName(fromType, cthis);
		List<DomainP> formalToTypeDomains = getDomainParamsDecls(qcnToType);

		List<DomainP> actToTypeDomains = getTargetActualDomains(instr.getTarget(), instr, value.getGamma(), cthis);
		if (!toType.isPrimitive()) {
			if (actToTypeDomains != null) {
				toTypeDomains.addAll(actToTypeDomains);
			} else {
				// HACK: check for subtyping relation for generics?
				if ((formalToTypeDomains != null) && (fromTypeDomains != null)
						&& (qcnToType.isSubTypeCompatible(qcnFromType))
						&& (formalToTypeDomains.size() == fromTypeDomains.size()))
					toTypeDomains.addAll(fromTypeDomains);
				else {
					toTypeDomains.add(new DomainP(null, "SHARED"));
					pointsToAnalysis.addWarning(instr.getNode(), CANNOT_DETERMINE_ACTUAL_DOMAINS_FOR_CAST);
					// throw (new
					// IllegalStateException("Cannot determine actual domains for cast expression "+instr));
				}
			}
			if (toTypeDomains!=null && toTypeDomains.size()>0){
				value.getGamma().put(instr.getTarget(), toTypeDomains);
				
				if (Config.HANDLE_LENT_UNIQUE ) {
					Variable source = instr.getOperand();
					List<DomainP> sourceDomain = value.getGamma().get(source);
					if (sourceDomain != null && sourceDomain.size() > 0) {
						FlowGraphNode src = new FlowGraphNode(value.getO(), source, sourceDomain.get(0));
						FlowGraphNode dst = new FlowGraphNode(value.getO(), instr.getTarget(), toTypeDomains.get(0));
						value.getFG().addInfoFlow(new FlowGraphEdge(src, dst, FlowAnnot.getEmpty()));
					}
				}
			}
			else
				returnWarning(instr.getNode(), instr.getTarget());
		}
		return super.transfer(instr, value);
	}

	/**
	 * if Gamma has an entry for the receiver, use the type in Gamma otherwise,
	 * if receiver is a SimpleName or this, resolve the actual domains using
	 * AnnoDB and put a new entry in Gamma. Ensure that the variables are
	 * uniquely identified.
	 * */
	protected List<DomainP> getReceiverActualDomains(Variable receiver, TACInstruction instr,
			Map<Variable, List<DomainP>> Gamma, QualifiedClassName C) {
		if (Gamma.containsKey(receiver))
			return Gamma.get(receiver);
		if (Utils.isThis(receiver)) {
			List<DomainP> thisFormalDomains = getDomainParamsDecls(C);
			Gamma.put(receiver, thisFormalDomains);
			return thisFormalDomains;
		}
		// handle super - keep the same no of parameters as for this
		// the ones in the tails are ignored
		if (Utils.isSuper(receiver)) {
			List<DomainP> thisFormalDomains = getDomainParamsDecls(C);
			Gamma.put(receiver, thisFormalDomains);
			return thisFormalDomains;
		}

		IVariableBinding varBinding = Utils.getReceiverVarBinding(instr);		
		if (varBinding != null) {						
			return addToGamma(Gamma, varBinding, C);
		}
		return null;
	}

	protected List<DomainP> getArgActualDomains(Variable arg, InvocationInstruction instr,
			Map<Variable, List<DomainP>> Gamma, QualifiedClassName C) {
		if (Gamma.containsKey(arg))
			return Gamma.get(arg);
		if (Utils.isThis(arg)) {
			QualifiedClassName argType = new QualifiedClassName(arg.resolveType(), C);
			List<DomainP> thisFormalDomains = getDomainParamsDecls(argType);
			Gamma.put(arg, thisFormalDomains);
			return thisFormalDomains;
		}
		IVariableBinding varBinding = Utils.getArgVarBinding(arg, instr);

		if (varBinding != null) {			
			return addToGamma(Gamma, varBinding, C);
		}
		return null;
	}

	// TODO: Do we need to take in Gamma?
	// XXX. This is no longer being called.
	// Does not make sense to retrieve formals from the arguments. Retrieve formal domains from the formal parameters on the MethodDeclaration.
	/**
	 * @deprecated DELETE ME. No longer needed.
	 */
	protected List<DomainP> getArgFormalDomains(Variable arg, Map<Variable, List<DomainP>> Gamma, QualifiedClassName C) {
//		if (Gamma.containsKey(arg))
//			return Gamma.get(arg);
//		
		QualifiedClassName argType = new QualifiedClassName(arg.resolveType(), C);
		List<DomainP> thisFormalDomains = getDomainParamsDecls(argType);
		return thisFormalDomains;
	}
	/**
	 * for a store instruction x = y, returns the domain of y
	 * 
	 * @param instr
	 * @param value
	 * @return
	 */
	protected List<DomainP> getSourceActualDomains(StoreInstruction instr, Map<Variable, List<DomainP>> Gamma,
			QualifiedClassName C) {
		Variable sourceOperand = instr.getSourceOperand();
		if (Gamma.containsKey(sourceOperand))
			return Gamma.get(sourceOperand);
		if (Utils.isThis(sourceOperand)) {
			QualifiedClassName sourceType = new QualifiedClassName(sourceOperand.resolveType(), C);
			List<DomainP> thisFormalDomains = getDomainParamsDecls(sourceType);
			Gamma.put(sourceOperand, thisFormalDomains);
			return thisFormalDomains;
		}
		IVariableBinding varBinding = Utils.getSourceVarBinding(instr);

		if (varBinding != null) {
			return addToGamma(Gamma, varBinding, C);
		}
		return null;
	}

	protected List<DomainP> getDeclaredVarDomains(SourceVariable v, Map<Variable, List<DomainP>> Gamma,
			QualifiedClassName C) {
		IVariableBinding varBinding = v.getBinding();
		if (varBinding != null) {
			return addToGamma(Gamma, varBinding, C);
		}
		return null;
	}

	/**
	 * @param instr
	 * @return a more precise type than resolveType if the receiver is this
	 */
	protected ITypeBinding getRecvPreciseClass(Variable recv, OObject O) {
		if (Utils.isThis(recv))
			return O.getQCN().getTypeBinding();
		else
			return recv.resolveType();
	}

	/*
	 * By default, do nothing TODO: LOW. Maybe pass O, from caller's
	 * value.getO();
	 */
	protected void doFields(OOGContext value, OObject O_C, OGraph G, QualifiedClassName qualifiedClassName) {
	}

	/*
	 * By default, do nothing TODO: LOW. Maybe pass O, from caller's
	 * value.getO();
	 */
	protected void creationEdges(OOGContext value, OObject O_C, OGraph G, QualifiedClassName qualifiedClassName, NewObjectInstruction instr) {
	}

	@Override
	public OOGContext transfer(MethodCallInstruction instr, OOGContext value) {
		AnnotationSummary summaryForMethod = pointsToAnalysis.getAnnoDB().getSummaryForMethod(instr.resolveBinding());
		return super.transfer(instr, value);
	}

}