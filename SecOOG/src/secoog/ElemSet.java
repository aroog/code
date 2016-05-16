package secoog;

import java.util.HashSet;
import java.util.Set;

import secoog.itf.IElemSet;

public abstract class ElemSet implements IElemSet {

	protected String name;
	protected Set<Property> props;

	public ElemSet(String name, Property[] props) {
		this.name = name;
		this.props = new HashSet<Property>();
		for (Property property : props) {
			this.props.add(property);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<Property> getProperties() {
		return props;
	}

}