/**
 * 
 */
package edu.wayne.ograph.analysis;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import util.TraceabilityEntry;
import edu.wayne.ograph.internal.OEdge;
import edu.wayne.ograph.internal.OGraph;
import edu.wayne.ograph.internal.OOGContext;
import edu.wayne.ograph.internal.OObject;
import edu.wayne.ograph.internal.OPTEdge;
import edu.wayne.ograph.internal.OPTEdgeKey;
import edu.wayne.ograph.internal.OwnershipType;
import edu.wayne.ograph.internal.QualifiedClassName;
import edu.wayne.pointsto.PointsToAnalysis;

/**
 * creates points to edges for field declarations
 * 
 * @author radu
 * 
 */
public class PtOOGTransferFunctions extends NodesOOGTransferFunctions {

	public PtOOGTransferFunctions(PointsToAnalysis pointsToAnalysis) {
		super(pointsToAnalysis);
	}

	@Override
	/**
	 * Process the fields to add the points-to edges
	 * Refactor: either OOGContext value,  or OGraph G.
	 */
	protected void doFields(OOGContext value, OObject O_C, OGraph G, QualifiedClassName C) {
		Map<String, OwnershipType> fields = auxFieldsWithoutSubst(C);

		// NOTE: These are the formal domains.
		for (Entry<String, OwnershipType> entry : fields.entrySet()) {
			String fk = entry.getKey();
			OwnershipType Tk = entry.getValue();

			QualifiedClassName QCNk = new QualifiedClassName(Tk.getKey(), null);
			Set<OObject> Oks = auxLookup(value, O_C, QCNk, Tk.getValue());
			if (Oks.size() == 0) {
				int debug = 0;
				debug++;
			}
			for (OObject Ok : Oks) {
				OPTEdgeKey Ekk = new OPTEdgeKey(O_C, Ok, fk);
				OEdge Ek = G.getOEdge(Ekk);
				if (Ek == null ) {
					Ek = new OPTEdge(Ekk);
					G.addOEdge(Ek);
				}
				TraceabilityEntry tLink = traceabilityFactory.createPTTraceability(C, fk, value.getO(), Tk);
				Ek.addToASTNodePath(value.getExpressionStack(), tLink, value.getO());
			}
		}
	}
}
