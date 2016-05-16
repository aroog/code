package secoog.itf;

import java.util.Iterator;

import oog.itf.IObject;

/**
 * define a named set of objects that share the same properties  
 * 
 * */
public interface IObjSet extends IElemSet{
	Iterator<IObject> objects();
}
