package edu.wayne.ograph.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;

import util.TraceabilityEntry;
import ast.ArrayCreation;
import ast.ArrayInit;
import ast.ArrayRead;
import ast.ArrayWrite;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import ast.EdgeDFTraceability;
import ast.EdgePTTraceability;
import ast.FieldAccess;
import ast.FieldDeclaration;
import ast.FieldWrite;
import ast.LoadLiteral;
import ast.MethodInvocation;
import ast.ObjectTraceability;
import ast.Type;
import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.TACFieldAccess;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.wayne.auxiliary.Utils;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.OObject;
import edu.wayne.ograph.internal.OwnershipType;
import edu.wayne.ograph.internal.QualifiedClassName;
import edu.wayne.pointsto.PointsToAnalysis;

// TODO: HIGH. XXX. Consolidate with one in MiniASTOOG, to avoid code duplication
public class TraceabilityFactory {

	private static TraceabilityFactory s_instance = null;
	private PointsToAnalysis ptAnalysis;
	
	private Map<TACInstruction, BaseTraceability> mapNodes = new HashMap<TACInstruction, BaseTraceability>();
	
	private TraceabilityFactory(PointsToAnalysis pointsToAnalysis) {
		this.ptAnalysis = pointsToAnalysis;
	}

	public static TraceabilityFactory getInstance(PointsToAnalysis pointsToAnalysis){
		if (s_instance == null)
			s_instance = new TraceabilityFactory(pointsToAnalysis);
		return s_instance;
	}
	
	/**
	 * @param instr
	 * @param iTypeBinding
	 * @param oObject
	 * @return
	 */
	public TraceabilityEntry createTraceability(NewObjectInstruction instr, ITypeBinding iTypeBinding,
			OObject oObject) {
		BaseTraceability objTrace = mapNodes.get(instr);
		if (objTrace == null) {
			ClassInstanceCreation cic = createClassInstanceCreation(instr, iTypeBinding);
			cic.enclosingDeclaration = adapter.TraceabilityFactory.getEnclosingFieldMethodDeclaration(instr.getNode());
			objTrace = new ObjectTraceability(cic);
			// TODO: maybe remove = derived information
			// objTrace.expressionType = cic.typeDeclaration.type;

			// TORAD: TODO: HIGH. Populate the annotations on cic
			// cic.annotation = AnnotationInfo.Shared;
			try {
				cic.annotation = Utils.getp_i2(instr, ptAnalysis.getAnnoDB());
			}
			catch (IllegalStateException ex) {
				// for some reason a new expression does not have a corresponding annotation, just add Shared.
				cic.annotation = AnnotationInfo.Shared.toString();
			}
			// DEBUG: not used.
			// cic.getAnnotation();
			// cic.annotation.getAnnotation(); // Owning domain
			// cic.getParameters();
			// cic.annotation.getParameters(); // the remaining parameters
			mapNodes.put(instr, objTrace);
		}
		return new TraceabilityEntry(oObject.getO_id(), objTrace);
	}

	/**
	 * creates traceability for field read
	 * */
	public TraceabilityEntry createTraceability(LoadFieldInstruction instr, OObject o) {
		BaseTraceability edgeTrace = mapNodes.get(instr);
		if (edgeTrace == null) {
			FieldAccess fieldAccess = createFieldAccess(instr);
			fieldAccess.enclosingDeclaration = adapter.TraceabilityFactory.getEnclosingFieldMethodDeclaration(instr.getNode());
			edgeTrace = new EdgeDFTraceability(fieldAccess);
			mapNodes.put(instr, edgeTrace);
		}
		return new TraceabilityEntry(o.getO_id(), edgeTrace);
	}

	public TraceabilityEntry createTraceability(MethodCallInstruction instr, OObject o) {
		BaseTraceability edgeTrace = mapNodes.get(instr);
		if (edgeTrace == null) {
			MethodInvocation methInvocation = createMethodInvocation(instr);
			methInvocation.enclosingDeclaration = adapter.TraceabilityFactory.getEnclosingFieldMethodDeclaration(instr.getNode());
			edgeTrace = new EdgeDFTraceability(methInvocation);
			mapNodes.put(instr,edgeTrace);
		}
		return new TraceabilityEntry(o.getO_id(), edgeTrace);
	}

	public TraceabilityEntry createTraceability(StoreFieldInstruction instr, OObject o) {
		BaseTraceability edgeTrace = mapNodes.get(instr);
		if (edgeTrace == null) {
			FieldWrite fieldWrite = createFieldWrite(instr);
			fieldWrite.enclosingDeclaration = adapter.TraceabilityFactory.getEnclosingFieldMethodDeclaration(instr.getNode());
			edgeTrace = new EdgeDFTraceability(fieldWrite);
			mapNodes.put(instr, edgeTrace);
		}
		return new TraceabilityEntry(o.getO_id(), edgeTrace);
	}

//	public TraceabilityEntry createTraceability(LoadLiteralInstruction instr, OObject o) {
//		BaseTraceability objTrace = mapNodes.get(instr);
//		if (objTrace == null) {
//			objTrace = new ObjectTraceability();
//			// HACK:
////			objTrace.enclosingDeclaration = adapter.TraceabilityFactory.getEnclosingFieldMethodDeclaration(instr
////					.getNode());
////			objTrace.enclosingType = adapter.TraceabilityFactory.getEnclosingTypeDeclaration(instr.getNode());
//			// ClassInstanceCreation cic = createClassInstanceCreation(instr);
//			// objTrace.expression = cic;
//			// // TODO: maybe remove = derived information
//			// objTrace.expressionType = cic.typeDeclaration.type;
//			// return new Pair<IObject, BaseTraceability>(o, objTrace);
//			// TORAD: FIX ME!
//			mapNodes.put(instr, objTrace);
//		}
//		return new TraceabilityEntry(o.getO_id(), objTrace);
//	}
	
	public TraceabilityEntry createTraceability(LoadLiteralInstruction instr, OObject o) {
		BaseTraceability edgeTrace = mapNodes.get(instr);
		if (edgeTrace == null) {
			LoadLiteral loadLiteral = createLoadLiteral(instr);
			edgeTrace = new ObjectTraceability(loadLiteral);
			mapNodes.put(instr, edgeTrace);
		}
		return new TraceabilityEntry(o.getO_id(), edgeTrace);
	}

	
	private LoadLiteral createLoadLiteral(LoadLiteralInstruction instr) {
		LoadLiteral loadLiteral = LoadLiteral.createFrom(instr.getNode());
		loadLiteral.complexExpression = instr.getNode().toString();
		return loadLiteral;
	}	

	private ArrayWrite createArrayWrite(StoreArrayInstruction instr) {
		ArrayWrite arrayWrite = ArrayWrite.createFrom(instr.getNode());
		arrayWrite.complexExpression = instr.getNode().toString();
		return arrayWrite;
	}	

	private ArrayRead createArrayRead(LoadArrayInstruction instr) {
		ArrayRead arrayRead = ArrayRead.createFrom(instr.getNode());
		arrayRead.complexExpression = instr.getNode().toString();
		return arrayRead;
	}
	
	private ArrayCreation createArrayCreation(NewArrayInstruction instr) {
		ArrayCreation arrayCreation = ArrayCreation.createFrom(instr.getNode());
		arrayCreation.complexExpression = instr.getNode().toString();
		return arrayCreation;
	}
	
	private ArrayInit createArrayInit(ArrayInitInstruction instr) {
		ArrayInit arrayInit = ArrayInit.createFrom(instr.getNode());
		arrayInit.complexExpression = instr.getNode().toString();
		return arrayInit;
	}
	
	
	public TraceabilityEntry createTraceability(StoreArrayInstruction instr, OObject o) {
		BaseTraceability edgeTrace = mapNodes.get(instr);
		if (edgeTrace == null) {
			ArrayWrite arrayWrite = createArrayWrite(instr);
			edgeTrace = new EdgeDFTraceability(arrayWrite);
			mapNodes.put(instr, edgeTrace);
		}
		return new TraceabilityEntry(o.getO_id(), edgeTrace);
	}

	
	public TraceabilityEntry createTraceability(ArrayInitInstruction instr, OObject o) {
		BaseTraceability edgeTrace = mapNodes.get(instr);
		if (edgeTrace == null) {
			ArrayInit arrayInit = createArrayInit(instr);
			edgeTrace = new EdgeDFTraceability(arrayInit);
			mapNodes.put(instr, edgeTrace);
		}
		return new TraceabilityEntry(o.getO_id(), edgeTrace);
	}

	// XXX. Should this not be an ObjectTraceability?
	public TraceabilityEntry createTraceability(NewArrayInstruction instr, OObject o) {
		BaseTraceability edgeTrace = mapNodes.get(instr);
		if (edgeTrace == null) {
			ArrayCreation arrayCreation = createArrayCreation(instr);
			edgeTrace = new EdgeDFTraceability(arrayCreation);
			mapNodes.put(instr, edgeTrace);
		}
		return new TraceabilityEntry(o.getO_id(), edgeTrace);
	}

	public TraceabilityEntry createTraceability(LoadArrayInstruction instr, OObject o) {
		BaseTraceability edgeTrace = mapNodes.get(instr);
		if (edgeTrace == null) {
			ArrayRead arrayRead = createArrayRead(instr);
			edgeTrace = new EdgeDFTraceability(arrayRead);
			mapNodes.put(instr, edgeTrace);
		}
		return new TraceabilityEntry(o.getO_id(), edgeTrace);
	}

	/**
	 * @param instr
	 * @param iTypeBinding
	 * @return
	 */
	public ast.ClassInstanceCreation createClassInstanceCreation(NewObjectInstruction instr, ITypeBinding iTypeBinding) {
		ast.ClassInstanceCreation cic = ast.ClassInstanceCreation.createFrom(instr.getNode());
		cic.complexExpression = instr.getNode().toString();
		ast.TypeDeclaration tdtemp = Utils.getTypeDeclaration(iTypeBinding);
		cic.typeDeclaration = tdtemp;
		if (cic.typeDeclaration != null) {
			cic.typeDeclaration.setDomains(Utils.getDomainDecls(iTypeBinding, ptAnalysis.getAnnoDB()));
			cic.typeDeclaration.setParameters(Utils.getDomainParamsDecl2(iTypeBinding, ptAnalysis.getAnnoDB()));
		}
		ast.Type type = Type.createFrom(iTypeBinding);
		cic.type = type;
		if (cic.typeDeclaration == null) {
			// attempt to construct from binding
			// XXX. Null check of td...previously dereferenced
			ast.TypeDeclaration td = ast.TypeDeclaration.createFrom(iTypeBinding);
			td.setDomains(Utils.getDomainDecls(iTypeBinding, ptAnalysis.getAnnoDB()));
			td.setParameters(Utils.getDomainParamsDecl2(iTypeBinding, ptAnalysis.getAnnoDB()));
			if (td != null)
				cic.typeDeclaration = td;
			else {
				// [CUT] type declaration can be also constructed from the
				// generic declaration.
				// TypeDeclaration typeDeclaration =
				// ptAnalysis.getTypeDecl(type);
				// if (typeDeclaration!=null)
				// cic.typeDeclaration =
				// ast.TypeDeclaration.createFrom(typeDeclaration);
				// else{
				int debug = 0;
				debug++;
			}

		}
		return cic;
	}

	private ClassInstanceCreation createClassInstanceCreation(LoadLiteralInstruction instr) {
		// ast.ClassInstanceCreation cic =
		// adapter.TraceabilityFactory.getDeclarationFromAssignment(instr.getNode());
		// cic.complexExpression = instr.getNode().toString();
		// //TORAD: FIX ME!
		// //cic.typeDeclaration =
		// getTypeDeclaration(instr.getTarget().resolveType());
		// return cic;

		// TODO: HIGH. Need a new type of MiniASTNode to handle initializers...
		return null;
	}

	/**
	 * @param instr
	 * @return
	 */
	private FieldAccess createFieldAccess(LoadFieldInstruction instr) {
		FieldAccess fieldAccess = new FieldAccess();
		fieldAccess.fieldDeclaration = getFieldDeclaration(instr);
		fieldAccess.complexExpression = instr.getNode().toString();
		fieldAccess.recvType = ast.Type.createFrom(instr.getAccessedObjectOperand().resolveType());
		fieldAccess.fieldType = ast.Type.createFrom(instr.getTarget().resolveType());
		return fieldAccess;
	}

	private FieldWrite createFieldWrite(StoreFieldInstruction instr) {
		FieldWrite fieldWrite = FieldWrite.createFrom(instr.getNode());
		fieldWrite.fieldDeclaration = getFieldDeclaration(instr);
		fieldWrite.complexExpression = instr.getNode().toString();
		fieldWrite.recvType = ast.Type.createFrom(instr.getAccessedObjectOperand().resolveType());
		fieldWrite.valueType = ast.Type.createFrom(instr.getDestinationObject().resolveType());
		return fieldWrite;
	}

	private ast.FieldDeclaration getFieldDeclaration(TACFieldAccess instr) {
		FieldDeclaration fieldDeclaration = adapter.TraceabilityFactory.getFieldDeclaration(instr.resolveFieldBinding()
				.getDeclaringClass().getErasure().getQualifiedName(),
				instr.getFieldName());
		return fieldDeclaration;
	}

	private MethodInvocation createMethodInvocation(MethodCallInstruction instr) {
		// //HACK: TODO:MED handle SupeMethodInvocation in MiniAstOOG

		ast.MethodInvocation minvk = ast.MethodInvocation.createFrom(instr.getNode());
		String typeQualifiedName = instr.resolveBinding().getDeclaringClass().getErasure().getQualifiedName();
		// TODO: test some more, do we need to search for a method declaration
		// recursively in the base class?
		// or is resolveBinding.getDeclaringClass takes care of it?
		if (minvk!=null){
			minvk.methodDeclaration = adapter.TraceabilityFactory.getMethodDeclaration(typeQualifiedName, instr
					.resolveBinding().getMethodDeclaration().getName());
			if (minvk.methodDeclaration == null)
				this.ptAnalysis.addWarning(instr.getNode(), "cannot find corresponding method declaration");
			minvk.complexExpression = instr.getNode().toString();
			minvk.recvType = ast.Type.createFrom(instr.getReceiverOperand().resolveType());
			List<Variable> argOperands = instr.getArgOperands();			
			List<Type> argTypes = new ArrayList<Type>();
			for (Variable arg : argOperands) {
				argTypes.add(Type.createFrom(arg.resolveType()));
			}
			minvk.argumentTypes = argTypes;
			minvk.retType =  ast.Type.createFrom(instr.getReceiverOperand().resolveType());
		}
		return minvk;
	}

	/**
	 * - Expression [null] (or use this to store the fieldname as a Variable?);
	 * [could use FieldWriteExpression?] - ExpressionType (Field Type) -
	 * EnclosingDeclaration (FieldDeclaration) - EnclosingType (TypeDeclaration)
	 * @param tk 
	 */
	public TraceabilityEntry createPTTraceability(QualifiedClassName c, String fieldName, OObject o, OwnershipType tk) {
		ast.FieldDeclaration fieldDeclaration = Utils.getRecursiveFieldDeclaration(c, fieldName);
		EdgePTTraceability baseTraceability = new EdgePTTraceability(fieldDeclaration);
		// XXX. Why would fieldDeclaration be null?
		if (fieldDeclaration != null) {
			// XXX. Avoid reassigning it.
			if (fieldDeclaration.annotation == null) {
				fieldDeclaration.annotation = getAnnotation2(tk);
			}
		}
		else {
			int debug=0;
			debug++;
		}
		return new TraceabilityEntry(o.getO_id(), baseTraceability);
	}

	// XXX. Add the rest of the parameters...not just the first one...
	// XXX. EXtract method: take List<DomainP> only
	private AnnotationInfo getAnnotation(OwnershipType tk) {
		AnnotationInfo info = null;
		if (tk != null) {
			List<DomainP> value = tk.getValue();
			if ( value != null && value.size() > 0 ) {
	            DomainP domainP = value.get(0);
	            if ( domainP != null ) {
	            	info = AnnotationInfo.parseAnnotation(domainP.getShortName());
	            }
            }
		}
		return info;
    }

	// XXX. Add the rest of the parameters...not just the first one...
	// XXX. EXtract method: take List<DomainP> only
	private static String getAnnotation2(OwnershipType tk) {
		String info = null;
		if (tk != null) {
			List<DomainP> value = tk.getValue();
			if ( value != null && value.size() > 0 ) {
	            DomainP domainP = value.get(0);
	            if ( domainP != null ) {
	            	info = domainP.getShortName();
	            }
            }
		}
		return info;
    }

	
	public static void reset() {
		if (s_instance != null) {
			// TODO: Extract method
			s_instance.mapNodes.clear();
		}
		s_instance = null;
	}
}