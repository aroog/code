package edu.wayne.ograph.analysis;

import java.util.List;
import java.util.Set;

import util.TraceabilityEntry;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.OCFEdge;
import edu.wayne.ograph.internal.OCFEdgeKey;
import edu.wayne.ograph.internal.OEdge;
import edu.wayne.ograph.internal.OGraph;
import edu.wayne.ograph.internal.OOGContext;
import edu.wayne.ograph.internal.OObject;
import edu.wayne.ograph.internal.QualifiedClassName;
import edu.wayne.pointsto.PointsToAnalysis;

/***
 * creates Control Flow edges for every Method Invocation
 * 
 * @author radu
 * 
 */
public class CfOOGTransferFunctions extends DfOOGTransferFunctions {

	private static final String CONTROL_EDGE_NOT_ADDED = "Control Edge not added";

	public CfOOGTransferFunctions(PointsToAnalysis pointsToAnalysis) {
		super(pointsToAnalysis);
	}

	@Override
	public OOGContext transfer(MethodCallInstruction instr, OOGContext value) {
		// System.out.println("MethodCall: " + instr + " O=" + value.getO());
		// System.out.println(value.getGamma().keySet());
		Variable receiver = instr.getReceiverOperand();

		QualifiedClassName cthis = getC_THIS(value);
		if (receiver != null && !instr.isStaticMethodCall()) {
			// not a static method call
			OObject o = value.getO();
			QualifiedClassName recvClass = new QualifiedClassName(getRecvPreciseClass(receiver, o), cthis);
			List<DomainP> receiverActualDomains = getReceiverActualDomains(receiver, instr, value.getGamma(), cthis);
			if (receiverActualDomains != null) {
				TraceabilityEntry tLink = traceabilityFactory.createTraceability(instr, o);
				Set<OObject> dstObjs = auxLookup(value, o, recvClass, receiverActualDomains);
				if (dstObjs.size() <= 0)
					this.pointsToAnalysis.addWarning(instr.getNode(), CONTROL_EDGE_NOT_ADDED);
				else {
					OGraph G = value.getG();
					for (OObject oObject : dstObjs) {
						OCFEdgeKey oEdgeKey = new OCFEdgeKey(o, oObject, instr.getMethodName());
						OEdge oEdge = G.getOEdge(oEdgeKey);
						if (oEdge == null) {
							oEdge = new OCFEdge(oEdgeKey);
							G.addOEdge(oEdge);
						}
						oEdge.addToASTNodePath(value.getExpressionStack(), tLink, o);
						// oEdge.setStringID(path.toString());
					}
				}
			} else
				returnWarning(instr.getNode(), receiver);

		}
		// TODO: LOW: do something about static method calls.

		return super.transfer(instr, value);
	}

}
