package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.ElementList;

public class DisplayEdgeCollection extends DisplayElement {

	@ElementList
	private Set<DisplayEdge> edges = new HashSet<DisplayEdge>();

	public boolean addEdge(DisplayEdge edge) {
		return edges.add(edge);
	}

	public Set<DisplayEdge> getEdges() {
		return edges;
	}

	public void clear() {
		edges.clear();
	}

	@Override
    public String getLabel() {
		return "Edges";
	}

	@Override
    public Object[] getChildren() {
		return edges.toArray(new DisplayEdge[0]);
	}

	@Override
	public Object getParent() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return edges.size() > 0;
	}

	@Override
    public boolean isSelectable() {
		return false;
	}

	@Override
	public String toString() {
		return edges.toString();
	}

	public boolean remove(DisplayEdge toRemove) {
		return edges.remove(toRemove);
    }

	public boolean removeAll(Set<DisplayEdge> toRemove) {
		return edges.removeAll(toRemove);
    }

	public boolean addAll(Set<DisplayEdge> summaryEdges) {
	    return edges.addAll(summaryEdges);
    }

}
