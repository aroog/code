package edu.wayne.legacy;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import edu.cmu.cs.crystal.tac.model.IVariableVisitor;
import edu.cmu.cs.crystal.tac.model.Variable;

public final class AliasXMLVariable extends Variable {
	/**
	 * 
	 */
	private final ITypeBinding varTypeBinding;
	private final String methodBindingKey;

	public AliasXMLVariable(IMethodBinding mb, ITypeBinding varTypeBinding) {		
		this.varTypeBinding = varTypeBinding;
		this.methodBindingKey = mb.getKey();
	}

	@Override
	public ITypeBinding resolveType() {					
		return varTypeBinding;
	}

	@Override
	public <T> T dispatch(IVariableVisitor<T> visitor) {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;		
		result = prime * result + ((methodBindingKey == null) ? 0 : methodBindingKey.hashCode());
		result = prime * result + ((varTypeBinding == null) ? 0 : varTypeBinding.getKey().hashCode());
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
		AliasXMLVariable other = (AliasXMLVariable) obj;
		if (varTypeBinding == null) {
			if (other.varTypeBinding != null)
				return false;
		} else if (!varTypeBinding.getKey().equals(other.varTypeBinding.getKey()))
			return false;
		if (methodBindingKey == null) {
			if (other.methodBindingKey != null)
				return false;
		} else if (!methodBindingKey.equals(other.methodBindingKey))
			return false;
		return true;
	}

}