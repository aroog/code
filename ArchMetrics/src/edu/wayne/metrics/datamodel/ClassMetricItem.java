/**
 * 
 */
package edu.wayne.metrics.datamodel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * the annotations for classes: Domains, DomainParams, DomainInherits, DomainLinks, DomainAssumes  
 *
 */
public class ClassMetricItem {

	//use a key as a unique as returned by resolveBinding.getKey() as an extra safety.
	private String key;
	
	//this is not necessarily unique see key.
	private String className;
	
	//this should be unique e.g. package.ClassName
	private String superClassName;
	
	private boolean hasPrivateDomains;
	
	private boolean hasPublicDomains;
	
	//the key is one of the following: @Domains, @DomainParams, @DomainInherits, @DomainLinks, @DomainAssumes
	private Hashtable<String, String> annotations;
	
	private List<String> publicDomains = new ArrayList<String>();
	
	// for now just owned
	private List<String> privateDomains = new ArrayList<String>();
	
	private List<String> domainParams = new ArrayList<String>();

	private List<String> domainInherits = new ArrayList<String>();
	
	private List<String> domainLinks = new ArrayList<String>();
	
	private List<String> domainAssumes = new ArrayList<String>();
	
	private String astNode;
	/**
     * @param className
     * @param superClassName
     * @param annotations
     * @param astNode
     */
    public ClassMetricItem(String key, String className, String superClassName, Hashtable<String, String> annotations,
            String astNode) {
    	this.key = key;
	    this.className = className;
	    this.superClassName = superClassName;
	    this.annotations = annotations;
	    this.astNode = astNode;
    }
    
    public ClassMetricItem(String key, String className, String superClassName, Hashtable<String, String> annotations,
            String astNode, List<String> domains, List<String> pDomains, List<String> domainParams, List<String> domainInherits, List<String> domainLinks, List<String> domainAssumes ) {
    	this.key = key;
	    this.className = className;
	    this.superClassName = superClassName;
	    this.annotations = annotations;
	    this.astNode = astNode;
	    this.publicDomains = domains;
	    this.privateDomains = pDomains;
	    this.domainParams = domainParams;
	    this.domainInherits = domainInherits;
	    this.domainLinks = domainLinks;
	    this.domainAssumes = domainAssumes;
    }
	/**
     * @return the className
     */
    public String getClassName() {
    	return className;
    }
	/**
     * @return the superClassName
     */
    public String getSuperClassName() {
    	return superClassName;
    }
    
	/**
     * @return the astNode
     */
    public String getAstNode() {
    	return astNode;
    }
    
//	/**
//     * @return the publicDomains
//     */
//    public List<String> getPublicDomains() {
//    //TODO: compute from annotations
//    	if (annotations!=null){
//    		publicDomains = AnnotationsUtils.extractItems(annotations.get("@Domains"));
//    		publicDomains.remove("owned");
//    	}
//    	return publicDomains;
//    }
//	/**
//     * @return the privateDomains
//     */
//    public List<String> getPrivateDomains() {
////    //TODO: compute from annotations
////    	if (annotations!=null){
////    	if (AnnotationsUtils.extractNoPrivateDomains(annotations.get("@Domains"))>0){
////    		privateDomains = new ArrayList<String>();
////    		privateDomains.add("owned");
////    	}
////    	}
//    	return privateDomains;
//    }
//	/**
//     * @return the domainParams
//     */
//    public List<String> getDomainParams() {
////    //TODO: compute from annotations
////    	if (annotations!=null)
////    		domainParams = AnnotationsUtils.extractItems(annotations.get("@DomainParams"));
//    	return domainParams;
//    }
//	/**
//     * @return the domainInherits
//     */
//    public List<String> getDomainInherits() {
//    	//TODO: compute from annotations
//    	if (annotations!=null)
//    		domainInherits = AnnotationsUtils.extractItems(annotations.get("@DomainInherits"));
//    	return domainInherits;
//    }
//	/**
//     * @return the domainLinks
//     */
//    public List<String> getDomainLinks() {
//    	//TODO: compute from annotations
//    	if (annotations!=null)
//    		domainLinks = AnnotationsUtils.extractItems(annotations.get("@DomainLinks"));
//    	return domainLinks;
//    }
//	/**
//     * @return the domainAssumes
//     */
//    public List<String> getDomainAssumes() {
//    	//TODO: compute from annotations
//    	if (annotations!=null)
//    		domainAssumes = AnnotationsUtils.extractItems(annotations.get("@DomainAssumes"));
//    	return domainAssumes;
//    }

    @Override
    public String toString() {
	    // TODO Auto-generated method stub
    	StringBuffer sb = new StringBuffer();
    	sb.append(key+",");
    	sb.append(className+",");
    	sb.append(getPublicDomains().toString().trim().replaceAll(",", ";")+","+getPublicDomains().size()+",");
    	sb.append(getPrivateDomains().toString().trim().replaceAll(",", ";")+","+getPrivateDomains().size()+",");
    	sb.append(getDomainParams().toString().trim().replaceAll(",", ";")+","+getDomainParams().size()+",");
    	sb.append(getDomainInherits().toString().trim().replaceAll(",", ";")+","+getDomainInherits().size()+",");
    	sb.append(getDomainLinks().toString().trim().replaceAll(",", ";")+","+getDomainLinks().size()+",");
    	sb.append(getDomainAssumes().toString().trim().replaceAll(",", ";")+","+getDomainAssumes().size()+",");
	    return sb.toString();
    }

	/**
     * @return the publicDomains
     */
    public List<String> getPublicDomains() {
    	return publicDomains;
    }

	/**
     * @return the privateDomains
     */
    public List<String> getPrivateDomains() {
    	return privateDomains;
    }

	/**
     * @return the domainParams
     */
    public List<String> getDomainParams() {
    	return domainParams;
    }

	/**
     * @return the domainInherits
     */
    public List<String> getDomainInherits() {
    	return domainInherits;
    }

	/**
     * @return the domainLinks
     */
    public List<String> getDomainLinks() {
    	return domainLinks;
    }

	/**
     * @return the domainAssumes
     */
    public List<String> getDomainAssumes() {
    	return domainAssumes;
    }


    


	
}
