package edu.wayne.ograph;

import org.simpleframework.xml.Element;

public class OCFEdge extends OEdge {

	@Element(required = true, name = "control")
	private String label;

	// Add default constructor for serialization
	protected OCFEdge() {
		super();
	}

	public OCFEdge(@Element(required = true, name = "osrc") OObject osrc,
			@Element(required = true, name = "odst") OObject odst,
			@Element(required = true, name = "control") String label) {
		super(osrc, odst);

		this.label = label;
	}

	// TODO: Weird name. Rename: getLabel?
	public String getControl() {
		return label;
	}

	@Override
	public String toString() {
		return super.toString()+"[label=" + label + "]";
	}

}
