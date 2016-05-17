package edu.wayne.ograph.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IObject;
import ast.Type;
import edu.cmu.cs.aliasjava.Constants;
import edu.wayne.auxiliary.Config;
import edu.wayne.ograph.OElement;
import edu.wayne.ograph.OGraphVisitor;

/**
 * This class represents ODomain instance in OGraph
 * 
 */
public class ODomain extends OElement implements IDomain {
	
	private String domainID;
	private DomainP domDecl;
	boolean isPrivate;

	private edu.wayne.ograph.ODomain realDomain;
	
	public edu.wayne.ograph.ODomain getReal() {
		if (realDomain == null ) {
			realDomain = new edu.wayne.ograph.ODomain(getD_id(), getC(), getD());
			realDomain.setC(getC());
			realDomain.setPublic(isPublic());
			realDomain.setPath(getPath());
			realDomain.setTopLevel(isTopLevel());
			// XXX. The ODomain.parents are set elsewhere?
			// During OObject.addDomain(...)
			Set<OObject> oObjects = getOObjects();
			for (OObject o : oObjects) {
				realDomain.addObject(o.getReal());
			}
		}
		return realDomain;
	}
	
	public ODomain(String id, DomainP dDecl) {
		this.domainID = IDDictionary.generateID(id);
		this.domDecl = dDecl;
		if (dDecl.getShortName().equalsIgnoreCase("owned"))
			isPrivate = true;
		else
			isPrivate = false;
	}

	public DomainP getDomainDecl() {
		return domDecl;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainID == null) ? 0 : domainID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ODomain other = (ODomain) obj;
		if (domainID == null) {
			if (other.domainID != null)
				return false;
		} else if (!domainID.equals(other.domainID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return domainID;
	}

	@Override
	public Set<IObject> getChildren() {
		Set<IObject> iObjects = new HashSet<IObject>();
		Set<OObject> oObjects = getOObjects();
		for (OObject o : oObjects) {
			iObjects.add(o);
		}
		return iObjects;
	}

	// XXX. Candidate for deletion
	private Set<edu.wayne.ograph.OObject> getRealChildren() {
		Set<edu.wayne.ograph.OObject> iObjects = new HashSet<edu.wayne.ograph.OObject>();
		Set<OObject> oObjects = getOObjects();
		for (OObject o : oObjects) {
			iObjects.add(o.getReal());
		}
		return iObjects;
	}

	@Override
	public String getD() {		
		String dname = domDecl.getShortName();
		if (dname.equals(Constants.UNIQUE)) dname = IDDictionary.generateUniqueName();
		return dname;
	}

	@Override
	public Type getC() {
		if (domDecl.getTypeBinding() == null)
			return Type.getUnknownType();
		return domDecl.C.getType();
	}

	@Override
	public String getD_id() {
		return domainID;
	}

	@Override
	/**
	 * The parent objects of an ODomain D are:
	 * all the objects that have:
	 *  { (O, C::d) -> D } \in DD
	 *  where d is domain declaration, not a domain parameter.
	 */
	public Set<IObject> getParents() {
		Set<IObject> oObjects = new HashSet<IObject>();
		OGraph oGraph = OGraph.getInstance();
		Map<DomainMapKey, ODomain> DD = oGraph.getDD();
		for (Entry<DomainMapKey, ODomain> entry : DD.entrySet()) {
			DomainMapKey key = entry.getKey();
			DomainP domDecl = key.getDomDecl();
			if(domDecl.isDeclaration() && entry.getValue().equals(this))
				oObjects.add(key.getO());
		}
		return oObjects;
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		return false;
	}

	// XXX. Inline this methods with getChidren().
	private Set<OObject> getOObjects() {
		Set<OObject> oObjects = new HashSet<OObject>();
		OGraph oGraph = OGraph.getInstance();
		OObject oWorld = OOGContext.getOWorld();
		// XXX. Use the cache that maps owning ODomain to set of OObjects
		for (OObject oO : oGraph.getDO()) {
			if (!oO.equals(oWorld))
				if (oO.getOwnerDomain().equals(this))
					oObjects.add(oO);
		}
		return oObjects;
	}

	@Override
	public boolean hasChildren() {
		return getChildren().size() > 0;
	}

	@Override
	public boolean isPublic() {
		return !isPrivate;
	}

	@Override
	// XXX. Can we compute this more efficiently, once?
	public boolean isTopLevel() {
		ODomain dShared = OOGContext.getDShared();
		IObject mainObject = null;

		for (IObject object : dShared.getChildren()) {
			if (object.getC().getFullyQualifiedName().endsWith(Config.MAINCLASS)) {
				mainObject = object;
				break;
			}
		}

		if (mainObject != null) {
			return mainObject.getChildren().contains(this);
		}

		return false;
	}

}