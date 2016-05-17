package edu.wayne.ograph.internal;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ast.Type;
import edu.wayne.auxiliary.Utils;
import edu.wayne.generics.GenHelper;

/**
 * Wrapper for string to represent a fully qualified name include package name
 * and simple class name
 * */
public class QualifiedClassName {
	private ITypeBinding classBinding;
	private QualifiedClassName cthis;
	private Type type;
	private QualifiedClassName[] intfs;

	public QualifiedClassName(ITypeBinding cBinding, QualifiedClassName cTHIS) {
		// this(cBinding);
		// substActualGenerics(cTHIS, typeDecl);
		if (cTHIS != null && cTHIS.getTypeBinding() != null && cBinding != null && cBinding.isParameterizedType()) {
			this.classBinding = GenHelper.translateFtoA(cBinding, cTHIS.getTypeBinding());
			if(classBinding == null ) {
				System.err.println("WARNING: null ITypeBinding for " + cTHIS); 
			}
			ast.TypeDeclaration.createFrom(this.classBinding);
		} else {
			this.classBinding = cBinding;
		}
		this.cthis = cTHIS;

		if (classBinding != null) {
			this.type = Type.createFrom(classBinding);
		} else if (cBinding != null) {
			this.type = Type.createFrom(cBinding);
		}

		if (cBinding == null && cTHIS == null) {
			this.type = new Type("DUMMY");
		}

		// TORAD: Look into this.
		// this.fullyQualifiedTypeName = classBinding.getQualifiedName();
		// else if (cBinding!=null)
		// this.fullyQualifiedTypeName = cBinding.getQualifiedName();
		// else
		// this.fullyQualifiedTypeName = "NEWWWType";
	}

	// DO NOT implement!!! keep this immutable
	// public void setClassBinging(ITypeBinding classBinding)
	// {
	// }
	@Override
	public String toString() {
		return type.getFullyQualifiedName();
	}

	public String getActualName() {
		if (classBinding != null)
			return classBinding.getQualifiedName();
		else
			return "";
	}

	/**
	 * @return Collection for a generic type Collection<T>
	 */

	public ITypeBinding getTypeBinding() {
		return classBinding;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classBinding == null) ? 0 : classBinding.getKey().hashCode());
		// result = prime * result + ((classBinding == null) ? 0 :
		// classBinding.getQualifiedName().hashCode());
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
		QualifiedClassName other = (QualifiedClassName) obj;
		if (classBinding == null) {
			if (other.classBinding != null)
				return false;
			// } else if
			// (!classBinding.getQualifiedName().equals(other.classBinding.getQualifiedName()))
		} else if (!classBinding.getKey().equals(other.classBinding.getKey()))
			// } else if (!classBinding.isEqualTo(other.classBinding))
			return false;
		return true;
	}

	public QualifiedClassName getSuperclass() {
		if (classBinding != null && (classBinding.getSuperclass() != null)
				&& (!classBinding.getSuperclass().getQualifiedName().equals(Utils.JAVA_LANG_OBJECT)))
			return new QualifiedClassName(classBinding.getSuperclass(), cthis);
		return null;
	}

	public boolean isSubTypeCompatible(QualifiedClassName c) {
		return this.classBinding.isSubTypeCompatible(c.getTypeBinding());
	}

	public Type getType() {
		return this.type;
	}

	public String getFullyQualifiedName() {
		return type.getFullyQualifiedName();
	}

	public QualifiedClassName[] getInterfaces() {
		if (intfs == null ) {
		intfs = new QualifiedClassName[0];

		if (this.classBinding != null) {
			ITypeBinding[] interfaces = this.classBinding.getInterfaces();
			intfs = new QualifiedClassName[interfaces.length];
			int i = 0;
			for (ITypeBinding itf : interfaces) {
				intfs[i] = new QualifiedClassName(itf, cthis);
				i++;
			}
		}
		}
		return intfs;
	}

	/**
	 * returns true if this is an interface of qcn
	 * also returns true if this is an interface of a superclass of qcn 
	 * */
	public boolean isInterfaceOf(QualifiedClassName qcn) {
		QualifiedClassName[] interfaces = qcn.getInterfaces();
		for (QualifiedClassName itf : interfaces) {
			if (this.equals(itf)) {
				return true;
			}
		}
		// Not found here, so keep looking
		if (qcn.getSuperclass()!=null)
			return isInterfaceOf(qcn.getSuperclass());
		
		return false;
	}
}
