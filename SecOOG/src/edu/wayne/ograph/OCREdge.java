package edu.wayne.ograph;

import org.simpleframework.xml.Element;

public class OCREdge extends OEdge {

	@Element(required = true, name = "flow")
	private OObject flow;

	// Add default constructor for serialization
	protected OCREdge() {
		super();
	}

	public OCREdge(@Element(required = true, name = "osrc") OObject osrc,
			@Element(required = true, name = "odst") OObject odst,
			@Element(required = true, name = "flow") OObject flow ) {
		super(osrc, odst);
		this.flow = flow;
	}

	public OObject getFlow() {
		return flow;
	}

	@Override
	public String toString() {
		return super.toString()+"[label=" + flow + "]";
	}
}
