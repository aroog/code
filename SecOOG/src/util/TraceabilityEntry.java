package util;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ast.BaseTraceability;

//TODO: Change the package! Why in util? This is really part of the model.
// Non-generic version
public class TraceabilityEntry {
	// first member of the pair
	@Attribute(required = true, name = "first")
	// TODO: Rename -> Oid
	private String first;

	// second member of the pair
	@Element(required = true, name = "second")
	@JsonIgnore
	private BaseTraceability second;

	// Add default constructor for serialization
	protected TraceabilityEntry(){}
	
	public TraceabilityEntry(@Attribute(required = true, name = "first")String first,
	        @Element(required = true, name = "second") BaseTraceability second) {
		this.first = first;
		this.second = second;
	}

	// TODO: Rename: --> getOid
	public String getFirst() {
		return first;
	}

	// TODO: Rename: --> getTraceability()
	public BaseTraceability getSecond() {
		return second;
	}
	
	// Implement value equality
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof TraceabilityEntry)) {
			return false;
		}

		// NOTE: first and second are always set.
		TraceabilityEntry key = (TraceabilityEntry) o;
		return this.first!= null && this.first.equals(key.first) && this.second != null && this.second.equals(key.second);
	}

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (first == null ? 0 : first.hashCode());
		result = 37 * result + (second == null ? 0 : second.hashCode());
		return result;
	}


}
