package oog.ui.content.wrappers;

import java.util.ArrayList;
import java.util.List;

import edu.wayne.ograph.OGraph;

public class OGraphInfoWrapper implements InfoWrapper {


	private List<TopLevel> topLevelObject;
	public OGraphInfoWrapper(OGraph ograph) {
		
		topLevelObject = new ArrayList<OGraphInfoWrapper.TopLevel>();
		topLevelObject.add(new TopLevel(ograph.getRoot().getChildren().toArray(), "Objects"));
		topLevelObject.add(new TopLevel(ograph.getEdges().toArray(), "Edges"));


		
	}

	@Override
	public Object[] toArray() {
		return topLevelObject.toArray();
	}
	
	public static class TopLevel {
		private Object[] children;
		private String title;

		public TopLevel(Object[] children, String title) {
			this.children = children;
			this.title = title;
		}
		
		public Object[] getChildren(){
			return children;
		}
		public String getTitle (){
			return title;
		}
		@Override
		public String toString() {
			return title;
		}
		
	}


}
