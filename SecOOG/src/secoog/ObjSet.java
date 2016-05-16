package secoog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import oog.itf.IObject;
import secoog.itf.IObjSet;

/**
 * TODO: use me
 * */
public class ObjSet extends ElemSet implements IObjSet {

	private Set<IObject> cachedObjects;

	public ObjSet(Set<IObject> objs, String name, Property[] props) {
		super(name, props);
		cachedObjects = new HashSet<IObject>();
		cachedObjects.addAll(objs);
	}

	@Override
	public Iterator<IObject> objects() {
		return cachedObjects.iterator();
	}

}
