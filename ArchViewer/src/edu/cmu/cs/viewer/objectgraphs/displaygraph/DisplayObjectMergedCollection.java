package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import java.util.HashSet;
import java.util.Set;

import edu.wayne.ograph.OObject;

public class DisplayObjectMergedCollection extends DisplayElement {

	private Set<OObject> mergedObjects = new HashSet<OObject>();

	public boolean addObject(OObject archObject) {
		return mergedObjects.add(archObject);
	}

	public boolean contains(OObject runtimeObject) {
	    return mergedObjects.contains(runtimeObject);
    }
	
	public Set<OObject> getMergedObjects() {
		return mergedObjects;
	}

	@Override
	public Object[] getChildren() {
		return mergedObjects.toArray(new OObject[0]);
	}

	@Override
	public Object getParent() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return mergedObjects.size() > 0;
	}

	@Override
    public boolean isSelectable() {
		return false;
	}

	@Override
	public String getLabel() {
		return "Merged Objects";
	}

	@Override
	public String toString() {
		return mergedObjects.toString();
	}

	public boolean hasObject(OObject archObject) {
		return mergedObjects.contains(archObject);
    }

}
