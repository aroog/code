package oogre.refinements.tac;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

import oog.itf.IObject;
import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.utils.Utils;

public class SplitUp extends Refinement {

	Stack<Object> worklist = new Stack<Object>();
	
	protected Variable srcAu;
	
	private boolean isPIP = false;

	// XXX. If getting passed the full dstObject, why need the dstType?! Simplify the class and its callers.
	// NOTE: For SplitUp: dstType is required; dstIObject is redundant; but used by the base class..
	protected String dstType;
	
	public SplitUp(IObject src, IObject dst, String domainName) {
		super(src, dst, domainName);
	}
	
	
	public SplitUp(IObject srcObject, IObject dstObject, String domainName, Variable srcAu, String dstType) {
		super(srcObject, dstObject, domainName);
	    this.srcAu = srcAu;
	    this.dstType = dstType;
    }

	//TODO: reflect the changes of PushIntoPD after adding inheritance here.
	@Override
	public boolean refine(TM tm, TMSolutionType solIndex){
		boolean isRefinementApplied = false;
		boolean isPublicParam = false;
		OOGContext context = OOGContext.getInstance();
		String srcTyp = this.srcAu.resolveType().getQualifiedName();
		Map<IObject, HashSet<IObject>> pc = context.getPC();
		HashMap<TypePair, ArrayList<TypePair>> pf = context.getPF();
		List<String> publicMethods = null;
		//XXX.The size of PC[O_src] is checked here.
		if( pc.get(getSrcIObject())!=null && pc.get(getSrcIObject()).size()>1){
			Set<OType> newSourceTyping = new HashSet<OType>();
			if(getDomainName().equals("owned")){
				if(Utils.isSubtypeCompatible(Utils.getVarType(srcAu),this.dstType)){
					if(Utils.isParameter(this.srcAu)){
						if(Utils.isVarPublic(this.srcAu)){
							isPublicParam = true;
						}
					}
    				if(!isPublicParam){
    					boolean isMain = Utils.getDeclaringClass(this.srcAu).equals(Config.MAINCLASS);
    					String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(), isMain);
    					if(solAlpha != null ) {
    						OType newSrcTyping = null;
//    						if(this.srcAu.getType().contains("<")){
//    							String paramType = this.srcAu.getType().substring(this.srcAu.getType().indexOf('<')+1, this.srcAu.getType().indexOf('>')-1);
//    							for (AnnotatableUnit paramTypeAu : tm.keySet()) {
//    								if(paramTypeAu.isTypeEqual(paramType)){
//    									Set<OType> paramAUTyping = tm.getTypeMapping(paramTypeAu);
//    									for (OType oType : paramAUTyping) {
//    										newSrcTyping = new OType(this.srcAu.getType(), "this.owned", oType.getOwner(), oType.getAlpha());
//    										newSourceTyping.add(newSrcTyping);
//    									}
//    									break;
//    								}
//    							}
//    						}
//    						else{
    							newSrcTyping = new OType("this.owned", solAlpha);
    							newSourceTyping.add(newSrcTyping);
//    						}
    							Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
    							tm.putTypeMapping(this.srcAu, analysisResult);

    							// Record the AU being modified directly based on the refinement
    							//this.addAU(au);
    							this.putVariableMap(this.srcAu, newSourceTyping);

    							// Record the new set of typings
    							this.setNewTyping(solIndex, newSrcTyping); 

    							isRefinementApplied = true;
    					}
                	}
				}
				else{
					tm.isDiscarded = true;
					StringBuilder reason = new StringBuilder();
	                reason.append("The source annotatable unit, ");
	                reason.append(this.srcAu.getSourceString());
	                reason.append(":");
	                reason.append(this.srcAu.resolveType().getQualifiedName());
	                reason.append(" is not in the scope of the destination class, ");
	                reason.append(this.dstType);
	                tm.discardReason = reason.toString();
	    			return isRefinementApplied;
				}
			}
			else{
				isPIP = true;
				boolean isPossible = false;
					boolean isMain = Utils.getDeclaringClass(this.srcAu).equals(Config.MAINCLASS);
					String solAlpha = SolutionManager.getSolution(solIndex, this.getRefinementType(),isMain);
					if(solAlpha!=null){
						OType newSrcTyping = null;
//						if(srcTyp.contains("<")){
//							String paramType = srcTyp.substring(srcTyp.indexOf('<')+1, srcTyp.indexOf('>'));
//							for (Variable paramTypeAu : tm.keySet()) {
//								if(Utils.isSubtypeCompatible(paramTypeAu.resolveType().getQualifiedName(),paramType)){
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
							newSrcTyping = new OType(srcTyp, "this.PD", solAlpha);
							newSourceTyping.add(newSrcTyping);
//						}
							
						Set<OType> analysisResult = new HashSet<OType>(newSourceTyping);
						tm.putTypeMapping(this.srcAu, analysisResult);

						// Record the AU being modified directly based on the refinement
						this.putVariableMap(this.srcAu, newSourceTyping);

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
		}
        else{
        	tm.isDiscarded = true;
        	StringBuilder reason = new StringBuilder();
            reason.append("The source object, ");
            reason.append(this.getSrcIObject().getInstanceDisplayName());
            reason.append(":");
            reason.append(this.getSrcIObject().getC());
            reason.append(" does not trace to more than one new expression in the code ");
            reason.append(this.dstType);
            tm.discardReason = reason.toString();
			return isRefinementApplied;
        }
		return isRefinementApplied;
	}


	private oog.re.Refinement realRefinement;

	@Override
	public oog.re.Refinement getReal() {
		if (realRefinement == null) {
			// DONE. need to populate more fields here...
			oog.re.SplitUp refinement = new oog.re.SplitUp(getSrcObject(), getDstObject(), getDomainName());
			refinement.setName(srcAu.getSourceString());
			//refinement.setKind(srcAu.getKind().toString());
			refinement.setType(Utils.getVarType(srcAu));
			refinement.setEnclosingMethod(Utils.getEnclosingMethod(srcAu));
			refinement.setEnclosingType(Utils.getDeclaringClass(srcAu));
			realRefinement = refinement;
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


	public boolean isPIP() {
		return isPIP;
	}
}