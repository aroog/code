package edu.wayne.ograph.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oog.itf.IDomain;

//this is OWorld
public class ORootObject extends OObject {

	// Add default constructor for serialization
	protected ORootObject() {
		super(new OObjectKey("DUMMY", null));
	}

	@Override
	// Special case: the root object has no parent
	public ODomain getParent() {
		return null;
	}

	@Override
	public Set<IDomain> getChildren() {
		Set<IDomain> dList = new HashSet<IDomain>();
		dList.add(OOGContext.getDShared());
		dList.add(OOGContext.getDUnique());
		dList.add(OOGContext.getDLent());
		//dList.addAll(getFlowDomains());
		return dList;
	}
	
	// Candidate for deletion
	private Set<edu.wayne.ograph.ODomain> getRealChildren() {
		Set<edu.wayne.ograph.ODomain> dList = new HashSet<edu.wayne.ograph.ODomain>();
		dList.add(OOGContext.getDShared().getReal());
		dList.add(OOGContext.getDUnique().getReal());
		dList.add(OOGContext.getDLent().getReal());
		//dList.addAll(getFlowDomains());
		return dList;
	}

	// XXX. Method not being called
	private Set<IDomain> getFlowDomains(){
		Set<IDomain> fDomains = new HashSet<IDomain>();
		Map<DomainMapKey, ODomain> dd = OGraph.getInstance().getDD();
		for (DomainMapKey key: dd.keySet()){
			// XXX. FindBugs: useless control flow to next line.
			if (key.getDomDecl().isUnique() && key.getDomDecl().isDeclaration());
				fDomains.add(dd.get(key));
		}
		return fDomains;
	} 
	
	@Override
	public String getO_id() {
		return "O_world";
	}

	@Override
	public String getTypeDisplayName() {
		return "DUMMY";
	}

	@Override
	public String getInstanceDisplayName() {
		return "dummy";
	}

	@Override
	public boolean hasParent() {
		return false;
	}

	@Override
	public QualifiedClassName getQCN() {
		return new QualifiedClassName(null, null);
	}
	
	private edu.wayne.ograph.ORootObject realObject;
	
	@Override
	public edu.wayne.ograph.OObject getReal() {
		if (realObject == null) {
			realObject = new edu.wayne.ograph.ORootObject(null);
			realObject.setObjectKey(getKey().toString());
			realObject.setInstanceDisplayName(getInstanceDisplayName());
			realObject.setTypeDisplayName(getTypeDisplayName());
			// XXX. Optimize this one. False?!?
			realObject.setMainObject(isMainObject());
			// XXX. Optimize this one. False?!?
			realObject.setTopLevel(isTopLevel());
			realObject.setPath(getPath());
			
			// Add the special domains
			realObject.addDomain(OOGContext.getDShared().getReal());
			realObject.addDomain(OOGContext.getDUnique().getReal());
			realObject.addDomain(OOGContext.getDLent().getReal());			
		}		
		return realObject;
	}	
}
