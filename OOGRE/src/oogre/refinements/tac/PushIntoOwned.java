package oogre.refinements.tac;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

import oog.itf.IObject;
import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.utils.Utils;
import ast.AstNode;
import ast.BaseTraceability;
import ast.TypeDeclaration;

public class PushIntoOwned extends Refinement implements oog.re.IPushIntoOwned {
        
    public PushIntoOwned(IObject src, IObject dst) {
    		// XXX. Domain name is hard-coded to be owned.
            super(src, dst, "owned");
            
    }
    
    

    /**
     * TOEBI: Implement me as follows:...
     * Get the declaration type of an expression
     * 
     * 
     * XXX. Move to Util method or even MiniAst
     * 
     * @param expression
     * @return
     */
    public static ast.TypeDeclaration getTypeDeclaration(AstNode expression) {
    	ast.TypeDeclaration typeDecl = null;
    	
    	if(expression instanceof ast.ClassInstanceCreation ) {
    		ast.ClassInstanceCreation newExpression = (ast.ClassInstanceCreation)expression;
    		typeDecl = newExpression.typeDeclaration;
    	}
    	
    	return typeDecl;
    	
    }
    
    //TODO: investigate the inferring owner in Milanova's example.
    @Override
    public boolean refine(TM tm, TMSolutionType solIndex){
    	//boolean isPublicParam = false;
    	boolean isRefinementApplied = false;
    	OOGContext context = OOGContext.getInstance();
        Map<IObject, HashSet<IObject>> pc = context.getPC();
        String dstTyp = getDstIObject().getC().getFullyQualifiedName();

        if(pc.get(getSrcIObject()).contains(getDstIObject())){
        	Set<BaseTraceability> traceability = this.getSrcIObject().getTraceability();
        	for (BaseTraceability baseTraceability : traceability) {
        		TypeDeclaration srcObjNewExprType = getTypeDeclaration(baseTraceability.getExpression());
        		Set<OType> newSourceTyping = new HashSet<OType>();

        		for (Variable variable : tm.getVariables()) {
        			//if(au.isTypeEqual(srcObjNewExprTypeName) && au.isEnclosingTypeEqual(dstTyp)){
        			if(variable instanceof TACVariable || variable instanceof SourceVariable){
        				IVariableBinding srcVariableBinding = null;
        				if(variable instanceof TACVariable){
        					TACVariable srcVariable = (TACVariable)variable;
        					srcVariableBinding =  srcVariable.getVarDecl();
        				}
        				else{
        					SourceVariable srcVariable = (SourceVariable)variable;
        					srcVariableBinding = srcVariable.getBinding();
        				}
        				
        				ITypeBinding srcVariableEnclosingType = null;
        				if(srcVariableBinding.isField()){
        					srcVariableEnclosingType = srcVariableBinding.getDeclaringClass();
        				}
        				else{
        					srcVariableEnclosingType = srcVariableBinding.getDeclaringMethod().getDeclaringClass();
        				}
        				
        				if(Utils.isSubtypeCompatible(Utils.getVarType(variable),srcObjNewExprType.getFullyQualifiedName()) && 
        						Utils.isSubtypeCompatible(srcVariableEnclosingType.getQualifiedName(),dstTyp)){

        					// Do we need to check is the source variable is not a public param?
        					// We only want to change the left hand side of a new expression
        					//                			if(srcVariableBinding.isParameter()){
        					//                				if(Modifier.isPublic(srcVariableBinding.getModifiers())){
        					//                					isPublicParam = true;
        					//                				}
        					//                			}
        					boolean isMain = srcVariableEnclosingType.equals(Config.MAINCLASS);
        					String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(), isMain);
        					if(solAlpha != null ) {
        						OType newSrcTyping = null;
        						ITypeBinding sourceType = srcVariableBinding.getType();
								if(sourceType.isParameterizedType()){
									ITypeBinding[] typeArguments = sourceType.getTypeArguments();
									for (ITypeBinding iTypeBinding : typeArguments) {
										for (Variable paramTypeAu : tm.keySet()) {
											if(paramTypeAu.resolveType().equals(iTypeBinding)){
												Set<OType> paramAUTyping = tm.getTypeMapping(paramTypeAu);
												for (OType oType : paramAUTyping) {
													newSrcTyping = new OType("this.owned", oType.getOwner(), oType.getAlpha());
													newSourceTyping.add(newSrcTyping);
												}
												break;
											}
										}
									}
								}
        						else{
        							newSrcTyping = new OType("this.owned", solAlpha);
        							newSourceTyping.add(newSrcTyping);
        						}
								Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
        						tm.putTypeMapping(variable, analysisResult);

        						// Record the AU being modified directly based on the refinement
        						//this.addAU(au);
        						this.putVariableMap(variable, newSourceTyping);
        						
        						// Record the new set of typings
        						this.setNewTyping(solIndex, newSrcTyping);  

        						//context.addneedMoreInfoAU(au);

        						isRefinementApplied = true;
        					}
        				}
        			}
                }
            }
        }
        else{
        	tm.isDiscarded = true;
            StringBuilder reason = new StringBuilder();
            reason.append("The source object, ");
            reason.append(this.getSrcIObject().getInstanceDisplayName());
            reason.append(":");
            reason.append(this.getSrcIObject().getC());
            reason.append(" is not created in the context of the destination obejct, ");
            reason.append(this.getDstIObject().getInstanceDisplayName());
            reason.append(":");
            reason.append(this.getDstIObject().getC());
            tm.discardReason = reason.toString();
			return isRefinementApplied;
        }
        
        return isRefinementApplied;
    }



	private oog.re.Refinement realRefinement;
	
	@Override
	public oog.re.Refinement getReal() {
		if (realRefinement == null ) {
			realRefinement = new oog.re.PushIntoOwned(getSrcObject(), getDstObject(), getDomainName());
		}
	    return realRefinement;
	}

	@Override
	public RefinementType getRefinementType() {
		return RefinementType.PushIntoOwned;
	}
	
}