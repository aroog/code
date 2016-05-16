package secoog;
import edu.wayne.ograph.OCFEdge;
import oog.itf.IEdge;

public class ControlFlowEdge extends SecEdge {
	
	public ControlFlowEdge(){
		edgeType = EdgeType.ControlFlow; 
	}

	public String getControl() {
		IEdge edge = getOEdge();
		if (edge instanceof OCFEdge){
			OCFEdge cfEdge = (OCFEdge)edge;
			return cfEdge.getControl();
		}
		return null;
	}
	
}
