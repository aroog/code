package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oog.itf.IObject;

import edu.cmu.cs.aliasjava.Constants;
import edu.cmu.cs.viewer.objectgraphs.EdgeEnum;
import edu.cmu.cs.viewer.objectgraphs.ObjectGraphUtil;
import edu.cmu.cs.viewer.objectgraphs.VisualReportOptions;
import edu.wayne.ograph.OGraphState;
import edu.wayne.ograph.OGraphStateMgr;

/**
 * TODO: Add an option whether or not to display empty DisplayDomain's. They are sometimes useful to show.
 *
 * TODO: Check how summary edges are being added
 * 
 * XXX. Replace: StringBuffer -> StringBuilder  
 */
public class DisplayGraph {

	private PrintWriter printWriter = null;

	private DisplayDomain rootDisplayDomain = null;
	
	private DisplayObject rootDisplayObject = null;
	
	private DisplayModel displayModel = null;
	
	private OGraphState graphState = null;
	protected OGraphStateMgr graphStateMgr = null;


	/**
	 * This data structure is used to avoid generating duplicate edges!
	 */
	private List<String> strings = new ArrayList<String>();

	private VisualReportOptions options = VisualReportOptions.getInstance();

	private String reportFullFilename;

	public DisplayGraph(DisplayModel displayModel, String dotFullFilename) {
		super();
		
		this.reportFullFilename = dotFullFilename;

		this.displayModel = displayModel;
		
		this.rootDisplayDomain = displayModel.getRootDomain();
		
		this.rootDisplayObject = displayModel.getRootObject();
	}

	public void generateGraph() {
		generateGraph(rootDisplayObject);
	}
	
	private void generateGraph(DisplayObject displayObject) {
		String reportFile = getReportFullFilename();

		System.out.println("Generating report: " + reportFile );
		
		try {
			printWriter = new PrintWriter(new FileWriter(reportFile));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		initFile();

		System.out.println("Building Ownership Object Graph...");
		
		Set<DisplayDomain> listDomains = new HashSet<DisplayDomain>();
		Set<DisplayObject> listObjects = new HashSet<DisplayObject>();
		if ( options.isShowTopLevelObject() ) {
			DisplayDomain parentDomain = displayObject.getDomain();
			if (parentDomain != null ) {
			reportDomain(parentDomain, " ", 0, listDomains, listObjects, displayObject);
			}
		}
		else {
			for (DisplayDomain displayDomain : displayObject.getDomains()) {
				reportDomain(displayDomain, " ", 0, listDomains, listObjects, displayObject);
			}
		}

		System.out.println("	Adding domain links...");
		for (DisplayDomainLink domainLink : displayModel.getDomainLinks()) {
			reportDomainLink(displayObject, domainLink, "  ", listDomains);
		}

		System.out.println("	Adding field references...");
		buildEdges(listObjects);
		// TODO: disable summary edges?
		reportSummaryEdges2(listDomains);
		printEdges();

		System.out.println("Done.");
		
		closeFile();
		
		reset();
	}
	
	private String getReportFullFilename() {
	    return reportFullFilename;
    }

	private void reset() {
		this.strings.clear();
	}
	
	private void printEdges() {
		for(String string: strings ) {
			printWriter.println(string);
		}
	}

	private void buildEdges(Set<DisplayObject> listObjects) {
		
		// Populate the edges in a separate set to avoid concurrent modification
		Set<DisplayEdge> summaryEdges = new HashSet<DisplayEdge>();
		
		for (DisplayEdge edge : displayModel.getEdges()) {
			DisplayObject fromObject = edge.getFromObject();
			DisplayObject toObject = edge.getToObject();

			if ( fromObject == null || toObject == null ) {
				continue;
			}
			buildEdge(summaryEdges, listObjects, fromObject, toObject, edge);
		}
		displayModel.addEdges(summaryEdges);
		
		//  Pre-process to see if we generate one edge with two arrowheads instead of two edges for a bi-directional connection (to reduce clutter)
		if ( options.isUseBiDirectionalArrows() ) {
			findBiDirectionalEdges(listObjects);
		}
		
		
		reportEdges(listObjects);
	}
	
	private void buildEdge(Set<DisplayEdge> summaryEdges, Set<DisplayObject> listObjects, DisplayObject fromObject, DisplayObject toObject, DisplayEdge edge) {

		// Avoid adding edges to ghost objects
		if (listObjects.contains(fromObject) && fromObject.isVisible() && listObjects.contains(toObject) && toObject.isVisible()) {

			// Summary edges are transient, and must be erased if the user makes the hidden objects visible again
			// TODO: move towards a more stateless implementation, that re-generates edges on the fly
			if ( fromObject != edge.getFromObject() || toObject != edge.getToObject()) {
				DisplayEdge summaryEdge = new DisplayEdge();
				summaryEdge.setFromObject(fromObject);
				summaryEdge.setToObject(toObject);
				summaryEdge.setSummary(true);
				summaryEdges.add(summaryEdge);
				// Copy the traceability
				summaryEdge.setTraceability(edge.getTraceability());
				// XXX. Copy the label?
				// - What if the summary edge summarizes multiple edges??
				summaryEdge.setEdgeLabel(edge.getEdgeLabel());
				// XXX. Copy the highlighted state?
				summaryEdge.setHighlighted(edge.isHighlighted());
			}
		}
		else if (listObjects.contains(fromObject) && !listObjects.contains(toObject)) {
			List<DisplayObject> toAncestors = getAncestors2(fromObject,toObject);
			//List<DisplayObject> toAncestors = getAncestors(toObject);
			for (DisplayObject pulledTo : toAncestors) {
				if (pulledTo == fromObject) {
					// Do not create induced self-edges
					continue;
				}
				buildEdge(summaryEdges, listObjects, fromObject, pulledTo, edge);
			}
		}
		else if (!listObjects.contains(fromObject) && listObjects.contains(toObject)) {
			List<DisplayObject> fromAncestors = getAncestors2(toObject,fromObject);
			// List<DisplayObject> fromAncestors = getAncestors(fromObject);
			for (DisplayObject pulledFrom : fromAncestors) {
				if (pulledFrom == toObject) {
					// Do not create induced self-edges
					continue;
				}
				buildEdge(summaryEdges, listObjects, pulledFrom, toObject, edge);
			}
		}

	}
	
	/**
	 * Check if the edge is bidrectional; if yes, show both arrowheads on this edge, and hide the other edge
	 * @param listObjects 
	 */
	private void findBiDirectionalEdges(Set<DisplayObject> listObjects) {
		for(DisplayEdge displayEdge : displayModel.getEdges() ) {
			DisplayObject fromObject = displayEdge.getFromObject();
			DisplayObject toObject = displayEdge.getToObject();
			
			if (listObjects.contains(fromObject) && fromObject.isVisible() && listObjects.contains(toObject) && toObject.isVisible()) {

				DisplayEdge otherWay = displayModel.getEdge(toObject, fromObject);
				// The same edge cannot be both excluded and bi-directional!
				if ( otherWay != null && !otherWay.isBiDirectional() ) {
					displayEdge.setBiDirectional(true);
					otherWay.setExcluded(true);
				}
			}
		}
	}
	
	private void reportEdges(Set<DisplayObject> listObjects) {
		for(DisplayEdge edge : displayModel.getEdges() ) {
			
			if(!includeEdge(edge)) {
				continue;
			}
			
			// Hide the edge in the other way for a bi-directional edge
			if ( edge.isExcluded() ) {
				continue;
			}
			
			DisplayObject fromObject = edge.getFromObject();
			DisplayObject toObject = edge.getToObject();
			
			// Do not show self DF edges
			if( (fromObject == toObject) && edge.getEdgeType() == EdgeEnum.DF) {
				continue;
			}
		
			if (listObjects.contains(fromObject) && fromObject.isVisible() && listObjects.contains(toObject) && toObject.isVisible()) {

				String fromObjectName = fromObject.getId();
				String toObjectName = toObject.getId();
				
				// HACK: The ZGRViewer does not parse the URL of an edge, just uses the DOT identifier.
				// So, make sure to use the same identifier in the hasthable to lookup DisplayEdge objects later
				StringBuffer url = new StringBuffer();
				url.append(fromObjectName);
				url.append("->");
				url.append(toObjectName);
				displayModel.addEdge(url.toString(), edge);				

				if ((fromObjectName != null) && (toObjectName != null)) {
					StringBuffer builder = new StringBuffer();
					builder.append(fromObjectName);
					builder.append(" -> ");
					builder.append(toObjectName);
					builder.append(" [style = ");
					builder.append(getEdgeStyle(edge));
					// XXX. Clean up comment.
					// builder.append( ", label= \"" );
					// builder.append( getEdgeLabel(edge) );
					// builder.append( "\"" );
					// builder.append( ", minlen = 4");
					// Note: This property is only set if the option to generate bi-directional arrows is set to true
					if ( edge.isBiDirectional() ) {
						builder.append(", dir = both");
					}
					// Add edge color
					builder.append(", ");
					builder.append(getColor(edge));
					// XXX. Make EdgeLabels work for DF edges, flow objects
					if (options.isShowEdgeLabels()) {
						builder.append(", label=\"");
						builder.append(edge.getEdgeLabel());
						builder.append("\"];");
					}
					else {
						builder.append("];");
					}
					String edgeString = builder.toString().intern();
					if (!strings.contains(edgeString))
						strings.add(edgeString);
				}
			}
		}
	}

	private boolean isDescendent(DisplayObject ancestorObject, DisplayObject descendentObject) {
		for(DisplayObject directChild : ancestorObject.getChildObjects() ) {
			if ( directChild == descendentObject)
				return true;
			else {
				return isDescendent(directChild, descendentObject);
			}
		}

		return false;
	}
	
	
	private List<DisplayObject> getAncestors(DisplayObject descendent) {
		List<DisplayObject> ancestors = new ArrayList<DisplayObject>();

		DisplayDomain parentDomain = descendent.getDomain();
		DisplayObject parentObject = null;
		while ( parentDomain != null ) {
			parentObject = parentDomain.getObject();
			if ( parentObject != null ) {
				ancestors.add(parentObject);
				parentDomain = parentObject.getDomain();
			}
			else {
				break;
			}
		}
		return ancestors;
	}
	
	private List<DisplayObject> getAncestors2(DisplayObject origin, DisplayObject descendent) {
		List<DisplayObject> ancestors = new ArrayList<DisplayObject>();

		DisplayDomain parentDomain = descendent.getDomain();
		DisplayObject parentObject = null;
		while ( parentDomain != null  ) {
			parentObject = parentDomain.getObject();
			if ( parentObject != null ) {
				if ( parentObject != origin ) {
					ancestors.add(parentObject);
					parentDomain = parentObject.getDomain();
				}
				else {
					break;
				}
			}
			else {
				break;
			}
		}
		return ancestors;
	}
	
	// TODO: Do we need a showAllEdges option?
	// - Or does that consist of setting everything to true?
	private boolean includeEdge(DisplayEdge edge) {
		EdgeEnum edgeType = edge.getEdgeType();
		VisualReportOptions options = VisualReportOptions.getInstance();

		boolean includeEdge = false;
		
		switch(edgeType ){
		case PT:
			if ( options.isShowReferenceEdges() ) {
				includeEdge = true;
			}
			break;
		case DF:
			if (options.isShowUsageEdges()) {
				includeEdge = true;
			}
			break;
		case CF:
			if (options.isShowControlFlowEdges()) {
				includeEdge = true;
			}
			break;
		case CR:
			if (options.isShowCreationEdges()) {
				includeEdge = true;
			}
			break;
		case UNKNOWN:
			includeEdge = true;
			break;
		}
		
		return includeEdge;
	}
	private void reportSummaryEdges2(Set<DisplayDomain> listDomains) {

		for(DisplayEdge edge : displayModel.getEdges() ) {
			
			if(!includeEdge(edge)) {
				continue;
			}
			
			DisplayObject fromObject = edge.getFromObject();
			DisplayObject toObject = edge.getToObject();
			
			if ( fromObject== null || toObject == null ) {
				// These are bogus edges!!!
				continue;
			}


			// Find any visible object that has the given object as one of its children...
			DisplayDomain fromDomain = getTopDomain(fromObject);
			DisplayDomain toDomain = getTopDomain(toObject);
			// Cluster cycle not supported
			if ( fromDomain == null || toDomain == null || fromDomain == toDomain ) {
				continue;
			}
			
			// Only show these summary inter-domain edges if the internals are elided...
			if ( !fromDomain.isShowInternals() && !toDomain.isShowInternals() ) {

			if ( listDomains.contains(fromDomain) && listDomains.contains(toDomain)) {
					String fromDomainId = fromDomain.getId();
					
					String toDomainId = toDomain.getId();
					
					if ((fromDomainId != null) && (toDomainId != null)) {
						StringBuffer builder = new StringBuffer();
						builder.append(fromDomainId);
						builder.append(" -> ");
						builder.append(toDomainId);
						builder.append(" [style = ");
						builder.append("dotted");
						// builder.append( ", label= \"" );
						// builder.append( getEdgeLabel(edge) );
						// builder.append( "\"" );
						// builder.append( ", minlen = 4");
						builder.append("];");
						String edgeString = builder.toString().intern();
						if ( !strings.contains(edgeString) )
							strings.add( edgeString );
					}
				}
				}
		}
	}

	private DisplayDomain getTopDomain(DisplayObject toObject) {
		DisplayDomain topDomain = null;
		
		DisplayObject displayObject = toObject;
		
		while(displayObject != null && displayObject != rootDisplayObject ) {
			topDomain = displayObject.getDomain();
			if (topDomain != null && topDomain.isFormal() ) {
				topDomain = null;
				break;
			}
			if ( topDomain != null && topDomain != rootDisplayDomain ) {
				displayObject = topDomain.getObject();
			}
		}
		
	    return topDomain;
    }

	private String getEdgeLabel(DisplayEdge edge) {
		String label = edge.getToObject().getLabel();
		return label;
	}

	/**
	 * Return the DOT syntax for the edge style, based on the edge type
	 */
	private String getEdgeStyle(DisplayEdge edge) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\"");
		// One idea is to show summary edge with dotted/bold line to distinguish them better.
		// But this seems artificial to distinguish between edges that way
		if (edge.isSummary()) {
			buffer.append("dotted");
		}
		
		switch(edge.getEdgeType() ) {
		case PT:
			// Still solid
			buffer.append("solid");
			break;
		case DF:
			buffer.append("solid");
			buffer.append(", setlinewidth(3)");
			break;
		case CR:
			break;
		case CF:
			break;
		}

		buffer.append("\"");
		return buffer.toString();
	}

	private void initFile() {
		printWriter.println("digraph G {");
		printWriter.println("compound = true;");
		// If center=true and the graph can be output on one page, using the default
		// page size of 8.5 by 11 inches if page is not set, the graph is repositioned to be centered on that page
		printWriter.println("center = true;");
		// printWriter.println("concentrate = true;");
		printWriter.println("fontname = Helvetica;");
		printWriter.println("// minlen = 2;");
		printWriter.println(VisualReportOptions.getInstance().getRankDir());
		printWriter.println("// size=\"8, 10\";");
		printWriter.println("// orientation=portrait;");
	}

	private void closeFile() {
		printLegend();

		// printReferences();

		printWriter.println("}");

		printWriter.flush();

		printWriter.close();
		
	}

	/**
	 * Produce a legend that shows the following:
	 * <li> Dashed border white-filled rectangle represents an actual domain </li>
	 * <li> Dotted border white-filled rectangle represents a formal domain </li>
	 * <li> Solid border grey-filled rectangle with a bold label represents an object </li>
	 * <li> Dashed edge represents a link permission between two ownership domains </li>
	 * <li> A solid edge represents creation, usage, or reference between two objects </li>
	 * <li> An object labelled ``obj : T" indicates an object of declared type $T$ as in UML object diagrams </li>
	 * Future work:
	 * <li> Distinguish between creation, reference, usage edges </li>
	 * <li> Distinguish between pulled edges and other edges </li>
	 * <li> Distinguish between pulled objects and other objects </li>
	 */
	private void printLegend() {
		String reportFile = getReportFullPath() + "legend.dot";
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new FileWriter(reportFile));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		printWriter.println("digraph G { ");
		printWriter.println("  compound=true; ");
		printWriter.println("     subgraph cluster_legend { ");
		printWriter.println("       subgraph cluster_root {  ");
		printWriter.println("         subgraph cluster_formal { ");
		printWriter.println("           subgraph cluster_formalobject {  ");
		printWriter.println("             root_formalobject[label=\"Object : DeclaredType\", shape = \"none\", fontname=\"Helvetica-Bold\", tooltip=\"\", URL=\"\"] ");
		printWriter.println("             style =\"filled, solid\"; ");
		printWriter.println("             fillcolor = lightgrey; ");
		printWriter.println("             shape = box; ");
		printWriter.println("           } ");
		printWriter.println("           label=\" formal_domain\"; ");
		printWriter.println("           labelloc = b; ");
		printWriter.println("           shape = rounded; ");
		printWriter.println("           fillcolor=white; ");
		printWriter.println("           style = \"filled, dotted\"; ");
		printWriter.println("         } ");
		printWriter.println("         subgraph cluster_domain1 {  ");
		printWriter.println("           subgraph cluster_subobject {  ");
		printWriter.println("             root_subobject[label=\"Sub_Object: DeclaredType\", shape = \"none\", fontname=\"Helvetica-Bold\", tooltip=\"\", URL=\"\"] ");
		printWriter.println("             style =\"filled, solid\"; ");
		printWriter.println("             fillcolor = lightgrey; ");
		printWriter.println("             shape = box; ");
		printWriter.println("           } ");
		printWriter.println("           label=\"actual_domain1\"; ");
		printWriter.println("           labelloc = b; ");
		printWriter.println("           shape = rounded; ");
		printWriter.println("           fillcolor=white; ");
		printWriter.println("           style = \"filled, dashed\"; ");
		printWriter.println("         } ");
		printWriter.println("         subgraph cluster_domain2 {  ");
		printWriter.println("           subgraph cluster_domain2_root_pulledObject {  ");
		printWriter.println("             root_pulledObject[label=\"Pulled_Object : DeclaredType\", shape = \"none\", fontname=\"Helvetica-Bold\", tooltip=\"\", URL=\"\"] ");
		printWriter.println("             style =\"filled, solid\"; ");
		printWriter.println("             fillcolor = lightgrey; ");
		printWriter.println("             shape = box; ");
		printWriter.println("           } ");
		printWriter.println("           label=\"actual_domain2\"; ");
		printWriter.println("           labelloc = b; ");
		printWriter.println("           shape = rounded; ");
		printWriter.println("           fillcolor=white ");
		printWriter.println("           style = \"filled, dashed\"; ");
		printWriter.println("         } ");
		printWriter.println("         root[label=\"Root_Object : DeclaredType\", shape = \"none\", fontname=\"Helvetica-Bold\", tooltip=\"system\", URL=\"\"] ");
		printWriter.println("         style =\"filled, solid\"; ");
		printWriter.println("         fillcolor = lightgrey; ");
		printWriter.println("         shape = box; ");
		printWriter.println("       } ");
		printWriter.println("       fontname=\"Helvetica-Bold\";     ");
		printWriter.println("       label=\" LEGEND\"; ");
		printWriter.println("       labelloc = b; ");
		printWriter.println("       shape = rounded; ");
		printWriter.println("       fillcolor=white; ");
		printWriter.println("       style = filled; ");
		printWriter.println("     } ");
		printWriter.println("      ");
		printWriter.println("     root_pulledObject -> root_formalobject [label=\"domain link\", minlen=2,style=dashed, lhead=cluster_formal, ltail=cluster_domain2]; ");
		printWriter.println("     root -> root_subobject[label=\"field reference\", style = solid]; ");
		printWriter.println("} ");

		printWriter.close();
	}

	
	private String getReportFullPath() {
		// HACK: ADD ME
	    return null;
    }

	private void reportDomainLink(DisplayObject parentObject, DisplayDomainLink domainLink, String indent,
			Set<DisplayDomain> listDomains) {
		DisplayDomain fromDomain = domainLink.getFromDomain();
		DisplayDomain toDomain = domainLink.getToDomain();

		if ( fromDomain != null && toDomain != null ) {
			String fromClusterName = fromDomain.getId();
			String toClusterName = toDomain.getId();

			if ((fromClusterName != null) && (toClusterName != null) && listDomains.contains(fromDomain)
					&& listDomains.contains(toDomain)) {
				StringBuffer builder = new StringBuffer();
				builder.append(indent);
				builder.append("cluster_" + fromClusterName);
				builder.append(" -> ");
				builder.append("cluster_" + toClusterName);
				builder.append("[label=\"");
				builder.append(fromDomain.getLabel());
				builder.append(" --> ");
				builder.append(toDomain.getLabel());
				builder.append("\", style = dashed];");
				// builder.append( "\", style = dashed, minlen = 4];" );
				printWriter.println(builder.toString());
			}
		}
	}

	private static boolean isExcluded(DisplayDomain displayDomain) {
		if (!displayDomain.isRoot()) {
			String name = displayDomain.getLabel();
			return ((name.compareTo(Constants.LENT) == 0) || (name.compareTo(Constants.UNIQUE) == 0) || (name.compareTo(Constants.SHARED) == 0));
		}
		return false;
	}

	/*
	 * Return true if the object has substructure; used to determine if a (+) must be added to the label
	 */
	private boolean reportDomain(DisplayDomain displayDomain, String indent, int depth,
	        Set<DisplayDomain> listDomains, Set<DisplayObject> listObjects, DisplayObject parentDisplayObject) {
		
		Collection<DisplayObject> displayObjects = displayDomain.getObjects();

		if (depth >= options.getDisplayDepth() || isExcluded(displayDomain) 
		        || (!parentDisplayObject.isShowFormals() && displayDomain.isFormal()) 
		        || ( !options.isShowPrivateDomains() && !displayDomain.isPublic()) || displayObjects.size() == 0 ) {
			return false;
		}

		listDomains.add(displayDomain);

		String clusterDomain = displayDomain.getId();
		printWriter.println(indent + "subgraph " + clusterDomain + "  /*" +  " Domain = " + displayDomain.getLabel() +  "  */" + " { ");
		
		boolean hasVisibleObjects = false;
		if (displayDomain.isShowInternals() ) {
			for (DisplayObject displayObject : displayDomain.getObjects()) {

				if (!displayObject.isVisible()) {
					continue;
				}

				hasVisibleObjects = true;
				reportObjects(displayObject, indent, depth, listDomains, listObjects, displayDomain);
			}
		}
		if ( !hasVisibleObjects ) {
			StringBuffer builder = new StringBuffer();
			builder.append(indent + indent);
			
			builder.append(clusterDomain.replaceFirst("cluster_", "")); // Remove the prefix cluster_
			builder.append(" [label=\""); 
			if (!displayDomain.isShowInternals() ) {
				// When hiding a domain's internals, display (+) as a reminder that there are objects that are not being shown.
				builder.append("(+)");
			}
			else {
				// This is just an empty domain, either because it has no objects, or because all the objects inside it are marked Visible = false;
				builder.append("");  
			}
			builder.append("\", shape = \"plaintext\", fontname=\"Helvetica-Bold\", tooltip=\"");
			builder.append("\" ];");
			printWriter.println(builder.toString());
		}

		printWriter.println(indent + "label=\" " + getLabel(displayDomain) + "\";");
		printWriter.println(indent + "labelloc = b;");
		printWriter.println(indent + "shape = rounded;");
		printWriter.println(indent + "fillcolor=white;");
		if (displayDomain.isFormal()) {
			printWriter.println(indent + "style = \"filled, dotted\";");
		}
		else {
			// Show both top-level domains and public domains as thin border		
			if ( displayDomain.isPublic() || displayDomain.isTopLevel() ) { 
				printWriter.println(indent + "style = \"filled, dashed\";");
			}
			else {
				printWriter.println(indent + "style = \"filled, dashed, setlinewidth(3)\";");
			}
		}
		printWriter.println(indent + "} "  + "  /*" +  " Domain = " + displayDomain.getLabel() +  "  */" );
		
		return true;
	}

	private String getLabel(DisplayDomain displayDomain) {
		StringBuffer builder = new StringBuffer();
	    //builder.append(displayDomain.getLabel());
		builder.append(displayDomain.getQualifiedName());
		// builder.append(displayDomain.getClassQualifiedName());
	    return builder.toString();
    }
    
	private String reportObjects(DisplayObject displayObject, String indent, int depth,
	        Set<DisplayDomain> listDomains, Set<DisplayObject> listObjects, DisplayDomain displayDomain) {

			String clusterObjectName = displayObject.getId();

			printWriter.println(indent + indent + "subgraph cluster_" + clusterObjectName + "  /*" + " Object = " + displayObject.getLabel() + "  */" + " { ");
			
			// If object has non-empty domains
			boolean showSubstructure = false;
			Collection<DisplayDomain> domains = displayObject.getDomains();
			if (displayObject.isShowInternals() && domains.size() > 0 ) {
				for (DisplayDomain domain : domains) {
					showSubstructure |= reportDomain(domain, indent + indent, depth + 1, listDomains, listObjects, displayObject);
				}
			}

			StringBuffer builder = new StringBuffer();
			builder.append(indent + indent);
			builder.append(clusterObjectName);
			builder.append(" [label=\"");
			builder.append(getLabel(displayObject,showSubstructure));
			builder.append("\", shape = \"none\", fontname=\"Helvetica-Bold\", tooltip=\"");
			// Note: tooltip is used for the client-side image map.
			// IMPORTANT: SHOULD NOT HAVE SPACES IN IT!!!!
			// TODO: Use getNameWithType() instead of getName(), e.g., there may be more than one "obj" object in a domain
			builder.append(ObjectGraphUtil.stripChars(displayObject.getLabel()));
			// Note: Using hashcode as URL for the client-side image map
			builder.append("\", URL=\"");
			builder.append(clusterObjectName);
			builder.append("\" ];");
			printWriter.println(builder.toString());
			printWriter.println(indent + indent + "style =\"filled, solid" + getObjectLineWidth(displayObject) + "\";");
			printWriter.println(indent + indent + getColor(displayObject));
			printWriter.println(indent + indent + getObjectLineColor(displayObject));
			printWriter.println(indent + indent + "shape = box;");
			printWriter.println(indent + indent + "}" + "  /*" + " Object = " + displayObject.getLabel() + "  */");

			// Add it to the list
			listObjects.add(displayObject);

			return clusterObjectName;
	}
	

	/*
	 * NOTE: For objects, must add ";"
	 */
	private String getColor(DisplayObject object) {
		// Objects are not highlighted, only edges
		StringBuilder buffer = new StringBuilder();
		buffer.append("fillcolor = "); 
		buffer.append(graphStateMgr.getDotObjectFillColor((IObject) object.getElement()));
		buffer.append(";");
		return buffer.toString();
		
/*		StringBuilder buffer = new StringBuilder();
		// buffer.append("fillcolor = lightgrey;");
		// TODO: Add options for colors, etc.	
		if(object.isHighlighted()) {
			buffer.append("fillcolor = orange;");	
		}
		else {
			buffer.append("fillcolor = lightyellow;");
		}
		return buffer.toString();
*/	}
	
	
	private String getObjectLineWidth(DisplayObject object) {
		// XXX. already has ",". Clean this up!
		String dotObjectLineWidth = graphStateMgr.getDotObjectLineWidth((IObject) object.getElement());
		if(!dotObjectLineWidth.isEmpty()) {
			StringBuilder builder = new StringBuilder();
//			builder.append(",");
			builder.append(dotObjectLineWidth);
			return builder.toString();
		}
		return dotObjectLineWidth;
	}

	private String getObjectLineColor(DisplayObject object) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("color = ");
		buffer.append(graphStateMgr.getDotObjectEdgeColor((IObject) object.getElement()));
		buffer.append(";");
		return buffer.toString();
	}

	/*
	 * NOTE: For edges, must NOT add ";"
	 */
	private String getColor(DisplayEdge edge) {
		StringBuffer buffer = new StringBuffer();
		if(edge.isHighlighted()) {
			buffer.append("color = orange");	
		}
		else {
			buffer.append("color = black");
		}
		return buffer.toString();
	}
	
	
	private String getLabel(DisplayObject object, boolean showSubstructure) {
		StringBuffer builder = new StringBuffer();
		if (!options.isShowVariableNames()) {
			builder.append(object.getTypeDisplayName());
			if ( appendPlus(object,showSubstructure) ) {
				builder.append("(+)");
			}
		}
		else {
			builder.append(object.getInstanceDisplayName());
			if ( builder.length() > 0 ) {
				if ( appendPlus(object,showSubstructure) ) {
					builder.append("(+)");
				}
			}
			if (options.isShowObjectTypes()) {
				builder.append(":");
				builder.append("\\n");
				builder.append(object.getTypeDisplayName());
			}
			if (options.isShowExtraLabels() ) {
				String extraLabels = object.getExtraLabels();
				if (extraLabels != null && extraLabels.length() > 0) {
					builder.append("\\n(");
					builder.append(extraLabels);
					builder.append(")");
				}
			}			
		}
		return builder.toString();
	}

	// Append a (+) only if the object's substructure is hidden, and there are actually sub-domains!
	// Note: Could potentially make this more precise, e.g., by ensuring that the domains are not empty
	private boolean appendPlus(DisplayObject object, boolean showSubstructure) {
		return object.hasDomains() && (!showSubstructure);
	}

	public DisplayDomain getRootDisplayDomain() {
    	return rootDisplayDomain;
    }

	public DisplayObject getRootDisplayObject() {
    	return rootDisplayObject;
    }


    public OGraphState getGraphState() {
	    return graphState;
    }

	public void setGraphState(OGraphState state) {
		this.graphState = state;
		graphStateMgr = new OGraphStateMgr(state);
    }

	public Set<DisplayEdge> getEdges() {
	    return displayModel.getEdges();
    }

}
