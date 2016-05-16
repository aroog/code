package edu.wayne.ograph;

import oog.itf.IObject;

/**
 * Map between (name, value) pairs and other things used in the system
 * 
 * NOTE: We get both "true" and "True", "false" and "False"... Gotta do case insensitive string comparison
 * 
 *  XXX. Why not traverse the state once and set the corresponding properties on the OObject?
 *  So all the layers can use these colors? 
 *  
 */
public class OGraphStateMgr {
	// X11 color scheme:
	private static final String COLOR_TRUST_LEVEL_TRUSTED = "chartreuse1"; // green
	private static final String COLOR_TRUST_LEVEL_UNTRUSTED = "crimson"; // red
	private static final String COLOR_TAINTED = "crimson"; // overloaded -> blue 
	private static final String COLOR_CONFIDENTIAL = "crimson"; // overloaded -> green
	
	// Global category names
	public static final String ANALYSIS_STATE = "analysis";
	public static final String GRAPH_STATE = "graph";
	
	// Global/analysis options
	public static final String RUN_OOGRE = "runOOGRE";
	public static final String RUN_POINTS_TO = "runPointsTo";
	
	// Global/graph options
	
	// Object-level options
	public static final String TRUST_LEVEL = "trustLevel";
	public static final String TRUSTED = "Trusted";
	public static final String UNTRUSTED = "Untrusted";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String IS_CONFIDENTIAL = "isConfidential";
	public static final String IS_SANITIZED = "isSanitized";
	
	protected OGraphState graphState = null;
	
	private GlobalState globalState = null;
	
	private DisplayState displayState = null;


	public OGraphStateMgr(OGraphState graphState) {
	    super();
	    this.graphState = graphState;
    }

	/**
	 * Pre-populate with global/analysis properties
	 */
	public GlobalState getGlobalState() {
		return new GlobalState(this);
	}
	
	/**
	 * Pre-populate with display/graph properties
	 */
	public DisplayState getDisplayState() {
		return new DisplayState(this);
	}
	
	protected String getGlobalPropertyValue(String category, String propertyName) {
		if(graphState != null ) {
    		PropertyList properties = graphState.getProperties(category); 
    		if(properties != null ) {
    			return properties.getProperty(propertyName);
    		}
    	}
		
		return null;
	}
	
	public void setGlobalPropertyValue(String category, String propertyName, String propertyValue) {
		if(graphState != null ) {
    		PropertyList properties = graphState.getProperties(category); 
    		if(properties != null ) {
    			properties.addProperty(propertyName, propertyValue);
    		}
    	}
	}	

	/**
	 * Return valid GraphViz style based on property value. Or reasonable default.
	 */
    public String getDotObjectLineWidth(IObject oObject) {
    	String style = ""; // Default
    	
    	String objectKey = oObject.getObjectKey();
    	if(graphState != null && objectKey != null ) {
    		PropertyList properties = graphState.getProperties(objectKey); 
    		if(properties != null ) {
    			String isConfidential = properties.getProperty(IS_CONFIDENTIAL);
    			if (TRUE.equalsIgnoreCase(isConfidential)) {
    				style = ", setlinewidth(3)";
    			}
    			String isSanitized = properties.getProperty(IS_SANITIZED);
    			if (FALSE.equalsIgnoreCase(isSanitized)) {
    				style =", setlinewidth(3)";
    			}    			
    		}
    	}
    	return style;
    }


	/**
	 * Return valid GraphViz dot color based on property value. Or reasonable default.
	 * TODO: Rename: ObjectEdge -> ObjectBorder! or ObjectLine
	 * 
	 * XXX. Write down all these graphical conventions...
	 */
    public String getDotObjectEdgeColor(IObject oObject) {
    	String color = "black"; // Default
    	
    	String objectKey = oObject.getObjectKey();
    	if(graphState != null && objectKey != null ) {
    		PropertyList properties = graphState.getProperties(objectKey); 
    		if(properties != null ) {
    			String isConfidential = properties.getProperty(IS_CONFIDENTIAL);
    			if (TRUE.equals(isConfidential)) {
    				color = COLOR_CONFIDENTIAL;
    			}
    			String isSanitized = properties.getProperty(IS_SANITIZED);
    			if (FALSE.equalsIgnoreCase(isSanitized)) {
    				color = COLOR_TAINTED;
    			}
    			
    		}
    	}
    	return color;
    }

    
	/**
	 * Return valid GraphViz dot color based on property value. Or reasonable default.
	 * 
	 * XXX. Could pass just the OObjectKey
	 */
    public String getDotObjectFillColor(IObject oObject) {
    	String color = "lightyellow"; // Default
    	
    	String objectKey = oObject.getObjectKey();
    	if(graphState != null && objectKey != null ) {
    		PropertyList properties = graphState.getProperties(objectKey); 
    		if(properties != null ) {
    			String trustLevel = properties.getProperty(TRUST_LEVEL);
				if (UNTRUSTED.equals(trustLevel)) {
					color = COLOR_TRUST_LEVEL_UNTRUSTED;
				}
				else if (TRUSTED.equals(trustLevel)) {
					color = COLOR_TRUST_LEVEL_TRUSTED;
				}
    		}
    	}
    	return color;
    }
    
	/**
	 * Return valid GraphViz dot color based on property value. Or reasonable default.
	 * 
	 * XXX. IEdge does not have an E_Id, or an OEdgeKey
	 * XXX. Why not set width based on highlight? Instead of changing color
	 * XXX. This method does not have to be here...
	 */
    /*CUT: public String getDotEdgeColor(IEdge edge) {
    	StringBuffer buffer = new StringBuffer();
    	if(edge.isHighlighted()){
    		buffer.append("color = crimson");	
    	}
    	else {
    		buffer.append("color = black");
    	}
    	return buffer.toString();
    }*/
	
}
