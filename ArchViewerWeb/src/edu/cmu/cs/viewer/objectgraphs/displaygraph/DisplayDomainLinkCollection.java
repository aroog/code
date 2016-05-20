package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.ElementList;

public class DisplayDomainLinkCollection extends DisplayElement {

	public DisplayDomainLinkCollection() {
    }

	@ElementList
	private Set<DisplayDomainLink> links = new HashSet<DisplayDomainLink>();

	public boolean addDomainLink(DisplayDomainLink domainLink) {
		return links.add(domainLink);
	}

	public Set<DisplayDomainLink> getDomainLinks() {
		return links;
	}

	@Override
	public String getLabel() {
		return "Links";
	}

	@Override
	public Object[] getChildren() {
		return links.toArray(new DisplayDomainLink[0]);
	}

	@Override
	public Object getParent() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return links.size() > 0;
	}

	@Override
    public boolean isSelectable() {
		return false;
	}

	@Override
	public String toString() {
		return links.toString();
	}
	
	public void clear() {
		this.links.clear();
	}
}
