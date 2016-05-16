package secoog;

import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;
import secoog.itf.IGraphQuery;

public abstract class AbstractGraphQuery implements IGraphQuery {

	@Override
	public <T extends IEdge> Set<T> connected(IObject a, IObject b) {
		return connected(a, b, EdgeType.values(), false, true, true, true, true);
	}

	// XXX. Arg eType not used...Pass null for now.
	@Override
	public <T extends IEdge> Set<T> connectedByEdgeType(IObject src, IObject dst, EdgeType[] eType) {
		return connected(src, dst, EdgeType.values(), false, false, false, false, false);
	}
	
	@Override
	// XXX. Arg eTypes not used...Pass null for now.
	public <T extends IEdge> Set<T> connectedByUndirected(IObject src, IObject dst, EdgeType[] eTypes) {
		return connected(src, dst, EdgeType.values(), true, false, false, false, false);
	}
	
	@Override
	public <T extends IEdge> Set<T> connectedByAncs(IObject src, IObject dst) {		
		return connected(src, dst, EdgeType.values(), false, true, false, false, false);
	}

	@Override
	public <T extends IEdge> Set<T> connectedByDescs(IObject src, IObject dst) {
		return connected(src, dst, EdgeType.values(), false, false, true, false, false);
	}


	@Override
	public <T extends IEdge> Set<T> connectedByObjectReachability(IObject src, IObject dst, EdgeType[] eType) {
		return connected(src, dst, EdgeType.values(), false, false, false, true, false);
	}


	@Override
	public <T extends IEdge> Set<T> connectedByEdgeTransitiviy(IObject src, IObject dst, EdgeType[] eType){
		return connected(src, dst, EdgeType.values(), false, false, false, false, true);
	}

	@Override
	public <T extends IEdge> Set<T> connectedByDF(IObject a, IObject b) {
		EdgeType[] eTypes = { EdgeType.DataFlow };
		return connected(a, b, eTypes, false, false, true, false, true);
	}

	@Override
	public <T extends IEdge> Set<T> connectedByPT(IObject a, IObject b) {
		EdgeType[] eTypes = { EdgeType.PointsTo };
		return connected(a, b, eTypes, false, false, true, false, true);
	}
}
