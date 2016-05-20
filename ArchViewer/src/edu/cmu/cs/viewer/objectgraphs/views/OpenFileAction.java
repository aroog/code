package edu.cmu.cs.viewer.objectgraphs.views;

import edu.cmu.cs.viewer.objectgraphs.ResourceLineKey;

public class OpenFileAction extends ShowAction {

	private ResourceLineKey traceability;
	
	private String originPath;
	
	public OpenFileAction() {
		super("Trace to Code");
	}

	@Override
	public void run() {
		OpenFile file = new OpenFile();

		file.openFile(treeViewer.getControl().getShell(), makeAbsolute(traceability.getResource(),originPath), traceability.getLine());
	}
	
	private static String makeAbsolute(String path, String origin) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(origin);
		buffer.append(path);
		return buffer.toString();
	}

	public ResourceLineKey getTraceability() {
    	return traceability;
    }

	public void setTraceability(ResourceLineKey traceability) {
    	this.traceability = traceability;
    }

	public String getOriginPath() {
    	return originPath;
    }

	public void setOriginPath(String originPath) {
    	this.originPath = originPath;
    }
}
