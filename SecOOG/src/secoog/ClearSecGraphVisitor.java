package secoog;

// TODO: Make this an inner class of SecAnalysis?
public class ClearSecGraphVisitor extends SecVisitorBase {

	private SecMap map = null;
	
	private SecGraph secGraph = null; 
	
	private int objectCount = 0;
	
	public ClearSecGraphVisitor() {
		this.map = SecMap.getInstance();
		this.secGraph = SecGraph.getInstance();
	}
	
	@Override
    public boolean visit(SecEdge edge) {
		edge.clear();
		return super.visit(edge);
    }

	@Override
    public boolean visit(SecObject node) {
		node.clear();
	    return super.visit(node);
    }

	@Override
    public boolean visit(SecGraph node) {
		node.clear();
	    return super.visit(node);
    }

	
}
