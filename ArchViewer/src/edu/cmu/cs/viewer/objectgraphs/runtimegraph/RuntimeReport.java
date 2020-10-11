package edu.cmu.cs.viewer.objectgraphs.runtimegraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IObject;
import edu.cmu.cs.aliasjava.Constants;
import edu.cmu.cs.viewer.objectgraphs.EdgeEnum;
import edu.cmu.cs.viewer.objectgraphs.VisualReportOptions;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayDomain;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayEdge;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayModel;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.wayne.ograph.OCFEdge;
import edu.wayne.ograph.OCREdge;
import edu.wayne.ograph.ODFEdge;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OObject;
import edu.wayne.ograph.OPTEdge;

// HACK: Construction of DisplayGraph may be leaving out edges. 
// Still using old algorithm, and not unfolding.
// HACK: Also, may not be correctly adding summary edges
public class RuntimeReport {

	private IObject rootObject = null;

	private IDomain rootDomain = null;

	private VisualReportOptions options = VisualReportOptions.getInstance();
	
	private DisplayModel displayModel;

	private OGraph runtimeGraph;
	
	public RuntimeReport(OGraph runtimeGraph, DisplayModel displayModel) {
		super();

		this.runtimeGraph = runtimeGraph;
		this.displayModel = displayModel;

		this.rootObject = runtimeGraph.getRoot();
		
		displayModel.clearSummaryEdges();
	}

	public void generateGraph() {
		generateGraph(rootObject);
	}

	public void generateGraph(IObject object) {

		System.out.println("Building display graph...");
		
		System.out.println("	Displaying domains and objects...");
		Set<DisplayDomain> listDomains = new HashSet<DisplayDomain>();
		Set<DisplayObject> listObjects = new HashSet<DisplayObject>();
		
		List<IDomain> set = new ArrayList<IDomain>(); 

		//reportDomain(object, object.getParent(), 0, listDomains, listObjects, "", null, set);
		reportObjects(object, object.getParent(), 0, listDomains, listObjects, "", null, set);

		System.out.println("	Displaying field references...");
		reportEdges(listObjects);
		
		System.out.println("	Displaying domain links...");
		reportDomainLinks(listDomains);

		System.out.println("Done.");		
	}

	private void reportEdges(Set<DisplayObject> listObjects) {
		for(DisplayObject displayObject : listObjects ) {
			reportEdges(displayObject);
		}
	}
	
	private void reportEdges(DisplayObject displayObject) {
		for(IEdge edge :  runtimeGraph.getEdges() ) {
			IObject fromObject = edge.getOsrc();
			IObject toObject = edge.getOdst();
			Set<DisplayObject> fromObjects = displayModel.getDisplayObject(fromObject);
			if (fromObjects != null) {
				for (DisplayObject fromDisplayObject : fromObjects) {
					Set<DisplayObject> toObjects = displayModel.getDisplayObject(toObject);
					if (toObjects != null) {
						for (DisplayObject toDisplayObject : toObjects) {
							DisplayEdge displayEdge = visualizeFieldRefs(fromDisplayObject, toDisplayObject);
							if ( displayEdge != null ) {
								displayEdge.setFromObject(fromDisplayObject);
								displayEdge.setToObject(toDisplayObject);
								displayEdge.setElement(edge);
								setEdgeType(displayEdge, edge);
								setEdgeLabel(displayEdge, edge);
							}
						}
					}
				}
			}
		}
	}
	
	// NOTE: This assumes that the edgeType has been set!
	private void setEdgeLabel(DisplayEdge displayEdge, IEdge edge) {
		StringBuffer buffer = new StringBuffer();

		switch (displayEdge.getEdgeType()) {
		case PT:
			buffer.append(((OPTEdge)edge).getFieldName());
			break;
		case DF:
			OObject flow = ((ODFEdge)edge).getFlow();
			buffer.append(flow.getInstanceDisplayName());
			buffer.append(": ");
			buffer.append(flow.getTypeDisplayName());
			break;
		case CR:
			break;
		case CF:
			break;
		}
		
		displayEdge.setEdgeLabel(buffer.toString());
	}

	private void setEdgeType(DisplayEdge displayEdge, IEdge edge) {
		displayEdge.setEdgeType(EdgeEnum.UNKNOWN);
		if (edge instanceof OPTEdge ) {
			displayEdge.setEdgeType(EdgeEnum.PT);
		}
		else if (edge instanceof ODFEdge ) {
			displayEdge.setEdgeType(EdgeEnum.DF);
		}
		else if (edge instanceof OCREdge) {
			displayEdge.setEdgeType(EdgeEnum.CR);
		}
		else if(edge instanceof OCFEdge) {
			displayEdge.setEdgeType(EdgeEnum.CF);
		}
	}

	// TODO: Rename method: too specific to fieldRefs
	private DisplayEdge visualizeFieldRefs(DisplayObject fromDisplayObject, DisplayObject toDisplayObject) {

		if ( fromDisplayObject == null || toDisplayObject == null ) {
			return null;
		}
		DisplayEdge displayEdge = new DisplayEdge();
		displayEdge.setFromObject(fromDisplayObject);
		displayEdge.setToObject(toDisplayObject);
		displayModel.addEdge(displayEdge);
		
		return displayEdge;
    }

	// TODO: Revisit this excluded business here
	private boolean isExcluded(IDomain domain) {
		if (!isRoot(domain)) {
			String name = getLabel(domain);
			return ((name.compareTo(Constants.LENT) == 0) || (name.compareTo(Constants.UNIQUE) == 0) || (name.compareTo(Constants.SHARED) == 0));
		}
		return false;
	}
	
		
	// TODO: Make this customizable
	private String getLabel(IDomain domain) {
	    return domain.getD();
    }
	
	private String getLabel(IObject runtimeObject) {
	    return runtimeObject.getInstanceDisplayName();
    }	

	private boolean isRoot(IDomain domain) {
	    // TODO Auto-generated method stub
	    return false;
    }

	private void reportDomainLinks(Set<DisplayDomain> listDomains) {
    }
	

	private void reportDomain(IObject parentObject, IDomain parentObjectDomain, int depth,
	        Set<DisplayDomain> listDomains, Set<DisplayObject> listObjects, String path, DisplayObject parentDisplayObject, List<IDomain> origin) {

		Set<IObject> parentDomainObjects = parentObjectDomain.getChildren();
		
		if (origin.contains(parentObjectDomain) ) {
			return; // Cycle detected, so return
		}
		origin.add(parentObjectDomain);
		
		if (depth >= options.getMaxDepth() || isExcluded(parentObjectDomain) || parentDomainObjects.size() == 0) {
			return;
		}
		
		String parentClusterName = getDomainId(path, parentObject, parentObjectDomain);
		
		DisplayDomain displayDomain = new DisplayDomain(parentDisplayObject);
		listDomains.add(displayDomain);
		displayModel.addDomain(parentClusterName, displayDomain);
		displayDomain.setId("cluster_" + parentClusterName);
		displayDomain.setLabel(getLabel(parentObjectDomain));
		// TODO: Careful: setElemetn also setting the label!
		displayDomain.setElement(parentObjectDomain);

		displayModel.addDomainToMap(parentObjectDomain, displayDomain);
		
		// When would this be the case?
		if (parentDisplayObject != null ){ 
			parentDisplayObject.addDomain(displayDomain);
		}

		if (parentObjectDomain == rootDomain) {
			this.displayModel.setRootDomain(displayDomain);
		}

		for (IObject runtimeObject : parentDomainObjects) {
			reportObjects(runtimeObject, parentObjectDomain, depth, listDomains, listObjects, parentClusterName, displayDomain, origin);
		}
	}

	// TODO: parentDomain not used
	private String reportObjects(IObject oObject, IDomain parentDomain, int depth, Set<DisplayDomain> listDomains, Set<DisplayObject> listObjects,
	        String path, DisplayDomain displayDomain, List<IDomain> origin) {

			// TODO: Instead of using 'path+' here, use it inside getObjectId!
			String clusterObjectName = /*path +  */getObjectId(path, parentDomain, oObject);
			
			// TODO: Find any existing object first???
			DisplayObject displayObject = null;
			if (displayObject == null) {
				displayObject = new DisplayObject(displayDomain);
				displayModel.addObjectToMap(oObject, displayObject);
				if(displayDomain != null ) {
					displayDomain.addObject(displayObject);
				}
				displayObject.setId(clusterObjectName);
				displayObject.setLabel(getLabel(oObject));
				displayModel.addObject(displayObject.getId(), displayObject);
			}
			displayObject.setElement(oObject);

			if ( oObject.isMainObject() ) {
				displayObject.setMainObject(true);
			}

			if ( oObject == rootObject ) {
				this.displayModel.setRootObject(displayObject);
			}

			for (IDomain domain : oObject.getChildren()) {
				reportDomain(oObject, domain, depth + 1, listDomains, listObjects, clusterObjectName,displayObject, origin);
			}

			// Add it to the list
			listObjects.add(displayObject);
			
			return clusterObjectName;
	}

	public DisplayModel getDisplayModel() {
    	return displayModel;
    }

	public void reset() {
		this.displayModel = null;
		this.runtimeGraph = null;
		this.rootDomain = null;
		this.rootObject = null;
    }
	
	private String getObjectId(String path, IDomain parentDomain, IObject runtimeObject) {
		String dID = runtimeObject.getO_id();
		dID = escapeID(dID);
		return dID;
    }
	
	public static String getObjectId(IObject runtimeObject) {
		String dID = runtimeObject.getO_id();
		dID = escapeID(dID);
		return dID;
    }

	// Why not convert into a single regex?
	private static String escapeID(String dID) {
	    // There should be no space in ID!
		dID = dID.replaceAll(" ", "_");
		dID = dID.replaceAll("\\:", "_");
		// Dashes (could be in string constants)
		dID = dID.replaceAll("\\-", "_");
		dID = dID.replaceAll("\\.", "_");
		// For generic types...
		dID = dID.replaceAll("\\<", "_");
		dID = dID.replaceAll("\\>", "_");
		// For arrays ...
		dID = dID.replaceAll("\\[", "_");
		dID = dID.replaceAll("\\]", "_");
		// For spaces
		dID = dID.replaceAll("\\s+", "_");
		// No commas for containers that take two generic types
		dID = dID.replaceAll("\\,", "_");

	    return dID;
    }	

	private String getDomainId(String path, IObject parentObject, IDomain parentObjectDomain) {
		String dID = parentObjectDomain.getD_id();
		dID = escapeID(dID);
		return dID;
    }
}
