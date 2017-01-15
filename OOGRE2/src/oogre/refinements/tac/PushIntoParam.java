package oogre.refinements.tac;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import oog.itf.IObject;
import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.utils.Utils;
import ast.AstNode;
import ast.BaseTraceability;
import ast.MiniAstUtils;
import ast.TypeDeclaration;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public class PushIntoParam extends Refinement implements oog.re.IPushIntoParam {
	

	public PushIntoParam(IObject src, IObject dst) {
		// XXX. Domain name is hard-coded to be 'p'.
		super(src, dst, "p");
	}
	
	
	
	@Override
	public boolean refine(TM tm, TMSolutionType solIndex){
		//boolean isPublicParam = false;
		boolean isRefinementApplied = false;
		OOGContext context = OOGContext.getInstance();
		Map<IObject, HashSet<IObject>> pc = context.getPC();
		String dstTyp = getDstIObject().getC().getFullyQualifiedName();

		Set<BaseTraceability> traceability = this.getSrcIObject().getTraceability();
		for (BaseTraceability baseTraceability : traceability) {
			AstNode expression = baseTraceability.getExpression();
			TypeDeclaration srcObjNewExprType = PushIntoOwned.getTypeDeclaration(expression);
			String srcTyp = srcObjNewExprType.getFullyQualifiedName();
			Set<OType> newSourceTyping = new HashSet<OType>();

			//Enclosing ITypeBinding
			TypeDeclaration enclTypeDecl = MiniAstUtils.getEnclosingTypeDeclaration(baseTraceability.getExpression());
			String encType = enclTypeDecl.getFullyQualifiedName();

			for (Variable variable : tm.getVariables()) {
				if(variable instanceof TACNewExpr){
					TACNewExpr newExprVar = (TACNewExpr)variable;
					ITypeBinding constructorType = newExprVar.resolveType();
					String constructorTypeName = constructorType.getQualifiedName();
					ITypeBinding enclosingTypeBinding = newExprVar.getEnclosingTypeBinding();
					String enclosingTypeName = enclosingTypeBinding.getQualifiedName();

					if(constructorTypeName.equals(srcTyp) && enclosingTypeName.equals(encType)){
						boolean isMain = encType.equals(Config.MAINCLASS);
						String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(), isMain);
						if(solAlpha != null) {
							OType newSrcTyping = null;
							sourceType = newExprVar.resolveType();
							if(sourceType.isParameterizedType()){
								newSourceTyping = tm.initParametrizedTypeMapping("owner",isMain);
							}
							else{
								newSrcTyping = new OType("owner", solAlpha);
								newSourceTyping.add(newSrcTyping);
							}
							Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
							tm.putTypeMapping(variable, analysisResult);

							// Record the AU being modified directly based on the refinement
							//this.addAU(au);
							this.putVariableMap(variable, "owner");
							this.putVariableSetMap(variable, newSourceTyping);

							// Record the new set of typings
							this.setNewTyping(solIndex, newSrcTyping);  

							//context.addneedMoreInfoAU(au);

							isRefinementApplied = true;
						}
					}
				}
			}
		}
		if(!isRefinementApplied){
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
	private ITypeBinding sourceType;

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
	
	@Override
	public ITypeBinding srcObjectType() {
		return this.sourceType;
	}
	
}
