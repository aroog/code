package oogre.refinements.tac;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.utils.Utils;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * NOTE: This class has similar logic as PushIntoPD refinement. Some differences:
 * - deals with AUs instead of IObjects, IDomains
 * - does not change annotations in the destination class; only in the enclosing class;
 * - in refinement, use traceability to get the AUs
 * 
 * 
 * The commonalities are:
 * - both use a worklist
 * - from IObject...can obtain an AU.  
 * 
 *  XXX. If we consolidate this logic, we can also consolidate with SplitUp:
 *  - use AUs
 *
 */
public class InferPDHeuristic extends Heuristic {

	private OOGContext context = OOGContext.getInstance();
	private Variable changedVar;

	public InferPDHeuristic(Variable changedVariable){
		this.changedVar = changedVariable;
	}

	@Override
	public boolean apply(TM tm, TMSolutionType solIndex){
		boolean isHeuristicApplied = false;
		Set<Entry<Variable, Set<OType>>> entrySet = tm.entrySet();
		Set<OType> newSourceTyping = new HashSet<OType>();
		for (Entry<Variable, Set<OType>> entry : entrySet) {
			Variable au = entry.getKey();
			if(au instanceof SourceVariable || au instanceof TACVariable){
				Variable srcVariable = null;
				IVariableBinding srcVariableBinding = null;
				if(au instanceof TACVariable){
					srcVariable = (TACVariable)au;
					srcVariableBinding = ((TACVariable)srcVariable).getVarDecl();
				}
				else{
					srcVariable = (SourceVariable)au;
					srcVariableBinding = ((SourceVariable)srcVariable).getBinding();
				}

				if(srcVariable.equals(this.changedVar) ){
					boolean isMain = srcVariableBinding.getDeclaringClass().getQualifiedName().equals(Config.MAINCLASS);
					String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(),isMain);
					if(solAlpha!=null){
						OType newSrcTyping = null;
						//						if(srcTyp.contains("<")){
						//							String paramType = srcTyp.substring(srcTyp.indexOf('<')+1, srcTyp.indexOf('>'));
						//							for (AnnotatableUnit paramTypeAu : tm.keySet()) {
						//								if(paramTypeAu.isTypeEqual(paramType)){
						//									Set<OType> paramAUTyping = tm.getTypeMapping(paramTypeAu);
						//									for (OType oType : paramAUTyping) {
						//										newSrcTyping = new OType(srcTyp, "this.PD", oType.getOwner(), oType.getAlpha());
						//										newSourceTyping.add(newSrcTyping);
						//									}
						//									break;
						//								}
						//							}
						//						}
						//						else{
						newSrcTyping = new OType("this.PD", solAlpha);
						newSourceTyping.add(newSrcTyping);
						//						}
						Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
						tm.putTypeMapping(srcVariable, analysisResult);

						// Record the AU being modified directly based on the refinement
						this.putVariableMap(srcVariable, newSourceTyping);

						// Record the new set of typings
						this.setNewTyping(solIndex,newSrcTyping); 

						isHeuristicApplied = true;
					}
				}
			}
		}

		return isHeuristicApplied;

	}

	@Override
	public String getSrcObject() {
		if(changedVar instanceof SourceVariable){
			return changedVar == null ? "" : Signature.getSimpleName(((SourceVariable)changedVar).getBinding().getType().getQualifiedName());
		}
		else {
			return changedVar == null ? "" : Signature.getSimpleName(((TACVariable)changedVar).getVarDecl().getType().getQualifiedName());
		}
	}

	@Override
	public String getDstObject() {
		if(changedVar instanceof SourceVariable){
			return changedVar == null ? "" : Signature.getSimpleName(((SourceVariable)changedVar).getBinding().getDeclaringClass().getQualifiedName());
		}
		else{
			return changedVar == null ? "" : Signature.getSimpleName(((TACVariable)changedVar).getVarDecl().getDeclaringClass().getQualifiedName());
		}

	}

	@Override
	public String getDomainName() {
		return "PD";
	}

	@Override
	public RefinementType getRefinementType() {
		return RefinementType.PushIntoPD;
	}	
}
