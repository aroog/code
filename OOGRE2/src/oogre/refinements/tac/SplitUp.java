package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ITypeBinding;

import oog.itf.IObject;
import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.utils.Utils;
import edu.cmu.cs.crystal.tac.model.Variable;

public class SplitUp extends Refinement {

	Stack<Object> worklist = new Stack<Object>();
	
	protected Set<Variable> srcVars;
	private boolean isPIP = false;

	// XXX. If getting passed the full dstObject, why need the dstType?! Simplify the class and its callers.
	// NOTE: For SplitUp: dstType is required; dstIObject is redundant; but used by the base class..
	protected String dstType;
	
	public SplitUp(IObject src, IObject dst, String domainName) {
		super(src, dst, domainName);
	}
	
	
	public SplitUp(IObject srcObject, IObject dstObject, String domainName, Set<Variable> srcAu, ITypeBinding srcTypBinding, String dstType) {
		super(srcObject, dstObject, domainName);
	    this.srcVars = srcAu;
	    this.dstType = dstType;
	    this.sourceType = srcTypBinding;
    }

	//TODO: reflect the changes of PushIntoPD after adding inheritance here.
	@Override
	public boolean refine(TM tm, TMSolutionType solIndex){
		boolean isRefinementApplied = false;
		boolean isPublicParam = false;
		OOGContext context = OOGContext.getInstance();
		String srcTyp = this.sourceType.getQualifiedName();
		Map<IObject, HashSet<IObject>> pc = context.getPC();
		HashMap<TypePair, ArrayList<TypePair>> pf = context.getPF();
		List<String> publicMethods = null;
		//XXX.The size of PC[O_src] is checked here.
		Set<OType> newSourceTyping = new HashSet<OType>();
		for (Variable srcVar : this.srcVars) {
			
		if(getDomainName().equals("owned")){
			if(Utils.isParameter(srcVar)){
				if(Utils.isVarPublic(srcVar)){
					isPublicParam = true;
				}
			}
			if(!isPublicParam){
				boolean isMain = Utils.getDeclaringClass(srcVar).equals(Config.MAINCLASS);
				String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(), isMain);
				if(solAlpha != null ) {
					OType newSrcTyping = null;
					if(sourceType.isParameterizedType()){
						newSourceTyping = tm.initParametrizedTypeMapping("this.owned",isMain);
					}
					else{
						newSrcTyping = new OType("this.owned", solAlpha);
						newSourceTyping.add(newSrcTyping);
					}
					Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
					tm.putTypeMapping(srcVar, analysisResult);

					// Record the AU being modified directly based on the refinement
					//this.addAU(au);
					this.putVariableMap(srcVar, "this.owned");
					this.putVariableSetMap(srcVar, newSourceTyping);

					// Record the new set of typings
					this.setNewTyping(solIndex, newSrcTyping); 

					isRefinementApplied = true;
				}
			}
			else{
				tm.isDiscarded = true;
				StringBuilder reason = new StringBuilder();
				reason.append("The source annotatable unit, ");
				reason.append(srcVar.getSourceString());
				reason.append(":");
				reason.append(this.sourceType.getQualifiedName());
				reason.append(" is not in the scope of the destination class, ");
				reason.append(this.dstType);
				tm.discardReason = reason.toString();
				int i=0;
				return isRefinementApplied;
			}
		}
		else if(getDomainName().equals("PD")){
			String dstTyp = getDstIObject().getC().getFullyQualifiedName();
			isPIP = true;
			boolean isPossible = false;
			if( !dstTyp.equals("DUMMY")){
				boolean isMain = Utils.getDeclaringClass(srcVar).equals(Config.MAINCLASS);
				String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(),isMain);
				if(solAlpha!=null){
					OType newSrcTyping = null;
					if(sourceType.isParameterizedType()){
						newSourceTyping = tm.initParametrizedTypeMapping("this.PD",isMain);
					}
					else{
						newSrcTyping = new OType("this.PD", solAlpha);
						newSourceTyping.add(newSrcTyping);
					}

					Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
					tm.putTypeMapping(srcVar, analysisResult);

					// Record the AU being modified directly based on the refinement
					this.putVariableMap(srcVar, "this.PD");
					this.putVariableSetMap(srcVar, newSourceTyping);

					// Record the new set of typings
					this.setNewTyping(solIndex,newSrcTyping); 
					isPossible = true;

					isRefinementApplied = true;
				}
			}
			else{
				OType newSrcTyping = null;
				if(sourceType.isParameterizedType()){
					newSrcTyping = new OType("shared","shared","shared");
				}
				else{
					newSrcTyping = new OType("shared","shared");
				}
				newSourceTyping.add(newSrcTyping);
				Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
				tm.putTypeMapping(srcVar, analysisResult);

				// Record the AU being modified directly based on the refinement
				this.putVariableMap(srcVar, "shared");
				this.putVariableSetMap(srcVar, newSourceTyping);

				// Record the new set of typings
				this.setNewTyping(solIndex,newSrcTyping); 
				isPossible = true;

				isRefinementApplied = true;
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
		}
		// DomainName == PARAM
		else{
			boolean isMain = Utils.getDeclaringClass(srcVar).equals(Config.MAINCLASS);
			String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(), isMain);
			if(!isMain){
				OType newSrcTyping = null;
				if(sourceType.isParameterizedType()){
					newSourceTyping = tm.initParametrizedTypeMapping("owner",isMain);
				}
				else{
					newSrcTyping = new OType("owner", solAlpha);
					newSourceTyping.add(newSrcTyping);
				}
				Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
				tm.putTypeMapping(srcVar, analysisResult);

				// Record the AU being modified directly based on the refinement
				//this.addAU(au);
				this.putVariableMap(srcVar, "owner");
				this.putVariableSetMap(srcVar, newSourceTyping);

				// Record the new set of typings
				this.setNewTyping(solIndex, newSrcTyping); 

				isRefinementApplied = true;
			}
		}
	}
		return isRefinementApplied;
	}


	private oog.re.Refinement realRefinement;

	private ITypeBinding sourceType;

	@Override
	public oog.re.Refinement getReal() {
		if (realRefinement == null) {
			if(!this.srcVars.isEmpty()){
				Variable srcVar = this.srcVars.iterator().next();
				// DONE. need to populate more fields here...
				oog.re.SplitUp refinement = new oog.re.SplitUp(getSrcObject(), getDstObject(), getDomainName());
				refinement.setName(srcVar.getSourceString());
				//refinement.setKind(srcAu.getKind().toString());
				refinement.setType(Utils.getVarType(srcVar));
				refinement.setEnclosingMethod(Utils.getEnclosingMethod(srcVar));
				refinement.setEnclosingType(Utils.getDeclaringClass(srcVar));
				realRefinement = refinement;
			}
		}
		return realRefinement;
	}
	
	@Override
    public String getDomainName() {
	    return this.dstDomain;
    }

	@Override
	public RefinementType getRefinementType() {
		return RefinementType.SplitUp;
	}

	@Override
	public ITypeBinding srcObjectType() {
		return this.sourceType;
	}

	public boolean isPIP() {
		return isPIP;
	}
}