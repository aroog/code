package edu.wayne.flowgraph;

import edu.cmu.cs.crystal.tac.model.Variable;
import edu.wayne.ograph.internal.DomainP;
import edu.wayne.ograph.internal.OObject;

public class FlowGraphNode {
	private OObject o;
	private Variable x;
	private DomainP b;

	public FlowGraphNode(OObject o, Variable x, DomainP b) {
		this.o = o;
		this.x = x;
		this.b = b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((o == null) ? 0 : o.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlowGraphNode other = (FlowGraphNode) obj;
		if (o == null) {
			if (other.o != null)
				return false;
		} else if (other.o != null) {
			if (!o.equals(other.o))
				return false;
		} else
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (other.x != null) {
			if (!x.equals(other.x))
				return false;
		} else
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (other.b != null) {
			if (!b.equals(other.b))
				return false;
		} else
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + o + ", " + x +":"+ x.resolveType().getQualifiedName() + ", " + b + ")";
	}

	public OObject getO() {
		return o;
	}

	public Variable getX() {
		return x;
	}

	public DomainP getB() {
		return b;
	}

}
