package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import edu.cmu.cs.viewer.objectgraphs.EdgeEnum;
import edu.cmu.cs.viewer.objectgraphs.ResourceLineKey;

public class DisplayEdge extends DisplayElement {

	@Transient
	private EdgeEnum edgeType = EdgeEnum.UNKNOWN;

	@Element
	private DisplayObject fromObject = null;

	@Element
	private DisplayObject toObject = null;
	
	@Attribute
	private boolean isSummary = false;
	
	@Attribute
	private boolean isBiDirectional = false;
	
	@Attribute
	private boolean isExcluded = false;
	
	@ElementList
	private Set<ResourceLineKey> traceability = new HashSet<ResourceLineKey>();
	
	@Attribute
	private String url;

	@Attribute
	private String edgeLabel;

	public DisplayEdge() {
		super();
	}

	public DisplayObject getFromObject() {
		return fromObject;
	}

	public void setFromObject(DisplayObject fromObject) {
		this.fromObject = fromObject;
	}

	public DisplayObject getToObject() {
		return toObject;
	}

	public void setToObject(DisplayObject toObject) {
		this.toObject = toObject;
	}

	// Implement value equality
	@Override
    public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof DisplayEdge)) {
			return false;
		}

		DisplayEdge key = (DisplayEdge) o;
		return (key.fromObject == fromObject) && (key.toObject == toObject);
	}

	// Always override hashcode when you override equals
	@Override
    public int hashCode() {
		int result = 17;

		result = 37 * result + (fromObject == null ? 0 : fromObject.hashCode());
		result = 37 * result + (toObject == null ? 0 : toObject.hashCode());

		return result;
	}

	public EdgeEnum getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(EdgeEnum edgeType) {
		this.edgeType = edgeType;
	}

	@Override
    public String toString() {
		return getLabel();
	}

	@Override
    public String getLabel() {
		StringBuffer builder = new StringBuffer();
		builder.append(fromObject);
		builder.append(" -> ");
		builder.append(toObject);

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

	public void reset() {

	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
    public boolean isSelectable() {
		return true;
	}

	public boolean isSummary() {
    	return isSummary;
    }

	public void setSummary(boolean isSummary) {
    	this.isSummary = isSummary;
    }

	public boolean isBiDirectional() {
    	return isBiDirectional;
    }

	public void setBiDirectional(boolean isBiDirectional) {
    	this.isBiDirectional = isBiDirectional;
    }

	public boolean isExcluded() {
    	return isExcluded;
    }

	public void setExcluded(boolean isExcluded) {
    	this.isExcluded = isExcluded;
    }

	public Set<ResourceLineKey> getTraceability() {
		return traceability;
	}
	
	public void setTraceability(Set<ResourceLineKey> set) {
		this.traceability = set;
	}

	public String getUrl() {
    	return url;
    }

	public void setUrl(String url) {
    	this.url = url;
    }
	
	public String getEdgeLabel() {
		return this.edgeLabel;
    }
	
	public void setEdgeLabel(String label) {
		this.edgeLabel = label;
	}
}
