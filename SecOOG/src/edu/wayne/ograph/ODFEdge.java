package edu.wayne.ograph;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class ODFEdge extends OEdge {

	@Element(required = true, name = "flow")
	private OObject flow;

	@Attribute(required = true, name = "flag")
	private EdgeFlag flag;
	
	@Attribute(required = false, name = "hasFlow")
	private boolean hasFlow;

	@Attribute(required = false, name = "flowType")
	private String flowType;
	
	// Add default constructor for serialization
	protected ODFEdge() {
		super();
	}

	public ODFEdge(@Element(required = true, name = "osrc") OObject osrc,
			@Element(required = true, name = "odst") OObject odst,
			@Element(required = true, name = "flow") OObject flow,
			@Attribute(required = true, name = "flag") EdgeFlag flag) {
		super(osrc, odst);
		this.flow = flow;
		this.flag = flag;
	}

	public ODFEdge(@Element(required = true, name = "osrc") OObject osrc,
			@Element(required = true, name = "odst") OObject odst,
			@Element(required = true, name = "flow") OObject flow,
			@Attribute(required = true, name = "flag") EdgeFlag flag,
			@Attribute(required = false, name = "hasFlow") boolean hasFlow,
			@Attribute(required = false, name = "flowType")String flowType ) {
		this(osrc, odst, flow, flag);
		this.hasFlow = hasFlow;
		this.flowType = flowType;
	}
	
	public OObject getFlow() {
		return flow;
	}

	public EdgeFlag getFlag() {
		return flag;
	}
	
	@Override
	public String toString() {
		return super.toString()+"["+ flag.name() + " " + flow + "]";
	}
}
