package secoog;
import ast.Type;
import edu.wayne.ograph.ODFEdge;
import oog.itf.IEdge;

// DONE: Rename: DataFlow -> DataFlowEdge
public class DataFlowEdge extends SecEdge {
	
	public DataFlowEdge(){
		edgeType = EdgeType.DataFlow; 
	}

	public SecObject getFlow() {
		IEdge edge = getOEdge();
		if (edge instanceof ODFEdge){
			ODFEdge dfEdge = (ODFEdge)edge;
			return SecMap.getInstance().getSecObject(dfEdge.getFlow());
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

	@Override
	public String toString() {
		return "DataFlowEdge [src=" + src + ", dst=" + dst + ", edgeType=" + edgeType + ", flow=" + getFlow() + "]";
	}	
}
