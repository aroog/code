package edu.wayne.ograph.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Assert;

import edu.cmu.cs.aliasjava.Constants;
import edu.cmu.cs.crystal.annotations.ICrystalAnnotation;
import edu.cmu.cs.crystal.tac.eclipse.EclipseTAC;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.ReturnInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.TypeVariable;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.wayne.alias.FieldVariable;
import edu.wayne.auxiliary.Utils;
import edu.wayne.flowgraph.FlowAnnot;
import edu.wayne.flowgraph.FlowAnnotType;
import edu.wayne.flowgraph.FlowGraphEdge;
import edu.wayne.flowgraph.FlowGraphNode;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.IDDictionary;
import edu.wayne.ograph.internal.OOGContext;
import edu.wayne.ograph.internal.OObject;
import edu.wayne.ograph.internal.OwnershipType;
import edu.wayne.ograph.internal.QualifiedClassName;
import edu.wayne.pointsto.PointsToAnalysis;

public class ValueFlowTransferFunctions extends NodesOOGTransferFunctions {

	private Map<String, Set<Variable>> returnVariables;
	// private Map<String, Integer> methodInvocations;
	private static long invocation;

	public ValueFlowTransferFunctions(PointsToAnalysis pointsToAnalysis) {
		super(pointsToAnalysis);
		returnVariables = new Hashtable<String, Set<Variable>>();
		invocation = 0;
	}

	@Override
	public OOGContext transfer(CopyInstruction instr, OOGContext value) {
		QualifiedClassName cthis = getC_THIS(value);
		List<DomainP> actDomains = getTargetActualDomains(instr.getTarget(), instr, value.getGamma(), cthis);
		if (actDomains != null) {
			value.getGamma().put(instr.getTarget(), actDomains);
			if (actDomains.size() > 0) {
				Variable source = instr.getOperand();
				List<DomainP> list = value.getGamma().get(source);

				if (list != null && list.size() > 0) {
					DomainP sourceDomain = list.get(0);
					FlowGraphNode src = new FlowGraphNode(value.getO(), source, sourceDomain);
					FlowGraphNode dst = new FlowGraphNode(value.getO(), instr.getTarget(), actDomains.get(0));
					value.getFG().addInfoFlow(new FlowGraphEdge(src, dst, FlowAnnot.getEmpty()));
				} else {
					// cannot find domain for righthandside
					int debug = 0;
					debug++;
				}
			}
		}
		return super.transfer(instr, value);
	}

	@Override
	public OOGContext transfer(LoadArrayInstruction instr, OOGContext value) {
		// System.out.println("LoadArray: " + instr.getNode() + " O=" +
		// value.getO());
		QualifiedClassName cthis = value.getO().getQCN();
		Variable recv = instr.getSourceArray();
		if (recv.resolveType().isArray()) {
			QualifiedClassName receiverClass = new QualifiedClassName(getRecvPreciseClass(recv, value.getO()), cthis);
			List<DomainP> receiverActualDomains = getReceiverActualDomains(recv, instr, value.getGamma(), cthis);
			if (receiverActualDomains != null) {
				ArrayAccess aa = (ArrayAccess) instr.getNode();
				List<DomainP> formalTKDomains = getArrayAccessActualDomains(aa, cthis);
				if (formalTKDomains != null) {
					ITypeBinding fieldKClass = instr.getSourceArray().resolveType().getElementType();
					value.getGamma().put(instr.getTarget(), formalTKDomains);
					QualifiedClassName labelClass = new QualifiedClassName(fieldKClass, cthis);
					Variable fk = instr.getArrayIndex();
					Variable l = instr.getTarget();
					addValueFlow(value, receiverClass, receiverActualDomains, labelClass, formalTKDomains, fk,  formalTKDomains, l );
				}
			}
		}
		return super.transfer(instr, value);
	}

	@Override
	public OOGContext transfer(LoadFieldInstruction instr, OOGContext value) {
		// System.out.println("FieldRead: " + instr.getNode() + " O=" +
		// value.getO());
		// System.out.println("Gamma: " + value.getGamma());
		if (!instr.isStaticFieldAccess()) {
			OObject o = value.getO();
			QualifiedClassName cthis = o.getQCN();
			Variable recv = instr.getSourceObject();
			if (!recv.resolveType().isArray()) {
				QualifiedClassName receiverClass = new QualifiedClassName(getRecvPreciseClass(recv, o),
						cthis);
				List<DomainP> receiverActualDomains = getReceiverActualDomains(recv, instr, value.getGamma(), cthis);
				Set<OObject> oRecvs = auxLookup(value, o, receiverClass, receiverActualDomains);
				for (OObject or : oRecvs) {								
					TypeVariable that = new TypeVariable(or.getQCN().getTypeBinding());
					if (that != null) {						
						DomainP thatOwner = new DomainP(or.getQCN(), Constants.OWNER);
						FlowGraphNode srcr = new FlowGraphNode(o, recv, receiverActualDomains.get(0));
						FlowGraphNode dstr = new FlowGraphNode(or, that, thatOwner);
						FlowAnnot flowAnnot = FlowAnnot.getEmpty();
						value.getFG().addInfoFlow(new FlowGraphEdge(srcr, dstr, flowAnnot));
					}
				}
				
				if (receiverActualDomains != null) {
					// IN TR: call fields with substitution
					Map<String, List<DomainP>> fieldActualTypes = auxFields(recv, receiverClass, receiverActualDomains);

					// !!! NOT IN TR: call fields without substitution since we
					// call
					// lookup(O_i,...)
					Map<String, OwnershipType> fieldFormalTypes = auxFieldsWithoutSubst(receiverClass);
					OwnershipType oType = fieldFormalTypes.get(instr.getFieldName());
					if (oType != null) {
						List<DomainP> formalTKDomains = oType.getValue();

						List<DomainP> actualTKDomains = fieldActualTypes.get(instr.getFieldName());
						Variable target = instr.getTarget();
						ITypeBinding fieldKClass = target.resolveType();
						value.getGamma().put(target, actualTKDomains);

						QualifiedClassName labelClass = new QualifiedClassName(fieldKClass, cthis);
						Variable fk = getFieldVariable(receiverClass.getTypeBinding(), instr);
						addValueFlow(value, receiverClass, receiverActualDomains, labelClass, formalTKDomains, fk,
								actualTKDomains, target);
					}
				}
			}
		}
		return super.transfer(instr, value);
	}

	@Override
	public OOGContext transfer(MethodCallInstruction instr, OOGContext value) {
		Variable receiver = instr.getReceiverOperand();
		IMethodBinding mb = instr.resolveBinding();
		QualifiedClassName cthis = getC_THIS(value);
		if (receiver != null) {
			// not a static method call
			QualifiedClassName recvClass = new QualifiedClassName(getRecvPreciseClass(receiver, value.getO()), cthis);
			List<DomainP> receiverActualDomains = getReceiverActualDomains(receiver, instr, value.getGamma(), cthis);
			OObject o = value.getO();
			IMethodBinding mb1 = instr.resolveBinding();
			FlowAnnot flowAnnot = new FlowAnnot(getInvocation(o,instr), FlowAnnotType.CALL);
			//mb1 = mb1.getMethodDeclaration();

			Set<OObject> oRecvs = auxLookup(value, o, recvClass, receiverActualDomains);
			for (OObject or : oRecvs) {
				TypeDeclaration typeDecl = pointsToAnalysis.getTypeDecl(or.getC());	
				//TODO: XXX: replace such that we do not create a separate variable instance at every pass. 
				TypeVariable that = new TypeVariable(or.getQCN().getTypeBinding());
				if (that != null) {
					QualifiedClassName tThat = new QualifiedClassName(that.resolveType(), cthis);
					DomainP thatOwner = new DomainP(tThat, Constants.OWNER);
					FlowGraphNode srcr = new FlowGraphNode(o, receiver, receiverActualDomains.get(0));
					FlowGraphNode dstr = new FlowGraphNode(or, that, thatOwner);
					
					value.getFG().addInfoFlow(new FlowGraphEdge(srcr, dstr, flowAnnot));
				}
				
				List<Variable> fparams = getFormalParams(mb1, typeDecl);				 
				List<Variable> argOperands = instr.getArgOperands();
				for (Variable arg : argOperands) {
					List<DomainP> argDomains = getArgActualDomains(arg, instr, value.getGamma(), cthis);
					boolean condition =argDomains != null 
							&& argDomains.size()>0
							&& fparams != null 
							&& fparams.size()==argOperands.size() 
							&& (!arg.resolveType().isPrimitive());
					if (condition) {
						Variable fParam = fparams.get(argOperands.indexOf(arg));
						List<DomainP> formalDomains = getDomainsOfFormalParams(mb1, typeDecl, cthis, argOperands.indexOf(arg));
						if (formalDomains != null && argDomains.size() > 0 && formalDomains.size() > 0) {
							FlowGraphNode src = new FlowGraphNode(o, arg, argDomains.get(0));
							FlowGraphNode dst = new FlowGraphNode(or, fParam, formalDomains.get(0));
							value.getFG().addInfoFlow(new FlowGraphEdge(src, dst, flowAnnot));
						}
						else{
							this.pointsToAnalysis.addWarning(instr.getNode(),
									"Cannot find domains for " + fParam);							
						}
					} else {
						if (!arg.resolveType().isPrimitive())
							this.pointsToAnalysis.addWarning(instr.getNode(),
									"Cannot find corresponding formal param for " + arg);
					}
				}
				if (!mb.getReturnType().isPrimitive()) {
					List<DomainP> methFormalRetDoms = auxMTypeRetWithoutSubst(mb1, recvClass);
					List<DomainP> methActualRetDoms = auxMTypeRet(mb1, receiver, recvClass, receiverActualDomains);
					value.getGamma().put(instr.getTarget(), methActualRetDoms);
					Set<Variable> retVars = getRetVar(mb1);
					if ((methFormalRetDoms != null && methFormalRetDoms.size() > 0)
							&& (methActualRetDoms != null && methActualRetDoms.size() > 0))
						for (Variable ret : retVars) {
							FlowGraphNode src = new FlowGraphNode(or, ret, methFormalRetDoms.get(0));
							FlowGraphNode dst = new FlowGraphNode(o, instr.getTarget(), methActualRetDoms.get(0));
							// TODO: FIXME FlowAnnot closei = new
							FlowAnnot flowAnnot1 = new FlowAnnot(getInvocation(o,instr), FlowAnnotType.RETURN);
							value.getFG().addInfoFlow(new FlowGraphEdge(src, dst, flowAnnot1));
						}
				} else {
					// do nothing for primitive types
				}
			}
		}
		return super.transfer(instr, value);
	}




//	private void addInvocation() {
//		invocation++;
//	}



	@Override
	public OOGContext transfer(ReturnInstruction instr, OOGContext value) {
		Variable returnedVariable = instr.getReturnedVariable();
		ASTNode node = instr.getNode();
		if (node instanceof ReturnStatement) {
			ReturnStatement rs = (ReturnStatement) node;
			MethodDeclaration md = getMethodDeclaration(rs);
			addToCachedDeclaration(md.resolveBinding(), returnedVariable);
		}
		return super.transfer(instr, value);
	}

	private void addToCachedDeclaration(IMethodBinding resolveBinding, Variable returnedVariable) {
		String key = resolveBinding.getKey();
		if (returnVariables.containsKey(key)) {
			returnVariables.get(key).add(returnedVariable);
		} else {
			Set<Variable> vars = new HashSet<Variable>();
			vars.add(returnedVariable);
			returnVariables.put(key, vars);
		}
	}

	private MethodDeclaration getMethodDeclaration(ASTNode rs) {
		ASTNode parent = rs.getParent();
		if (parent instanceof MethodDeclaration)
			return (MethodDeclaration) parent;
		else
			return getMethodDeclaration(parent);
	}

	@Override
	public OOGContext transfer(StoreFieldInstruction instr, OOGContext value) {
		if (!Utils.isNullAssignment(instr)) {
			QualifiedClassName cthis = value.getO().getQCN();
			Variable recv = instr.getDestinationObject();
			QualifiedClassName recvClass = new QualifiedClassName(getRecvPreciseClass(recv, value.getO()), cthis);
			List<DomainP> recvActualDomains = getReceiverActualDomains(recv, instr, value.getGamma(), cthis);
			if (recvActualDomains != null) {
				Map<String, OwnershipType> auxFieldsWithoutSubst = auxFieldsWithoutSubst(recvClass);
				OwnershipType fieldType = auxFieldsWithoutSubst.get(instr.getFieldName());

				List<DomainP> rDomains = getSourceActualDomains(instr, value.getGamma(), cthis);
				// TODO:XXX fix me, what if the right hand side is a SimpleName?
				if (fieldType != null && fieldType.getValue() != null && rDomains!=null) {
					Variable fk = new FieldVariable(instr.resolveFieldBinding());
					Variable r = instr.getSourceOperand();
					
					if (!addValueFlow2(value, recvClass, recvActualDomains,
							new QualifiedClassName(fieldType.getKey(), cthis), fieldType.getValue(), fk, r, rDomains)){
						pointsToAnalysis.addWarning(instr.getNode(), MISSING_DOMAINS);
					}
				}
			}
		}
		return super.transfer(instr, value);
	}

	@Override
	public OOGContext transfer(SourceVariableDeclaration instr, OOGContext value) {
		QualifiedClassName declaringClass = value.getO().getQCN();
		if (declaringClass != null) {
			SourceVariable v = instr.getDeclaredVariable();
			getDeclaredVarDomains(v, value.getGamma(), declaringClass);
		}
		return super.transfer(instr, value);
	}

	private Variable getFieldVariable(ITypeBinding typeBinding, LoadFieldInstruction instr) {
		Variable ffield = null;
		IVariableBinding field = instr.resolveFieldBinding();
		return new FieldVariable(field);
		// BodyDeclaration bd =
		// Utils.getEnclosingFieldMethodDeclaration(instr.getNode());
		// if (bd instanceof MethodDeclaration) {
		// MethodDeclaration md = (MethodDeclaration) bd;
		// EclipseTAC methodTAC =
		// this.pointsToAnalysis.getSavedInput().getMethodTAC(md);
		// return methodTAC.sourceVariable(field);
		// } else {
		// this.pointsToAnalysis.getTypeDecl(Type.createFrom());
		// }
		//
		// return ffield;
	}

	private void addValueFlow(OOGContext value, QualifiedClassName receiverClass, List<DomainP> receiverActualDomains,
			QualifiedClassName labelClass, List<DomainP> formalTKDomains, Variable fk, List<DomainP> actualTKDomains, Variable l) {
		if (formalTKDomains != null && formalTKDomains.size() > 0) {
			OObject o = value.getO();
			Set<OObject> oRecvs = auxLookup(value, o, receiverClass, receiverActualDomains);
			for (OObject or : oRecvs) {
				FlowGraphNode src = new FlowGraphNode(or, fk, formalTKDomains.get(0));
				FlowGraphNode dst = new FlowGraphNode(o, l, actualTKDomains.get(0));
				value.getFG().addInfoFlow(new FlowGraphEdge(src, dst, FlowAnnot.getEmpty()));
			}
		} else {
			int debug = 0;
			debug++;
		}
	}

	private Set<Variable> getRetVar(IMethodBinding mb) {
		Set<Variable> set = this.returnVariables.get(mb.getKey());
		if (set != null)
			return Collections.unmodifiableSet(set);
		else
			return Collections.unmodifiableSet(new HashSet<Variable>());
	}

	private boolean addValueFlow2(OOGContext value, QualifiedClassName recvClass, List<DomainP> recvActualDomains,
			QualifiedClassName labelQCN, List<DomainP> labelActualDomains, Variable fk, Variable r, List<DomainP> rDomains) {
		if (rDomains.isEmpty()) return false;
		if (labelActualDomains.isEmpty()) return false;
		OObject o = value.getO();
		Set<OObject> oRecvs = auxLookup(value, o, recvClass, recvActualDomains);
		for (OObject or : oRecvs) {
			FlowGraphNode src = new FlowGraphNode(o, r, rDomains.get(0));
			FlowGraphNode dst = new FlowGraphNode(or, fk, labelActualDomains.get(0));
			value.getFG().addInfoFlow(new FlowGraphEdge(src, dst, FlowAnnot.getSTAR()));
		}
		return true;
	}
}
