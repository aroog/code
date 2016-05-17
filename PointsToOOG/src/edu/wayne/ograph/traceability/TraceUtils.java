package edu.wayne.ograph.traceability;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import ast.AstNode;
import ast.BaseTraceability;
import ast.EdgeDFTraceability;
import ast.MethodInvocation;
import ast.Type;
import ast.VariableDeclaration;
import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.wayne.ograph.analysis.NodesOOGTransferFunctions;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.QualifiedClassName;

public class TraceUtils {

	public static void setMethDeclFormals(MethodDeclaration eMethodDecl, ast.MethodDeclaration methDecl,
	        AnnotationDatabase annoDB) {

		if (methDecl != null && !methDecl.annotationsSet) {

			IMethodBinding mb = eMethodDecl.resolveBinding();
			ITypeBinding declaringClass = mb.getDeclaringClass();
			// XXX. Could be problematic...passing null for CThis turns off generic type subst.
			// But there no Cthis here since dealing with Decl.
			QualifiedClassName C = new QualifiedClassName(declaringClass, null);

			List<DomainP> returnDs = NodesOOGTransferFunctions.getReturnDeclaredDomains(C, mb, annoDB);
			methDecl.returnAnnotation = getAnnotationInfo(returnDs);

			// XXX. Fill me in.
			// NOTE: Currently, we do not use the receiver annotations.
			// ScoriaX does not read @DomainReceiver yet.
			methDecl.receiverAnnotation = "";

			List<SingleVariableDeclaration> eParams = eMethodDecl.parameters();
			List<VariableDeclaration> parameters = methDecl.parameters;

			int ii = 0;
			for (SingleVariableDeclaration eParam : eParams) {
				IVariableBinding varBinding = eParam.resolveBinding();
				
				ITypeBinding varTypeBinding = varBinding.getType();
				// Skip primitive type
				if(varTypeBinding.isPrimitive()) {
					continue;
				}
				
				List<DomainP> varDoms = NodesOOGTransferFunctions.getDeclaredDomains(C, varBinding, annoDB);

				VariableDeclaration param = parameters.get(ii);
				if(varDoms != null) {
					param.annotation = getAnnotationInfo(varDoms);
				}
				else {
					int debug = 0; debug++;
				}

				ii++;
			}

			// XXX. Do we still need this? Can this still be called multiple times for ONE MethodDeclaration?
			// Since we call this from PointsToAnalysis.analyzeMethod(MethodDeclaration methodDecl)
			methDecl.annotationsSet = true;
		}
	}

	/**
	 * NOTE: This is returning just the owning domain for now. 
	 * TODO: Include domain param?
	 */
	public static String getAnnotationInfo(List<DomainP> domainPs) {
		String annotInfo = null;

		// Just include the owning domain now
		for (DomainP domainP : domainPs) {
			annotInfo = domainP.getShortName();
			break;
		}

		return annotInfo;
	}

	/**
	 * XXX. Multiple traceability entries may point to the same MethodInvocation object in MiniAST. Because one method
	 * invocation may generate multiple edges due to the multiple args. So have to be careful to not overwrite the
	 * changes. Should we break this aliasing, and have multiple MiniAST nodes?
	 */
	// XXX. Avoid duplication here
	public static void setArgAnnotations(MethodInvocation methInvk, List<Variable> argOperands, List<DomainP> argAnnotation, int ii) {
		List<Type> argumentTypes = methInvk.argumentTypes;

		// Check that the object has been set
		List<String> argActuals = methInvk.argumentAnnotations;

		// This may not be set
		// TODO: Add sanity check that they are equal?
		if (argumentTypes != null) {
			int size = argumentTypes.size();
			// Initialize this once here
			if (argActuals == null) {
				argActuals = new ArrayList<String>();
				for (int jj = 0; jj < size; jj++) {
					argActuals.add("");
				}
			}
		}

		if (argOperands != null) {
			int size = argOperands.size();
			// Initialize this once here
			if (argActuals == null) {
				argActuals = new ArrayList<String>();
				for (int jj = 0; jj < size; jj++) {
					argActuals.add("");
				}
			}
		}
		
		// XXX. When could this happen? Varargs?
		if (argumentTypes != null && argOperands != null ) {
			if ( argumentTypes.size() != argActuals.size() ) {
				System.out.println("Formals and actuals have different sizes");
			}
		}

		methInvk.argumentAnnotations = argActuals;
		methInvk.argumentAnnotations.set(ii, getDomain(argAnnotation));
	}

	public static void setRetAnnotation(MethodInvocation methInvk, List<DomainP> methActualRetDoms) {
		methInvk.retAnnotation = getDomain(methActualRetDoms);
	}

	public static void setRecvAnnotation(MethodInvocation methInvk, List<DomainP> argDomains) {
		methInvk.recvAnnotation = getDomain(argDomains);
	}

	/**
	 * Use only the first param, i.e., the owning domain. NOTE: Does not use C::d; just d!
	 * 
	 * @param argDomains
	 * @return
	 */
	public static String getDomain(List<DomainP> argDomains) {

		// List<String> annots = new ArrayList<String>();
		// for(DomainP argDom : argDomains ) {
		// annots.add(argDom.toString());
		// }

		String domain = "";

		if (argDomains.size() > 0) {
			DomainP domainP = argDomains.get(0);
			if (domainP != null) {
				// NOTE: To use C::d, use domainP.toString();
				domain = domainP.getShortName();
			}
		}
		return domain;
	}
}
