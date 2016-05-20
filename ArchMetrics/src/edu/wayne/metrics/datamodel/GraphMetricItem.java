package edu.wayne.metrics.datamodel;


public class GraphMetricItem {
	String id; //either O_id or D_id
	NodeType type; 
	int depth;
	int inPtDegree; //should be 0 if type is domain
	int outPtDegree; // should be 0 if type is domain
	int inOutsidePtDegree; // number of pt-edges coming from a non-sibling OObject.
	int inOwnDegree;
	int outOwnDegree;
	int noPublicDomains;
	int noPrivateDomains;
	String declaredTypeName;
	String displayTypeName;
	boolean objectInPublicDomain;
	int noTrLinks;
	int objectScattering;
	/**
     * @param id
     * @param type
     * @param depth
     * @param inPtDegree
     * @param outPtDegree
     * @param inOwnDegree
     * @param outOwnDegree
     */
    public GraphMetricItem(String id, NodeType type, int depth, int inPtDegree, int outPtDegree, int inOwnDegree,
            int outOwnDegree, boolean inPublicDomain) {
	    this.id = id;
	    this.type = type;
	    this.depth = depth;
	    this.inPtDegree = inPtDegree;
	    this.outPtDegree = outPtDegree;
	    this.inOwnDegree = inOwnDegree;
	    this.outOwnDegree = outOwnDegree;
	    this.objectInPublicDomain = inPublicDomain;
    }
    
	/**
     * @param id
     * @param type
     * @param depth
     * @param inPtDegree
     * @param outPtDegree
     * @param inOwnDegree
     * @param outOwnDegree
     * @param typeName
	 * @param traceabilityLinks 
     */
    public GraphMetricItem(String id, NodeType type, int depth, int inPtDegree, int outPtDegree, int inOusidePtDegree, int inOwnDegree,
            int outOwnDegree, int noPublicDomains, int noPrivateDomains, boolean inPublicDomain, String typeName, int traceabilityLinks, String displayTypeName, int scattering) {
	    this.id = id;
	    this.type = type;
	    this.depth = depth;
	    this.inPtDegree = inPtDegree;
	    this.outPtDegree = outPtDegree;
	    this.inOutsidePtDegree = inOusidePtDegree;
	    this.inOwnDegree = inOwnDegree;
	    this.outOwnDegree = outOwnDegree;
	    this.noPublicDomains = noPublicDomains;
	    this.noPrivateDomains = noPrivateDomains;
	    this.objectInPublicDomain = inPublicDomain;
	    this.declaredTypeName = typeName;
	    this.noTrLinks = traceabilityLinks;
	    this.displayTypeName = displayTypeName;
	    this.objectScattering = scattering;
    }
	/**
     * @return the id
     */
    public String getId() {
    	return id;
    }
	/**
     * @return the type
     */
    public NodeType getType() {
    	return type;
    }
	/**
     * @return the depth
     */
    public int getDepth() {
    	return depth;
    }
	/**
     * @return the inPtDegree
     */
    public int getInPtDegree() {
    	return inPtDegree;
    }
	/**
     * @return the outPtDegree
     */
    public int getOutPtDegree() {
    	return outPtDegree;
    }
	/**
     * @return the inOwnDegree
     */
    public int getInOwnDegree() {
    	return inOwnDegree;
    }
	/**
     * @return the outOwnDegree
     */
    public int getOutOwnDegree() {
    	return outOwnDegree;
    }
    
    public boolean isInPublicDomain(){
    	return objectInPublicDomain;
    }
    
    @Override
    public String toString(){
    	StringBuffer sb = new StringBuffer();
	    	sb.append("\""+id+"\"");
	    	sb.append(",");
	    	sb.append(type.toString());
	    	sb.append(",");
	    	sb.append(depth);
	    	sb.append(",");
	    	sb.append(inPtDegree);
	    	sb.append(",");
	    	sb.append(outPtDegree);
	    	sb.append(",");
	    	sb.append(inOutsidePtDegree);
	    	sb.append(",");
	    	sb.append(inOwnDegree);
	    	sb.append(",");
	    	sb.append(outOwnDegree);
	    	sb.append(",");
	    	sb.append(noPublicDomains);
	    	sb.append(",");
	    	sb.append(noPrivateDomains);
	    	sb.append(",");
	    	sb.append(objectInPublicDomain);
    	return sb.toString();
    }
    
    public void increaseInOwnDegree(){
    	inOwnDegree++;
    }

    public String getTypeName() {
    	return declaredTypeName;
    }

    public int getNoTrLinks() {
    	return noTrLinks;
    }
    

}
