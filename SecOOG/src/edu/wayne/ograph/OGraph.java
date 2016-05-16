package edu.wayne.ograph;

import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IGraph;

// TODO: No common superclass as OObject, OEdge, etc.
// TODO: Maybe have OGraph implement IVisitable
// XXX. Expose RootClass on OGraph. Needed for many things.
public class OGraph implements IGraph {

	// DONE. keep just the root object
	@Element(required=true, name="root")
	private OObject root;

	@ElementList(required=true, name="edges")
	private Set<IEdge> edges = new HashSet<IEdge>();
	
	@Transient
	private IDomain dSHARED = null;

	// Add default constructor for serialization
	private OGraph() {
	}
	
	public OGraph(@Element(required = true, name = "root") OObject root, @ElementList(required=true,name="edges")Set<IEdge> edges) {
		super();
		this.root = root;
		this.edges = edges;
	}
	
	public OGraph(@Element(required = true, name = "root") OObject root) {
		this(root, new HashSet<IEdge>());
		this.root = root;
	}	

	public boolean addEdge(IEdge edge) {
		return this.edges.add(edge);
	}
	
	public boolean accept(OGraphVisitor visitor) {
		boolean visitChildren = visitor.visit(this);

		if (visitChildren) {
			root.accept(visitor);
		}

		for (IEdge edge : edges) {
			edge.accept(visitor);
		}
		
		return true;
	}

	public void clear() {	
		//TODO: XXX. clean the root.
		edges.clear();		
		OSharedDomain.getInstance().clear();
		root = null;
		dSHARED = null;
	}

	public Set<IEdge> getEdges() {
		return edges;
	}

	public OObject getRoot() {
		return root;
	}

	public IDomain getDShared() {
		if(dSHARED == null ) {
			for(IDomain domain : root.getChildren() ) {
				// XXX. Is this the best way to identify SHARED?
				if (domain.getD().equals("SHARED") ) {
					dSHARED = domain;
					break;
				}
			}
		}
	    return dSHARED;
    }
}
