package edu.wayne.alias;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.tac.model.IVariableVisitor;
import edu.cmu.cs.crystal.tac.model.Variable;

public class FieldVariable extends Variable {

	private IVariableBinding binding;

	public FieldVariable(IVariableBinding binding) {
		this.binding = binding;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((binding == null) ? 0 : binding.hashCode());
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
		FieldVariable other = (FieldVariable) obj;
		if (binding == null) {
			if (other.binding != null)
				return false;
		} else if (!binding.equals(other.binding))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return binding.getName();
	}

	@Override
	public ITypeBinding resolveType() {
		return binding.getType();
	}

	@Override
	public <T> T dispatch(IVariableVisitor<T> visitor) {
		return null;
	}

}
