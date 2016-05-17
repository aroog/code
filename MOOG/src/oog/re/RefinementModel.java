package oog.re;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.simpleframework.xml.ElementList;

/**
 * The refinement model will hold the list of refinements.
 * And the list of refinement options, if any.
 * 
 * TODO: LOW. Change to a queue? No. Let the client deal with it. They can call clear when they're done.
 * 
 */
public class RefinementModel {

	@ElementList(required=false)
	// TODO: HIGH. XXX. Replace this with queue.
	private List<IRefinement> list = new ArrayList<IRefinement>();
	
	@ElementList(required=false)
	private List<IOtherRefinement> otherList = new ArrayList<IOtherRefinement>();

	@ElementList(required=false)
	private List<IHeuristic> heuristics = new ArrayList<IHeuristic>();
	
	public void add(Refinement ref) {
		list.add(ref);
		
		// XXX. HACK: Save after each refinement..., for debugging...
		// XXX. HACK: hard-coded path
		// Persist.save(this, "C:\\Temp\\OOGRE.xml");
    }
	

	// TODO: HIGH. Generalize type to Collection instead of List, handles Queue 
	public List<IRefinement> getRefinements() {
		// TODO: Return a copy to avoid representation exposure
    	return list;
    }
	
	/**
	 * Expose part of facade? In case we fix representation exposure?
	 * XXX. Generalize to take the state to remove. E.g., Unsupported.
	 */
	public void removePending() {
		Iterator<IRefinement> it = list.iterator();
		while(it.hasNext()) {
			IRefinement refinement = it.next();
			if(refinement.getState() == RefinementState.Pending) {
				it.remove();
			}
		}
	}
	

	public void clear() { 
		this.list.clear();
	}
	
	public void addOther(IOtherRefinement ref) {
		otherList.add(ref);
    }

	// TODO: HIGH. Generalize type to Collection instead of List, handles Queue 
	public List<IOtherRefinement> getOtherRefinements() {
		// TODO: Return a copy to avoid representation exposure
    	return otherList;
    }

	public void clearOthers() {
		otherList.clear();
	}

	public void addHeuristic(IHeuristic heuristic) {
		this.heuristics.add(heuristic);
	}

	public List<IHeuristic> getHeuristics() {
		return heuristics;
	}

}
