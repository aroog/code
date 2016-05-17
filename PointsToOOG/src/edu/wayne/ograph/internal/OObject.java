package edu.wayne.ograph.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Popup;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.ui.model.AdaptableList;

import oog.itf.IDomain;
import oog.itf.IObject;
import util.TraceabilityListSet;
import adapter.Adapter;
import ast.AstNode;
import ast.BaseTraceability;
import ast.Type;
import edu.wayne.auxiliary.Config;
import edu.wayne.ograph.AUString;
import edu.wayne.ograph.OGraphVisitor;
import edu.wayne.ograph.ResourceLineKey;

/**
 * This class represents OObject instance in OGraph O = {C<\ob{D}>}
 */
public class OObject extends OElement implements IObject {

	private OObjectKey key;

	private boolean isTemp;
	
	private boolean isLent;
	
	private boolean isUnique;

	// private QualifiedClassName QCN;
	private Type type;
	
	private String O_id;

	// The instanceDisplayName, i.e., the "o" in "o: C"; ideally, extracted from a variable name
	private String simpleName;
	
	OObject(OObjectKey key) {
		// super(type.getQualifiedName(), true); //create a temporary OObject
		this.key = key;
		this.isTemp = false;
		this.isLent = false;
		this.isUnique = false;
	}

	public OObjectKey getKey() {
		return key;
	}

	public QualifiedClassName getQCN() {
		// XXX. Why always creating a new object?
		return new QualifiedClassName(OGraph.getInstance().getBinding(type), null);
	}

	public void setQCN(QualifiedClassName qCN) {
		type = Type.createFrom(qCN.getTypeBinding());
		OGraph.getInstance().addType(qCN.getTypeBinding());
	}

	public void setAsLent(){
		isLent = true;
	}
	
	public boolean isLent(){
		return isLent;
	}

	public boolean isUnique(){
		return isUnique;
	}

	
	public void setAsUnique(){
		isUnique = true;
	}
	
	// Do not expose setter. Once the object is created, should not change that
	// field!
	// public void setClassType(ITypeBinding classType) {
	// this.className = classType;
	// }

	public ODomain getOwnerDomain() {
		return key.getOwnerDomain();
	}

	// TODO: LOW. Fix rep. exposure
	public List<ODomain> getDomains() {
		return key.getDomains();
	}

	// Do not expose setter. Once the object is created, should not change that
	// field!
	// public void setDomains(List<ODomain> domains) {
	// this.domainsList = domains;
	// }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	// XXX. Does not consider all other fields...
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OObject other = (OObject) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else {
			if (!key.equals(other.key))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getO_id();
	}
	
	public String toReadableString() {
		StringBuilder builder = new StringBuilder();
		builder.append("new ");
		builder.append(getTypeDisplayName());
		builder.append("<");
		List<ODomain> domains = getDomains();
		if(domains != null && domains.size() > 0 ) {
			builder.append(domains.get(0).getD());
		}
		builder.append("...>");
		return builder.toString();
	}

	public String getTypeName() {
		return key.getTypeName();
	}

	@Override
	public Type getC() {
		Type type = Type.getUnknownType();
		if (this.type != null) {
			type = this.type;
		}
		return type;
	}

	@Override
	public Set<IDomain> getChildren() {
		Set<IDomain> resultSet = new HashSet<IDomain>();
		for (ODomain oDomain : getChildODomains())
			resultSet.add(oDomain);
		return resultSet;
	}
	
	// Candidate for deletion
	private Set<edu.wayne.ograph.ODomain> getRealChildren() {
		Set<edu.wayne.ograph.ODomain> resultSet = new HashSet<edu.wayne.ograph.ODomain>();
		for (ODomain oDomain : getChildODomains())
			resultSet.add(oDomain.getReal());
		return resultSet;
	}

	@Override
	// Compute O_id once on demand. O_id is based on the OObjectKey and is immutable
	public String getO_id() {
		if(O_id == null ) {
			O_id = OObject.buildOObjectPath(getKey());
		}
		return this.O_id;
	}
	
	// Do not use. No Op. Just for serialization.
	public void setO_id(String id) {
		// XXX. O_id has to be immutable; many things reference it; do not change it here!
	}

	@Override
	public IDomain getParent() {
		// XXX. Unify this. Avoid extra call. But it's a covariant signature.
		return getOwnerDomain();
	}

	public edu.wayne.ograph.ODomain getRealParent() {
		return getOwnerDomain().getReal();
	}

	/**
	 * Return true, since every object is in a domain. ORootObject overrides
	 * this method to return false.
	 */
	@Override
	public boolean hasParent() {
		return true;
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {
		return false;
	}

	/**
	 * The child ODomains of an OObject O are:
	 * all the domains Di such that :
	 *  { (O, C::d) -> Di } \in DD
	 *  where d is locally declared domain, not a formal domain parameter.
	 */
	// XXX. Next: inline this method with getChildren(). Very frequently called. Leads to extra allocation of collection.
	private Set<ODomain> getChildODomains() {
		Set<ODomain> resultODomain = new HashSet<ODomain>();
		OGraph oGraph = OGraph.getInstance();
		Map<DomainMapKey, ODomain> DD = oGraph.getDD();
		for (Entry<DomainMapKey, ODomain> entry : DD.entrySet()) {
			DomainMapKey key = entry.getKey();
			if (key.getO().equals(this) && key.getDomDecl().isDeclaration())
				resultODomain.add(entry.getValue());
		}
		return resultODomain;
	}

	@Override
	public String getInstanceDisplayName() {
		if (simpleName != null)
			return simpleName;
		return "";
	}

	public void setInstanceDisplayName(String name) {
		this.simpleName = name;
	}

	
	@Override
	// Return a non-qualified type name, since the fully qualified name is available from getC() or getTypeName()
	public String getTypeDisplayName() {
		// TODO: Could avoid this extra operation, by storing more information in OObjectKey
		String fullName = getTypeName();
		// Lookup a shortname
		String shortName = org.eclipse.jdt.core.Signature.getSimpleName(fullName);
		// TODO: Check what we got?
		fullName = shortName;
		return fullName;
	}

	@Override
	public boolean hasChildren() {
		return key.hasChildren();
	}

	@Override
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
			for (IDomain tld : mainObject.getChildren()) {
				return tld.getChildren().contains(this);
			}
		}

		return false;
	}

	@Override
	public Set<IObject> getAncestors() {
		Set<IObject> returnSet = new HashSet<IObject>();
		returnSet.add(this);
		Set<IObject> parents = getParentObjects();
		if (parents != null) {
			returnSet.addAll(parents);
			for (IObject parent : parents) {
				if (!parent.equals(this) && !returnSet.contains(parent))
					returnSet.addAll(parent.getAncestors());
			}
		}
		return returnSet;

	}
	
	@Override
	public Set<IObject> getChildObjects() {
		// implement if needed
		return null;
	}

	@Override
	public Set<IObject> getDescendants() {
		// implement if needed
		return null;
	}

	@Override
	public Set<IObject> getParentObjects() {
		return getParent().getParents();
	}

	@Override
	public boolean isMainObject() {
		return this.getC().getFullyQualifiedName().equals(Config.MAINCLASS) ;
	}	
	
	private edu.wayne.ograph.OObject realObject;
	
	public edu.wayne.ograph.OObject getReal() {
		if (realObject == null) {
			realObject = new edu.wayne.ograph.OObject(getO_id(), getC(), getRealParent());
			realObject.setObjectKey(getObjectKey());
			realObject.setAsLent(isLent());
			realObject.setAsUnique(isUnique());
			realObject.setInstanceDisplayName(getInstanceDisplayName());
			realObject.setTypeDisplayName(getTypeDisplayName());
			realObject.setMainObject(isMainObject());
			realObject.setTopLevel(isTopLevel());
			// XXX. If not using MiniAst, do not save this
			realObject.setPath(getPath());
			
			Set<ResourceLineKey> set2 = getSimpleTraceability(getPath());
			realObject.setTraceability2(set2);
			
			// Factor out logic to speed this up across: getSimpleTraceability, getAUs, etc.
			Set<AUString> aus = getAUs(getPath());
			realObject.setExpressions(aus);
			
			Set<ODomain> childODomains = getChildODomains();
			for (ODomain oDomain : childODomains)
				realObject.addDomain(oDomain.getReal());
		}		
		return realObject;
	}
	
	// XXX. Iterating twice over getSetOfLasts
	private Set<AUString> getAUs(TraceabilityListSet path) {
		Set<AUString> aus = new HashSet<AUString>();
		
		Set<BaseTraceability> traceability = path.getSetOfLasts();
		if (traceability != null)
			for (BaseTraceability trace : traceability) {
				AUString au = AUString.createFrom(trace);
				aus.add(au);
			}
		
		return aus;
	}
	
	private Set<ResourceLineKey> getSimpleTraceability(TraceabilityListSet path) {
		Adapter factory = Adapter.getInstance();
		ResourceLineKeyMgr objFactory = ResourceLineKeyMgr.getInstance();

		Set<ResourceLineKey> traces = new HashSet<ResourceLineKey>();
		Set<BaseTraceability> traceability = path.getSetOfLasts();
		if (traceability != null)
			for (BaseTraceability trace : traceability) {

				AstNode expression = trace.getExpression();
				ASTNode astNode = factory.get(expression);
				if (astNode != null) {
					ResourceLineKey res = objFactory.getResourceLine(astNode);
					// XXX. Store also qualified name?
					// res.setQualifiedName()
					traces.add(res);
				}
			}

		return traces;
    }

	private static String buildOObjectPath(OObjectKey key) {
		String typeName = key.getTypeName();
		List<ODomain> listD = key.getDomains();
    		
		StringBuilder path = new StringBuilder();
		// XXX. Ideally, skip over the Dummy objects so we do not add it at the beginning of every path.
		// But causes other problems later in the Oid generation (used for DOT generation)
		//		if (!(O instanceof ORootObject) ) {
		for(ODomain dD : listD) {
			path.append(dD.getD_id());
			path.append(".");
		}
		path.append(typeName);
		return path.toString();
	}

	@Override
    public String getObjectKey() {
	    return key.toString();
    }	
}