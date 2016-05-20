package edu.cmu.cs.viewer.objectgraphs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

// TODO: Move this up one package-level since it has options applicable to other than the VisualGraph
/**
 * 
 * XXX. Usage edges: Rename with PT, DF, CF, etc.
 * What does ArchDoc do?
 * 
 */
public class VisualReportOptions {

	public static final int NESTING_DEPTH = 5;
	private static final int TREE_DEPTH = 5;

	private int maxDepth = VisualReportOptions.NESTING_DEPTH;
	
	// XXX. Expose this in the UI
	private int displayDepth = VisualReportOptions.NESTING_DEPTH;  
	
	private int treeDepth =  VisualReportOptions.TREE_DEPTH;

	private boolean showDomainLinks = false;

	private boolean showObjectTypes = true;

	private boolean showExtraLabels = true;

	// CF
	private boolean showControlFlowEdges = true;

	// PT
	private boolean showReferenceEdges = true;
	
	// CR
	private boolean showCreationEdges = false;

	// DF
	private boolean showUsageEdges = false;

	private boolean showVariableNames = true;
	
	private boolean useFDP = false;
	
	private boolean layoutLR = true;
	
	private boolean showPrivateDomains = true;

	private boolean showEdgeLabels = false;

	private boolean useFullyQualifiedName = false;
	
	// Show Qualified Domain Names by default, to make sense of Abstraction By Types
	private boolean showQualifiedDomainNames = false;
	
	private boolean showTopLevelObject = false;
	
	private static VisualReportOptions instance = null;

	private boolean initialized = false;
	
	private String att_dot_dir;
	
	private File tempDirectory;
	
	private boolean enableShowingFormals = false;
	
	private boolean useTrivialTypes = false;
	
	private boolean useDesignIntentTypes = false;
	
	private boolean instantiationView = false;
	
	/**
	 * Set to true to display arrowheads on both ends of an edge, to give the ilussion of a bi-directional edge; care
	 * must be taken to avoid also displaying the edge in the other direction.
	 */
	private boolean useBiDirectionalArrows = false;
	
	/**
	 * Set to true to generate an architectural model from the OOG when the Finish button is clicked
	 */
	private boolean generateArchitecturalModel = false;
	
	public static VisualReportOptions getInstance() {
		if (VisualReportOptions.instance == null) {
			VisualReportOptions.instance = new VisualReportOptions();
		}

		return VisualReportOptions.instance;
	}

	public boolean isShowDomainLinks() {
		return showDomainLinks;
	}

	public void setShowDomainLinks(boolean showDomainLinks) {
		this.showDomainLinks = showDomainLinks;
	}

	public boolean isShowObjectTypes() {
		return showObjectTypes;
	}

	public void setShowObjectTypes(boolean showObjectTypes) {
		this.showObjectTypes = showObjectTypes;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public boolean isShowControlFlowEdges() {
		return showControlFlowEdges;
	}

	public void setShowControlFlowEdges(boolean showFieldLinks) {
		this.showControlFlowEdges = showFieldLinks;
	}
	
	// CR
	public boolean isShowCreationEdges() {
		return showCreationEdges;
	}

	public void setShowCreationEdges(boolean showCreationEdges) {
		this.showCreationEdges = showCreationEdges;
	}

	// PT
	public boolean isShowReferenceEdges() {
		return showReferenceEdges;
	}

	public void setShowReferenceEdges(boolean showReferenceEdges) {
		this.showReferenceEdges = showReferenceEdges;
	}

	// DF
	public boolean isShowUsageEdges() {
		return showUsageEdges;
	}

	public void setShowUsageEdges(boolean showUsageEdges) {
		this.showUsageEdges = showUsageEdges;
	}

	public boolean isShowVariableNames() {
		return showVariableNames;
	}

	public void setShowVariableNames(boolean useVariableNames) {
		showVariableNames = useVariableNames;
	}

	public boolean isUseFDP() {
    	return useFDP;
    }

	public void setUseFDP(boolean useFDP) {
    	this.useFDP = useFDP;
    }

	public int getTreeDepth() {
    	return treeDepth;
    }

	public void setTreeDepth(int treeDepth) {
    	this.treeDepth = treeDepth;
    }

	public boolean isShowPrivateDomains() {
	    return showPrivateDomains;
    }

	public void setShowPrivateDomains(boolean showPrivateDomains) {
    	this.showPrivateDomains = showPrivateDomains;
    }

	public int getDisplayDepth() {
    	return displayDepth;
    }

	public void setDisplayDepth(int displayDepth) {
    	this.displayDepth = displayDepth;
    }

	public boolean isUseFullyQualifiedName() {
    	return useFullyQualifiedName;
    }

	public void setUseFullyQualifiedName(boolean useFullyQualifiedName) {
    	this.useFullyQualifiedName = useFullyQualifiedName;
    }
	
	
	public boolean isShowQualifiedDomainNames() {
    	return showQualifiedDomainNames;
    }

	public void setShowQualifiedDomainNames(boolean showQualifiedDomainNames) {
    	this.showQualifiedDomainNames = showQualifiedDomainNames;
    }


	public String getExecutable(Shell parent) {
	    if ( !initialized ) {
			att_dot_dir = System.getenv("ATT_DOT_DIR");
			if (att_dot_dir == null) {
				MessageBox messageBox = new MessageBox(parent, SWT.OK);
				messageBox.setText("Ownership Object Graph (OOG) Wizard");
				messageBox.setMessage("Environment variable for GraphViz 'ATT_DOT_DIR' not set!");
				messageBox.open();
				return null;
			}
			
			String temp = System.getenv("temp");
			tempDirectory = new File(temp);

	    	initialized = true;
	    }
	    
	    StringBuffer buffer = new StringBuffer();
	    buffer.append(att_dot_dir);
	    buffer.append( File.separator );
	    if ( useFDP ) {
	    	buffer.append("fdp.exe");
	    }
	    else {
	    	buffer.append("dot.exe");
	    }
	    
	    return buffer.toString();
    }

	public String getEngine() {
	    StringBuffer buffer = new StringBuffer();
	    if ( useFDP ) {
			// Force layout engine to be FDP
	    	buffer.append("-Kfdp");
	    }
	    else {
	    	buffer.append("-Kdot");
	    }
	    
	    return buffer.toString();
    }

	public File getTempDirectory() {
		return tempDirectory;
    }

	public void setLayoutLR(boolean layoutLR) {
		this.layoutLR = layoutLR;
    }

	public boolean isLayoutLR() {
    	return layoutLR;
    }
	
	public String getRankDir() {
		StringBuilder builder = new StringBuilder("rankdir = ");
		if ( layoutLR ) {
			builder.append("LR;");
		}
		else{
			builder.append("TB;");
		}
		
		return builder.toString();
	}

	public boolean isShowTopLevelObject() {
    	return showTopLevelObject;
    }

	public void setShowTopLevelObject(boolean showTopLevelObject) {
    	this.showTopLevelObject = showTopLevelObject;
    }

	public boolean isEnableShowingFormals() {
	    return this.enableShowingFormals;
    }

	public void setEnableShowingFormals(boolean showFormals) {
		this.enableShowingFormals = showFormals;
    }

	public boolean isUseTrivialTypes() {
    	return useTrivialTypes;
    }

	public void setUseTrivialTypes(boolean useTrivialTypes) {
    	this.useTrivialTypes = useTrivialTypes;
    }

	public boolean isInstantiationView() {
    	return instantiationView;
    }

	public void setInstantiationView(boolean instantationView) {
    	this.instantiationView = instantationView;
    }

	public boolean isUseBiDirectionalArrows() {
    	return useBiDirectionalArrows;
    }

	public void setUseBiDirectionalArrows(boolean useBiDirectionalArrows) {
    	this.useBiDirectionalArrows = useBiDirectionalArrows;
    }

	public boolean isGenerateArchitecturalModel() {
    	return generateArchitecturalModel;
    }

	public void setGenerateArchitecturalModel(boolean generateArchitecturalModel) {
    	this.generateArchitecturalModel = generateArchitecturalModel;
    }
	
	public boolean isUseDesignIntentTypes() {
    	return useDesignIntentTypes;
    }

	public void setUseDesignIntentTypes(boolean useDesignIntentTypes) {
    	this.useDesignIntentTypes = useDesignIntentTypes;
    }
	
	public boolean isShowEdgeLabels() {
    	return showEdgeLabels;
    }

	public void setShowEdgeLabels(boolean showEdgeLabels) {
    	this.showEdgeLabels = showEdgeLabels;
    }

	public boolean isShowExtraLabels() {
    	return showExtraLabels;
    }

	public void setShowExtraLabels(boolean showExtraLabels) {
    	this.showExtraLabels = showExtraLabels;
    }

}
