package util;

import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ast.BaseTraceability;

//TODO: Change the package! Why in util? This is really part of the model.

public class TraceabilityListSet {

	@JsonIgnore
	@ElementList(required = true)
	private Set<TraceabilityList> setOfLists = new HashSet<TraceabilityList>();

	public boolean add(TraceabilityList list) {
		return setOfLists.add(list);
	}
	
	/** @deprecated Stop using this... bad idea. rep. exposure.
	 * 
	 * @return
	 */
	@Deprecated
	@Transient
	public Set<TraceabilityList> getSetOfLists() {
		return setOfLists;
	}	
	
	// Implements value equality
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof TraceabilityListSet)) {
			return false;
		}

		TraceabilityListSet key = (TraceabilityListSet) o;
		return this.setOfLists.equals(key.setOfLists);
	}

	public boolean addAll(TraceabilityListSet c) {
	    return setOfLists.addAll(c.setOfLists);
    }

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (setOfLists == null ? 0 : setOfLists.hashCode());
		return result;
	}
	

	public Set<BaseTraceability> getSetOfLasts() {
		Set<BaseTraceability> lastLinks = new HashSet<BaseTraceability>();
		for (TraceabilityList link : setOfLists) {
			lastLinks.add(link.getLast());
		}
		return lastLinks;
	}
}
