package moogrex.testing;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import oog.itf.IObject;

/**
 * Alt. way of looking IObjects up. In case the OObjectKey is dangling.
 * If lookup by ObjectKey fails, lookup IObjects by type.
 * 
 * This is highly imprecise. But OK to unit testing.
 * 
 * 1 new C() -->  many OObject
 * many new C() ---> 1 OObject
 *
 */
public class TypeResolver {

	private Map<String, ArrayList<IObject>> typeToObjects = new Hashtable<String, ArrayList<IObject>>();

	/**
	 * Use the same transformation on OObjectKey to make sure we will find the same objects.
	 * E.g., to avoid mismatch of fully vs. unqualified type name.
	 * 
	 * @param object
	 */
	public void addObject(IObject object) {
		// NOTE: getTypeDisplayName() may return unqualified name
		//String typeDisplayName = object.getTypeDisplayName();
		String typeDisplayName = getTypeFromKey(object.getObjectKey());
		addEntry(typeDisplayName, object);
	}

	private void addEntry(String type, IObject object) {
		ArrayList<IObject> objects = this.typeToObjects.get(type);
		if (objects == null) {
			objects = new ArrayList<IObject>();
			this.typeToObjects.put(type, objects);
		}
		objects.add(object);
	}

	private ArrayList<IObject> getObjects(String type) {
		return typeToObjects.get(type);
	}

	/**
	 * If there is more than one match (multiple OObjects of the same type), pick one of the objects
	 * XXX. Also, output a warning
	 * 
	 * @param oObjectKey
	 * @return
	 */
	public IObject getObject(String oObjectKey) {
		String typeFromKey = getTypeFromKey(oObjectKey);
		ArrayList<IObject> objects = getObjects(typeFromKey);
		if(objects != null && objects.size() > 0) {
			return objects.get(0);
		}
		return null;
    }
	
	/**
	 * Given an OObjectKey C<D1,..,Dn>, return the C. 
	 * 
	 * XXX. Expose as a static method on OObject, to avoid duplication
	 */
	private static String getTypeFromKey(String oObjectKey) {
	    int indexOf = oObjectKey.indexOf('<');
		if(indexOf != -1){
			return oObjectKey.substring(0, indexOf);
		}
		return oObjectKey;
    }
}
