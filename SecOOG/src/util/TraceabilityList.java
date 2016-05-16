package util;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ast.BaseTraceability;

//TODO: Change the package! Why in util? This is really part of the model.
public class TraceabilityList {

	@JsonIgnore
	@ElementList(required = true)
	List<TraceabilityEntry> traceabilityEntries = new ArrayList<TraceabilityEntry>();

	public void add(TraceabilityEntry entry) {
		traceabilityEntries.add(entry);
	}

	/**
	 * @deprecated Stop using this. bad idea. rep. exposure
	 * @return
	 */
	@Transient
	@JsonIgnore
	public List<TraceabilityEntry> getRawList() {
	    return traceabilityEntries;
    }
	
//	public TraceabilityEntry get(int index) {
//	    return traceabilityEntries.get(index);
//    }
//
//	public int size() {
//	    return traceabilityEntries.size();
//    }
//
//	public boolean addAll(Collection<? extends TraceabilityEntry> c) {
//	    return traceabilityEntries.addAll(c);
//    }

	
	public boolean addAll(TraceabilityList c) {
	    return traceabilityEntries.addAll(c.traceabilityEntries);
    }
	
	@Transient
	@JsonIgnore
	public BaseTraceability getLast() {
		int size = traceabilityEntries.size();
		if (size > 0) {
			TraceabilityEntry traceabilityEntry = traceabilityEntries.get(size - 1);
			return traceabilityEntry.getSecond();
		}
		return null;
	}

	// Implements value equality
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof TraceabilityList)) {
			return false;
		}

		TraceabilityList key = (TraceabilityList) o;
		return this.traceabilityEntries.equals(key.traceabilityEntries);
	}

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (traceabilityEntries == null ? 0 : traceabilityEntries.hashCode());
		return result;
	}
	
}
