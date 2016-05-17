package edu.wayne.ograph.analysis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import util.TraceabilityEntry;
import ast.AstNode;
import ast.BaseTraceability;
import ast.EdgeDFTraceability;
import ast.MethodInvocation;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.wayne.auxiliary.Utils;
import edu.wayne.ograph.EdgeFlag;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.ODFEdge;
import edu.wayne.ograph.internal.ODFEdgeKey;
import edu.wayne.ograph.internal.OEdge;
import edu.wayne.ograph.internal.OGraph;
import edu.wayne.ograph.internal.OOGContext;
import edu.wayne.ograph.internal.OObject;
import edu.wayne.ograph.internal.OwnershipType;
import edu.wayne.ograph.internal.QualifiedClassName;
import edu.wayne.ograph.traceability.TraceUtils;
import edu.wayne.pointsto.PointsToAnalysis;

public class DfOOGTransferFunctions extends ValueFlowTransferFunctions {
	private static final String IMPORT_EDGE_NOT_ADDED = "IMPORT edge not added: ";
	private static final String EXPORT_EDGE_NOT_ADDED = "EXPORT edge not added: ";
	private static final String CANNOT_ADD_EXPORT_EDGE_FOR_EXPRESSION = "cannot add export edge for expression";

	public DfOOGTransferFunctions(PointsToAnalysis pointsToAnalysis) {
		super(pointsToAnalysis);
	}

	public DfOOGTransferFunctions(PointsToAnalysis pointsToAnalysis, boolean withFlow) {
		super(pointsToAnalysis);
		collectFlow(withFlow);
	}
	
	@Override
	public OOGContext transfer(LoadFieldInstruction instr, OOGContext value) {
		// System.out.println("FieldRead: " + instr.getNode() + " O=" +
		// value.getO());
		// System.out.println("Gamma: " + value.getGamma());
		if (!instr.isStaticFieldAccess()) {
			QualifiedClassName cthis = value.getO().getQCN();
			Variable recv = instr.getSourceObject();
			if (!recv.resolveType().isArray()) {
				QualifiedClassName receiverClass = new QualifiedClassName(getRecvPreciseClass(recv, value.getO()),
						cthis);
				List<DomainP> receiverActualDomains = getReceiverActualDomains(recv, instr, value.getGamma(), cthis);
				if(receiverActualDomains!=null){
					// XXX. Could be a side-effect to calling this?
					// XXX. This was not previously called. 
					// IN TR: call fields with substitution
					Map<String, List<DomainP>> fieldActualTypes = auxFields(recv, receiverClass, receiverActualDomains);

					// !!! NOT IN TR: call fields without substitution since we call
					// lookup(O_i,...)
					Map<String, OwnershipType> fieldFormalTypes = auxFieldsWithoutSubst(receiverClass);
					OwnershipType oType = fieldFormalTypes.get(instr.getFieldName());
					if (oType!=null){
						List<DomainP> formalTKDomains = oType.getValue();

						// XXX. Dead store to local...
						List<DomainP> actualTKDomains = fieldActualTypes.get(instr.getFieldName());
						Variable target = instr.getTarget();
						ITypeBinding fieldKClass = target.resolveType();
//						value.getGamma().put(target, actualTKDomains);
						TraceabilityEntry tLink = traceabilityFactory.createTraceability(instr, value.getO());
						//addValueFlowFieldRead()
						QualifiedClassName labelClass = new QualifiedClassName(fieldKClass, cthis);						
						if (!auxImport(value, receiverClass, receiverActualDomains, labelClass,	formalTKDomains, tLink))
							this.pointsToAnalysis.addWarning(instr.getNode(), IMPORT_EDGE_NOT_ADDED);
						else{
							//do nothing;
						}
					}
					else{
						// do nothing. The field has no annotations, may be of a primitive type
					}
				}
				else{
					returnWarning(instr.getNode(), recv);
				}
			} else {
				// TODO: Handle arrays, i.e., array.length
			}
		} else {
			// TODO: handle static fields
		}
		return super.transfer(instr, value);
	}




	/**
	 * @param value
	 *            - DO, DD, DE
	 * @param srcClass
	 *            - Tsrc = srcClass<srcDomains>
	 * @param srcDomains
	 * @param labelClass
	 *            - Tlabel = labelClass<labelDomains>
	 * @param labelDomains
	 */
	private boolean auxImport(OOGContext value, QualifiedClassName srcClass, List<DomainP> srcDomains,
			QualifiedClassName labelClass, List<DomainP> labelDomains, TraceabilityEntry path) {
		boolean success = true;
		Set<OObject> srcLookup = auxLookup(value, value.getO(), srcClass, srcDomains);
		// uncomment to add more warnings
		success = srcLookup.size() > 0;
		OGraph G = value.getG();
		for (OObject O_i : srcLookup) {
			Set<OObject> labelLookup = auxLookup(value, O_i, labelClass, labelDomains);
			for (OObject O_j : labelLookup) {
				ODFEdgeKey oEdgeKey = new ODFEdgeKey(O_i, value.getO(), O_j, EdgeFlag.Import);
				
				OEdge oEdge = G.getOEdge(oEdgeKey);
				if (oEdge == null) {
					oEdge = new ODFEdge(oEdgeKey);
					G.addOEdge(oEdge);
				}
				
				oEdge.addToASTNodePath(value.getExpressionStack(), path, value.getO());
				// oEdge.setStringID(path.toString());
			}
			// HACK: NOT IN TR
			// XXX. Nullcheck of labelClass previously dereferenced 
			if (labelLookup.size() == 0 && !labelClass.getTypeBinding().isPrimitive())
				if (labelDomains == null || labelDomains.size() == 0 || isShared(labelDomains.get(0))
						&& labelClass != null) {
					// TODO: HIGH. Revisit commented out code. Code smell!
					// List<ODomain> listD = new ArrayList<ODomain>();
					// listD.add(OOGContext.getDShared());
					// OObject O_j = new OObject(labelClass,listD);
					// ODFEdge oEdge = new ODFEdge(O_i, value.getO(), O_j,
					// EdgeFlag.Import );
					// oEdge.addToASTNodePath(value.getExpressionStack(), path,
					// value.getO());
					// //oEdge.setStringID(path.toString());
					// value.getG().getDE().add(oEdge);
				} else {
					//System.out.println(IMPORT_EDGE_NOT_ADDED + O_i.getTypeName() + " -> "
					//		+ value.getO().getTypeName() + " Label: " + labelClass.getActualName() + " " + path.getSecond().expression);
					return success = false;
				}
					
		}
		return success;
	}

	/**
	 * @param value
	 *            - DO, DD, DE
	 * @param dstClass
	 *            - Tsrc = srcClass<srcDomains>
	 * @param dstDomains
	 * @param labelClass
	 *            - Tlabel = labelClass<labelDomains>
	 * @param labelDomains
	 */
	private boolean auxExport(OOGContext value, QualifiedClassName dstClass, List<DomainP> dstDomains,
			QualifiedClassName labelClass, List<DomainP> labelDomains, TraceabilityEntry path) {
		boolean success = true; 
		if (value.getO() != null) {
			Set<OObject> recvLookup = auxLookup(value, value.getO(), dstClass, dstDomains);
			// uncomment to get more warnings, when the receiver object was not found
			success = recvLookup.size() > 0;
			OGraph G = value.getG();
			for (OObject O_i : recvLookup) {
				Set<OObject> labelLookup = auxLookup(value, value.getO(), labelClass, labelDomains);
				for (OObject O_j : labelLookup) {
					// do not add value.getO() since value.getO() <>
					// G.getDO.get(indexof(value.getO())
					ODFEdgeKey oEdgeKey = new ODFEdgeKey(value.getO(), O_i, O_j, EdgeFlag.Export);

					OEdge oEdge = G.getOEdge(oEdgeKey);
					if (oEdge == null ) {
						oEdge = new ODFEdge(oEdgeKey);
						G.addOEdge(oEdge);
					}
					oEdge.addToASTNodePath(value.getExpressionStack(), path, value.getO());
					// oEdge.setStringID(path.toString());
				}
				// XXX. Nullcheck of labelClass previously dereferenced 
				if (labelLookup.size() == 0 && !labelClass.getTypeBinding().isPrimitive()) {
					if (labelDomains == null || labelDomains.size() == 0 || isShared(labelDomains.get(0))
							&& labelClass != null) {
						// TODO: HIGH. Revisit commented out code. Code smell!
						// List<ODomain> listD = new ArrayList<ODomain>();
						// listD.add(OOGContext.getDShared());
						// OObject O_j = new OObject(labelClass,listD);
						// ODFEdge oEdge = new ODFEdge(value.getO(), O_i, O_j,
						// EdgeFlag.Export );
						// oEdge.addToASTNodePath(value.getExpressionStack(),
						// path, value.getO());
						// //oEdge.setStringID(path.toString());
						// value.getG().getDE().add(oEdge);
					} else {
						//System.out.println(EXPORT_EDGE_NOT_ADDED + value.getO() + " -> " + O_i + " Label: "
						//		+ labelClass + " " + path.getSecond().expression);
						success = false;
					}
					// System.out.println(value.getG().getDD());
					// lookup(value, value.getO(), labelClass, labelDomains);
				}
			}
		}
		return success;
	}

	@Override
	public OOGContext transfer(StoreFieldInstruction instr, OOGContext value) {
		// System.out.println("FieldWrite: " + instr + " O=" + value.getO());
		// System.out.println("Gamma: " + value.getGamma());
		if (!Utils.isNullAssignment(instr)){
			QualifiedClassName cthis = value.getO().getQCN();
			Variable recv = instr.getDestinationObject();
			QualifiedClassName recvClass = new QualifiedClassName(getRecvPreciseClass(recv, value.getO()), cthis);
			List<DomainP> recvActualDomains = getReceiverActualDomains(recv, instr, value.getGamma(), cthis);
			if (recvActualDomains!=null){

				Variable sourceOperand = instr.getSourceOperand();
				ITypeBinding labelClass = sourceOperand.resolveType();
				QualifiedClassName labelQCN = new QualifiedClassName(labelClass, cthis);
				List<DomainP> labelActualDomains = getSourceActualDomains(instr, value.getGamma(), cthis);
				// TODO:XXX fix me, what if the right hand side is a SimpleName?
				if (labelActualDomains != null) {
					TraceabilityEntry tLink = traceabilityFactory.createTraceability(instr, value.getO());
					if (!auxExport(value, recvClass, recvActualDomains, labelQCN, labelActualDomains, tLink))
						this.pointsToAnalysis.addWarning(instr.getNode(), EXPORT_EDGE_NOT_ADDED);
					else{
						//do nothing
					}
				}
				else{
					returnWarning(instr.getNode(), sourceOperand);
				}
			}
			else{
				returnWarning(instr.getNode(),recv);
			}
		}
		return super.transfer(instr, value);
	}



	@Override
	public OOGContext transfer(MethodCallInstruction instr, OOGContext value) {
		// System.out.println("MethodCall: " + instr + " O=" + value.getO());
		// System.out.println(value.getGamma().keySet());
		Variable receiver = instr.getReceiverOperand();
		IMethodBinding mb = instr.resolveBinding();
		
		
		
		QualifiedClassName cthis = getC_THIS(value);
		if (receiver != null) {
			// not a static method call
			QualifiedClassName recvClass = new QualifiedClassName(getRecvPreciseClass(receiver, value.getO()), cthis);
			List<DomainP> receiverActualDomains = getReceiverActualDomains(receiver, instr, value.getGamma(), cthis);
			TraceabilityEntry tLink = traceabilityFactory.createTraceability(instr, value.getO());
			BaseTraceability tEntry = tLink.getSecond();
			
			// XXX. XXX. Check this. Why relying on traceability information during analysis?
			MethodInvocation methInvk = null;
	        if (tEntry instanceof EdgeDFTraceability) {
	        	EdgeDFTraceability dfEntry = (EdgeDFTraceability) tEntry;
	        	AstNode methInvkNode = dfEntry.getExpression();
	        	
	        	if(methInvkNode instanceof MethodInvocation) {
	        		methInvk = (MethodInvocation)methInvkNode;
	        	}
	        }
			
//			addValueFlowMInvk(value,instr, recvClass, receiverActualDomains);
			if (receiverActualDomains!=null){
				// Add to traceability the recv. actuals
				if (methInvk != null) {
					TraceUtils.setRecvAnnotation(methInvk, receiverActualDomains);
				}
				
				List<Variable> argOperands = instr.getArgOperands();
				int ii = 0;
				for (Variable arg : argOperands) {
					// TODO: XXX: resolve type for arguments. see add(l)
					List<DomainP> argDomains = getArgActualDomains(arg, instr, value.getGamma(), cthis);
					if (argDomains!=null){

						// Add to traceability the arg actuals 
						if (methInvk != null ) {
							TraceUtils.setArgAnnotations(methInvk, argOperands, argDomains, ii);
						}

						QualifiedClassName labelClass = new QualifiedClassName(arg.resolveType(), cthis);						
						if (!auxExport(value, recvClass, receiverActualDomains, labelClass, argDomains, tLink))
							this.pointsToAnalysis.addWarning(instr.getNode(), EXPORT_EDGE_NOT_ADDED);
						else{
							//do nothing;
						}
						
						ii++;
					}
					else{
						returnWarning(instr.getNode(), arg);
					}
				}
				if (!mb.getReturnType().isPrimitive()) {
					// method does not return a primitive type (including void)
					ITypeBinding labelTRclass = mb.getReturnType();
					QualifiedClassName labelClass = new QualifiedClassName(labelTRclass, cthis);

//					// IN TR:
					List<DomainP> methActualRetDoms = auxMTypeRet(mb, receiver, recvClass,
							receiverActualDomains);
					
					// !!!NOT IN TR: call mtype without substitution
					List<DomainP> methFormalRetDoms = auxMTypeRetWithoutSubst(mb, recvClass);
					
					// Add to traceability the ret. actuals
					if (methInvk != null ) {
						TraceUtils.setRetAnnotation(methInvk, methActualRetDoms);
					}
					
					if (!auxImport(value, recvClass, receiverActualDomains, labelClass, methFormalRetDoms, tLink))
						this.pointsToAnalysis.addWarning(instr.getNode(), IMPORT_EDGE_NOT_ADDED);
					else{
						//do nothing;
					}
					// System.out.println("Gamma: " + value.getGamma());
					// TODO: add import edges
				}
			}
			else 
				returnWarning(instr.getNode(), receiver);
			// for temp = x.m(a1, a2, a3, ...) Gamma should already have a1,a2,
			// a3 BAD ASSUMPTION
			// if not, call mtypeArg
			// TODO: add export edges			
		}
		// TODO: LOW: do something about static method calls.

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
			if (receiverActualDomains!=null){
				ArrayAccess aa = (ArrayAccess) instr.getNode();
				List<DomainP> formalTKDomains = getArrayAccessActualDomains(aa, cthis);
				if (Utils.getArrayAccessVarBinding(aa)==null){
					int debug = 0;
					debug++;
					Expression array = aa.getArray();
					Variable accessedArrayOperand = instr.getAccessedArrayOperand();
					List<DomainP> list = value.getGamma().get(accessedArrayOperand);
				}
				if (formalTKDomains != null) {
					ITypeBinding fieldKClass = instr.getSourceArray().resolveType().getElementType();
					//value.getGamma().put(instr.getTarget(), formalTKDomains);
					TraceabilityEntry tLink = traceabilityFactory.createTraceability(instr, value.getO());
					QualifiedClassName labelClass = new QualifiedClassName(fieldKClass, cthis);				
					if (!auxImport(value, receiverClass, receiverActualDomains, labelClass, formalTKDomains, tLink))
						this.pointsToAnalysis.addWarning(instr.getNode(), IMPORT_EDGE_NOT_ADDED);
					else{
						//do nothing;
					}
				} else {
					// cannot find formal domains for the array
					throw new IllegalStateException("Domains not found for the array: " + instr.getNode());
				}
			} else {
				// this should never happen
			}
		}
		else{
			//cannot find actual domains for the receiver
			returnWarning(instr.getNode(),recv);
		}
		return super.transfer(instr, value);
	}


	@Override
	public OOGContext transfer(StoreArrayInstruction instr, OOGContext value) {
		// System.out.println("StoreArray: " + instr.getNode() + " O=" +
		// value.getO());
		QualifiedClassName cthis = value.getO().getQCN();
		Variable recv = instr.getDestinationArray();
		if (recv.resolveType().isArray() && !Utils.isNullAssignment(instr)) {
			QualifiedClassName receiverClass = new QualifiedClassName(getRecvPreciseClass(recv, value.getO()), cthis);
			List<DomainP> receiverActualDomains = getReceiverActualDomains(recv, instr, value.getGamma(), cthis);
			if (receiverActualDomains!=null){
				ITypeBinding labelClass = instr.getSourceOperand().resolveType();
				QualifiedClassName labelQCN = new QualifiedClassName(labelClass, cthis);
				List<DomainP> labelActualDomains = getSourceActualDomains(instr, value.getGamma(), cthis);
				if (labelActualDomains != null) {
					TraceabilityEntry tLink = traceabilityFactory.createTraceability(instr, value.getO());
					if( !auxExport(value, receiverClass, receiverActualDomains, labelQCN, labelActualDomains, tLink))
						this.pointsToAnalysis.addWarning(instr.getNode(), EXPORT_EDGE_NOT_ADDED);
					else{
						//do nothing;
					}
				} else {
					// do nothing cannot find formal domains for the array
					returnWarning(instr.getNode(),instr.getSourceOperand());
					// IllegalStateException("Domains not found for the array: "+instr.getNode());
				}
			}
			else{
				returnWarning(instr.getNode(),recv);
			}
		} else {
			// this should never happen
		}
		return super.transfer(instr, value);
	}

}
