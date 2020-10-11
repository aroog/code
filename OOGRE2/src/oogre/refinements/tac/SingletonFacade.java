package oogre.refinements.tac;

import java.util.Set;

import oog.common.OGraphFacade;
import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.analysis.OOGContext;
import oogre.annotations.SaveAnnotationStrategy;
import oogre.annotations.SaveAnnotationsImpl;
import oogre.utils.OOGHelper;
import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OOGUtils;
import edu.wayne.ograph.OObject;

// XXX. Gotta clean up the state. The graph may change
public class SingletonFacade implements Facade {
	
	private static SingletonFacade _instance = null;
	
	private OGraph oGraph;

//	private SecGraph secGraph;

	private IDomain dShared = null;
	
//	public SecGraph getSecGraph() {
//    	return secGraph;
//    }


	private SingletonFacade(){
	}

	
	public static Facade getInstance(){
		if(_instance==null){
			_instance = new SingletonFacade();
		}
		return _instance;
	}

	/**
	 * First call must be to getGraph with a path.
	 */
	public OGraph loadFromFile(String path) {
		if (oGraph == null) {
			oGraph = OOGUtils.loadGZIP(path);
			if (oGraph == null) {
				System.err.println("Cannot load the OGraph. Check the file.");
			}

			processGraph(oGraph);
		}
		return oGraph;
	}

	// This has to get called once for each new graph. Otherwise, the graph is traversed multiple times...
	@Override
    public OGraph loadFromMotherFacade() {
		// Clear the state
		reset();
		
		oogre.plugin.Activator default1 = oogre.plugin.Activator.getDefault();
		OGraphFacade motherFacade = default1.getMotherFacade();
		oGraph = motherFacade.getGraph();

		processGraph(oGraph);
		
		return oGraph;
	}

	// XXX. Why not read the field?
	private void processGraph(OGraph oGraph) {
		if (oGraph != null ) {
			MappingBuilder visitor = new MappingBuilder(oGraph);
			oGraph.accept(visitor);
			OOGContext.getInstance().setPC(visitor.getPC());
		}
    }

	@Override
	public boolean isTheContext(IObject oContext, IObject obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAMergedObject(IObject obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IDomain getPublicDomain(IObject obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDomain getPrivateDomain(IObject obj) {
		// TODO: Extract common method.
		IDomain domain = null;
		for (IDomain dom : obj.getChildren()) {
			if(dom.getD().equals("owned")){
				domain = dom;
				break;
			}
		}
		return domain;
	}

	// XXX. Cannot get to the the "D2" in SecOOG
	// This needs to be fixed, but it rarely arises.
	@Override
	public IObject getObject(String type, IDomain D1, IDomain D2) {
		IObject root = oGraph.getRoot();
		Set<? extends IObject> childObjects = root.getChildObjects();
		for (IObject iObject : childObjects) {
			// TODO: what about otherDomain, API does not provide it
			if (iObject.getC().getFullyQualifiedName().equals(type)
			        && iObject.getParent() == D1 ) {
				return iObject;
			}
		}
		return null;
	}

	// XXX. You don't have to use this. If you don't, then...we gotta remove SecGraph 
	@Override
	public IObject getObject(IObject parentIObject, IDomain parentIDomain) {
//		IObject oObject = null;
//		InstanceOf instOfIntent = new InstanceOf(type);
//
//		// Assume there's one... what if there are more than one...
//		Set<SecObject> objectsByCond = secGraph.getObjectsByCond(instOfIntent);
//		for (SecObject secObj : objectsByCond) {
//			oObject = (OObject) secObj.getOObject();
//			break;
//		}

		//return oObject;
		
		return OOGHelper.findObject(oGraph,parentIObject,parentIDomain);
	}

	// XXX. Gotta call this to finish cleaning up this Singleton.
	@Override
    public void reset() {
		this.oGraph = null;
		this.dShared = null;
		
		// XXX. Gotta clear the state on the context singleton
		// the code may have changed between invocations => the mappings need to be rebuilt
		// e.g., the developer may have fixed cases of near encapsulation
		// some of the maps may be getting bigger => slower lookup
		// OOGContext.getInstance().reset();
    }

	@Override
	public IDomain getDShared() {
		if (dShared == null) {
			if (this.oGraph != null) {
				OObject root = oGraph.getRoot();
				for (IDomain child : root.getChildren()) {
					if (child.getD().equals("SHARED")) {
						dShared = child;
						break;
					}
				}
			}
		}

		return dShared;
	}
}
