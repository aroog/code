package oogre.refinements.tac;

import java.util.HashSet;
import java.util.Map;
import oog.itf.IDomain;
import oog.itf.IObject;
import oogre.annotations.SaveAnnotationStrategy;
import edu.wayne.ograph.OGraph;


/**
 * XXX. Maybe move the Graph Singleton into separate class.
 * - Create/manage the singleton in SecOOG; just like we have SecGraph.getInstance()  
 * - We probably need OGraph.getInstance();
 *
 * XXX. Add a resetGraph() method to nullify the graph to force a reload when things change
 */
public interface Facade {
	
	/**
	 * Loads the OGraph from a specified location
	 * 
	 * @param path
	 * @return
	 */
	public OGraph loadFromFile(String path);
	

	/**
	 * Loads the OGraph from the Mother Facade
	 * @return
	 */
	public OGraph loadFromMotherFacade();
	
	/**
	 * Get OObject by C<D1,D2>
	 */
	public IObject getObject(String type, IDomain D1, IDomain D2);
	
	/**
	 * checks if oContext is a context object of obj.
	 */
	public boolean isTheContext(IObject oContext, IObject obj); 
	
	/**
	 * checks if obj traces back to more than one new expression in the code.
	 */
	public boolean isAMergedObject(IObject obj);
	
	public IDomain getPublicDomain(IObject obj);
	
	public IDomain getPrivateDomain(IObject obj);
	
	public void reset();

	public IDomain getDShared();

	public IObject getObject(IObject parentIObject, IDomain parentIDomain);
}

