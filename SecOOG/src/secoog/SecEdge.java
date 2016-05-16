package secoog;
import java.util.Set;

import org.simpleframework.xml.Transient;

import ast.BaseTraceability;
import edu.wayne.ograph.EdgeFlag;
import edu.wayne.ograph.ODFEdge;
import edu.wayne.ograph.OGraphVisitor;
import oog.itf.IEdge;
import oog.itf.IObject;
import util.TraceabilityListSet;

// TODO: May need to make this abstract to enforce correct equals discipline.
public abstract class SecEdge extends SecElement implements IEdge {

	public EdgeType edgeType;

	public SecObject src;
	public SecObject dst;
	
	// Do not save this
	@Transient
	protected boolean highlighted = false;
	
	// DONE. Rename: --> isExport
	public boolean isExport(){
		IEdge edge = getOEdge();
		if (edge instanceof ODFEdge){
			if (((ODFEdge)edge).getFlag().equals(EdgeFlag.Export))
				return true;
		}
		return false;
	} 

	// DONE. Rename: --> isImport
	public boolean isImport(){
		IEdge edge = getOEdge();
		if (edge instanceof ODFEdge){
			if (((ODFEdge)edge).getFlag().equals(EdgeFlag.Import))
				return true;
		}
		return false;
	}

	public IEdge getOEdge() {
		IEdge edge = SecMap.getInstance().getOEdge(this);
		return edge;
	}

	@Override
	public boolean accept(SecVisitor visitor) {
		// TODO: OK to discard return value of visit?
		visitor.visit(this);
		
		return super.accept(visitor);
	}

	@Override
	public String toString() {
		return "SecEdge [src=" + src + ", dst=" + dst + ", edgeType=" + edgeType + "]";
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IObject getOdst() {
		return dst.getOObject();
	}

	@Override
	public IObject getOsrc() {		
		return src.getOObject();
	}

	@Override
	public boolean accept(OGraphVisitor visitor) {	
		return getOEdge().accept(visitor);
	}

	@Override
	public TraceabilityListSet getPath() {		
		return getOEdge().getPath();
	}

	@Override
	public Set<BaseTraceability> getTraceability() {
		return getOEdge().getTraceability();
	}

	public boolean isHighlighted() {
		IEdge edge = getOEdge();
		return edge.isHighlighted();
    }

	public void setHighlighted(boolean highlighted) {
		IEdge edge = getOEdge();
		edge.setHighlighted(highlighted);
    } 
	
}