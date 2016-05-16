package conditions;

import java.util.Set;

import ast.Type;
import oog.itf.IDomain;
import oog.itf.IObject;

public class IsChildOf implements Condition<IObject> {
	public static final String ALLDOMAINS = "*";
	private Type parent;
	private Type child;
	private String domainname = ALLDOMAINS; // if domain name is null (?) (*)
											// then just take all domains

	// otherwise look into a specific domain
	// TODO:

	// XXX. Would be nice to add another overload that takes Strings...
	// Simple to user than Class.
	public IsChildOf(Class parent, Class child, String domainname) {
		this(parent, child);
		this.domainname = domainname;
	}
	
	public IsChildOf(Type parent, Type child, String domainname) {
		this(parent, child);
		this.domainname = domainname;
	}

	public IsChildOf(Type parent, Type child) {
		this.parent = parent;
		this.child = child;
	}

	public IsChildOf(Class<?> parent2, Class<?> child2) {
		this.parent = new Type(parent2.getName());
		this.child = new Type(child2.getName());
	}

	@Override
	public boolean satisfiedBy(IObject obj) {

		if (!obj.getC().isSubtypeCompatible(child))
			return false;

		if (domainname.equals(ALLDOMAINS)) {
			if (isInParent(obj))
				return true;
		} else {
			IDomain parent = obj.getParent();
			if (parent == null)
				return false;
			String d = parent.getD();
			if (d == null)
				return false;
			if (d.endsWith("::"+domainname) && isInParent(obj))
				return true;
		}
		return false;
	}

	/**
	 * @param obj
	 * @return
	 */
	private boolean isInParent(IObject obj) {
		Set<? extends IObject> parentObjects = obj.getParentObjects();
		for (IObject pObject : parentObjects) {
			if (pObject.getC().isSubtypeCompatible(parent))
				return true;
		}
		return false;
	}

}
