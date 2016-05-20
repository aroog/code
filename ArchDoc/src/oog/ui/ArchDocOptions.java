package oog.ui;


/**
 * Temporary solution to enable/disable specific windows/views in the Runtime Perspective.
 * For debugging slow loading time on large files.
 * 
 * TODO: Update/load things more lazily, on demand;
 * 
 * TODO: Store these options persistently in the Eclipse Preference store  
 */
public class ArchDocOptions {
	private static ArchDocOptions instance;
	
	// TODO: Turn this back on
	private boolean enablePartialOOG = true;
	
	// RelatedObjects/Edges builds/maintains an expensive map; disable to speed things up
	private boolean enableRelatedObjectsEdges = true;	

	private ArchDocOptions(){
	}
	
	public static ArchDocOptions getInstance(){
		if(instance == null){
			instance = new ArchDocOptions();
		}
		
		return instance;
	}
	
	public boolean isEnablePartialOOG() {
    	return enablePartialOOG;
    }

	public void setEnablePartialOOG(boolean enablePartialOOG) {
    	this.enablePartialOOG = enablePartialOOG;
    }	
}
