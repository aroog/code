package edu.cmu.cs.viewer.objectgraphs.displaygraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import oog.itf.IDomain;
import oog.itf.IObject;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;

import de.fub.graph.DFS;
import de.fub.graph.DefaultVisitor;
import de.fub.graph.Edge;
import de.fub.graph.Graph;
import de.fub.graph.Vertex;
import edu.wayne.ograph.ODomain;

/**
 * TODO: LOW. Create DOT/DOM graph programmatically, and directly set tag to to be the corresponding DisplayObject,
 * DisplayDomain, and DisplayEdge. Using URLs and hashtables is very brittle.
 */
@Root
public class DisplayModel extends DisplayElement {

	@Element(required=false)
	private DisplayObject rootObject = null;

	@Element
	private DisplayDomain rootDomain = null;

	@Element
	private DisplayEdgeCollection edges = new DisplayEdgeCollection();
	
	@Element
	private DisplayDomainLinkCollection links = new DisplayDomainLinkCollection();

	@Transient
	private Hashtable<String, DisplayObject> hashObjects = new Hashtable<String, DisplayObject>();
	
	@Transient
	private Hashtable<String, DisplayDomain> hashDomains = new Hashtable<String, DisplayDomain>();
	
	@Transient
	private Hashtable<String, DisplayEdge> hashEdges = new Hashtable<String, DisplayEdge>();
	
	/**
	 * Map string id's used for the DOT representation to actual model elements
	 * XXX. This is not mapping IDs!!! What is this for?!?!
	 */
	@Transient
	private Hashtable<IObject, DisplayObject> mapKeyToObject = new Hashtable<IObject, DisplayObject>();

	@Transient
	private Hashtable<IDomain, DisplayDomain> mapKeyToDomain = new Hashtable<IDomain, DisplayDomain>();
	
	public DisplayModel() {
		super();

	}
	
	@Override
    public Object[] getChildren() {
		return new Object[] { rootObject, edges, links };
    }

	@Override
    public Object getParent() {
	    return null;
    }

	@Override
    public boolean hasChildren() {
	    return true;
    }

	public DisplayDomain getRootDomain() {
    	return rootDomain;
    }

	public DisplayObject getRootObject() {
    	return rootObject;
    }

	public Set<DisplayEdge> getEdges() {
    	return edges.getEdges();
    }
	
	public void addObject(String key, DisplayObject object) {
		this.hashObjects.put(key, object);
		
		generateGraph2();
		
	}
	
	public DisplayObject getObject(String key) {
	    return hashObjects.get(key);
    }
	
	public DisplayEdge getEdge(String url) {
	    return hashEdges.get(url);
    }
	
	public void addDomain(String key, DisplayDomain domain) {
		this.hashDomains.put(key, domain);
		
		generateGraph2();
	}
	
	public DisplayDomain getDomain(String key) {
	    return hashDomains.get(key);
    }

	
	public boolean addEdge(DisplayEdge displayEdge) {
		
		String fromObjectName = displayEdge.getFromObject().getId();
		String toObjectName = displayEdge.getToObject().getId();

		// HACK: The ZGRViewer does not parse the URL of an edge, just uses the DOT identifier.
		// So, make sure to use the same identifier in the hasthable to lookup DisplayEdge objects later
		StringBuffer url = new StringBuffer();
		url.append(fromObjectName);
		url.append("->");
		url.append(toObjectName);
		
		hashEdges.put(url.toString(), displayEdge);
		
		return edges.addEdge(displayEdge);
    }

	// TODO: Rename: Summary -> Lifted
	public boolean addSummaryEdge(DisplayEdge displayEdge) {
		displayEdge.setSummary(true);
		return edges.addEdge(displayEdge);
    }

	public void setRootDomain(DisplayDomain displayDomain) {
		this.rootDomain = displayDomain;
    }

	public void setRootObject(DisplayObject rootObject) {
    	this.rootObject = rootObject;
    }

	public Set<DisplayDomainLink> getDomainLinks() {
    	return links.getDomainLinks();
    }

	public void addLink(DisplayDomainLink link) {
		this.links.addDomainLink(link);
    }
	
	public DisplayEdge getEdge(DisplayObject from, DisplayObject to) {
		DisplayEdge displayEdge = null;
		for(DisplayEdge edge : getEdges() ) {
			if ( edge.getFromObject() == from && edge.getToObject() == to ) {
				displayEdge = edge;
				break;
			}
		}
		return displayEdge;
	}
	
	public Collection<DisplayObject> getObjects() {
		return this.hashObjects.values();
	}
	
	public Collection<DisplayDomain> getDomains() {
		return this.hashDomains.values();
	}
	
	public void reset() {
		mapKeyToObject.clear();
		mapKeyToDomain.clear();
		
		hashObjects.clear();
		hashDomains.clear();
		hashEdges.clear();
		edges.clear();
		links.clear();
		
		this.rootDomain = null;
		this.rootObject = null;
	}
	
	public void addObjectToMap(IObject runtimeObject, DisplayObject displayObject) {
		mapKeyToObject.put(runtimeObject, displayObject);
	}

	// HACK: Figure out...return value...Is it a single DisplayObject or a set?
	public Set<DisplayObject> getDisplayObject(IObject fromObject) {
		Set<DisplayObject> toObjects  = new HashSet<DisplayObject>();
//		// HACK: This is highly inefficient; do a reverse hashlookup
//		for(DisplayObject displayObject : mapKeyToObject.values()) {
//			if ( displayObject.hasRuntimeObject(runtimeObject) ) {
//				toObjects.add(displayObject);
//			}
//		}
		
		DisplayObject displayObject = mapKeyToObject.get(fromObject);
		toObjects.add(displayObject);
		
		return toObjects;
	}

	public void addDomainToMap(IDomain parentArchDomain, DisplayDomain displayDomain) {
		mapKeyToDomain.put(parentArchDomain, displayDomain);
    }
	
	// TODO: Hack. Figure out...return value...Is it a single DisplayDomain or a set?
	public DisplayDomain getDisplayDomain(ODomain key) {
		return mapKeyToDomain.get(key);
	}

	public void clearSummaryEdges() {
		Set<DisplayEdge> toRemove = new HashSet<DisplayEdge>();
		for(DisplayEdge summaryEdge : edges.getEdges()) {
			if ( summaryEdge.isSummary() ) {
				toRemove.add(summaryEdge);
			}
		}
		edges.removeAll(toRemove);
    }
	
	public DisplayEdgeCollection getEdgeCollection() {
		return edges;
	}

	public boolean addEdges(Set<DisplayEdge> summaryEdges) {
		return edges.addAll(summaryEdges);    
	}

	public void addEdge(String url, DisplayEdge displayEdge) {
		displayEdge.setUrl(url);
		hashEdges.put(url, displayEdge);
    }
	
// TODO: Why do we need this?	
//	public boolean removeObject(DisplayObject object) {
//		String key = object.getId();
//		boolean contains = hashObjects.contains(key);
//		if ( contains ) {
//			hashObjects.remove(key);
//			object.setDeleted(true);
//		}
//		return contains;
//	}

	// TODO: What it his for???
	public void generateGraph2() {
		if (rootDomain != null ) {
			final Graph graph = new Graph(true);
			Hashtable<DisplayElement, Vertex> hashtable = new Hashtable<DisplayElement, Vertex>();
			Vertex root = generateGraphDomain(graph, hashtable, rootDomain);
			new DFS(new DefaultVisitor() {
				@Override
				public void discoverVertex(Vertex v) {
					for(Vertex neighbour : graph.getVertices(v) ) {
						if (neighbour.getColor() == de.fub.graph.Constants.GRAY ) {
							int debug = 0; debug++;
						}
					}

				}
				@Override
				public void finishEdge(Edge e) {
				}
			}, false).start(graph, root);
		}
	}
	
	public Vertex generateGraphObject(Graph graph, Hashtable<DisplayElement, Vertex> hashtable, DisplayObject object) {
		DisplayObjectVertex objectVertex = (DisplayObjectVertex) hashtable.get(object);
		if ( objectVertex == null ) {
			objectVertex = new DisplayObjectVertex(object);
			hashtable.put(object, objectVertex);
			graph.addVertex(objectVertex);
		}
		
		for(DisplayDomain domain : object.getDomains() ) {
			Vertex domainVertex = generateGraphDomain(graph, hashtable, domain);
			graph.addEdge(objectVertex, domainVertex);
		}
		
		return objectVertex;
	}
	
	public Vertex generateGraphDomain(Graph graph, Hashtable<DisplayElement, Vertex> hashtable, DisplayDomain domain) {
		DisplayDomainVertex domainVertex = (DisplayDomainVertex) hashtable.get(domain);
		if ( domainVertex == null ) {
			domainVertex = new DisplayDomainVertex(domain);
			hashtable.put(domain, domainVertex);
			graph.addVertex(domainVertex);
		}
		
		for(DisplayObject object : domain.getObjects() ) {
			Vertex objectVertex = generateGraphObject(graph, hashtable, object);
			graph.addEdge(domainVertex, objectVertex);
		}
		
		return domainVertex;
	}

	public void finish() {
	    // TODO Auto-generated method stub
	    
    }
}

