package secoog;

import java.util.HashSet;
import java.util.Set;

//DONE: Add cycle detection
public class SecVisitorBase implements SecVisitor {
	
	private Set<SecObject> visitedElements = new HashSet<SecObject>();

	@Override
    public boolean visit(SecEdge node) {
		return true;
    }

	@Override
    public boolean visit(SecObject node) {
		if (visitedElements.contains(node))
			return false;
		visitedElements.add(node);
		return true;
    }

	@Override
    public boolean visit(SecGraph node) {
	    return true;
    }

	@Override
	public boolean preVisit(SecElement node) {
		if (visitedElements.contains(node))
			return false;
		return true;
	}

	public Set<SecObject> getVisitedObjects(){
		return visitedElements;
	}
}
