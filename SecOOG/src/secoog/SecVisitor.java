package secoog;

// TODO: This somewhat duplicates OGraphVisitor
// Maybe we need a middleground that is a SecVisitor, with visit(T node) where T bounded wildcard extends IObject??
public interface SecVisitor {

	boolean visit(SecEdge node);
	
	boolean visit(SecObject node);
	
	boolean visit(SecGraph node);

	// DONE: This does not follow the Eclipse model: ideally, this should be preVisit(general type); see TODO.txt
	// Can you detect the cycles in the Base Visitor Impl *Class* without exposing that into the Visitor InterfacE?
	//TODO: LOW: maybe complete the EclipseModel and add postVisit, endVisit. 
	boolean preVisit(SecElement node);
}
