package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IElement;
import oog.itf.IObject;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import edu.cmu.cs.viewer.objectgraphs.ResourceLineKey;
import edu.cmu.cs.viewer.ui.ITreeElement;

/**
 * An object may contain several domains
 * IMPORTANT: DisplayObject is used as a key inside Hashtable and Set. So it must override equals() and hashCode() to  satisfy immutability contract.
 * NOTE: But the immutability contract does not use all the declared fields.
 */
public class DisplayObject extends DisplayElement {

	@ElementList(required=false)
	private Set<DisplayDomain> domains = new HashSet<DisplayDomain>();

	@Element(required=false)
	private DisplayDomain displayDomain = null;

	@Attribute
	private boolean showInternals = false;

	@Attribute
	private boolean showFormals = false;
	
	@Transient
	static int count = 0;
	
	/**
	 * Used to store the DOT unique ID
	 */
	@Attribute(name="dotid",required=false)
	private String id = "";
	
	@Attribute(required=false)
	private String typeDisplayName;
	
	@Attribute(required=false)
	private String instanceDisplayName;
	
	@ElementList
	private Set<ResourceLineKey> traceability = new HashSet<ResourceLineKey>();
	
	@Attribute(required=false)
	private String extraLabels;
	
	
	@Transient
	private boolean isMainObject = false;
	
	public DisplayObject() {
	}
	
	public DisplayObject(DisplayDomain parent) {
		super();
		
		this.displayDomain = parent;
	}

	public boolean addDomain(DisplayDomain domain) {
		return domains.add(domain);
	}

	@Override
    public Object[] getChildren() {
		return domains.toArray(new ITreeElement[0]);
	}

	public void setShowInternals(boolean transparent) {
		showInternals = transparent;
	}

	public boolean isShowInternals() {
		return showInternals;
	}

	public boolean isShowFormals() {
		return showFormals;
	}

	public void setShowFormals(boolean showFormal) {
		showFormals = showFormal;
	}

	public String getQualifiedName() {
		StringBuffer builder = new StringBuffer();
		builder.append(displayDomain);
		builder.append(":");
		// builder.append(visualObject.getLabel());
		builder.append(getTypeDisplayName());
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return getQualifiedName();
	}

	@Override
	public Object getParent() {
		return displayDomain;
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
    public boolean isSelectable() {
		return true;
	}

	public String getId() {
    	return id;
    }

	public void setId(String id) {
    	this.id = id;
    }


	public Set<DisplayDomain> getDomains() {
	    return domains;
    }

	public String getTypeDisplayName() {
	    return typeDisplayName;
    }
	
	public void setTypeDisplayName(String typeDisplayName) {
    	this.typeDisplayName = typeDisplayName;
    }

	/**
	 * Get the sum of all objects in all domains...
	 */
	public Set<DisplayObject> getChildObjects() {
		Set<DisplayObject> list = new HashSet<DisplayObject>();
		
		for(DisplayDomain displayDomain : this.getDomains()) {
			// Note: Skip formal domains; 
			// Note: We do not need to skip 'shared' and 'lent' domains since those are not converted to DisplayDomain's
			if ( displayDomain.isFormal() ) {
				continue;
			}
        	for(DisplayObject childObject : displayDomain.getObjects() ) {
        		list.add(childObject);
        	}
        }
		
	    return list;
    }

	public DisplayDomain getDomain() {
	    return this.displayDomain;
    }

	@Transient
	public String getNameWithType() {
		StringBuffer builder = new StringBuffer();
		builder.append(getInstanceDisplayName());
		builder.append(" : ");
		builder.append(getTypeDisplayName());
		// Do not add (+) decoration here

		return builder.toString();
	}

	public boolean hasDomains() {
	    return this.domains.size() > 0;
    }

	public String getInstanceDisplayName() {
	    return this.instanceDisplayName;
    }
	
	public void setInstanceDisplayName(String instanceDisplayName) {
    	this.instanceDisplayName = instanceDisplayName;
    }
	
	@Deprecated
	// TODO: Take this out
	public Set<ResourceLineKey> getTraceability() {
		return traceability;
	}
	
	@Deprecated
	// TODO: Take this out	
	public void setTraceability(Set<ResourceLineKey> set) {
		this.traceability = set;
	}
	
	public String getExtraLabels() {
	    return extraLabels;
    }
	
	public void setExtraLabels(String labels) {
		this.extraLabels = labels;
	}
	
	@Override
	@Transient
	// IMPORTANT: Override the base getLabel() method to include the type of the object in addition to the label;
	// sometimes the label is not very informative, and the user is really searching by type!
	// This is how it works in the extraction tool as well!
	public String getLabel() {
		StringBuffer builder = new StringBuffer();
		builder.append(getInstanceDisplayName());
		builder.append(" : ");
		builder.append(getTypeDisplayName());
		return builder.toString();
	}
	
	@Override
	public void setElement(IElement element) {
		IObject oObject = (IObject)element;
		super.setElement(element);		

		//NOTE: We do not use getLabel() to DisplayObject
		this.instanceDisplayName = oObject.getInstanceDisplayName();
		this.typeDisplayName = oObject.getTypeDisplayName();
	
		// TODO: Do not use this.
		// Get the IObject. Get the traceability from there.
//		populateTraceability();
    }

	public boolean isMainObject() {
	    return isMainObject;
    }

	// TODO: Rename: isTopLevel
	public void setMainObject(boolean isMainObject) {
    	this.isMainObject = isMainObject;
    }
	
//	private void populateTraceability() {
//		for (BaseTraceability trace : this.iobject.getTraceability() ) {
//			Type expressionType = trace.expressionType;
//			// expressionType is not always set?
//			if (expressionType != null) {
//				ResourceLineKey key = new ResourceLineKey();
//				key.setFullyQualifiedTypeName(expressionType.getFullyQualifiedName());
//				this.traceability.add(key);
//			}
//		}
//	}
	
}
