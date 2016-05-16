package secoog;

import edu.wayne.ograph.OCFEdge;
import edu.wayne.ograph.OCREdge;
import edu.wayne.ograph.ODFEdge;
import edu.wayne.ograph.OGraphVisitorBase;
import edu.wayne.ograph.OPTEdge;
import oog.itf.IEdge;
import oog.itf.IGraph;
import oog.itf.IObject;

// TODO: Make this an inner class of SecAnalysis?
// XXX: Not visiting ODomains.
public class CopyOGraphVisitor extends OGraphVisitorBase {

	private SecMap map = null;
	
	private SecGraph secGraph = null; 
	
	private int objectCount = 0;
	
	public CopyOGraphVisitor() {
		this.map = SecMap.getInstance();
		// XXX. Should re-initialize the SecGraph
		if(this.map != null ) {
			this.map.clear();
		}
		this.secGraph = SecGraph.getInstance();
		if(this.secGraph != null ) {
			this.secGraph.clear();
		}
	}
	
	@Override
    public boolean visit(IEdge edge) {
		//System.out.println("Copying OEdge " + edge);
	
		SecEdge secEdge = null; 
		if (edge instanceof ODFEdge){
			ODFEdge odfedge = (ODFEdge)edge;
			secEdge = new DataFlowEdge();
			secEdge.name = odfedge.getFlow().getO_id();
		}
		else if (edge instanceof OPTEdge){
			OPTEdge optedge = (OPTEdge)edge;
			secEdge = new PointsToEdge();
			secEdge.name = optedge.getFieldName();
		}
		else if (edge instanceof OCREdge){
			OCREdge ocredge = (OCREdge)edge;
			secEdge = new CreationEdge();
			secEdge.name = ocredge.getFlow().getO_id();
		}
		else if (edge instanceof OCFEdge){
			OCFEdge optedge = (OCFEdge)edge;
			secEdge = new ControlFlowEdge();
			secEdge.name = optedge.getControl();
		}
		else return super.visit(edge);
		
		secEdge.src = map.getSecObject(edge.getOsrc());
		secEdge.dst = map.getSecObject(edge.getOdst());
		if ((secEdge.src!=null) && (secEdge.dst!=null)){
			map.mapEdges(edge, secEdge);
			secGraph.addEdge(secEdge);
		}
		else
			System.err.println("Could not copy edge: "+edge.getOsrc()+"->"+edge.getOdst());		
	    return super.visit(edge);
    }

	@Override
    public boolean visit(IObject node) {
		//System.out.println("Copying OObject " + node.getO_id());

		// Hackish way to set the root object; assumes that it's the first object we visit		
		if (objectCount > 0) {
			SecObject secObject = new SecObject();
			secObject.name = node.getO_id();

			map.mapObjects(node, secObject);
		}
		else {
			// Must create the RootSecObject instead of SecObject
			SecObject secObject = new SecRootObject();
			secObject.name = node.getO_id();

			map.mapObjects(node, secObject);

			// Set the root
			secGraph.setRoot(secObject);
			objectCount++;
		}
		
	    return super.visit(node);
    }

	@Override
    public boolean visit(IGraph node) {
		System.out.println("Copying the OGraph into a SecGraph");
	    return super.visit(node);
    }

	
}
