package oogre.refinements.tac;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IObject;
import oogre.analysis.OOGContext;
import oogre.utils.Utils;
import ast.BaseTraceability;
import ast.TypeDeclaration;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public class PushIntoParam extends Refinement implements oog.re.IPushIntoParam {
	

	public PushIntoParam(IObject src, IObject dst) {
		// XXX. Domain name is hard-coded to be 'p'.
		super(src, dst, "p");
	}
	
	
	
	@Override
	public boolean refine(TM tm, TMSolutionType solIndex) {
		boolean isRefinementApplied = false;
		OOGContext context = OOGContext.getInstance();
		
		String dstTyp = getDstIObject().getC().getFullyQualifiedName();
	    
	    Set<BaseTraceability> traceability = this.getSrcIObject().getTraceability();
        for (BaseTraceability baseTraceability : traceability) {
        	TypeDeclaration srcObjNewExprType = PushIntoOwned.getTypeDeclaration(baseTraceability.getExpression());
        	String srcObjNewExprTypeName = srcObjNewExprType.getFullyQualifiedName();
			
        	//Enclosing ITypeBinding
        	Set<OType> newSourceTyping = new HashSet<OType>();
        	for (Variable au : tm.keySet()) {
        		if(au instanceof SourceVariable){
        			SourceVariable srcVariable = (SourceVariable)au;
        		if(au.resolveType().getQualifiedName().equals(srcObjNewExprTypeName)){
        			if(Utils.isSubtypeCompatible(srcVariable.getBinding().getDeclaringClass().getQualifiedName(),dstTyp)){
        				OType newSrcTyping = new OType(srcObjNewExprTypeName, "p", "p");
        				newSourceTyping.add(newSrcTyping);
        				tm.putTypeMapping(srcVariable, newSourceTyping);
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
			realRefinement = new oog.re.PushIntoParam(getSrcObject(), getDstObject(), getDomainName());
		}
	    return realRefinement;
	}

	@Override
	public RefinementType getRefinementType() {
		return RefinementType.PushIntoParam;
	}
	
}
