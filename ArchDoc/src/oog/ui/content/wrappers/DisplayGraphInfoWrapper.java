package oog.ui.content.wrappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayEdge;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayGraph;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.wayne.ograph.OGraph;

public class DisplayGraphInfoWrapper implements InfoWrapper {


	private List<TopLevel> topLevelObject;
	private Set<DisplayObject> objects;
	private Set<DisplayEdge> edges;
	public DisplayGraphInfoWrapper(DisplayModel graph) {
		
		topLevelObject = new ArrayList<DisplayGraphInfoWrapper.TopLevel>();
		objects = graph.getRootObject().getChildObjects();
		topLevelObject.add(new TopLevel(objects.toArray(), "Objects"));
		edges = graph.getEdges();
		topLevelObject.add(new TopLevel(edges.toArray(), "Edges"));

	}
	public DisplayGraphInfoWrapper(Set<DisplayObject> objects, Set<DisplayEdge> edges){
		topLevelObject = new ArrayList<DisplayGraphInfoWrapper.TopLevel>();
		topLevelObject.add(new TopLevel(objects.toArray(), "Objects"));
		topLevelObject.add(new TopLevel(edges.toArray(), "Edges"));
	}

	@Override
	public Object[] toArray() {
		return topLevelObject.toArray();
	}
	
	public class TopLevel {
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
