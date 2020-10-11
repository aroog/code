package oogre.refinements.tac;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oog.itf.IObject;
import oogre.analysis.Config;
import oogre.analysis.OOGContext;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ast.AstNode;
import ast.BaseTraceability;
import ast.MiniAstUtils;
import ast.TypeDeclaration;
import edu.cmu.cs.crystal.tac.model.Variable;

public class PushIntoOwned extends Refinement implements oog.re.IPushIntoOwned {

	public PushIntoOwned(IObject src, IObject dst) {
		// XXX. Domain name is hard-coded to be owned.
		super(src, dst, "owned");

	}

	private ITypeBinding sourceType = null;

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

		Set<BaseTraceability> traceability = this.getSrcIObject().getTraceability();
		for (BaseTraceability baseTraceability : traceability) {
			AstNode expression = baseTraceability.getExpression();
			TypeDeclaration srcObjNewExprType = PushIntoOwned.getTypeDeclaration(expression);
			String srcTyp = srcObjNewExprType.getFullyQualifiedName();
			Set<OType> newSourceTyping = new SetOType<OType>();

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

					if(constructorTypeName.equals(srcTyp) && enclosingTypeName.equals(dstTyp)){
						boolean isMain = encType.equals(Config.MAINCLASS);
						String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(), isMain);
						if(solAlpha != null ) {
							OType newSrcTyping = null;
							sourceType = newExprVar.resolveType();
							if(sourceType.isParameterizedType()){
								newSourceTyping = tm.initParametrizedTypeMapping("this.owned",isMain);
							}
							else{
								newSrcTyping = new OType("this.owned", solAlpha);
								newSourceTyping.add(newSrcTyping);
							}
							Set<OType> analysisResult = new SetOType<OType>(newSourceTyping);
							tm.putTypeMapping(variable, analysisResult);

							// Record the AU being modified directly based on the refinement
							//this.addAU(au);
							this.putVariableMap(variable, "this.owned");
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



	@Override
	public ITypeBinding srcObjectType() {
		return this.sourceType;
	}

}