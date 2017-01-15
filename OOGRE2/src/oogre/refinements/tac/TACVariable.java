package oogre.refinements.tac;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.tac.model.IVariableVisitor;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 *
 * TACVariable for fields...
 * 
 * TODO: Rename: --> FieldVariable
 * 
 * Why do we need these additional flags?
 * - containsOnlyPD
 * 
 * WARNING. Flags are not included in equality?
 * - that's usually not a good design
 */
public class TACVariable extends Variable {
	
	final IVariableBinding varDecl;
	
	private TACVariable() {
		super();
		this.varDecl = null;
	}
	
	public TACVariable(IVariableBinding varDecl) {
		super();
		assert varDecl != null;
		this.varDecl = varDecl.getVariableDeclaration();
	}

	@Override
	public ITypeBinding resolveType() {
		return this.varDecl.getType();
	}

	@Override
	public <T> T dispatch(IVariableVisitor<T> arg0) {
		return null;
	}
	
	public String getVariableName() {
		return this.varDecl.getName();
	}
	
	@Override
	public String getSourceString() {
		return this.varDecl.getName();
	}

	@Override
	public String toString() {
		return this.varDecl.getName();
	}

	public IVariableBinding getVarDecl() {
		return varDecl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((varDecl == null) ? 0 : varDecl.getKey().hashCode());
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
		TACVariable other = (TACVariable) obj;
		if (varDecl == null) {
			if (other.varDecl != null)
				return false;
			// XXX. Cannot do IBinding.equals. Either use IBinding.isEqualTo() or compare IBinding.getKey()
		} else if (!varDecl.isEqualTo(other.varDecl))
			return false;
		return true;
	}
}
