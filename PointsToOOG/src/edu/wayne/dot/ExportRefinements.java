package edu.wayne.dot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IGraph;
import oog.itf.IObject;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;
import edu.wayne.ograph.ORootObject;

/**
 * Export refinements of the form:
 * 
 * srcType > dstType::dstDomain
 * 
 * XXX. Output to file?
 * 
 * XXX. Group by srcType or dstType?
 * Group by dstType, then by dstDomain
 * 
 * XXX. Use "PIO" or "PIP" instead of always "Refinement"
 * 
 * XXX. Export the PushIntoParams: object allocated in param? Relation to pulled objects?
 * 
 * XXX. Export to OOGRE.XML that can be loaded.
 * 
 * XXX. Export as Facade objects to make them as pending!
 * 
 * XXX. Fix display: use o:C ? Instead of just C? 
 */
public class ExportRefinements {
	
	protected Set<String> refs = new HashSet<String>();
	
	public void outputRefs() {
		System.err.println("Begin Refinements");
		for(String ref: refs) {
			System.err.println("Refinement: " + ref);
		}
		System.err.println("End Refinements");
	}
	
	
	public void export(OGraph graph) {
		
		ExportOGraphVisitor visitor = new ExportOGraphVisitor();
		graph.accept(visitor);
	}

	
	public class ExportOGraphVisitor extends OGraphVisitorBase {
		public ExportOGraphVisitor() {
		}
		
		@Override
	    public boolean visit(IEdge edge) {
		    return super.visit(edge);
	    }

		@Override
	    public boolean visit(IObject node) {
			if(!(node instanceof ORootObject)) {
				addRefs(node.getParent(), node);
			}
		    return super.visit(node);
	    }

		@Override
	    public boolean visit(IGraph node) {
		    return super.visit(node);
	    }
	}

	// NOTE: For ORootObject, domain is null
	private void addRefs(IDomain domain, IObject oObject) {
		StringBuilder builder = new StringBuilder();
		// SrcType
		builder.append(oObject.getTypeDisplayName());
		// DstType
		builder.append(" > ");
		// XXX. Does it make sense to pick only first? Yes, for a refinement.
		// Refinements handle recursive types.
		builder.append(getFirst(domain.getParents()).getTypeDisplayName());
		// DstDomain
		builder.append("::");
		builder.append(domain.getD());
		refs.add(builder.toString());
    }


	private IObject getFirst(Set<IObject> parents) {
		Iterator<IObject> iterator = parents.iterator();
		while (iterator.hasNext()) {
			IObject next = iterator.next();
			return next;
		}
	    return null;
    }

}
