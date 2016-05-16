package conditions;

import oog.itf.IElement;

public interface Condition<T extends IElement> {
	public boolean satisfiedBy(T obj);
}
