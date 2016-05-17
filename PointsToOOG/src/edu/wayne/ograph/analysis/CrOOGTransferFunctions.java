package edu.wayne.ograph.analysis;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;

import util.TraceabilityEntry;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.OCREdge;
import edu.wayne.ograph.internal.OCREdgeKey;
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
public class CrOOGTransferFunctions extends DfOOGTransferFunctions {

	@Override
	protected void creationEdges(OOGContext value, OObject O_C, OGraph G, QualifiedClassName cthis, NewObjectInstruction instr) {		
		super.creationEdges(value, O_C, G, cthis, instr);
		List<Variable> args = instr.getArgOperands();
		for (Variable variable : args) {
			List<DomainP> list = getArgActualDomains(variable, instr, value.getGamma(), cthis);
			if (list != null) {
				ITypeBinding C = variable.resolveType();
				if (!C.isPrimitive()) {
					OObject o = value.getO();
					Set<OObject> labels = auxLookup(value, o, new QualifiedClassName(C, o.getQCN()), list);
					if (labels.size() == 0) {
						int debug = 0;
						debug++;
						pointsToAnalysis.addWarning(instr.getNode(), "Cannot add creation edge for "+C.getQualifiedName()+" and " +list);
					}
					for (OObject Ok : labels) {
						OCREdgeKey Ekk = new OCREdgeKey(o, O_C, Ok);
						OEdge Ek = G.getOEdge(Ekk);
						if(Ek == null ) {
							Ek = new OCREdge(Ekk);
							G.addOEdge(Ek);
						}
						TraceabilityEntry tLink = traceabilityFactory.createTraceability(instr, O_C.getQCN().getTypeBinding(), O_C);
						Ek.addToASTNodePath(value.getExpressionStack(), tLink, o);
					}
				}
			}
		}
	}

	/**
	 * @param value
	 * @param variable
	 * @return
	 */
	private List<DomainP> getArgumentType(OOGContext value, Variable variable) {
		return value.getGamma().get(variable);
	}

	public CrOOGTransferFunctions(PointsToAnalysis pointsToAnalysis) {
		super(pointsToAnalysis);
	}

}
