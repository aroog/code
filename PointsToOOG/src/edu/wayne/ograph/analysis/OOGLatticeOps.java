package edu.wayne.ograph.analysis;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.util.TypeHierarchy;
import edu.wayne.flowgraph.FlowGraph;
import edu.wayne.ograph.internal.DomainMapKey;
import edu.wayne.ograph.internal.ODomain;
import edu.wayne.ograph.internal.OGraph;
import edu.wayne.ograph.internal.OOGContext;

public class OOGLatticeOps implements ILatticeOperations<OOGContext> {

	private TypeHierarchy types;

	public OOGLatticeOps(TypeHierarchy hierarchy) {
		this.types = hierarchy;
	}

	@Override
	public OOGContext bottom() {
		return new OOGContext(types);
	}

	@Override
	public OOGContext join(OOGContext someInfo, OOGContext otherInfo, ASTNode node) {
		OOGContext joined = someInfo.clone();

		// NOTE: Upsilon not really part of the lattice, no union for Upsilon
		// joined.getUpsilon().addAll(otherInfo.getUpsilon());
		OGraph joinedG = joined.getG();
		OGraph otherG = otherInfo.getG();
		joinedG.addAllOObjects(otherG.getDOMap());
		// DONE. Stop using addAllOEdges: makes it a copy of set of values 
		// joinedG.addAllOEdges(otherG.getDE());
		joinedG.addAllOEdges2(otherG.getDEMap());
		joinedG.addAllDD(otherG.getDD());
		
		
		FlowGraph joinedFG = joined.getFG();
		if(joinedFG != null)
			joinedFG.copy(otherInfo.getFG());

		// DONE: what about joined.getO()? O does not change
		return joined;
	}

	@Override
	public boolean atLeastAsPrecise(OOGContext info, OOGContext reference, ASTNode node) {
		OGraph referenceG = reference.getG();
		OGraph infoG = info.getG();
		if (!referenceG.getDO().containsAll(infoG.getDO()))
			return false;
		if (!referenceG.getDE().containsAll(infoG.getDE()))
			return false;
		if (!isSubMap(info, reference))
			return false;
		// When NOT using the Flow Analysis!
		FlowGraph fg = reference.getFG();
		if (fg != null && !fg.isSubGraph(info.getFG()))
			return false;
		
		return true;
	}

	// There could be some bugs in the code, whereby the same map entry is
	// updated in place (same key, different value)
	private boolean isSubMap(OOGContext info, OOGContext reference) {
		Map<DomainMapKey, ODomain> referenceDD = reference.getG().getDD();
		Map<DomainMapKey, ODomain> infoDD = info.getG().getDD();
		if (!referenceDD.keySet().containsAll(infoDD.keySet()))
			return false;
		for (DomainMapKey key : referenceDD.keySet()) {
			if (!referenceDD.get(key).equals(infoDD.get(key)))
				return false;
		}
		return true;
	}

	@Override
	public OOGContext copy(OOGContext original) {
		return original.clone();
	}

}
