package oog.ui.content.wrappers;

import java.util.ArrayList;
import java.util.List;

import oog.itf.IEdge;
import oog.itf.IObject;
import edu.wayne.ograph.ODFEdge;
import edu.wayne.ograph.OPTEdge;

public class EdgeInfoWrapper implements InfoWrapper {
	private IObjectInfo srcObj;
	private IObjectInfo dstObj;
	private EdgeExtra edgeExtra;
	
	
	public EdgeInfoWrapper(IEdge edge) {
		this.srcObj = new IObjectInfo(edge.getOsrc(), true);
		this.dstObj = new IObjectInfo(edge.getOdst(), false);
		this.edgeExtra = new EdgeExtra(edge);

	}
	@Override
	public Object[] toArray(){
		List<Info> infoList = new ArrayList<Info>();
		infoList.add(srcObj);
		infoList.add(dstObj);
		infoList.add(edgeExtra);
		return infoList.toArray();
		
	}
	
	
	public class EdgeExtra implements Info{
		
		private IEdge edge;

		public EdgeExtra(IEdge edge) {
			this.edge = edge;
		}

		@Override
		public String getLabel() {
			String label = "";
			if(edge instanceof OPTEdge){
				label = ((OPTEdge) edge).getFieldName();
			}else if(edge instanceof ODFEdge){
				IObjectInfo info = new IObjectInfo(((ODFEdge) edge).getFlow(), false);
				label = info.getLabel();
			}
			return label;
		}

		@Override
		public String getTitle() {
			String label = "";
			if(edge instanceof OPTEdge){
				label = "Field Name";
			}else if(edge instanceof ODFEdge){
				label = "Flow Object";
			}
			return label;
		}
		
	}
	public class IObjectInfo implements Info{
		private IObject object;
		private boolean isSrc;
		public IObjectInfo(IObject object, boolean isSrc) {
			this.object = object;
			this.isSrc = isSrc;
		}

		@Override
		public String getLabel() {
			String label = object.getInstanceDisplayName()+" : "+object.getC();
			return label;
		}

		public boolean isSrc() {
			return isSrc;
		}

		@Override
		public String getTitle() {
			return isSrc? "Source":"Destination";
		}

		
	}
	
	
	

}
