/**
 * 
 */
package conditions;

import ast.Type;
import oog.itf.IDomain;
import oog.itf.IObject;

// XXX. What about 'shared'...? How to express that?
public final class IsInDomain implements Condition<IObject> {
	private String domain;
	private Type type;
	
	public IsInDomain(String dom, Type t){
		domain = dom;
		type = t;
	}
	
	public IsInDomain(String dom, Class<?> t){
		domain = dom;
		type = new Type(t.getName());
	}

	public IsInDomain(String dom, String tName){
		domain = dom;
		type = new Type(tName);
	}
	@Override
	public boolean satisfiedBy(IObject obj) {
		IDomain parent = obj.getParent();
		if (parent == null)
			return false;
		String d = parent.getD();
		if (d == null)
			return false;
		return d.endsWith(domain) && obj.getC().isSubtypeCompatible(type);
	}
}