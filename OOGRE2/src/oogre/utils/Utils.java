package oogre.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oogre.refinements.tac.AnnotateUnitEnum;
import oogre.refinements.tac.OType;
import oogre.refinements.tac.TACNewExpr;
import oogre.refinements.tac.TACVariable;
import oogre.refinements.tac.TM;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;

import edu.cmu.cs.crystal.internal.WorkspaceUtilities;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.TypeVariable;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.util.Option;
import edu.cmu.cs.crystal.util.typehierarchy.CachedTypeHierarchy;

// XXX. Clean this up. Most of this is not needed. Should not be used.
public class Utils {
	private static CachedTypeHierarchy hierarchy;
	
	public static IProject getCurrentProject(IWorkbenchWindow window) {
		IProject project = null;
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			IFile file = null;
			if (input instanceof IFileEditorInput) {
				file = ((IFileEditorInput) input).getFile();
			}
			if (file != null) {
				project = file.getProject();
			}
		}
		return project;
	}
	
	public static IJavaProject getJavaProject() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null) {
			System.out.println("No workspace");
			return null;
		}
		IWorkspaceRoot root = workspace.getRoot();
		if (root == null) {
			System.out.println("No workspace root");
			return null;
		}

		IJavaModel javacore = JavaCore.create(root);// this gets you the java version of the workspace
		IJavaProject project = null;
		if (javacore != null) {
			IJavaProject[] javaProjects  = null;
			try {
				javaProjects = javacore.getJavaProjects();
            }
            catch (JavaModelException e) {
	            e.printStackTrace();
            }
			
			// Return the first one.
			// XXX. Display message box if there is more than one open project
			if (javaProjects != null && javaProjects.length > 0 ) {
				project = javaProjects[0];
			}
		}

		return project;
	}
	
	public static boolean isEqual(IBinding o1, IBinding o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false; // o2 != null if we reach this point
		}
		if (o1.isEqualTo(o2)) {
			return true;
		}
		return false;
	}

	// XXX. For optimization, add extra check that method must have:
	// - At least, one method param of non-primitive type
	// - A method return of non-primitive type
	// - We should exclude things like equals(...), toString(), etc.
	public static IMethodBinding getMethodBinding(ITypeBinding superclass, String methodName, ITypeBinding[] paramTypes, ITypeBinding returnType) {
		IMethodBinding overriddenMethod = null;
		
		boolean include = false;

		IMethodBinding[] declaredMethods = superclass.getDeclaredMethods();
		for (int ii = 0; ii < declaredMethods.length; ii++) {
			IMethodBinding declaredMethod = declaredMethods[ii];
			if (declaredMethod.getName().compareTo(methodName) != 0) {
				continue;
			}

			// Overridden method must be public or protected
			if (Modifier.isPrivate(declaredMethod.getModifiers())) {
				continue;
			}
			
			// Overridden method cannot be static
			if (Modifier.isStatic(declaredMethod.getModifiers())) {
				continue;
			}

			if (!isEqual(declaredMethod.getReturnType(), returnType)) {
				continue;
			}
			
			// Include if method return is non-primitive
			include |= (returnType != null && !returnType.isPrimitive());

			ITypeBinding[] parameterTypes = declaredMethod.getParameterTypes();
			if (parameterTypes.length != paramTypes.length) {
				continue;
			}

			boolean equal = true;
			for (int jj = 0; jj < parameterTypes.length; jj++) {
				ITypeBinding parameterType = parameterTypes[jj];
				equal &= parameterType.isEqualTo(paramTypes[jj]);
				if (!equal) {
					break;
				}

				// Include if at least one param is non-primitive
				include |= (parameterType != null && !parameterType.isPrimitive());
			}

			if (equal) {
				overriddenMethod = declaredMethod;
				break;
			}
		}
		
		// Not an interesting method
		if(!include) {
			overriddenMethod = null;
		}
		
		return overriddenMethod;
	}
	
	/*
	 * Driver: uses the declaring class
	 */
	public static IMethodBinding getOverriddenMethod(IMethodBinding methodBinding) {
	    return getOverriddenMethod(methodBinding.getDeclaringClass(), methodBinding);
    }
	
	/**
	 * General, recursive version.
	 * @param declaringClass
	 * @param methodBinding
	 * @return
	 */
	private static IMethodBinding getOverriddenMethod(ITypeBinding declaringClass, IMethodBinding methodBinding) {
		IMethodBinding overriddenMethod = null;

		ITypeBinding[] paramTypes = methodBinding.getParameterTypes();
		String methodName = methodBinding.getName();

		ITypeBinding returnType = methodBinding.getReturnType();

		// Look at superclass
		ITypeBinding superclass = declaringClass.getSuperclass();
		if (superclass != null) {
			overriddenMethod = getMethodBinding(superclass, methodName, paramTypes, returnType);
			// Recursively look at superclass
			if (overriddenMethod == null ) {
				overriddenMethod = getOverriddenMethod(superclass, methodBinding);
			}
		}
		// Look at interfaces
		if (overriddenMethod == null) {
			for (ITypeBinding itfTypeBinding : declaringClass.getInterfaces()) {
				overriddenMethod = getMethodBinding(itfTypeBinding, methodName, paramTypes, returnType);
				if (  overriddenMethod != null ) {
					break;
				}
			}
		}

		return overriddenMethod;
	}

	public static boolean isEqual(String type1, String type2) {
		return type1.equals(type2);
	}

	
	public static void registerTypeHierarch(IJavaProject javaProject) {
		try {
	        hierarchy = new CachedTypeHierarchy(javaProject);
        }
        catch (JavaModelException e) {
	        e.printStackTrace();
        }
    }
	
	/**
	 * Return true if subType <: superType.
	 *  
	 * @param subType
	 * @param superType
	 * @return
	 */
	public static boolean isSubtypeCompatible(String subType, String superType) {
		if(hierarchy != null )
			return hierarchy.isSubtypeCompatible(subType, superType);
		
		// Return base case of <: i.e., subType == superType
		return subType.equals(superType);
	}	
	
	/**
	 * returns "AnnotateUnitEnum.f" for filed variable, "AnnotateUnitEnum.p" for method parameters and "AnnotateUnitEnum.v" for local variables, otherwise returns null
	 * @param variable
	 * @return
	 */
	
	public static AnnotateUnitEnum getVariableAUKind(Variable variable) {

		if (variable instanceof SourceVariable) {
			SourceVariable srcVariable = (SourceVariable) variable;
			IVariableBinding varBinding = srcVariable.getBinding();

			if (varBinding.isField()) {
				return AnnotateUnitEnum.f;
			}
			else if (varBinding.isParameter()) {
				return AnnotateUnitEnum.p;
			} 
			else{
				return AnnotateUnitEnum.v;
			}
		}
		if (variable instanceof TempVariable) {
			TempVariable tmpVaribale = (TempVariable) variable;
			ASTNode node = tmpVaribale.getNode();
			
			switch(node.getNodeType()) {
			case ASTNode.SIMPLE_NAME:{
				SimpleName simpleNode = (SimpleName)node;
				IBinding binding = simpleNode.resolveBinding();
				IVariableBinding varBinding = (IVariableBinding) binding;
				if (varBinding.isField()) {
					return AnnotateUnitEnum.f;
				}
				else if (varBinding.isParameter()) {
					return AnnotateUnitEnum.p;
				} 
				else{
					return AnnotateUnitEnum.v;
				}
			}
			case ASTNode.CAST_EXPRESSION:{
				CastExpression castNode = (CastExpression)node;
				Expression expression = castNode.getExpression();
				if(expression instanceof SimpleName){
					SimpleName sName = (SimpleName) expression;
					IBinding resolveBinding = sName.resolveBinding();
					if (resolveBinding instanceof IVariableBinding) {
						IVariableBinding varBinding = (IVariableBinding) resolveBinding;
						if (varBinding.isField()) {
							return AnnotateUnitEnum.f;
						}
						else if (varBinding.isParameter()) {
							return AnnotateUnitEnum.p;
						} 
						else{
							return AnnotateUnitEnum.v;
						}
					}
				}
				break;
			}
			case ASTNode.FIELD_ACCESS:{
				return AnnotateUnitEnum.f;
			}
			default:
				return AnnotateUnitEnum.Unknown;
			}
		}	
		else if(variable instanceof ThisVariable || variable instanceof TypeVariable){
			return AnnotateUnitEnum.Unknown;
		}
	
		return AnnotateUnitEnum.Unknown;

		//throw new IllegalStateException("Unexpected variable type: " + variable.getSourceString());
	}
	
	/**
	 * NOTE: This returns null, when called from a Method declared inside an enum! 
	 */
	public static TypeDeclaration getEnclosingType(ASTNode node) {
		ASTNode parent = node;
		while ((parent != null) && !(parent instanceof TypeDeclaration)) {
			parent = parent.getParent();
		}

		return (TypeDeclaration) parent;
	}	
	
	public static String getEnclosingTypeName(ASTNode node) {
		String ret = "";
		TypeDeclaration typeDecl = getEnclosingType(node); 
		if(typeDecl != null ) {
			ret = typeDecl.getName().getFullyQualifiedName();
		}
		
		return ret;

	}

	public static boolean isFinal(IVariableBinding receiverbinding) {
		boolean isFinal = false;
		if(receiverbinding!=null){
			int modifiers = receiverbinding.getModifiers();
			if(Modifier.isFinal(modifiers)){
				isFinal = true;
			}
		}
		return isFinal;
	}

	public static String getVarType(Variable variable) {
		IVariableBinding srcVariableBinding = null;
		if (variable instanceof TACVariable) {
			TACVariable srcVariable = (TACVariable) variable;
			srcVariableBinding = srcVariable.getVarDecl();
		} else if (variable instanceof SourceVariable) {
			SourceVariable srcVariable = (SourceVariable) variable;
			srcVariableBinding = srcVariable.getBinding();
		}
		return srcVariableBinding.getType().getQualifiedName();
	}

	public static boolean isParameter(Variable variable) {
		if (variable instanceof SourceVariable) {
			SourceVariable srcVariable = (SourceVariable) variable;
			IVariableBinding srcVariableBinding = srcVariable.getBinding();
			return srcVariableBinding.isParameter();
		}

		return false;
	}

	public static boolean isVarPublic(Variable variable) {
		IVariableBinding srcVariableBinding = null;
		if (variable instanceof TACVariable) {
			TACVariable srcVariable = (TACVariable) variable;
			srcVariableBinding = srcVariable.getVarDecl();
		} else if (variable instanceof SourceVariable) {
			SourceVariable srcVariable = (SourceVariable) variable;
			srcVariableBinding = srcVariable.getBinding();
		}

		return Modifier.isPublic(srcVariableBinding.getModifiers());
	}

	public static String getDeclaringClass(Variable variable) {
		String declType = "";
		IVariableBinding srcVariableBinding = null;
		if (variable instanceof TACVariable) {
			TACVariable srcVariable =  (TACVariable) variable;
			srcVariableBinding = srcVariable.getVarDecl();
			declType = srcVariableBinding.getDeclaringClass().getQualifiedName();
		} 
		else if (variable instanceof SourceVariable) {
			SourceVariable srcVariable = (SourceVariable) variable;
			srcVariableBinding = srcVariable.getBinding();
			declType = srcVariableBinding.getDeclaringMethod().getDeclaringClass().getQualifiedName();
		}
		else if (variable instanceof TACNewExpr) {
			TACNewExpr srcVariable = (TACNewExpr) variable;
			IMethodBinding constructorBinding = srcVariable.getConstructorBinding();
			declType = constructorBinding.getDeclaringClass().getQualifiedName();
		}

		return declType;
	}

	public static String getEnclosingMethod(Variable variable) {
		if (variable instanceof SourceVariable) {
			SourceVariable srcVariable = (SourceVariable) variable;
			IVariableBinding srcVariableBinding = srcVariable.getBinding();
			return srcVariableBinding.getDeclaringMethod().getName();
		}
		return "";
	}
	
	public static Set<OType> libraryMethod(IMethodBinding rBinding) {
		ITypeBinding mehtodReturnType = rBinding.getReturnType();
		boolean isTypeVariable = rBinding.getDeclaringClass().isParameterizedType();
		Set<OType> methodAUTypings = null;
		if(!mehtodReturnType.isPrimitive() && !Modifier.isStatic(rBinding.getModifiers())){
			methodAUTypings = createLibraryTypingSet(mehtodReturnType, isTypeVariable);
		}
		return methodAUTypings;
	}
	
	public static List<Set<OType>> libraryMethodParams(IMethodBinding rBinding) {
		List<Set<OType>> methodParsListTyping = new ArrayList<Set<OType>>();
		boolean isTypeVariable = (rBinding.getDeclaringClass().isParameterizedType() || (rBinding.getDeclaringClass().getSuperclass()!= null && rBinding.getDeclaringClass().getSuperclass().isParameterizedType()));
		for (ITypeBinding paramType : rBinding.getParameterTypes()) {
			if(!paramType.isPrimitive()){
				Set<OType> libraryMethodAUTypings = createLibraryTypingSet(paramType,isTypeVariable);
				methodParsListTyping.add(libraryMethodAUTypings);
			}
		}
		return methodParsListTyping;
	}
	
	public static Set<OType> createLibraryTypingSet(ITypeBinding variableType, boolean isATypeVariable) {
		Set<OType> libraryTypingSet = new HashSet<OType>();

		OType oType = null;
		// Handle generic/parameterized types
		if (variableType.isParameterizedType()) {
			oType = new OType("owner", "owner", "p");
			libraryTypingSet.add(oType);
			oType = new OType("owner", "p", "p");
			libraryTypingSet.add(oType);
			oType = new OType("p", "p", "p");
			libraryTypingSet.add(oType);
		}
		// Special case: String
		else if (variableType.getQualifiedName().equals("java.lang.String") ) {
			oType = new OType("shared","shared");
			libraryTypingSet.add(oType);
		}
		else {
			if(isATypeVariable){
				oType = new OType("p",null);
				libraryTypingSet.add(oType);
			}
			else if(variableType.getQualifiedName().equals("java.lang.Object")){
				oType = new OType("lent","p");
				libraryTypingSet.add(oType);
				oType = new OType("owner","p");
				libraryTypingSet.add(oType);
				oType = new OType("p","p");
				libraryTypingSet.add(oType);
//				oType = new OType("shared","shared");
//				libraryTypingSet.add(oType);
			}
			else
			{
				oType = new OType("lent","p");
				libraryTypingSet.add(oType);
				oType = new OType("owner","p");
				libraryTypingSet.add(oType);
				oType = new OType("p","p");
				libraryTypingSet.add(oType);
				oType = new OType("shared","shared");
				libraryTypingSet.add(oType);
			}
		}
		return libraryTypingSet;
	}
	
	public static void extractParametersFromSource(List<Variable> parametersVarList,IMethod javaElement, TM tm) {
		if (javaElement == null) {
			int debug = 0; debug++;
		}
		Option<MethodDeclaration> mDecl = WorkspaceUtilities.getMethodDeclFromModel(javaElement);
		MethodDeclaration mmDecl = mDecl.unwrap();
		if(mmDecl == null) {
			int debug = 0; debug++;
		}

		List<SingleVariableDeclaration> parameters = mmDecl.parameters();
		for (SingleVariableDeclaration param : parameters) {
			IVariableBinding paramBinding = param.resolveBinding();
			if(!paramBinding.getType().isPrimitive()){
				Variable paramVariable = tm.getVariableFromBindingKey(paramBinding.getKey());
				if(paramVariable!=null){
					parametersVarList.add(paramVariable);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param node: ASTNode corresponding to the TAC instruction that is being analyzed
	 * @return The qualified name of the enclosing class of the TAC instruction that is being analyzed
	 */
	public static String findEnclosingClassName(ASTNode node) {
		while(!(node instanceof TypeDeclaration)){
			node=node.getParent();
		}
		TypeDeclaration enclosingType = (TypeDeclaration)node;
		ITypeBinding enclosingClass = enclosingType.resolveBinding();
		String encClassName = enclosingClass.getQualifiedName();
		return encClassName;
	}
	
	/**
	 * 
	 * @param node: ASTNode corresponding to the TAC instruction that is being analyzed
	 * @return The type binding of the enclosing class of the TAC instruction that is being analyzed
	 */
	public static ITypeBinding findEnclosingClassBinding(ASTNode node) {
		while(!(node instanceof TypeDeclaration)){
			node=node.getParent();
		}
		TypeDeclaration enclosingType = (TypeDeclaration)node;
		ITypeBinding enclosingClass = enclosingType.resolveBinding();
		return enclosingClass;
	}
	
	/**
	 * 
	 * @param node: ASTNode corresponding to the TAC instruction that is being analyzed
	 * @return The method binding of the enclosing method of the TAC instruction that is being analyzed
	 */
	public static IMethodBinding findEnclosingMethodBinding(ASTNode node) {
		while(!(node instanceof MethodDeclaration)){
			if(node instanceof FieldDeclaration){
				return null;
			}
			node=node.getParent();
		}
		MethodDeclaration enclosingMethod = (MethodDeclaration)node;
		IMethodBinding enclosingMethodBinding = enclosingMethod.resolveBinding();
		return enclosingMethodBinding;
	}
}



