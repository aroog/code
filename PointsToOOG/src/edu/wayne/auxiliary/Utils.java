package edu.wayne.auxiliary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ast.FieldDeclaration;
import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.aliasjava.Constants;
import edu.cmu.cs.aliasjava.parser.DomainParams;
import edu.cmu.cs.aliasxml.FieldAnnotationInfo;
import edu.cmu.cs.aliasxml.TypeAnnotationInfo;
import edu.cmu.cs.aliasxml.TypeParams;
import edu.cmu.cs.aliasxml.VariableAnnotationInfo;
import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.ICrystalAnnotation;
import edu.cmu.cs.crystal.cfg.eclipse.TypeDecl;
import edu.cmu.cs.crystal.tac.model.AssignmentInstruction;
import edu.cmu.cs.crystal.tac.model.InvocationInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.StoreInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.wayne.generics.GenHelper;
import edu.wayne.moogdb.TypeDeclAnnotationInfo;
import edu.wayne.moogdb.VariableDeclAnnotationInfo;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.OwnershipType;
import edu.wayne.ograph.internal.QualifiedClassName;

public class Utils {

	public static final String THIS = "this";

	public static final String IMPLICIT_THIS = "implicit-this";

	private static final String RETURN = "return";

	public static final String JAVA_LANG_OBJECT = "java.lang.Object";

	public static final String VOID = "void";

	/**
	 * Called to resolve variablebinding for arg_i of a method invocation
	 * 
	 * @param arg
	 * @param instr
	 * @return
	 */
	public static IVariableBinding getArgVarBinding(Variable arg, InvocationInstruction instr) {
		MethodInvocation parentInstr = null;
		if (instr.getNode() instanceof MethodInvocation)
			parentInstr = (MethodInvocation) instr.getNode();
		else
			return null;
		IVariableBinding varBinding = null;
		List args = parentInstr.arguments();
		int i = instr.getArgOperands().indexOf(arg);
		Object object = args.get(i);
		{
			if (object instanceof SimpleName)// most likely since TAC did not parse it
				varBinding = (IVariableBinding) ((SimpleName) object).resolveBinding();
			else {
				// HACK: XXX: don't we have to deal with arrays...
				// Maybe not, everything has been turned into a temp variable.
				// Double check!
			}
		}
		return varBinding;
	}

	/**
	 * retrieves the variable binding from a TACInstruction
	 * 
	 * @param instr
	 */
	public static IVariableBinding getReceiverVarBinding(TACInstruction instr) {
		ASTNode parentInstr = instr.getNode().getParent();
		IVariableBinding varBinding = null;
		if (instr.getNode() instanceof ArrayAccess) {
			ArrayAccess arrayAccess = (ArrayAccess) instr.getNode();
			varBinding = getArrayAccessVarBinding(arrayAccess);
		}
		if (instr.getNode() instanceof VariableDeclarationFragment) {
			varBinding = ((VariableDeclarationFragment) instr.getNode()).resolveBinding();
		}

		if (instr.getNode() instanceof SimpleName) {
			IBinding bind = ((SimpleName) instr.getNode()).resolveBinding();
			if (bind instanceof IVariableBinding) {
				varBinding = ((IVariableBinding) bind).getVariableDeclaration();
			}
		}
		// if (parentInstr instanceof VariableDeclarationFragment) {
		// varBinding = ((VariableDeclarationFragment)
		// parentInstr).resolveBinding();
		// }
		if (parentInstr instanceof Assignment) {
			Expression leftHnsd = ((Assignment) parentInstr).getLeftHandSide();
			if (leftHnsd instanceof SimpleName) {
				IBinding bind = ((SimpleName) leftHnsd).resolveBinding();
				if (bind instanceof IVariableBinding) {
					varBinding = ((IVariableBinding) bind).getVariableDeclaration();
				}
			}
			if (leftHnsd instanceof ArrayAccess) {
				ArrayAccess arrayAccess = (ArrayAccess) leftHnsd;
				varBinding = getArrayAccessVarBinding(arrayAccess);
			}
		}
		return varBinding;
	}

	/**
	 * @param varBinding
	 * @param arrayAccess
	 * @return
	 */
	public static IVariableBinding getArrayAccessVarBinding(ArrayAccess arrayAccess) {
		IVariableBinding varBinding = null;
		Expression array = arrayAccess.getArray();
		if (array instanceof SimpleName) {
			IBinding bind = ((SimpleName) array).resolveBinding();
			if (bind instanceof IVariableBinding) {
				varBinding = ((IVariableBinding) bind).getVariableDeclaration();
			}
		}
		else if (array instanceof ArrayAccess) {
			return getArrayAccessVarBinding((ArrayAccess) array);
		}
		return varBinding;
	}

	/***
	 * for a.f = b, get the variable binding for b, where b is a TAC temp variable or a simpleName. HACK: what if b is
	 * an array b[i],
	 * 
	 * @param instr
	 * @return
	 */
	public static IVariableBinding getSourceVarBinding(TACInstruction instr) {
		ASTNode node = instr.getNode();
		if (node instanceof Assignment) {
			Assignment assInstr = (Assignment) node;
			if (assInstr.getLeftHandSide() instanceof SimpleName) {
				SimpleName new_name = (SimpleName) assInstr.getLeftHandSide();
				IBinding resolveBinding = new_name.resolveBinding();
				if (resolveBinding instanceof IVariableBinding)
					return (IVariableBinding) resolveBinding;
			}
		}
		if (node instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment assInstr = (VariableDeclarationFragment) node;
			Expression initializer = assInstr.getInitializer();
			if (initializer != null)
				if (initializer instanceof SimpleName) {
					SimpleName new_name = (SimpleName) initializer;
					IBinding resolveBinding = new_name.resolveBinding();
					if (resolveBinding instanceof IVariableBinding)
						return (IVariableBinding) resolveBinding;
				}
			if (initializer instanceof ClassInstanceCreation) {
				return (IVariableBinding) assInstr.getName().resolveBinding();
			}

		}
		return null;
	}

	public static IVariableBinding getTargetVarBinding(TACInstruction instr) {
		ASTNode parentInstr = instr.getNode().getParent();

		IVariableBinding varBinding = null;
		if (instr.getNode() instanceof VariableDeclarationFragment) {
			varBinding = ((VariableDeclarationFragment) instr.getNode()).resolveBinding();
		}
		if (parentInstr instanceof VariableDeclarationFragment) {
			varBinding = ((VariableDeclarationFragment) parentInstr).resolveBinding();
		}
		if (instr.getNode() instanceof Assignment) {
			Assignment assignment = (Assignment) instr.getNode();
			varBinding = getLeftHandSideVarBinding(assignment);
		}
		if (parentInstr instanceof Assignment) {
			Assignment assignment = (Assignment) parentInstr;
			varBinding = getLeftHandSideVarBinding(assignment);
		}
		return varBinding;
	}

	/**
	 * for an assignment x = y returns the variable binding of x
	 * 
	 * @param varBinding
	 * @param assignment
	 * @return
	 */
	private static IVariableBinding getLeftHandSideVarBinding(Assignment assignment) {
		Expression leftHnsd = assignment.getLeftHandSide();
		if (leftHnsd instanceof SimpleName) {
			IBinding bind = ((SimpleName) leftHnsd).resolveBinding();
			if (bind instanceof IVariableBinding) {
				return ((IVariableBinding) bind).getVariableDeclaration();
			}
		}
		if (leftHnsd instanceof FieldAccess) {
			FieldAccess fa = (FieldAccess) leftHnsd;
			return fa.resolveFieldBinding();
		}
		// Leave it null - cannot determine actual domains for arrays
		// workaround for bugs related to objects created in complex expression
		// if (leftHnsd instanceof ArrayAccess) {
		// ArrayAccess aa = (ArrayAccess) leftHnsd;
		// return getArrayAccessVarBinding(aa);
		// }
		return null;
	}

	/**
	 * returns the name associated with an assignment instruction
	 * 
	 * @param instr
	 * @return
	 */
	public static String getSimpleAssignName(AssignmentInstruction instr) {
		if (instr instanceof LoadLiteralInstruction)
			return ((LoadLiteralInstruction) instr).getLiteral().toString();
		ASTNode parentInstr = instr.getNode().getParent();

		if (parentInstr instanceof VariableDeclarationFragment) {
			return ((VariableDeclarationFragment) parentInstr).getName().getFullyQualifiedName();
		}
		if (parentInstr instanceof Assignment) {
			Expression leftHnsd = ((Assignment) parentInstr).getLeftHandSide();
			if (leftHnsd instanceof SimpleName) {
				return ((SimpleName) leftHnsd).getFullyQualifiedName();
			}
			else if (leftHnsd instanceof FieldAccess) {
				return ((FieldAccess) leftHnsd).getName().getFullyQualifiedName();
			}
		}
		throw new IllegalStateException("cannot find local variable associated with " + instr);
	}

	/**
	 * @param recv
	 * @return
	 */
	public static boolean isThis(Variable recv) {
		return (recv.getSourceString().equals(IMPLICIT_THIS)) || (recv.getSourceString().equals(THIS));
	}

	public static boolean isSuper(Variable recv) {
		return (recv.getSourceString().equals("super"));
	}

	/**
	 * Returns the list of declared domains associated with a class
	 * 
	 * @param instantiatedType
	 * @return \ob{dom}
	 */
	// XXX. Cache the list of declared domains. Not very inefficient, since does not require parsing
	public static List<String> getDomainDecls(ITypeBinding instantiatedType, AnnotationDatabase annoDB) {
		List<String> domainDecls = null;
		
		String fullyQualifiedName = instantiatedType.getQualifiedName();
		domainDecls = TypeDeclAnnotationInfo.get(fullyQualifiedName);
		if(domainDecls == null ) {
		domainDecls = new ArrayList<String>();
		List<ICrystalAnnotation> annots = annoDB.getAnnosForType(instantiatedType);
		if (annots == null || annots.isEmpty()) {
			// Try to load XML Loader.
			String key = instantiatedType.getErasure().getKey();
			TypeAnnotationInfo typeDecl = TypeAnnotationInfo.getBinding(key);
			if (typeDecl != null) {
				domainDecls = typeDecl.getDomains();
			}
		}
		else
			for (ICrystalAnnotation iCrystalAnnotation : annots) {
				if (iCrystalAnnotation.getName().endsWith(Constants.DOMAINS)) {
					if (iCrystalAnnotation.getObject("value") instanceof Object[]) {
						Object[] declDomains = ((Object[]) iCrystalAnnotation.getObject("value"));
						for (int i = 0; i < declDomains.length; i++) {
							String domName = (String) declDomains[i];
							domainDecls.add(domName);
						}
					}
					else
						domainDecls.add((String) iCrystalAnnotation.getObject("value"));
				}
			}
		// domainDecls.add(Constants.LENT);
		// domainDecls.add(Constants.UNIQUE);
		
		TypeDeclAnnotationInfo.put(fullyQualifiedName, domainDecls);
		}
		return domainDecls;
	}

	/**
	 * return declared domain parameters includes OWNER!
	 */
	public static List<DomainParams> getDomainParamsDecl(ITypeBinding tb, AnnotationDatabase annoDB) {
		if (tb.isNested()) {
			ITypeBinding declaringClass = tb.getDeclaringClass();
			return getDomainParamsDecl(declaringClass, annoDB);
		}
		List<DomainParams> result = new ArrayList<DomainParams>();
		result.add(DomainParams.create(Constants.OWNER));
		List<ICrystalAnnotation> annots = annoDB.getAnnosForType(tb);
		if (annots == null || annots.isEmpty()) {
			// Try to load XML Loader.
			String key = tb.getErasure().getKey();
			TypeAnnotationInfo typeDecl = TypeAnnotationInfo.getBinding(key);
			if (typeDecl != null) {
				for (String domParam : typeDecl.getParameters()) {
					result.add(DomainParams.create(domParam));
				}
				return result;
			}
		}
		for (ICrystalAnnotation iCrystalAnnotation : annots) {
			if (iCrystalAnnotation.getName().endsWith(Constants.DOMAIN_PARAMS)) {
				Object[] annot = (Object[]) iCrystalAnnotation.getObject("value");
				for (int i = 0; i < annot.length; i++) {
					result.add(DomainParams.create((String) annot[i]));
				}
				return result;
			}
		}
		return result;
	}

	public static List<String> getDomainParamsDecl2(ITypeBinding tb, AnnotationDatabase annoDB) {
		if (tb.isNested()) {
			ITypeBinding declaringClass = tb.getDeclaringClass();
			return getDomainParamsDecl2(declaringClass, annoDB);
		}
		List<String> result = new ArrayList<String>();
		result.add(Constants.OWNER);
		List<ICrystalAnnotation> annots = annoDB.getAnnosForType(tb);
		if (annots == null || annots.isEmpty()) {
			// Try to load XML Loader.
			String key = tb.getErasure().getKey();
			TypeAnnotationInfo typeDecl = TypeAnnotationInfo.getBinding(key);
			if (typeDecl != null) {
				for (String domParam : typeDecl.getParameters()) {
					result.add(domParam);
				}
				return result;
			}
		}
		for (ICrystalAnnotation iCrystalAnnotation : annots) {
			if (iCrystalAnnotation.getName().endsWith(Constants.DOMAIN_PARAMS)) {
				Object[] annot = (Object[]) iCrystalAnnotation.getObject("value");
				for (int i = 0; i < annot.length; i++) {
					result.add((String) annot[i]);
				}
				return result;
			}
		}
		return result;
	}
	
	/**
	 * @return declared domains for a new expression. Since we require developers to add local vars for new expressions,
	 *         we can simply look for that variable declaration
	 */
	public static AnnotationInfo getp_i(TACInstruction instr, AnnotationDatabase annoDB) {
		AnnotationInfo anInfo = null;
		IVariableBinding varBinding = getTargetVarBinding(instr);
		if (varBinding != null) {
			anInfo = VariableDeclAnnotationInfo.getInfo(varBinding);
			if (anInfo == null ) {
			
			if (varBinding.getType().isArray()) {
				// TODO: FIXME: target varBinding is an Array, but the righthandside might be
				int debug = 0; debug++;
			}
			// else
			{
				List<ICrystalAnnotation> annots = annoDB.getAnnosForVariable(varBinding);
				if (annots != null && !annots.isEmpty() && annots.get(0).getName().endsWith(Constants.DOMAIN)) {
					String annot = (String) annots.get(0).getObject("value");
					if (annot.isEmpty()) {
						int debug = 0;
						debug++;
					}
					
					// XXX. This is inefficient. Oftentimes, we just want a string, no need to convert to AnnotationInfo object
					anInfo = AnnotationInfo.parseAnnotation(annot);
				}
				else {
					anInfo = getAnnotationAliasXML(varBinding);
				}
				
				
				if (anInfo == null) {
					throw new IllegalStateException("cannot find local variable");
				}
				
				// Cache the result
				VariableDeclAnnotationInfo.putInfo(varBinding, anInfo);
				
			}
			}
		}
		else {
			// Instead of throwing an error and stop the whole analysis, better
			// display a message, in problems window
			// TODO: MED: group somehow these messages before reporting them
			// ptAnalysis.getSavedReporter().reportUserProblem("cannot find annotation: Extract local variable and add annotation",
			// instr.getNode(), ptAnalysis.getName());
			// return AnnotationInfo.Shared;
			throw new IllegalStateException("cannot find local variable associated");
		}
		return anInfo;
	}

	public static String getp_i2(TACInstruction instr, AnnotationDatabase annoDB) {
		String anInfo = null;
		IVariableBinding varBinding = getTargetVarBinding(instr);
		if (varBinding != null) {
			if (varBinding.getType().isArray()) {
				// TODO: FIXME: target varBinding is an Array, but the righthandside might be
				int debug = 0; debug++;
			}
			// else
			{
				List<ICrystalAnnotation> annots = annoDB.getAnnosForVariable(varBinding);
				if (annots != null && !annots.isEmpty() && annots.get(0).getName().endsWith(Constants.DOMAIN)) {
					String annot = (String) annots.get(0).getObject("value");
					if (annot.isEmpty()) {
						int debug = 0;
						debug++;
					}
					
					// Oftentimes, we just want a string, no need to convert to AnnotationInfo object
					anInfo = annot;
				}
				else {
					AnnotationInfo annot = getAnnotationAliasXML(varBinding);
					anInfo = annot == null ? "" : annot.toString();
				}
				if (anInfo == null) {
					throw new IllegalStateException("cannot find local variable");
				}
			}
		}
		else {
			// Instead of throwing an error and stop the whole analysis, better
			// display a message, in problems window
			// TODO: MED: group somehow these messages before reporting them
			// ptAnalysis.getSavedReporter().reportUserProblem("cannot find annotation: Extract local variable and add annotation",
			// instr.getNode(), ptAnalysis.getName());
			// return AnnotationInfo.Shared;
			throw new IllegalStateException("cannot find local variable associated");
		}
		return anInfo;
	}
	/**
	 * @param anInfo
	 * @param varBinding
	 * @return
	 */
	public static AnnotationInfo getAnnotationAliasXML(IVariableBinding varBinding) {
		AnnotationInfo anInfo = null;
		VariableAnnotationInfo methParamDecl = null;
		// distinguish between arguments and fields (a field has no declaring
		// method)
		if (varBinding.getDeclaringMethod() != null)
			methParamDecl = VariableAnnotationInfo.getBinding(varBinding.getDeclaringMethod().getKey()
			        + varBinding.getType().getKey());
		else
			methParamDecl = VariableAnnotationInfo.getBinding(varBinding.getType().getKey());
		if (methParamDecl != null) {
			anInfo = methParamDecl.getAnnotationInfo();
		}
		return anInfo;
	}

	public static boolean isSummaryType(QualifiedClassName aQCN) {
		String key = aQCN.getTypeBinding().getErasure().getKey();
		TypeAnnotationInfo typeDecl = TypeAnnotationInfo.getBinding(key);
		return typeDecl != null;
	}

	public static Map<String, OwnershipType> getDeclaredFieldsXML(QualifiedClassName resolveType) {
		Map<String, OwnershipType> fields = new Hashtable<String, OwnershipType>();
		ITypeBinding typeBinding = resolveType.getTypeBinding();
		TypeAnnotationInfo typeDecl = TypeAnnotationInfo.getBinding(typeBinding.getErasure().getKey());
		if (typeDecl == null) {
			int debug = 0;
			debug++;
			// return empty list if the type is not in AliasXML
			return fields;
		}
		List<FieldAnnotationInfo> fieldsXML = typeDecl.getFields();
		for (FieldAnnotationInfo fieldAnnotationInfo : fieldsXML) {
			List<DomainP> listP = new ArrayList<DomainP>();
			ITypeBinding actTypeBinding = getActualFieldType(typeBinding, fieldAnnotationInfo);
			if (actTypeBinding != null) {
				AnnotationInfo annoInfo = fieldAnnotationInfo.getAnnotationInfo();
				if (annoInfo.getAnnotation().getDomain().isEmpty()) {
					annoInfo = AnnotationInfo.Shared;
				}
				listP = getDomainP(resolveType, annoInfo);
				fields.put(fieldAnnotationInfo.getFieldName(), new OwnershipType(actTypeBinding, listP));
			}
		}
		return fields;
	}

	/**
	 * @param typeBinding
	 * @param fieldAnnotationInfo
	 * @return
	 */
	private static ITypeBinding getActualFieldType(ITypeBinding typeBinding, FieldAnnotationInfo fieldAnnotationInfo) {
		String fieldTypeBinding = fieldAnnotationInfo.getFieldTypeBinding();
		if (fieldTypeBinding == null) {
			int debug = 0;
			debug++;
			return null;
		}
		
		// XXX. Why isn't this cached. This is very expensive!!!! Does GenHelper do caching?
		// Assert.assertNotNull(fieldTypeBinding);
		ITypeBinding fTypeBinding = (ITypeBinding) GenHelper.createBindingFromKey(fieldTypeBinding);
		if (fTypeBinding == null)
			return null;
		Assert.assertNotNull("Cannot create type from" + fieldTypeBinding, fTypeBinding);
		ITypeBinding actTypeBinding = GenHelper.translateFtoA(fTypeBinding, typeBinding);
		// if (actTypeBinding!=null)
		return actTypeBinding;
		// else{
		// Assert.assertNotNull(fTypeBinding);
		// return fTypeBinding;
		// }
	}

	/**
	 * @param resolveType
	 * @param listP
	 * @param anInfo
	 */
	public static List<DomainP> getDomainP(QualifiedClassName resolveType, AnnotationInfo anInfo) {
		List<DomainP> listP = new ArrayList<DomainP>();
		listP.add(Utils.getOwner(resolveType, anInfo));
		listP.addAll(Utils.getListDomainP(resolveType, anInfo));
		return listP;
	}

	/**
	 * from an annotation info returns the owner DomainP
	 * 
	 * @param resolveType
	 * @param anInfo
	 * @return
	 */
	public static DomainP getOwner(QualifiedClassName resolveType, AnnotationInfo anInfo) {
		return new DomainP(resolveType, anInfo.getAnnotation().getDomain());
	}

	/**
	 * from an annotation info returns the list of parameters
	 * 
	 * @param resolveType
	 * @param listP
	 * @param anInfo
	 */
	public static List<DomainP> getListDomainP(QualifiedClassName resolveType, AnnotationInfo anInfo) {
		List<DomainP> listP = new ArrayList<DomainP>();
		List<String> parameters = getParameters(anInfo);
		for (String p : parameters)
			listP.add(new DomainP(resolveType, p));
		return listP;
	}

	/**
	 * returns a list of parameters from an AnnotationInfo. Includes also parameters of array parameters
	 * 
	 * @param pInfo
	 * @return the list of parameters, including the array parameters
	 */
	public static List<String> getParameters(AnnotationInfo pInfo) {
		List<String> params = new ArrayList<String>();
		List<DomainParams> parameters = pInfo.getParameters();
		for (DomainParams p : parameters) {
			params.add(p.getDomain());
			params.addAll(p.getParams());
		}
		// TODO: HIGH. XXX. NOTE: this is combining ArrayParams with regular Params into one List.
		// Will this give the right semantics for lookup?
		// E.g., when there is an interface with fewer parameters and a class with more parameters?
		// II[M<M>] vs. CII[M<M,V,C>]
		// Also, nesting of parameters is being handled by simply using DomainParams
		for (DomainParams p : pInfo.getArrayParameters()) {
			params.add(p.getDomain());
			params.addAll(p.getParams());
		}
		return params;
	}

	/**
	 * @param c
	 * @param fieldName
	 * @return
	 */
	public static ast.FieldDeclaration getRecursiveFieldDeclaration(QualifiedClassName c, String fieldName) {
		ast.FieldDeclaration fieldDeclaration = adapter.TraceabilityFactory.getFieldDeclaration(c.getFullyQualifiedName(),
		        fieldName);
		if (fieldDeclaration == null)
			if (c.getSuperclass() != null)
				fieldDeclaration = getRecursiveFieldDeclaration(c.getSuperclass(), fieldName);
		return fieldDeclaration;
	}

	/**
	 * substitutes fields declaration with the ones in alias XML if such a type exists.
	 * 
	 * @param iTypeBinding
	 * @return
	 */
	public static ast.TypeDeclaration getTypeDeclaration(ITypeBinding iTypeBinding) {
		ast.TypeDeclaration tdtemp = adapter.TraceabilityFactory.getTypeDeclaration(iTypeBinding.getQualifiedName());
		TypeAnnotationInfo tai = TypeAnnotationInfo.getBinding(iTypeBinding.getErasure().getKey());
		if (tai != null && tdtemp != null) {
			// empty list
			List<FieldAnnotationInfo> fields = tai.getFields();
			// override existing fields with fields from XML
			ArrayList<FieldDeclaration> tmpfields = new ArrayList<FieldDeclaration>();

			for (FieldAnnotationInfo fieldAnnotationInfo : fields) {
				if (!fieldAnnotationInfo.isVirtual()) {
					String variableBindingKey = fieldAnnotationInfo.getVariableBindingKey();
					IVariableBinding vdf = null;
					try {
						vdf = (IVariableBinding) GenHelper.createBindingFromKey(variableBindingKey);
					}
					catch (Exception ex) {
						System.out.println("Cannot create field from XML" + iTypeBinding.getQualifiedName() + " "
						        + fieldAnnotationInfo.getFieldName());
						continue;
					}
					Assert.assertNotNull("Cannot create field for" + variableBindingKey, vdf);
					ast.FieldDeclaration fd = FieldDeclaration.createFrom(vdf);
					tmpfields.add(fd);
				}
				else {
					// do nothing for now.
					// would it be interesting to find how many edges are
					// created for one virtual field?
				}
			}
			tdtemp.fields = tmpfields;
		}
		return tdtemp;
	}

	public static Set<String> getItfDomainDecls(QualifiedClassName aQCN, AnnotationDatabase annoDB) {
		QualifiedClassName[] itfs = aQCN.getInterfaces();
		Set<String> result = new HashSet<String>();
		for (int j = 0; j < itfs.length; j++) {
			List<String> domainDecls = Utils.getDomainDecls(itfs[j].getTypeBinding(), annoDB);
			result.addAll(domainDecls);
		}
		return result;
	}

	public static boolean isNullAssignment(StoreInstruction instr) {
		ASTNode node = instr.getNode();
		if (node instanceof Assignment) {
			Assignment assInstr = (Assignment) node;
			if (assInstr.getRightHandSide() instanceof NullLiteral) {
				return true;
			}
		}
		if (node instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment assInstr = (VariableDeclarationFragment) node;
			Expression initializer = assInstr.getInitializer();
			if (initializer != null)
				if (initializer instanceof NullLiteral) {
					return true;
				}
		}
		return false;
	}

	/**
	 * @param methodKey
	 * @param typeDecl
	 * @return formal parameters of method given as a binding key
	 */
	public static MethodDeclaration getMethodDeclaration(IMethodBinding mb, TypeDeclaration typeDecl) {
		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration md : methods) {
			if (md.resolveBinding().getKey().equals(mb.getKey())) {
				return md;
			}
		}
		return null;
	}

	public static List<String> getDomainDeclsRecursive(ITypeBinding typeBinding, AnnotationDatabase annoDB) {
		Set<String> domDecl = new HashSet<String>();
		domDecl.addAll(getDomainDecls(typeBinding, annoDB));
		if (typeBinding.getSuperclass() != null)
			domDecl.addAll(getDomainDecls(typeBinding.getSuperclass(), annoDB));
		ITypeBinding[] interfaces = typeBinding.getInterfaces();
		for (ITypeBinding iTypeBinding : interfaces) {
			domDecl.addAll(getDomainDecls(iTypeBinding, annoDB));
		}
		List<String> domDeclList = new ArrayList<String>();
		domDeclList.addAll(domDecl);
		return domDeclList;
	}

	public static List<DomainParams> getDomInherits(QualifiedClassName aQCN, AnnotationDatabase annoDB) {
		List<DomainParams> inhParams = new ArrayList<DomainParams>();
		List<ICrystalAnnotation> annoType = annoDB.getAnnosForType(aQCN.getTypeBinding());
		for (ICrystalAnnotation anno : annoType) {
			if (anno.getName().endsWith(Constants.DOMAIN_INHERITS)) {
				Object[] annot = (Object[]) anno.getObject("value");
				for (int i = 0; i < annot.length; i++) {
					AnnotationInfo annoInfo = AnnotationInfo.parseAnnotation((String) annot[i]);
					DomainParams dp = DomainParams.createFromAnnotation((String) annot[i]);
					List<DomainParams> parameters = annoInfo.getParameters();
					inhParams.addAll(parameters);
				}
			}
		}
		return inhParams;
	}

	/***
	 * given subC<alpha1,alpha2,beta1,beta2> <: C<alpha1,beta1> 
	 * returns the indexes for which domain params in subC match in C [0,2]
	 * 
	 * @param subType
	 * @param c
	 * @param annoDB
	 * @return
	 * 
	 * XXX. Can we get rid of using AnnotationDB and just get them from the MiniAST?
	 */
	public static int[] getDomInhPairs(QualifiedClassName subType, QualifiedClassName c, AnnotationDatabase annoDB) {
		List<ICrystalAnnotation> annoType = annoDB.getAnnosForType(subType.getTypeBinding());
		List<DomainParams> domainParamsDecl = getDomainParamsDecl(subType.getTypeBinding(), annoDB);
		int[] result = new int[domainParamsDecl.size()];
		int size = 0;
		if (subType.equals(c)) {
			for (int i = 0; i < domainParamsDecl.size(); i++) {
				result[size] = size;
				size++;
			}
			return result;
		}
		boolean hasDomInherits = false;
		String className = "";
		if (annoType == null || annoType.isEmpty()) {
			// Try to load XML Loader.
			String key = subType.getTypeBinding().getErasure().getKey();
			TypeAnnotationInfo typeDecl = TypeAnnotationInfo.getBinding(key);
			if (typeDecl != null) {
				List<DomainParams> parameters = typeDecl.getInheritParams(c.getTypeBinding());
				if (parameters != null && parameters.size() > 0) {
					result[size] = 0;
					size++;
					for (DomainParams domainParams : parameters) {
						result[size] = domainParamsDecl.indexOf(domainParams);
						size++;
					}
					for (int j = size; j < domainParamsDecl.size(); j++)
						result[j] = -1;
					return result;
				}
				else {
					List<TypeParams> inheritParams = typeDecl.getInheritParams();
					hasDomInherits = inheritParams != null && inheritParams.size() > 0;
				}
			}
		}
		else {
			for (ICrystalAnnotation anno : annoType) {
				if (anno.getName().endsWith(Constants.DOMAIN_INHERITS)) {
					hasDomInherits = true;
					Object[] annot = (Object[]) anno.getObject("value");
					for (int i = 0; i < annot.length; i++) {
						AnnotationInfo annoInfo = AnnotationInfo.parseAnnotation((String) annot[i]);
						DomainParams dp = DomainParams.createFromAnnotation((String) annot[i]);
						className = annoInfo.getAnnotation().getDomain();
						if (c.getTypeBinding().getName().equals(className)) {
							result[size] = 0;
							size++;
							List<DomainParams> parameters = annoInfo.getParameters();
							for (DomainParams domainParams : parameters) {
								result[size] = domainParamsDecl.indexOf(domainParams);
								size++;
							}
							for (int j = size; j < domainParamsDecl.size(); j++)
								result[j] = -1;
						}
					}
				}
			}
		}
		if (hasDomInherits) {
			if (className.equals(c.getTypeBinding().getName())) {
				return result;
			}
			else {
				int[] domInhPairs = new int[0];
				if (subType.getSuperclass() != null
				        && subType.getSuperclass().getFullyQualifiedName() != JAVA_LANG_OBJECT) {
					domInhPairs = getDomInhPairs(subType.getSuperclass(), c, annoDB);
				}
				else {
					QualifiedClassName[] interfaces = subType.getInterfaces();
					for (QualifiedClassName itf : interfaces) {
						if (itf.isSubTypeCompatible(c))
							domInhPairs = getDomInhPairs(itf, c, annoDB);
					}
				}
				for (int i = 0; i < domInhPairs.length; i++)
					if (domInhPairs[i] > -1)
						result[i] = result[domInhPairs[i]];
					else
						result[i] = -1;
				for (int i = domInhPairs.length; i < result.length; i++)
					result[i] = -1;
				return result;
			}
		}
		result[0] = 0;
		for (int i = 1; i < result.length; i++)
			result[i] = -1;
		return result;
	}
}
