package oogre.refinements.tac;

import oog.re.IOperation;
import edu.cmu.cs.crystal.internal.CrystalRuntimeException;

// TODO: Rename: to OperationUnsupportedException since it covers both Heuristic and Refinement
public class RefinementUnsupportedException extends CrystalRuntimeException {

	private static final long serialVersionUID = -8527503338305188847L;
	
	private TM failedTM = null;

	public RefinementUnsupportedException(String message) {
		super(message);
	}

	public RefinementUnsupportedException(String message, TM failedTM) {
		super(message);
		this.failedTM = failedTM;
	}

	
	public static String createMessage(IOperation ref, String reason) {
		StringBuilder builder = new StringBuilder();
		builder.append("Unsupported operation");
		
		// ref could be null, during TMPP, etc.
		if (ref != null ) {
			builder.append(ref.toDisplayName());
		}
		
		// Add a reason?
		builder.append(reason);
		return builder.toString();
	}

	
	public static RefinementUnsupportedException create(IOperation ref, String reason,TM fTM) {
		String createMessage = createMessage(ref, reason);
		//TM failedTM = fTM.copyTypeMapping(fTM.getKey());
		return new RefinementUnsupportedException(createMessage, fTM);
	}
	
	public TM getFailedTM() {
		return failedTM;
	}
}
