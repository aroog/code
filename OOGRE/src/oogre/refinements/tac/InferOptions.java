package oogre.refinements.tac;

/**
 * Control which type system we are inferring to compute comparative metrics.
 * 
 * 
 *
 */
public class InferOptions {

	public enum InferTypes {OT, UT, OD};
	
	// The default is to infer OD
	private InferTypes inferStyle = InferTypes.OD;
	
	private boolean turnOFFowned = false;
	
	private static InferOptions instance = null;

	public static InferOptions getInstance() {
		if (InferOptions.instance == null) {
			InferOptions.instance = new InferOptions();
		}

		return InferOptions.instance;
	}
	
	/**
	 * Ownership Domains (OD) mode:
	 * - infer everything: shared, owned, n.PD, owner, p
	 * 
	 */
	public boolean inferOD() {
		return inferStyle == InferTypes.OD;
	}
	
	/**
	 * Ownership Types (OT) mode:
	 * - do not add 'PD' to initial sets
	 * - shared (OT: norep), owned (OT: rep), owner (OT: own), and p(OT: p)
	 * 
	 * Translation between OT and our types:
	 * - see Table 1 in paper
	 * 
	 */
	public boolean inferOT() {
		return inferStyle == InferTypes.OT;
	}

	/**
	 * Ownership Domains (OD) mode:
	 * - infer only 'PD'; no 'owned
	 * - promote logical containment: 'PD'
	 * - support cases of near encapsulation
	 */
	// TODO: rename? -> isTurnOFFowned
	public boolean turnOFFowned() {
		return turnOFFowned;
	}

	public void setTurnOFFowned(boolean turnOFFowned) {
    	this.turnOFFowned = turnOFFowned;
    }

	public void setInferStyle(InferTypes inferStyle) {
    	this.inferStyle = inferStyle;
    }
}
