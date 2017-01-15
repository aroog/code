package oogre.refinements.tac;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import edu.cmu.cs.crystal.tac.model.IVariableVisitor;
import edu.cmu.cs.crystal.tac.model.Variable;

public class TACNewExpr extends Variable {

	final IMethodBinding constructorDecl;
	
	private ITypeBinding enclosingType;
	
	private ITypeBinding instantiatedType;
	
	private IMethodBinding enclosingMethod;

	private TACNewExpr() {
		super();
		this.constructorDecl = null;
	}

	public TACNewExpr(IMethodBinding constructorDecl, ITypeBinding enclosingType, IMethodBinding enclosingMethod, ITypeBinding instantiatedType) {
		super();
		assert constructorDecl != null;
		this.constructorDecl = constructorDecl.getMethodDeclaration();
		this.enclosingType = enclosingType;
		this.instantiatedType = instantiatedType;
		this.enclosingMethod = enclosingMethod;
	}

	@Override
	public <T> T dispatch(IVariableVisitor<T> arg0) {
		return null;
	}

	@Override
	public ITypeBinding resolveType() {
		return this.instantiatedType;
	}

	public String getMethodName() {
		return this.constructorDecl.getName();
	}

	@Override
	public String toString() {
		return this.constructorDecl.getName();
	}

	public IMethodBinding getConstructorBinding() {
		return constructorDecl;
	}
	
	public ITypeBinding getEnclosingTypeBinding() {
		return enclosingType;
	}
	
	public IMethodBinding GetEnclosingMethod(){
		return enclosingMethod;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constructorDecl == null) ? 0 : constructorDecl.hashCode());
		result = prime * result
				+ ((enclosingMethod == null) ? 0 : enclosingMethod.hashCode());
		result = prime * result
				+ ((enclosingType == null) ? 0 : enclosingType.hashCode());
		result = prime
				* result
				+ ((instantiatedType == null) ? 0 : instantiatedType.hashCode());
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
		TACNewExpr other = (TACNewExpr) obj;
		if (constructorDecl == null) {
			if (other.constructorDecl != null)
				return false;
		} else if (!constructorDecl.equals(other.constructorDecl))
			return false;
		if (enclosingMethod == null) {
			if (other.enclosingMethod != null)
				return false;
		} else if (!enclosingMethod.equals(other.enclosingMethod))
			return false;
		if (enclosingType == null) {
			if (other.enclosingType != null)
				return false;
		} else if (!enclosingType.equals(other.enclosingType))
			return false;
		if (instantiatedType == null) {
			if (other.instantiatedType != null)
				return false;
		} else if (!instantiatedType.equals(other.instantiatedType))
			return false;
		return true;
	}
	


}
