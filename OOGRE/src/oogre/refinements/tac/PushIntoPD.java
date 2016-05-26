package oogre.refinements.tac;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oog.itf.IObject;
import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.utils.Utils;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;

import ast.BaseTraceability;
import ast.MiniAstUtils;
import ast.TypeDeclaration;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public class PushIntoPD extends Refinement implements oog.re.IPushIntoPD  {

	public PushIntoPD(IObject src, IObject dst, String domainName){
		super(src, dst, domainName);
	}

	private oog.re.Refinement realRefinement;

	@Override
	public boolean refine(TM tm, TMSolutionType solIndex){
		boolean isPossible = false;
		boolean isRefinementApplied = false;
		OOGContext context = OOGContext.getInstance();
		String dstTyp = getDstIObject().getC().getFullyQualifiedName();
		Map<IObject, HashSet<IObject>> pc = context.getPC();

		Set<BaseTraceability> traceability = this.getSrcIObject().getTraceability();
		for (BaseTraceability baseTraceability : traceability) {
			TypeDeclaration srcObjNewExprType = PushIntoOwned.getTypeDeclaration(baseTraceability.getExpression());
			String srcTyp = srcObjNewExprType.getFullyQualifiedName();

			//Enclosing ITypeBinding
			TypeDeclaration enclTypeDecl = MiniAstUtils.getEnclosingTypeDeclaration(baseTraceability.getExpression());
			String encType = enclTypeDecl.getFullyQualifiedName();

			Set<OType> newSourceTyping =  new HashSet<OType>();
			for (Variable variable : tm.getVariables())
				//DO not change the annotation of AUs that are method parameters or method returns
				// srcType <: au.getType() 
				if(variable instanceof TACVariable || variable instanceof SourceVariable){
    				Variable srcVariable = null;
    				IVariableBinding srcVariableBinding = null;
    				if(variable instanceof TACVariable){
    					srcVariable = (TACVariable)variable;
    					srcVariableBinding = ((TACVariable)srcVariable).getVarDecl();
    				}
    				else{
    					srcVariable = (SourceVariable)variable;
    					srcVariableBinding = ((SourceVariable)srcVariable).getBinding();
    				}
    				
    				ITypeBinding srcVariableEnclosingType = null;
    				if(srcVariableBinding.isField()){
    					srcVariableEnclosingType = srcVariableBinding.getDeclaringClass();
    				}
    				else{
    					srcVariableEnclosingType = srcVariableBinding.getDeclaringMethod().getDeclaringClass();
    				}
					if( !dstTyp.equals("DUMMY")){
						if(Utils.isSubtypeCompatible(srcVariable.resolveType().getQualifiedName(),srcObjNewExprType.getFullyQualifiedName()) && 
								Utils.isSubtypeCompatible(srcVariableEnclosingType.getQualifiedName(),dstTyp)){
							boolean isMain = srcVariableEnclosingType.equals(Config.MAINCLASS);
							String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(),isMain);
							if(solAlpha!=null){
								OType newSrcTyping = null;
								ITypeBinding sourceType = srcVariableBinding.getType();
								if(sourceType.isParameterizedType()){
									ITypeBinding[] typeArguments = sourceType.getTypeArguments();
									for (ITypeBinding iTypeBinding : typeArguments) {
										for (Variable paramTypeAu : tm.keySet()) {
											if(paramTypeAu.resolveType().equals(iTypeBinding)){
												Set<OType> paramAUTyping = tm.getTypeMapping(paramTypeAu);
												for (OType oType : paramAUTyping) {
													newSrcTyping = new OType("this.PD", oType.getOwner(), oType.getAlpha());
													newSourceTyping.add(newSrcTyping);
												}
												break;
											}
										}
									}
								}
								else{
									newSrcTyping = new OType("this.PD", solAlpha);
									newSourceTyping.add(newSrcTyping);
								}
								Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
								tm.putTypeMapping(variable, analysisResult);

								// Record the AU being modified directly based on the refinement
								this.putVariableMap(variable, newSourceTyping);

								// Record the new set of typings
								this.setNewTyping(solIndex,newSrcTyping);  

								//context.addneedMoreInfoAU(variable);
								isPossible = true;

								isRefinementApplied = true;
							}
						}
					}
					else{
						if(Utils.isSubtypeCompatible(srcVariable.resolveType().getQualifiedName(),srcObjNewExprType.getFullyQualifiedName()) && 
								Utils.isSubtypeCompatible(srcVariableEnclosingType.getQualifiedName(),dstTyp)){
							OType newSrcTyping = new OType(srcTyp, "shared", "shared");
							newSourceTyping.add(newSrcTyping);

							Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
							tm.putTypeMapping(variable, analysisResult);

							// Record the AU being modified directly based on the refinement
							this.putVariableMap(variable, newSourceTyping);

							// Record the new set of typings
							this.setNewTyping(solIndex,newSrcTyping);  

							//context.addneedMoreInfoAU(variable);
							isPossible = true;

							isRefinementApplied = true;
						}
					}
				}
		}

		if(!isPossible){
			tm.isDiscarded = true;
			StringBuilder reason = new StringBuilder();
			reason.append("There is no field of type: ");
			reason.append(this.getDstIObject().getC());
			reason.append(" in the class ");
			reason.append(this.getSrcIObject().getC());
			reason.append(" to access its public domain.");
			tm.discardReason = reason.toString();
			return isRefinementApplied;
		}
		return isRefinementApplied;
	}

	@Override
	public oog.re.Refinement getReal() {
		if (realRefinement == null) {
			realRefinement = new oog.re.PushIntoPD(getSrcObject(), getDstObject(), getDomainName());
		}
		return realRefinement;
	}

	@Override
	public RefinementType getRefinementType() {
		return RefinementType.PushIntoPD;
	}
}
