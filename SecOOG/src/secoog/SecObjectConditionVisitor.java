package secoog;

import conditions.Condition;
import oog.itf.IObject;

public class SecObjectConditionVisitor extends SecVisitorBase {
	private Condition<IObject> cond; //given a condition
	private Property prop;  // assign a property value
	
	public SecObjectConditionVisitor(Property prop, Condition<IObject> condition) {
		this.cond = condition;
		this.prop = prop;
	}

	@Override
	public boolean visit(SecObject node) {
		if (cond.satisfiedBy(node)){
			node.setPropertyValue(prop);
			return true;
		}
		return super.visit(node);
	}


}
