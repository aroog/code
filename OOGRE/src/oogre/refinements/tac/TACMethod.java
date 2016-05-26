package oogre.refinements.tac;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import edu.cmu.cs.crystal.tac.model.IVariableVisitor;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 *
 * TAC Variable for Method return...
 * 
 * WARNING. Flags are not included in equality?
 * - that's usually not a good design
 * 
 */
public class TACMethod extends Variable {
	
	final IMethodBinding methDecl;
	
	private TACMethod() {
		super();
		this.methDecl = null;
	}
	
	public TACMethod(IMethodBinding methDecl) {
		super();
		assert methDecl != null;
		this.methDecl = methDecl.getMethodDeclaration();
	}

	@Override
	public <T> T dispatch(IVariableVisitor<T> arg0) {
		return null;
	}

	@Override
	public ITypeBinding resolveType() {
		return this.methDecl.getReturnType();
	}
	
	public String getMethodName() {
		return this.methDecl.getName();
	}

	@Override
	public String toString() {
		return this.methDecl.getName();
	}

	public IMethodBinding getMethDecl() {
		return methDecl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methDecl == null) ? 0 : methDecl.getKey().hashCode());
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
		TACMethod other = (TACMethod) obj;
		if (methDecl == null) {
			if (other.methDecl != null)
				return false;
			// XXX. Cannot do IBinding.equals. Either use IBinding.isEqualTo() or compare IBinding.getKey()			
		} else if (!methDecl.isEqualTo(other.methDecl))
			return false;
		return true;
	}

}
