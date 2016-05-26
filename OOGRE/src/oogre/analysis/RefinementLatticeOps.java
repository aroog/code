package oogre.analysis;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.flow.ILatticeOperations;

public class RefinementLatticeOps implements ILatticeOperations<OOGContext> {
	
	private OOGContext context = OOGContext.getInstance();
	
	public RefinementLatticeOps() {
	}

	@Override
    public OOGContext bottom() {
	    return context;
    }

	@Override
    public OOGContext join(OOGContext someInfo, OOGContext otherInfo, ASTNode node) {
	    return context;
    }

	@Override
	/**
	 * @return true if info has more precise aliasing information than reference. That is, info is a strict subset of reference
	 */	
    public boolean atLeastAsPrecise(OOGContext info, OOGContext reference, ASTNode node) {
	    return true;
    }

	@Override
    public OOGContext copy(OOGContext original) {
	    return context;
    }

}
