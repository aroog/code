package edu.wayne.ograph;

import java.util.Set;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ast.BaseTraceability;
import oog.itf.IElement;
import oog.itf.IObject;
import util.TraceabilityEntry;
import util.TraceabilityList;
import util.TraceabilityListSet;

/***
 * 
 * for serialization to json we need to avoid infinite loops
 * we generate obj identity as integers  in the field id.
 * TODO:XXX see if it breaks other stuff in the UI where we also generate a field id in the json 
 * see 
 * [JACKSON-107]: Add support for Object Identity (to handled cycles, shared refs),
 *  with @JsonIdentityInfo
 * http://wiki.fasterxml.com/JacksonFeatureObjectIdentity 
 */
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="id")
public abstract class OElement implements IElement {

	// TODO: HIGH. XXX. Where is this path set?
	// TODO: HIGH. XXX. Why is it Transient?
	
	// DONE: I don't think path is a List<TLink>; this is a traceabilityEntries of Expressions,
	// or a traceabilityEntries of (O_C, Expression) pairs (SELECTED)
	@Element(required=false)
	protected TraceabilityListSet path = new TraceabilityListSet();

	@Transient
	private Set<BaseTraceability> _traceability = null;

	// TODO: LOW. Arguably, should we also include the traceabilityEntries of bindings?
	// i.e., save the DD?
	// NO: We are not saving the full context, i.e., the stuff on the LHS of the
	// turnstyle in the rules.

	// DONE: Rename this: 'traceability' -> 'path'.
	// DONE: Define Traceability as being calculated from the Path (i.e., the
	// last element in the path)
	// - just define a read-only getter, getTraceability();
	/**
	 * For each element in the set returns the last element of the path
	 * */
	@JsonIgnore	
	public Set<BaseTraceability> getTraceability() {
		if (this._traceability == null) {
			// Cache the result, since this is computed very frequently
			this._traceability = path.getSetOfLasts();
		}
		return _traceability;
	}
	
	// TODO: Maybe move this to an IVisitable interface; then having everything
	// at least implement IVisitable (including OGraph)
	public boolean accept(OGraphVisitor visitor) {
		return true;
	}

	/**
	 * Empty setter. Do NOT use. Used by serialization only.
	 * XXX. Have to use this for now, since we are not using constructor injection for Simple.XML
	 */
	@JsonIgnore
	public void setPath(TraceabilityListSet path) {
		this.path = path;
	}

	@JsonIgnore
	public TraceabilityListSet getPath() {
		return path;
	}
	
	// Expose this for testing, for now.
    public boolean add(TraceabilityList pathList) {
        return path.add(pathList);
    }
    
    // TODO: Expose in interface?
	public void addTraceability(IObject element, BaseTraceability trace) {
		// TODO: Lookup existing list instead of creating a new one?
		TraceabilityList pathList = new TraceabilityList();
		pathList.add(new TraceabilityEntry(element.getO_id(), trace));
		
		add(pathList);
    }    
}
