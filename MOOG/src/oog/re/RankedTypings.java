package oog.re;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

// XXX. Have modified flag to indicate that the user changed the typings associated with a refinement
public class RankedTypings {


	// Map from AU to rankedTypings: ordered by preference, from most preferred to least preferred
	// XXX. Populate this more lazily!
	protected Map<String, ArrayList<String>> ranking = new Hashtable<String,ArrayList<String>>();

	public void addMapEntry(String au, ArrayList<String> typings) {
		this.ranking.put(au, typings);
    }

    public ArrayList<String> getRankedTypings(String au) {
	    return ranking.get(au);
    }

    // XXX. Do not return immutable copy; we want to mutate it!
    public Map<String, ArrayList<String>> getRankedTypings() {
    	return ranking;
    }

}
