package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import org.simpleframework.xml.Element;


public class DisplayDomainLink extends DisplayElement {

	@Element
	private DisplayDomain fromDomain = null;

	@Element
	private DisplayDomain toDomain = null;

	public DisplayDomainLink() {
		super();
	}

	public DisplayDomain getFromDomain() {
		return fromDomain;
	}

	public void setFromDomain(DisplayDomain fromDomain) {
		this.fromDomain = fromDomain;
	}

	public DisplayDomain getToDomain() {
		return toDomain;
	}

	public void setToDomain(DisplayDomain toDomain) {
		this.toDomain = toDomain;
	}

	// Implement value equality
	@Override
    public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof DisplayDomainLink)) {
			return false;
		}

		DisplayDomainLink key = (DisplayDomainLink) o;
		return (toDomain == key.toDomain) && (fromDomain == key.fromDomain);
	}

	// Always override hashcode when you override equals
	@Override
    public int hashCode() {
		int result = 17;

		result = 37 * result + (fromDomain == null ? 0 : fromDomain.hashCode());
		result = 37 * result + (toDomain == null ? 0 : toDomain.hashCode());

		return result;
	}

	public String toString() {
		return getLabel();
	}

	@Override
	public String getLabel() {
		StringBuffer builder = new StringBuffer();
		builder.append(fromDomain);
		builder.append(" -> ");
		builder.append(toDomain);
		return builder.toString();

	}

	@Override
	public Object[] getChildren() {
		return new Object[0];
	}

	@Override
	public Object getParent() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
    public boolean isSelectable() {
		return true;
	}
}
