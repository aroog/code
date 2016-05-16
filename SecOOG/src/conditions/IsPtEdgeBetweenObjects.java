package conditions;

import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;
import secoog.EdgeType;
import secoog.SecEdge;

/**
 * 
 */

public final class IsPtEdgeBetweenObjects implements Condition<IEdge> {
	private Set<IObject> src;
	private Set<IObject> dst;

	public IsPtEdgeBetweenObjects(Set<IObject> src, Set<IObject> dst) {
		this.src = src;
		this.dst = dst;
	}

	@Override
	public boolean satisfiedBy(IEdge edge) {
		if (edge instanceof SecEdge) {
			SecEdge secEdge = (SecEdge) edge;
			return secEdge.edgeType == EdgeType.PointsTo 
					&& src.contains(secEdge.src)
					&& dst.contains(secEdge.dst);
		}
		return false;

	}
}