package secoog;
import ast.Type;
import edu.wayne.ograph.OCREdge;
import oog.itf.IEdge;

public class CreationEdge extends SecEdge {
	
	public CreationEdge(){
		edgeType = EdgeType.Creation; 
	}

	public SecObject getFlow() {
		IEdge edge = getOEdge();
		if (edge instanceof OCREdge){
			OCREdge crEdge = (OCREdge)edge;
			return SecMap.getInstance().getSecObject(crEdge.getFlow());
		}
		return null;
	}
	
	// Helper method; may return the UnknownType
	public Type getFlowType() {
		Type type = Type.getUnknownType();
		
		SecObject flow = getFlow();
		if ( flow != null ) {
			type = flow.getObjectType();
		}
		return type;
	}

}
