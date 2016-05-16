package secoog;

import conditions.Condition;
import oog.itf.IEdge;

public class SecEdgeConditionVisitor extends SecVisitorBase {
	private Condition<IEdge> cond; //given a condition
	private Property prop;  // assign a property value
	
	public SecEdgeConditionVisitor(Property prop, Condition<IEdge> condition) {
		this.cond = condition;
		this.prop = prop;
	}

	@Override
	public boolean visit(SecEdge edge) {
		if (edge.edgeType == EdgeType.PointsTo)
			if (cond.satisfiedBy(edge)){
				edge.setPropertyValue(prop);
				return true;
			}
		return super.visit(edge);
	}


}
