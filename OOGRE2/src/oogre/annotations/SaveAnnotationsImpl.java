package oogre.annotations;


import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.refinements.tac.OType;
import oogre.refinements.tac.RankingStrategy;
import oogre.refinements.tac.TACMethod;
import oogre.refinements.tac.TACVariable;
import oogre.refinements.tac.TM;
import oogre.refinements.tac.ThatThisSubst;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * TODO: HIGH. Pass appropriate data structures to look up annotation for AnnotatableUnit 
 * Data structures: PF, TM, etc.
 * Lookup: 
 * AST FieldDeclaration: find the field name, the enclosing class; use that to lokup the annotation from XX; return a string.
 * etc.
 *
 * XXX. Remove hard-coded p<p>, shared<shared>. It is not the responsibility of this class to figure these out.
 * The only resonsibility of this class is to convert typings from TM to Java 1.5 annotations
 * 
 * XXX. Fix iteration over TM. Here, could be looking things up very precisely.
 * 
 * XXX. XXX. Code is very messy. Very duplicative.
 * - split looking up an AU from converting AU Typing to a String
 * - that second part is repeated 4 times, once for each AU type
 * -- if(!isRefined){
 */
public class SaveAnnotationsImpl implements SaveAnnotationStrategy {
	
	private RankingStrategy ranking = RankingStrategy.getInstance();

	private TM tm;
	
	private OOGContext context = OOGContext.getInstance();
	
	/**
	 * Hard-code a default value to avoid saving an empty string annotation.
	 * @Domain("")C f; 
	 * 
	 * causes PointsToOOG to crash.
	 * 
	 * Use shared<shared>. But probably need to issue a warning too.
	 * To distinguish between valid cases of shared<shared> and cases where cannot find something in TM.
	 * so we end up using the hard-coded default.
	 *  
	 */
	private static final String DEFAULT = "";

	public SaveAnnotationsImpl(TM tm) {
	    super();
	    
	    this.tm = tm;
    }

	@Override
    public String getAnnotationForFieldDeclaration(FieldDeclaration fieldDecl) {
 
		IVariableBinding fieldVariableBinding = null;
		List<VariableDeclarationFragment> fragments = fieldDecl.fragments();
		for (VariableDeclarationFragment frg : fragments) {
			fieldVariableBinding = frg.resolveBinding();
		}
		
		//BodyDeclaration bodyDecl = (BodyDeclaration)fieldDecl.getParent();
		TypeDeclaration typeDecl = (TypeDeclaration)fieldDecl.getParent();
		ITypeBinding resolveBinding = typeDecl.resolveBinding();
		
		// Support Generic types 
		String fieldEnclosingType = getEnclosingType(resolveBinding);
		
		Set<OType> fieldOType=null;
		OType selectedType = null;
		
		// Iterates over TM. Because need to go from Eclipse ASTNode to TAC SourceVariable
		// XXX. Is there a better way to do this?
		for (Entry<Variable, Set<OType>> entry : tm.entrySet()) {
			Variable au = entry.getKey();
			if(au instanceof TACVariable){
				TACVariable srcVariable = (TACVariable)au;
				if(srcVariable.getVarDecl().getKey().equals(fieldVariableBinding.getKey())){
					fieldOType = entry.getValue();
					selectedType = ranking.pickFromSet(fieldOType);
					break;
				}
			}
		}
		
		String resultAnnotation = DEFAULT;

		
		//XXX. Get the first element of set of add as annotation for now
		//The correct way is to select one based on a "ranking strategy"
		// XXX. This is very hackish. Review and clean this up.
		if(fieldOType!=null){
			StringBuilder annotation = new StringBuilder();
			if(!fieldEnclosingType.equals(Config.MAINCLASS)){
				if(this.tm.isInitial()){
					OType defaultTyping = new OType("owner","owner","owner");
					if(!fieldVariableBinding.getType().isTypeVariable()){
						annotation.append(defaultTyping.getOwner());
						annotation.append("<");
						annotation.append(defaultTyping.getAlpha());
						if(fieldVariableBinding.getType().isParameterizedType()){
							annotation.append("<");
							annotation.append(defaultTyping.getInner());
							annotation.append(">");
						}
						annotation.append(">");
					}
					else{
						annotation.append("p");
					}
					resultAnnotation = annotation.toString();
				}
				else{
					annotation.append(ThatThisSubst.unQualify(selectedType.getOwner()));
					alphaInnerBuider(annotation, selectedType);
					resultAnnotation = annotation.toString();
				}
			}
			else{
				if(this.tm.isInitial()){
					OType defaultTyping = new OType("shared","shared","shared");
					annotation.append(defaultTyping.getOwner());
					if(!fieldVariableBinding.getType().isTypeVariable()){
						annotation.append("<");
						annotation.append(defaultTyping.getAlpha());
						if(fieldVariableBinding.getType().isParameterizedType()){
							annotation.append("<");
							annotation.append(defaultTyping.getInner());
							annotation.append(">");
						}
						annotation.append(">");
					}
					resultAnnotation = annotation.toString();
				}
				else{
					annotation.append(ThatThisSubst.unQualify(selectedType.getOwner()));
					alphaInnerBuider(annotation, selectedType);
					resultAnnotation = annotation.toString();
				}
			}
		}

		
		
	    return resultAnnotation;
    }

	public void alphaInnerBuider(StringBuilder annotation, OType selectedType) {
		if(selectedType.getAlpha()!=null){
			annotation.append("<");
			annotation.append(ThatThisSubst.unQualify(selectedType.getAlpha()));
			if(selectedType.getInner()!=null){
				annotation.append("<");
				annotation.append(ThatThisSubst.unQualify(selectedType.getInner()));
				annotation.append(">");
			}
			annotation.append(">");
		}
	}

	@Override
    public String getAnnotationForMethodParameter(MethodDeclaration methDecl, SingleVariableDeclaration param) {
		String paramType = param.getType().resolveBinding().getQualifiedName();
		String paramName = param.getName().getFullyQualifiedName();
		
		
	    ASTNode node = param.getParent();
		while(!(node instanceof TypeDeclaration)){
			node=node.getParent();
		}
		TypeDeclaration enclosingType = (TypeDeclaration)node;
		ITypeBinding enclosingClass = enclosingType.resolveBinding();
		
		String mthdDeclSig = context.getSignature(methDecl);
	
		//BodyDeclaration bodyDecl = (BodyDeclaration)methDecl.getParent();
		TypeDeclaration typeDecl = (TypeDeclaration)methDecl.getParent();
		ITypeBinding resolveBinding = typeDecl.resolveBinding();
		
		// Support Generic types
		String paramEnclosingType = getEnclosingType(resolveBinding);
		
		// Iterates over TM. Because need to go from Eclipse ASTNode to TAC SourceVariable
		// XXX. Is there a better way to do this? 
		Set<OType> paramOType=null;
		OType selectedType = null;
		for (Entry<Variable, Set<OType>> entry : tm.entrySet()) {
			Variable au = entry.getKey();
			if(au instanceof SourceVariable){
				SourceVariable srcVariable = (SourceVariable)au;
				if(srcVariable.getBinding().getKey().equals(param.resolveBinding().getKey())){
					paramOType = entry.getValue();
					selectedType = ranking.pickFromSet(paramOType);
					break;
				}
			}
		}
		
		// A paramter which was not used as a SourceVariable, e.g. "String[] args"
/*		if(paramOType==null){
			for (Entry<Variable, Set<OType>> entry : tm.entrySet()) {
				Variable au = entry.getKey();
				if(au instanceof TACVariable){
					TACVariable srcVariable = (TACVariable)au;
					if(srcVariable.getVarDecl().equals(param.resolveBinding())){
						paramOType = entry.getValue().asReadOnlySet();
						selectedType = ranking.pickFromSet(paramOType);
						break;
					}
				}
			}
		}*/

		String resultAnnotation = DEFAULT;

		if(paramOType!=null){
			StringBuilder annotation = new StringBuilder();
			if(paramType.equals("java.lang.String[]") &&  methDecl.getName().toString().equals("main") && Modifier.isStatic(methDecl.getModifiers()) && enclosingClass.getQualifiedName().equals(Config.MAINCLASS)){
				annotation.append("lent");
				annotation.append("[");
				annotation.append("shared");
				annotation.append("]");
				resultAnnotation = annotation.toString();
			}
			else{
				if(!paramEnclosingType.equals(Config.MAINCLASS)){
					if(this.tm.isInitial()){
						OType defaultTyping = new OType("owner","owner","owner");
						if(!param.resolveBinding().getType().isTypeVariable()){
							annotation.append(defaultTyping.getOwner());
							annotation.append("<");
							if(param.getType().isParameterizedType()){
								ITypeBinding[] typeArguments = param.getType().resolveBinding().getTypeArguments();
								if(!typeArguments[0].isTypeVariable()){
									annotation.append(defaultTyping.getAlpha());
									annotation.append("<");
									annotation.append(defaultTyping.getInner());
									annotation.append(">");
								}
								else{
									annotation.append("p");
								}
							}
							else{
								annotation.append(defaultTyping.getAlpha());
							}
							annotation.append(">");
						}
						else{
							annotation.append("p");
						}
						resultAnnotation = annotation.toString();
					}
					else{
						annotation.append(ThatThisSubst.unQualify(selectedType.getOwner()));
						alphaInnerBuider(annotation, selectedType);
						resultAnnotation = annotation.toString();
					}
				}
				else{
					annotation.append(ThatThisSubst.unQualify(selectedType.getOwner()));
					alphaInnerBuider(annotation, selectedType);
					resultAnnotation = annotation.toString();
				}
			}
		}
		
		
	    return resultAnnotation;
    }

	@Override
    public String getAnnotationForMethodReturn(MethodDeclaration methDecl) {
	    
		ITypeBinding returnTypeBinding = methDecl.resolveBinding().getReturnType();
	
		//BodyDeclaration bodyDecl = (BodyDeclaration)methDecl.getParent();
		TypeDeclaration typeDecl = (TypeDeclaration)methDecl.getParent();
		ITypeBinding resolveBinding = typeDecl.resolveBinding();
		
		// Support Generic types 
		String methodEnclosingType = getEnclosingType(resolveBinding);
		
		Set<OType> methodOType=null;
		OType selectedType = null;
		
		// Iterates over TM. Because need to go from Eclipse ASTNode to TAC SourceVariable
		// XXX. Is there a better way to do this? For Method Return		
		for (Entry<Variable, Set<OType>> entry : tm.entrySet()) {
			Variable au = entry.getKey();
			if(au instanceof TACMethod){
				TACMethod methodVariable = (TACMethod)au;
				if(methodVariable.getMethDecl().getKey().equals(methDecl.resolveBinding().getKey())){
					methodOType = entry.getValue();
					selectedType = ranking.pickFromSet(methodOType);
					break;
				}
			}
		}
		
		String resultAnnotation = DEFAULT;
		
		if(methodOType!=null){
			StringBuilder annotation = new StringBuilder();
			if(!methodEnclosingType.equals(Config.MAINCLASS)){
				if(this.tm.isInitial()){
					OType defaultTyping = new OType("owner","owner","owner");
					if(!returnTypeBinding.isTypeVariable()){
						annotation.append(defaultTyping.getOwner());
						annotation.append("<");
						if(returnTypeBinding.isParameterizedType()){
							ITypeBinding[] typeArguments = returnTypeBinding.getTypeArguments();
							if(!typeArguments[0].isTypeVariable()){
								annotation.append(defaultTyping.getAlpha());
								annotation.append("<");
								annotation.append(defaultTyping.getInner());
								annotation.append(">");
							}
							else{
								annotation.append("p");
							}
						}
						else{
							annotation.append(defaultTyping.getAlpha());
						}
						annotation.append(">");
					}
					else{
						annotation.append("p");
					}
					resultAnnotation = annotation.toString();
				}
				else{
					annotation.append(ThatThisSubst.unQualify(selectedType.getOwner()));
					alphaInnerBuider(annotation, selectedType);
					resultAnnotation = annotation.toString();
				}
			}
			else{
				if(this.tm.isInitial()){
					OType defaultTyping = new OType("shared","shared","shared");
					annotation.append(defaultTyping.getOwner());
					if(!returnTypeBinding.isTypeVariable()){
						annotation.append("<");
						annotation.append(defaultTyping.getAlpha());
						if(returnTypeBinding.isParameterizedType()){
							ITypeBinding[] typeArguments = returnTypeBinding.getTypeArguments();
							if(!typeArguments[0].isTypeVariable()){
								annotation.append("<");
								annotation.append(defaultTyping.getInner());
								annotation.append(">");
							}
						}
						annotation.append(">");
					}
					resultAnnotation = annotation.toString();
				}
				else{
					annotation.append(ThatThisSubst.unQualify(selectedType.getOwner()));
					alphaInnerBuider(annotation, selectedType);
					resultAnnotation = annotation.toString();
				}
			}
		}
		
		
	    return resultAnnotation;
    }

	Type getDeclaredType(VariableDeclarationFragment frg) {
		ASTNode parent = frg.getParent();
		if(parent instanceof VariableDeclarationStatement ) {
			VariableDeclarationStatement varDecl = (VariableDeclarationStatement)parent;
			return varDecl.getType();
		}
		else if (parent instanceof VariableDeclarationExpression ) {
			VariableDeclarationExpression varExpr = (VariableDeclarationExpression)parent;
			return varExpr.getType();
		}
		
		return null;
	}
	
	@Override
    public String getAnnotationForLocalVariable(VariableDeclarationFragment frg) {
		Type declaredType = getDeclaredType(frg);
		IVariableBinding variableBinding = frg.resolveBinding();
		ITypeBinding variableTypeBinding = variableBinding.getType();
		
		String varType = declaredType != null ? declaredType.resolveBinding().getQualifiedName() : "";
		
		ASTNode node = frg.getParent();
		while(!(node instanceof TypeDeclaration)){
			node=node.getParent();
			if(node instanceof MethodDeclaration){
				MethodDeclaration mthdDeclNode = (MethodDeclaration)node;
			}
		}
		TypeDeclaration enclosingType = (TypeDeclaration)node;
		ITypeBinding enclosingClass = enclosingType.resolveBinding();
		
		// Support Generic types 
		String enclosingClassName = getEnclosingType(enclosingClass);
		
		Set<OType> variableOType=null;
		OType selectedType = null;
		
		// Iterates over TM. Because need to go from Eclipse ASTNode to TAC SourceVariable
		// XXX. Is there a better way to do this? For Method Return		
		for (Entry<Variable, Set<OType>> entry : tm.entrySet()) {
			Variable au = entry.getKey();
			if(au instanceof SourceVariable){
				SourceVariable srcVariable = (SourceVariable)au;
				if(srcVariable.getBinding().getKey().equals(variableBinding.getKey())){
					variableOType = entry.getValue();
					selectedType = ranking.pickFromSet(variableOType);
					break;
				}
			}
		}
		
		// HACK: Use default instead of leaving empty; because empty will cause PointsTo to crash?
		String resultAnnotation = DEFAULT;
		
		if(variableOType!=null){
			StringBuilder annotation = new StringBuilder();
			if(!enclosingClassName.equals(Config.MAINCLASS)){
				if(this.tm.isInitial()){
					OType defaultTyping = new OType("owner","owner","owner");
					if(!variableTypeBinding.isTypeVariable()){
						annotation.append(defaultTyping.getOwner());
						annotation.append("<");
						if(variableTypeBinding.isParameterizedType()){
							ITypeBinding[] typeArguments = variableTypeBinding.getTypeArguments();
							if(!typeArguments[0].isTypeVariable()){
								annotation.append(defaultTyping.getAlpha());
								annotation.append("<");
								annotation.append(defaultTyping.getInner());
								annotation.append(">");
							}
							else{
								annotation.append("p");
							}
						}
						else{
							annotation.append(defaultTyping.getAlpha());
						}
						annotation.append(">");
					}
					else{
						annotation.append("p");
					}
					resultAnnotation = annotation.toString();
				}
				else{
					annotation.append(ThatThisSubst.unQualify(selectedType.getOwner()));
					alphaInnerBuider(annotation, selectedType);
					resultAnnotation = annotation.toString();
				}
			}
			else{
				if(this.tm.isInitial()){
					OType defaultTyping = new OType("shared","shared","shared");
					if(!varType.equals(Config.MAINCLASS)){
						annotation.append(defaultTyping.getOwner());
						if(!variableTypeBinding.isTypeVariable()){
							annotation.append("<");
							annotation.append(defaultTyping.getAlpha());
							if(variableTypeBinding.isParameterizedType()){
								ITypeBinding[] typeArguments = variableTypeBinding.getTypeArguments();
								if(!typeArguments[0].isTypeVariable()){
									annotation.append("<");
									annotation.append(defaultTyping.getInner());
									annotation.append(">");
								}
							}
							annotation.append(">");
						}
						resultAnnotation = annotation.toString();
					}
					else{
						annotation.append(defaultTyping.getOwner());
						resultAnnotation = annotation.toString();
					}
				}
				else{
					if(variableTypeBinding.getQualifiedName().equals(Config.MAINCLASS)){
						annotation.append("shared");
						resultAnnotation = annotation.toString();
					}
					else{
						annotation.append(ThatThisSubst.unQualify(selectedType.getOwner()));
						alphaInnerBuider(annotation, selectedType);
						resultAnnotation = annotation.toString();
					}
				}
			}
		}
		
		
	    return resultAnnotation;
    }

	public TM getTM() {
    	return this.tm;
    }
	
	public static String getEnclosingType(ITypeBinding type){
		String enclosingType = "";
		if(type.isGenericType()){
			enclosingType = type.getQualifiedName() + "<";
			ITypeBinding[] typeArguments = type.getTypeParameters();
			// XXX. Coverage: make sure we can handle multiple generic type args/params
			for (ITypeBinding iTypeBinding : typeArguments) {
				enclosingType += iTypeBinding.getQualifiedName();
			}
			enclosingType+=">";
		}
		else{
			enclosingType = type.getQualifiedName();
		}
		return enclosingType;
	}
}
