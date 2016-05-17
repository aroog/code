package edu.wayne.dot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import edu.wayne.flowgraph.FlowGraph;
import edu.wayne.flowgraph.FlowGraphEdge;
import edu.wayne.flowgraph.FlowGraphNode;

public class DotFlowGraphExport {
private FlowGraph fg;
private static final String U_SEP = "_";
private static final String DOT_SEP = "\\.|:|<|>|,|\\[|\\]|@";

public DotFlowGraphExport(FlowGraph fg) {
	this.fg = fg;
}

public void writeToFile(String filepath) {
	
	Writer output = null;
	File file = new File(filepath);

	try {

		output = new BufferedWriter(new FileWriter(file));

		// Begin the dot file
		output.write(getTextHeader());
		List<FlowGraphEdge> edges = fg.getEdges();
		for(FlowGraphNode node:fg.getVertices()){
			StringBuffer sb = new StringBuffer();
			String replaceAll = node.getO().getO_id().replaceAll(DOT_SEP, U_SEP);
			sb.append(replaceAll);
			sb.append("_");
			sb.append(node.getX().toString().replaceAll(DOT_SEP, U_SEP));
			sb.append("_");
			sb.append(node.getB().getShortName().replaceAll(DOT_SEP, U_SEP));
			sb.append("[label=\"");
			sb.append(node.getO().getInstanceDisplayName() + ","+node.getX()+","+node.getB());
			sb.append("\"];\n");
			output.write(sb.toString());
		}
		for (FlowGraphEdge edge : edges) {
			StringBuffer sb = new StringBuffer();
			sb.append(edge.getSrc().getO().getO_id().replaceAll(DOT_SEP, U_SEP));
			sb.append("_");
			sb.append(edge.getSrc().getX().toString().replaceAll(DOT_SEP, U_SEP));
			sb.append("_");
			sb.append(edge.getSrc().getB().getShortName().replaceAll(DOT_SEP, U_SEP));
			sb.append("->");
			sb.append(edge.getDst().getO().getO_id().replaceAll(DOT_SEP, U_SEP));
			sb.append("_");
			sb.append(edge.getDst().getX().toString().replaceAll(DOT_SEP, U_SEP));
			sb.append("_");
			sb.append(edge.getDst().getB().getShortName().replaceAll(DOT_SEP, U_SEP));
			sb.append("[label=\"");
			sb.append(edge.getLabel());
			sb.append("\"];\n");
			//sb.append(";");
			output.write(sb.toString());
		}
		
		// End the dot file
		output.write(getTextFooter());

		// Close the writer
		output.close();

	} catch (IOException e) {
		e.printStackTrace();
	} catch (Exception e1) {
		e1.printStackTrace();
	}
}

protected String getTextHeader() {
	String start = "digraph G { \n" + "compound = true;\n"
	+ "center = true;\n" + "fontname = Helvetica;\n"
	+ "rankdir = TD;\n" + "size=\"8, 10\";\n"
	+ "orientation=portrait;\n";
	return start;
}


protected String getTextFooter() {
	String end = "}";
	return end;
}
}
