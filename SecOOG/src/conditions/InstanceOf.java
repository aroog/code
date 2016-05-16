package conditions;

import ast.Type;
import oog.itf.IObject;

public class InstanceOf implements Condition<IObject>{

	private Type type;
	
	public InstanceOf(Type type) {
		this.type = type;
	}

	// TODO: Instead of creating a new Type object, should we not lookup in a factory somewhere?
	public InstanceOf(String typeName){
		this(new Type(typeName));
	}

	public InstanceOf(Class<?> clazz){
		this(new Type(clazz.getName()));
	}

	@Override
	public boolean satisfiedBy(IObject obj) {	
		return obj.getC().isSubtypeCompatible(type) ;
	}

}
