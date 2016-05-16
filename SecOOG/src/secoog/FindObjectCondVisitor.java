package secoog;

import java.util.HashSet;
import java.util.Set;

import conditions.Condition;
import oog.itf.IObject;

public class FindObjectCondVisitor<T extends IObject> extends SecVisitorBase {
	private Condition<IObject> cond;
	private Set<T> result;
	public FindObjectCondVisitor(Condition<IObject> c){
		this.cond = c;
		result = new HashSet<T>();
	}
	@Override
	public boolean visit(SecObject node) {
		if (cond.satisfiedBy(node))
			result.add((T)node);
		return super.visit(node);
	}

	public Set<T> getFoundObjects(){
		return result;
	}
}
