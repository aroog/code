package edu.wayne.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import edu.wayne.ograph.OCFEdge;
import edu.wayne.ograph.OCREdge;
import edu.wayne.ograph.ODFEdge;
import edu.wayne.ograph.OGraphState;
import edu.wayne.ograph.OGraphStateMgr;
import edu.wayne.ograph.OPTEdge;
import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IGraph;
import oog.itf.IObject;

// XXX. Do not use System.out.
// MOVED from PointsTo OOG
public abstract class ExportTemplate {
	// XXX. Inconsistent constants...Missing @
	protected static final String U_SEP = "_";
	protected static final String DOT_SEP = "\\.|:|<|>|,|\\[|\\]|\"";

	protected IGraph oGraph;
	private IDomain dShared;
	private OGraphState graphState = null;
	protected OGraphStateMgr graphStateMgr = null;

	
	private Hashtable<String, String> listOfObjects;
	private Hashtable<String, String> listOfDomains;
	protected Set<String> ownershipEdges;

	public ExportTemplate(IGraph oGraph, IDomain dShared) {
		super();
		this.oGraph = oGraph;
		this.dShared = dShared;
	}
	

	public void writeToFile(String filepath) {
		Writer output = null;
		File file = new File(filepath);
	
		try {
			// root object
			IObject rootObject = oGraph.getRoot();
			if (rootObject == null)
				throw new Exception("No root object");
	
			output = new BufferedWriter(new FileWriter(file));
	
			// Begin the dot file
			output.write(getTextHeader());
			output.write(getBeginNodes());
			// Write ODomains and OObjects
			// outputDGraph2(output, rootObject);
			//List<IObject> visitedIObjects = new ArrayList<IObject>();
			listOfObjects = new Hashtable<String, String>();
			listOfDomains = new Hashtable<String, String>();
			ownershipEdges = new HashSet<String>();
			
			visitDomain(output, rootObject, dShared, 1);
			
			output.write(getEndNodes());
			output.write(getBeginEdges());
			// Write edges
			outputOwnEdges(output);
	
			// write OEdges
			outputPtEdges(output);
			output.write(getEndEdges());
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


	protected abstract void outputOwnEdges(Writer output) throws IOException;
	protected abstract void outputPtEdges(Writer output) throws IOException;

	protected abstract String getTextHeader();
	protected abstract String getTextFooter(); 
	protected abstract String getEndEdges();
	protected abstract String getBeginEdges();
	protected abstract String getBeginNodes(); 
	protected abstract String getEndNodes(); 
	
	protected abstract void saveObject(Writer output, IObject rootObject) throws IOException;
	protected abstract void saveDomain(Writer output, IDomain sharedDomain, String sharedDomainId) throws IOException; 

	protected boolean excludeEmptyDomains = true;
	
	private void visitDomain(Writer output, IObject parentObject, IDomain domain, int depthCounterTemp) throws IOException {
	    String domainId = getDomainId(domain);
	    saveDomain(output, domain, domainId);

		for (IObject oObject : domain.getChildren()) {
			visitObject(output, oObject, depthCounterTemp + 1);
			ownershipEdges.add(domainId + " -> " + getObjectId(oObject));
		}
    }
	
	private void visitObject(Writer output, IObject object, int depthCounterTemp) throws IOException {
		
    	saveObject(output, object);
    	listOfObjects.put(getObjectId(object), object.getInstanceDisplayName());
    	
		Set<IDomain> domains = object.getChildren();
		for (IDomain oDomain : domains) {			
			String domainId = getDomainId(oDomain);
			if(domainId != null ) {
				
				// Do not show ownership edges to empty domains...
				if (this.excludeEmptyDomains && !oDomain.hasChildren()) {
					continue;
				}
				
				ownershipEdges.add(getObjectId(object) + " -> " + domainId);
				
				if (!listOfDomains.containsKey(domainId)) {
					listOfDomains.put(domainId, domainId); // XXX. Why using hashtable, instead of just set?
					visitDomain(output, object, oDomain, depthCounterTemp+1);
				}
				else{
					StringBuilder builder = new StringBuilder();
					builder.append("OGRAPH: CycleDetected at depth:"+ depthCounterTemp);
					builder.append("ObjectID: " + object.getO_id());
					builder.append("DomainID: " + oDomain.getD_id());
					
					// XXX. Cannot have null ASTNode arg.
					// Can we obtain root ASTNode?
					//reporter.reportUserProblem(builder.toString(), null, "ERROR");
					System.err.println("XXX.");
					System.err.println(builder.toString());
					System.err.println("XXX.");
				}
			}
			else {
				// HACK: Why should this ever be null?
				int debug = 0; debug++;
				System.out.println("Unexpected null domain id");
			}
		}
	}
	
	protected String getObjectId(IObject oObject) {
	    return replaceAll(oObject.getO_id());
    }
	
	protected String getDomainId(IDomain oDomain) {
	    return replaceAll(oDomain.getD_id());
    }

	private String replaceAll(String id) {
	    String replaceAll = id.replaceAll(DOT_SEP, U_SEP);
	    // DONE. Double-check to remove all spaces; including this in the DOT_SEP regex does not seem to do it!
		return  replaceAll.replaceAll("\\s+", U_SEP);
    }

	protected String getURL(IObject oObject) {
	    return oObject.getO_id();
    }

	protected String getURL(IDomain oDomain) {
	    return oDomain.getD_id();
    }
	
	protected String getName(IDomain oDomain) {
	    return oDomain.getD();
    }

	protected String getName(IObject oObject) {
	    return oObject.getInstanceDisplayName() + ":" + oObject.getTypeDisplayName();
    }
	
	// XXX. Not using replaceAll consistently? But cannot simply call it
	// - constructing special string here, then re-parsing it later on (by splitting on "::")
	// - if call replaceAll, then "::" will get removed
	protected String getName(IEdge edge) {
		// XXX. Replace conditionals with polymorphism. 
		if (edge instanceof ODFEdge) {
			ODFEdge dfEdge = (ODFEdge) edge;
			return dfEdge.getFlag().toString() + "::" + dfEdge.getFlow().getInstanceDisplayName() + ":"
					+ dfEdge.getFlow().getTypeDisplayName();
		} else if (edge instanceof OPTEdge) {
			OPTEdge ptEdge = (OPTEdge) edge;
			return replaceAll(ptEdge.getFieldName());
		} else if (edge instanceof OCREdge) {
			OCREdge crEdge = (OCREdge) edge;
			return "creation::" + crEdge.getFlow().getInstanceDisplayName() + ":"
					+replaceAll(crEdge.getFlow().getTypeDisplayName());
		}  else if (edge instanceof OCFEdge) {
			OCFEdge crEdge = (OCFEdge) edge;
			return "control::" + replaceAll(crEdge.getControl());
		}
	    
		// XXX. Change call toString() to call IEdge.getLabel();
		//return replaceAll(edge.toString());
		return edge.toString();
    }
	
	protected String getQualifiedName(IDomain oDomain) {
	    return oDomain.getD();
    }


    public OGraphState getGraphState() {
	    return graphState;
    }

	public void setGraphState(OGraphState state) {
		this.graphState = state;
		graphStateMgr = new OGraphStateMgr(state);
    }


}