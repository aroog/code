package edu.wayne.summary.strategies;

// TODO: Rename Options -> ArchSummaryOptions
public class Options {
  
	private static Options options = null;
	private boolean includeConstructors = false;
	private boolean includeStaticMethods = false;
	private boolean includeInterfaces = false;
	
	// MIC options
	private boolean includeObjectsInPublicDomains = false;
	private boolean includeObjectsInSHARED = false;

	// Singleton pattern: private constructor
	private Options(){
	}
	
	public static Options getInstance(){
		if(options == null){
			options = new Options();
		}
		return options;
	}

	public boolean includeConstructors() {
		return includeConstructors;
	}
	
	public boolean includeInterfaces() {
		return includeInterfaces;
	}
	
	public boolean includeStaticMethods() {
		return includeStaticMethods;
	}

	public boolean includeObjectsInPublicDomains() {
    	return includeObjectsInPublicDomains;
    }

	public boolean isIncludeObjectsInSHARED() {
    	return includeObjectsInSHARED;
    }

}
