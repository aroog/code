package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import de.fub.graph.Vertex;

public class DisplayObjectVertex extends Vertex {

    private static final long serialVersionUID = 3954583404470189390L;
	
    private DisplayObject displayObject;
	
	public DisplayObjectVertex(DisplayObject displayObject) {
		this.displayObject = displayObject;
    }

	@Override
    public String toString(int verbose) {
		return displayObject.toString();
	}
}
