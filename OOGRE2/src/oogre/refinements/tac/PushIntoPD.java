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

public class PushIntoPD extends Refinement implements oog.re.IPushIntoPD  {

	public PushIntoPD(IObject src, IObject dst, String domainName){
		super(src, dst, domainName);
	}

	private oog.re.Refinement realRefinement;
	private ITypeBinding sourceType;

	@Override
	public boolean refine(TM tm, TMSolutionType solIndex){
		boolean isPossible = false;
		boolean isRefinementApplied = false;
		OOGContext context = OOGContext.getInstance();
		String dstTyp = getDstIObject().getC().getFullyQualifiedName();
		Map<IObject, HashSet<IObject>> pc = context.getPC();

		Set<BaseTraceability> traceability = this.getSrcIObject().getTraceability();
		for (BaseTraceability baseTraceability : traceability) {
			AstNode expression = baseTraceability.getExpression();
			TypeDeclaration srcObjNewExprType = PushIntoOwned.getTypeDeclaration(expression);
			String srcTyp = srcObjNewExprType.getFullyQualifiedName();

			//Enclosing ITypeBinding
			TypeDeclaration enclTypeDecl = MiniAstUtils.getEnclosingTypeDeclaration(baseTraceability.getExpression());
			String encType = enclTypeDecl.getFullyQualifiedName();

			Set<OType> newSourceTyping =  new SetOType<OType>();
			for (Variable variable : tm.getVariables())
				//DO not change the annotation of AUs that are method parameters or method returns
				// srcType <: au.getType() 
				if(variable instanceof TACNewExpr){
					TACNewExpr newExprVar = (TACNewExpr)variable;
					ITypeBinding constructorType = newExprVar.resolveType();
					String constructorTypeName = constructorType.getQualifiedName();
					ITypeBinding enclosingTypeBinding = newExprVar.getEnclosingTypeBinding();
					String enclosingTypeName = enclosingTypeBinding.getQualifiedName();

					if( !dstTyp.equals("DUMMY")){
						if(constructorTypeName.equals(srcTyp) && enclosingTypeName.equals(dstTyp)){
							boolean isMain = encType.equals(Config.MAINCLASS);
							String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(),isMain);
							if(solAlpha!=null){
								OType newSrcTyping = null;
								sourceType = newExprVar.resolveType();
								if(sourceType.isParameterizedType()){
									newSourceTyping = tm.initParametrizedTypeMapping("this.PD",isMain);
								}
								else{
									newSrcTyping = new OType("this.PD", solAlpha);
									newSourceTyping.add(newSrcTyping);
								}
								Set<OType> analysisResult = new SetOType<OType>(newSourceTyping);
								tm.putTypeMapping(variable, analysisResult);

								// Record the AU being modified directly based on the refinement
								this.putVariableMap(variable, "this.PD");
								this.putVariableSetMap(variable, newSourceTyping);

								// Record the new set of typings
								this.setNewTyping(solIndex,newSrcTyping);  

								//context.addneedMoreInfoAU(variable);
								isPossible = true;

								isRefinementApplied = true;
							}
						}
					}
					else{
						if(constructorTypeName.equals(srcTyp) && enclosingTypeName.equals(encType)){
							OType newSrcTyping = null;
							sourceType = newExprVar.resolveType();
							if(sourceType.isParameterizedType()){
								newSrcTyping = new OType("shared", "shared","shared");
								newSourceTyping.add(newSrcTyping);
							}
							else{
								newSrcTyping = new OType("shared", "shared");
								newSourceTyping.add(newSrcTyping);
							}

							Set<OType> analysisResult = new SetOType<OType>(newSourceTyping);
							tm.putTypeMapping(variable, analysisResult);

							// Record the AU being modified directly based on the refinement
							this.putVariableMap(variable, "shared");
							this.putVariableSetMap(variable, newSourceTyping);

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
	
	@Override
	public ITypeBinding srcObjectType() {
		return this.sourceType;
	}
}
