package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import de.fub.graph.Vertex;

public class DisplayDomainVertex extends Vertex {

    private static final long serialVersionUID = 3954583404470189390L;
	
    private DisplayDomain displayDomain;
	
	public DisplayDomainVertex(DisplayDomain displayDomain) {
		this.displayDomain = displayDomain;
    }

	@Override
    public String toString(int verbose) {
		return displayDomain.toString();
	}
}
