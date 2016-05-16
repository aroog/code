package edu.wayne.export;

import java.io.IOException;
import java.io.Writer;

import edu.wayne.ograph.EdgeFlag;
import edu.wayne.ograph.OGraph;
import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IObject;

// MOVED from PointsTo OOG.
public class DgmlExport extends ExportTemplate {

	public DgmlExport(OGraph oGraph, IDomain dShared) {
		super(oGraph, dShared);
	}

	/**
	 * @return
	 */
	@Override
	protected String getEndEdges() {
		return "</Links>";
	}


	/**
	 * @return
	 */
	@Override
	protected String getBeginEdges() {
		return "<Links>\n";
	}


	/**
	 * @return
	 */
	@Override
	protected String getEndNodes() {
		return " </Nodes>\n";
	}


	/**
	 * @return
	 */
	@Override
	protected String getTextFooter() {
		String end = "</DirectedGraph>";
		return end;
	}


	/**
	 * @return
	 */
	@Override
	protected String getTextHeader() {
		String start = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
		"<DirectedGraph xmlns=\"http://schemas.microsoft.com/vs/2009/dgml\">\n";
		return start;
	}

	@Override
	protected String getBeginNodes() {
		return "<Nodes>\n";
	}

	//<Link Source="a" Target="b" />
	protected void outputOwnEdges(Writer output) throws IOException{
		for(String edge:ownershipEdges){
			output.write("<Link Source=\""
					+ edge.split("->")[0].trim()
					+ "\" Target =\""
					+ edge.split("->")[1].trim()
					+ "\" StrokeDashArray=\"2.2\" Category=\"Contains\" />"
					+ "\n"
			);
		}

	}

	@Override
	protected void outputPtEdges(Writer output) throws IOException{
		for(IEdge edge:oGraph.getEdges()){
			//<Link Source="a" Target="b" />
			String edgeLabel = getName(edge);			
			StringBuffer stringEdge = new StringBuffer("<Link Source=\""); 
			stringEdge.append(getObjectId(edge.getOsrc()));
			stringEdge.append("\" Target=\"");
			stringEdge.append(getObjectId(edge.getOdst()));
			stringEdge.append("\" ");
			stringEdge.append("Label=\"");
			if (edgeLabel.contains("::"))
				stringEdge.append(edgeLabel.split("::")[1].trim());
			else
				stringEdge.append(edgeLabel.trim());	
			stringEdge.append("\" ");

			if(edgeLabel.startsWith(EdgeFlag.Export.name()))
				output.write(stringEdge
						+ " Stroke=\"red\" />"
						//+ " [style = \"solid, setlinewidth(3)\", color=red, label=\""+edge.getName().split("::")[1]+"\"];"
						+ "\n"
				);
			else if(edgeLabel.startsWith(EdgeFlag.Import.name()))
				output.write(stringEdge
						+ " Stroke=\"blue\" />"
						//+ " [style = \"solid, setlinewidth(3)\", color=blue, label=\""+edge.getName().split("::")[1]+"\"];"
						+ "\n"
				);
			else
				output.write(stringEdge
						+ " Stroke=\"black\" />"
						//+ " [style = \"solid, setlinewidth(3)\", color=blue, label=\""+edge.getName().split("::")[1]+"\"];"
						+ "\n"
				);
				
		}
	}

	/**
	 * @param output
	 * @param rootObject
	 * @throws IOException
	 * <Node Id="a" Label="a" Size="10" />
	 */
	@Override
	protected void saveObject(Writer output, IObject rootObject) throws IOException {
		output
		.write("<Node Id=\"" 
				+ getObjectId(rootObject) 
				+ "\" Label=\""
				+ rootObject.getInstanceDisplayName()
				+ ":"
//				+ "\" Type=\""
				+rootObject.getTypeDisplayName().replaceAll("<", "&lt;").replaceAll(">", "&gt;")
				+ "\" Foreground=\"#FFF8C6\" Background=\"#FFF8C6\" FontFamily=\"Arial-Bold\" StrokeThickness=\"1\"  Size=\"10\" Group=\"Expanded\" />"				
						//" fontname=\"Helvetica-Bold\", style =\"filled, solid\", fillcolor = lightyellow, shape=\"box\"];  /* Object  "
//				+ rootObject.getName() + " */ " 
				+ "\n");
	}




	/**
	 * @param output
	 * @param oDomain
	 * @param ODomainId
	 * @throws IOException
	 */
	@Override
	protected void saveDomain(Writer output, IDomain oDomain, String ODomainId) throws IOException {
		String dname = getQualifiedName(oDomain);
		dname = dname.replaceAll("[<>,]", "");
		if (!oDomain.isPublic()) {
			output
			.write("<Node Id=\""
					+ ODomainId
					+ "\" Label=\""
					+ dname
					+ "\" Background=\"white\" FontFamily=\"Helvetica-Bold\" StrokeThickness=\"5\" StrokeDashArray=\"2.2\" Size=\"15\" Group=\"Expanded\" />" 
//					+ "\", shape = \"box\", fontname=\"Helvetica-Bold\", style =\"setlinewidth(3), dashed\"]; /* Domain  "
//					+ ODomainId + " */" 
					+ "\n");
		} else {
			output
			.write("<Node Id=\""
					+ ODomainId
					+ "\" Label=\""
					+ dname
					+ "\" Background=\"white\" FontFamily=\"Helvetica-Bold\" StrokeThickness=\"1\" StrokeDashArray=\"2.2\" Size=\"15\" Group=\"Expanded\"/>"
//					+ "\", shape = \"box\", fontname=\"Helvetica-Bold\", style =\"dashed\"]; /* Domain  "
//					+ ODomainId + " */"
					+ "\n");
		}
	}
}
