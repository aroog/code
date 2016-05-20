package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IElement;

import org.eclipse.jdt.core.Signature;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import edu.cmu.cs.viewer.objectgraphs.VisualReportOptions;
import edu.cmu.cs.viewer.ui.ITreeElement;
import edu.wayne.ograph.ODomain;

/**
 * DisplayDomain maintains a reference to the underlying VisualDomain for traceability.
 * Also,  there is a one-to-one mapping between a VisualDomain and the underlying ArchDomain
 * 
 * IMPORTANT: DisplayObject is used as a key inside Hashtable and Set. So it must override equals() and hashCode() to  satisfy immutability contract.
 * NOTE: But the immutability contract does not use all the declared fields.    
 */
public class DisplayDomain extends DisplayElement {

	@ElementList(required=false)
	private Set<DisplayObject> objects = new HashSet<DisplayObject>();
	
	@Element(required=false)
	private DisplayObject displayObject;
	
	/**
	 * Used to store the DOT unique ID
	 */
	@Attribute(name="dotid",required=false)
	private String id;

	@Attribute
	private boolean showInternals = true;

	@Attribute(name="public")
	private boolean isPublic = false;
	
	@Attribute(name="public")
	private boolean isTopLevel = false;

	public DisplayDomain() {
    }
	
	public DisplayDomain(DisplayObject parent) {
		super();
		
		this.displayObject = parent;
	}

	public boolean addObject(DisplayObject object) {
		return objects.add(object);
	}

	@Override
    public Object[] getChildren() {
		return objects.toArray(new ITreeElement[0]);
	}

	public String getQualifiedName() {
		StringBuffer builder = new StringBuffer();

		if (VisualReportOptions.getInstance().isShowQualifiedDomainNames() && !"SHARED".equals(getLabel()) ) {
			// Will return a fully qualified name.
			String archClass =  Signature.getSimpleName(((ODomain) element).getTypeName());
			if (archClass != null) {
				builder.append(archClass);
				builder.append("::");
			}
		}
		builder.append(getLabel());
		return builder.toString();
	}

	@Override
	public String toString() {
		return getQualifiedName();
	}

	@Override
	public Object getParent() {
		return displayObject;
	}

	@Override
	public boolean hasChildren() {
		return !isEmpty();
	}

	@Override
    public boolean isSelectable() {
		return true;
	}

	public void setId(String id) {
	    this.id = id;
    }

	public String getId() {
    	return id;
    }

	public Set<DisplayObject> getObjects() {
	    return objects;
    }

	public boolean isFormal() {
	    return false;
    }
	
	public boolean isEmpty() {
	    return objects.isEmpty();
    }

	public boolean isPublic() {
	    return isPublic;
    }

	public void setPublic(boolean isPublic) {
    	this.isPublic = isPublic;
    }

	public DisplayObject getObject() {
		return this.displayObject;
	}
	
	public boolean isRoot() {
    	return false;
    }

	public boolean isShowInternals() {
    	return showInternals;
    }

	public void setShowInternals(boolean showInternals) {
    	this.showInternals = showInternals;
    }

	@Override
	public void setElement(IElement element) {
		super.setElement(element);
		
		IDomain oDomain = (IDomain) element;
		this.label = oDomain.getD();
		this.isPublic = oDomain.isPublic();
		this.isTopLevel = oDomain.isTopLevel();
    }

	public boolean isTopLevel() {
    	return isTopLevel;
    }

	public void setTopLevel(boolean isTopLevel) {
    	this.isTopLevel = isTopLevel;
    }
}

