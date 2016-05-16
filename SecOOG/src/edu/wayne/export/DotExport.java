package edu.wayne.export;

import java.io.IOException;
import java.io.Writer;

import edu.wayne.ograph.EdgeFlag;
import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IGraph;
import oog.itf.IObject;

// MOVED from PointsTo OOG.
// DONE. Convert this to no longer use OOG DM!
public class DotExport extends ExportTemplate {
	
	private static final String EDGE_COLOR_DEFAULT = "black";
	
	// XXX. Pick same convention as for DGraph
	private static final String EDGE_COLOR_HIGHLIGHTED = "orange"; // crimson (overloaded)

	private static final String EDGE_COLOR_EXPORT = EDGE_COLOR_DEFAULT; // "red";
	private static final String EDGE_COLOR_IMPORT = EDGE_COLOR_DEFAULT; // "blue";
	
	private static long UNIQUEID = 0;
	
    public DotExport(IGraph oGraph, IDomain dShared) {
		super(oGraph, dShared);
		
	    UNIQUEID = 0;
    }
    
    private String getFillColor(IObject oObject) {
    	String color = "lightyellow"; // Default
    	
    	if(graphStateMgr != null ) {
    		color = graphStateMgr.getDotObjectFillColor(oObject);
    	}
    	return color;
    }
    
    private String getStyle(IObject oObject) {
    	String style = ""; // Default
    	
    	if(graphStateMgr != null ) {
    		style = graphStateMgr.getDotObjectLineWidth(oObject);
    	}
    	return style;
    }

    private String getColor(IObject oObject) {
    	String color = EDGE_COLOR_DEFAULT; // Default
    	
    	if(graphStateMgr != null ) {
    		color = graphStateMgr.getDotObjectEdgeColor(oObject);
    	}
    	return color;
    }

    // Generalize: getEdgeHighlight...
    // Instead of changing color...how about change the width??
    /*CUT: private String getColor(IEdge edge) {
    	String color = EDGE_COLOR_DEFAULT; // Default

    	if(graphStateMgr != null ) {
    		color = graphStateMgr.getDotEdgeColor(edge);
    	}
    	return color;
	}
     */
	protected void outputOwnEdges(Writer output) throws IOException{
	    for(String edge:ownershipEdges){
	    	output.write(edge
					+ " [style = \"dashed\"];"
					+ "\n"
					);
	    }
    }

	// XXX. Extract options for colors for edges: if we don't want to do red/blue for DF edges
	// XXX. Revise this. Extra string manipulation, add/remove "::"; use the edge type
	// XXX. Calling getName() multiple times on the same object! Does a lot of string concatenation
	protected void outputPtEdges(Writer output) throws IOException{
		// XXX. Why not check edge type instead of relying on string contents?
		// XXX. Bad idea to concatenate, then split, then pick one element!!
		for(IEdge edge:oGraph.getEdges()){
			String srcObjectId = getObjectId(edge.getOsrc());
			String dstObjectId = getObjectId(edge.getOdst());
			if (!srcObjectId.equals(dstObjectId)){
			String stringEdge = srcObjectId + " -> " + dstObjectId;
			String edgeName = getName(edge);
			if(edgeName.startsWith(EdgeFlag.Export.name()))
				output.write(stringEdge
						+ " [style = \"solid, "+  getEdgeLineWidth(edge) + "\", color="+ getEdgeColor(edge) + ", label=\""+edgeName.split("::")[1]+"\"];"
						+ "\n"
				);
			else if(edgeName.startsWith(EdgeFlag.Import.name()))
				output.write(stringEdge
						+ " [style = \"solid, "+  getEdgeLineWidth(edge) + "\", color=" + getEdgeColor(edge) + ", label=\""+edgeName.split("::")[1]+"\"];"
						+ "\n"
				);
			else if(edgeName.startsWith("creation"))
				output.write(stringEdge
						+ " [style = \"dotted, setlinewidth(3)\", color=red, label=\""+edgeName.split("::")[1]+"\"];"
						+ "\n"
				);
			else if(edgeName.startsWith("control"))
				output.write(stringEdge
						+ " [style = \"dotted, setlinewidth(1)\", color=black, label=\""+edgeName.split("::")[1]+"\"];"
						+ "\n"
				);
			else{
				output.write(stringEdge
						+ " [style = \"solid, setlinewidth(1)\", color=black, label=\""+edgeName+"\"];"
						+ "\n"
				);
				
			}
			}
		}
	    
    }
	

	private String getEdgeColor(IEdge edge) {
		String color = EDGE_COLOR_DEFAULT;
		 if(edge.isHighlighted()) {
			 return EDGE_COLOR_HIGHLIGHTED;
		 }
		 /*	CUT: if (edge instanceof ODFEdge) {
			 ODFEdge dfEdge = (ODFEdge) edge;
			 switch(dfEdge.getFlag()) {
			 case Export:
				 color = EDGE_COLOR_EXPORT;
				 break;
			 case Import:
				 color = EDGE_COLOR_IMPORT;
				 break;
			 }

		 } else if (edge instanceof OPTEdge) {
			 OPTEdge ptEdge = (OPTEdge) edge;
		 } else if (edge instanceof OCREdge) {
			 OCREdge crEdge = (OCREdge) edge;
		 }  else if (edge instanceof OCFEdge) {
			 OCFEdge crEdge = (OCFEdge) edge;
		 }*/
	    return color;
    }

	// XXX. Switch on edgeType
	// XXX. Right now, called only for DF Edges...
	private String getEdgeLineWidth(IEdge edge) {
		// Increase width (for b&w display) and highlight in red (for color display)
		if(edge.isHighlighted()) {
			return "setlinewidth(5)";
		}
		
	    return "setlinewidth(3)";
    }

	/**
     * @param output
     * @param oObject
     * @throws IOException
     */
    protected void saveObject(Writer output, IObject oObject) throws IOException {
	    output
	    .write(getObjectId(oObject)
	    		+ " [label=\""
	    		+ oObject.getInstanceDisplayName()
					+ ":\\n"
					+ oObject.getTypeDisplayName()
	    		+ "\", fontname=\"Helvetica-Bold\", style =\"filled, solid" + getStyle(oObject)+ "\", fillcolor = " + getFillColor(oObject)+ ", color=" + getColor(oObject) +", shape=\"box\", URL=\"" + 
				getURL(oObject) +	
	    		"\"];  /* Object  "
	    		+ getName(oObject) + " */ " 
	    		+ "\n");
    }






	/**
     * @param output
     * @param oDomain
     * @param domainId
     * @throws IOException
     */
    @Override
    // XXX. Make sure to get C::D in comment area
    // XXX. Refactor this: too much code duplication 
    protected void saveDomain(Writer output, IDomain oDomain, String domainId) throws IOException {
    	// CUT: []
		// if (excludeEmptyDomains && !oDomain.hasChildren()) {
		// return;
		// }
    		
    	String dname = getName(oDomain);
    	// XXX. Could use constant here...
    	if (dname.equals("unique")) dname +=UNIQUEID++;
    	dname = dname.replaceAll("[<>,]", "");
	    if (!oDomain.isPublic()) {
	    	output
	    	.write(domainId
	    			+ " [label=\""
	    			+ dname
	    			+ "\", shape = \"box\", fontname=\"Helvetica-Bold\", style =\"setlinewidth(3), dashed\", URL=\"" + getURL(oDomain) + "\"]; /* Domain  "
	    			+ oDomain.getD() + " */" 
	    			+ "\n");
	    } else {
	    	output
	    	.write(domainId
	    			+ " [label=\""
	    			+ dname
	    			+ "\", shape = \"box\", fontname=\"Helvetica-Bold\", style =\"dashed\", URL=\"" + getURL(oDomain) + "\"]; /* Domain  "
	    			+ oDomain.getD() + " */"
	    			+ "\n");
	    }
    }

	@Override
	protected String getBeginEdges() {
		return "";
	}


	@Override
	protected String getBeginNodes() {
		return "";
	}


	@Override
	protected String getEndEdges() {
		return "";
	}


	@Override
	protected String getEndNodes() {
		return "";
	}


	@Override
	protected String getTextFooter() {
		String end = "}";
		return end;
	}


	@Override
	protected String getTextHeader() {
		String start = "digraph G { \n" + "compound = true;\n"
		+ "center = true;\n" + "fontname = Helvetica;\n"
		+ "rankdir = TD;\n" + "size=\"8, 10\";\n"
		+ "orientation=portrait;\n";
		return start;
	}
}
