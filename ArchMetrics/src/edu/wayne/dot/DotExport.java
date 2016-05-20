package edu.wayne.dot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IEdge;
import oog.itf.IObject;
import edu.wayne.metrics.datamodel.NodeType;
import edu.wayne.ograph.OGraph;

//Adapted from OODafA

public class DotExport {
	
	private OGraph runtimeModel;
	//key = id and value = label
	
	// TODO: Explain this map.
	private Hashtable<String, String> listOfObjects;
	
	// TODO: Explain this map.
	private Hashtable<String, String> listOfDomains;
	private Set<String> ownershipEdges; 
	
    public DotExport(OGraph runtimeModel) {
	    this.runtimeModel = runtimeModel;
    }

	
	public void writeToDotFile(String filepath) {

		Writer output = null;
		String start = "digraph G { \n" + "compound = true;\n"
		+ "center = true;\n" + "fontname = Helvetica;\n"
		+ "rankdir = TD;\n" + "size=\"8, 10\";\n"
		+ "orientation=portrait;\n";

		String end = "}";

		File file = new File(filepath);

		try {
			// root object
			IObject rootObject = runtimeModel.getRoot();
			if (rootObject == null)
				throw new Exception("No root object");

			output = new BufferedWriter(new FileWriter(file));

			// Begin the dot file
			output.write(start);

			// Write ODomains and OObjects
			// outputDGraph2(output, rootObject);
			//List<IObject> visitedOobjects = new ArrayList<IObject>();
			listOfObjects = new Hashtable<String, String>();
			listOfDomains = new Hashtable<String, String>();
			ownershipEdges = new HashSet<String>();
			saveObject(output, rootObject);
			listOfObjects.put(rootObject.getO_id(), rootObject.getInstanceDisplayName());
			visitAllObjects(output, rootObject, 2);

			// Write edges
			outputOwnEdges(output);
			
			// write OEdges
			outputPtEdges(output);
			
			// End the dot file
			output.write(end);

			// Close the writer
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}


	private void outputOwnEdges(Writer output) throws IOException{
	    for(String edge:ownershipEdges){
	    	output.write(edge
					+ " [style = \"solid\"];"
					+ "\n"
					);
	    }
	    
    }

	private void outputPtEdges(Writer output) throws IOException{
		for(IEdge edge:runtimeModel.getEdges()){
			String stringEdge = edge.getOsrc().getO_id() + " -> " + edge.getOdst().getO_id();
			output.write(stringEdge
					+ " [style = \"solid, setlinewidth(3)\", color=red];"
//					+ " [style = \"solid\"];"
					+ "\n"
			);
		}
	    
    }
	
	/**
     * @param output
     * @param rootObject
     * @throws IOException
     */
    private void saveObject(Writer output, IObject rootObject) throws IOException {
	    output
	    .write(rootObject.getO_id()
	    		+ " [label=\""
	    		+ rootObject.getInstanceDisplayName()
					+ ":\\n"
					+ rootObject.getTypeDisplayName()
	    		+ "\", fontname=\"Helvetica-Bold\", style =\"filled, solid\", fillcolor = lightyellow, shape=\"box\"];  /* Object  "
	    		+ rootObject.getC().toString() + " */ "  // TOMAR: TODO: HIGH. Check if we want to use getC() here
	    		+ "\n");
    }

    private void visitAllObjects(Writer output, IObject parentObject, int depthCounterTemp) throws IOException{
		Set<IDomain> domains = parentObject.getChildren();
		for (IDomain runtimeDomain : domains) {
			String runtimeDomainId = runtimeDomain.getD_id();
			if(runtimeDomainId != null ) {
				ownershipEdges.add(parentObject.getO_id() + " -> " + runtimeDomainId);
				if (!listOfDomains.containsKey(runtimeDomainId)) {
				//	depthCounterTemp++;
					NodeType nodeType = NodeType.PrD;
					if (runtimeDomain.isPublic()) nodeType = NodeType.PD;
					
					saveDomain(output, runtimeDomain, runtimeDomainId);
					listOfDomains.put(runtimeDomainId, runtimeDomain.getD());
					//DEBUG: check that domain id is uniquely identified
					//System.out.println("ODomain:"+runtimeDomainId);
					for (IObject runtimeObject : runtimeDomain.getChildren()) {
						String objectid = runtimeObject.getO_id();
						ownershipEdges.add(runtimeDomainId + " -> " + objectid);

						saveObject(output, runtimeObject);
						listOfObjects.put(objectid, runtimeObject.getInstanceDisplayName());

						visitAllObjects(output, runtimeObject, depthCounterTemp+1);
					}
				}
				else{
					System.out.println("OGraph: CycleDetected at depth:"+ depthCounterTemp);
					System.out.println(parentObject.getO_id());
					System.out.println(runtimeDomainId);
					//listOfDomains.get(runtimeDomainId).increaseInOwnDegree();
				}
			}
			else {
				// HACK: Why should this ever be null?
				int debug = 0; debug++;
				System.out.println("Unexpected null domain id");
				
			}
		}
	}


	/**
     * @param output
     * @param runtimeDomain
     * @param runtimeDomainId
     * @throws IOException
     */
    private void saveDomain(Writer output, IDomain runtimeDomain, String runtimeDomainId) throws IOException {
    	String dname = runtimeDomain.getD();
    	dname = dname.replaceAll("[<>,]", "");
	    if (!runtimeDomain.isPublic()) {
	    	output
	    	.write(runtimeDomainId
	    			+ " [label=\""
	    			+ dname
	    			+ "\", shape = \"box\", fontname=\"Helvetica-Bold\", style =\"setlinewidth(3), dashed\"]; /* Domain  "
	    			+ runtimeDomainId + " */" 
	    			+ "\n");
	    } else {
	    	output
	    	.write(runtimeDomainId
	    			+ " [label=\""
	    			+ dname
	    			+ "\", shape = \"box\", fontname=\"Helvetica-Bold\", style =\"dashed\"]; /* Domain  "
	    			+ runtimeDomainId + " */"
	    			+ "\n");
	    }
    }
	
}
