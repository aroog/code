package oogre.utils;

import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IObject;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OGraphVisitorBase;

/**
 * Use OGraphVisitor, which detects cycles, when traversing the OGraph.
 * The OGraph may contain cycles.
 *
 * Or use SecOOG queries.  (less efficient)  Which will return SecGraph elements.
 * But can get underlying OObject/OEdge.
 * Will require building the SecGraph from the OGraph.
 * This way, we can unify the queries between SecGraph and OOGRE.
 * 
 * XXX. Combine with Facade.
 */
public class OOGHelper {
	
	static class MyVisitor extends OGraphVisitorBase {
		
		private IObject parentIObject;
		private IDomain parentIDomain;
		
		// XXX. Change this to a set.
		// Almost everything will return a set; then figure out which element of the set you want.
		IObject targetNode = null;

		public MyVisitor(IObject parentIObject, IDomain parentIDomain) {
	        super();
	        this.parentIObject = parentIObject;
	        this.parentIDomain = parentIDomain;
        }


		@Override
        public boolean visit(IObject node) {
			if(node == this.parentIObject){
				Set<IDomain> childDomains = node.getChildren();
				for (IDomain domain : childDomains) {
					if(domain==this.parentIDomain){
						Set<IObject> children = domain.getChildren();
						for (IObject child : children) {
							targetNode=child;
						}
					}
				}
			}
	        return super.visit(node);
        }


		@Override
        public boolean visit(IDomain node) {
	        return super.visit(node);
        }


		public IObject getObject() {
	        return targetNode;
        }
		
	}
	
	// XXX. Remove hard-coded owned: pass as arg.
	// Are you looking for any object in owned?! That seems pretty broad.
	public static IObject findObject(OGraph ograph,IObject parentIObject, IDomain parentIDomain) {
		MyVisitor myVisitor = new MyVisitor(parentIObject, parentIDomain);
		ograph.accept(myVisitor);
		
		return myVisitor.getObject();
	}

}
