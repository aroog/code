package oogre.refinements.tac;


import oog.re.RankedTypings;

/**
 * Base class for the heuristics.
 * 
 * TODO: Wrap the underlying Heuristic object?
 */
public abstract class Heuristic extends BaseOperation implements oog.re.IHeuristic {

	// TODO: return true if the heuristic found anything
	public boolean apply(TM tm, TMSolutionType solIndex) {
		return false;
	}

	@Override
	public String toDisplayName() {
		return "Heuristic";
	}
	
	@Override
    public RankedTypings getRankedTypings() {
	    // TODO Auto-generated method stub
	    return null;
    }
	
	// XXX. vs. getFacadeOpr?
    public oog.re.IOperation getReal() {
		if(facadeOperation == null ) {
			// XXX. Not saving an OObjectKey here! But that's OK, this is a heuristic! we don't replay it
			// But the display looks inconsistent. At least, use fully qualified names
			facadeOperation = new oog.re.Heuristic(getSrcObject(), getDstObject(), getDomainName());
		}
		return facadeOperation;
    }

}
