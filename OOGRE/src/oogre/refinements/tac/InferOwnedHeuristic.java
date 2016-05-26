package oogre.refinements.tac;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import oogre.analysis.Config;
import oogre.analysis.OOGContext;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.flow.worklist.AnalysisResult;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public class InferOwnedHeuristic extends Heuristic {

	private OOGContext context = OOGContext.getInstance();

	private Variable changedVar;

	public InferOwnedHeuristic(Variable changedAU){
		this.changedVar = changedAU;
	}

	@Override
	public boolean apply(TM tm, TMSolutionType solIndex){
		boolean isHeuristicApplied = false;
		boolean isPublicParam = false;
		Set<Entry<Variable, Set<OType>>> entrySet = tm.entrySet();
		Set<OType> newSourceTyping = new HashSet<OType>();
		// XXX. Can we replace iteration with just 1 lookup: entrySet.get()?
		for (Entry<Variable, Set<OType>> entry : entrySet) {
			Variable var = entry.getKey();	
			if(var.equals(this.changedVar) /*au.isTypeEqual(auType) && au.isEnclosingTypeEqual(auEnclosingType)*/){
				
				if(var instanceof TACVariable || var instanceof SourceVariable){
    				Variable srcVariable = null;
    				IVariableBinding srcVariableBinding = null;
    				if(var instanceof TACVariable){
    					srcVariable = (TACVariable)var;
    					srcVariableBinding = ((TACVariable)srcVariable).getVarDecl();
    					if(srcVariableBinding.isParameter()){
    						if(Modifier.isPublic(srcVariableBinding.getModifiers())){
    							isPublicParam = true;
    						}
    					}
    				}
    				else{
    					srcVariable = (SourceVariable)var;
    					srcVariableBinding = ((SourceVariable)srcVariable).getBinding();
    				}
					if(!isPublicParam){
						boolean isMain = srcVariableBinding.getDeclaringClass().getQualifiedName().equals(Config.MAINCLASS);
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
							tm.putTypeMapping(var, analysisResult);

							// Record the AU being modified directly based on the refinement
							//this.addAU(au);
							this.putVariableMap(var, newSourceTyping);

							// Record the new set of typings
							this.setNewTyping(solIndex, newSrcTyping); 

							isHeuristicApplied = true;
						}
					}
				}
			}
		}

		return isHeuristicApplied;
	}

	// XXX. Change this...
	@Override
	public String toDisplayName() {
		StringBuilder builder = new StringBuilder();
		builder.append(changedVar.getSourceString());
		builder.append(": ");
		builder.append(changedVar.resolveType().getQualifiedName());
		builder.append(" in ");
		builder.append(((TACVariable)changedVar).getVarDecl().getDeclaringClass().getQualifiedName());

		return builder.toString();
	}

	@Override
	public String getSrcObject() {
		return changedVar == null ? "" : Signature.getSimpleName(changedVar.resolveType().getQualifiedName());
	}

	// Return the fully qualified type
	public String getSrcObjectFullyQualifiedType() {
		return changedVar == null ? "" : changedVar.resolveType().getQualifiedName();
	}

	@Override
	public String getDstObject() {
		return changedVar == null ? "" : Signature.getSimpleName(((TACVariable)changedVar).getVarDecl().getDeclaringClass().getQualifiedName());
	}

	@Override
	public String getDomainName() {
		return "owned";
	}
	@Override
	public RefinementType getRefinementType() {
		return RefinementType.PushIntoOwned;
	}

}
