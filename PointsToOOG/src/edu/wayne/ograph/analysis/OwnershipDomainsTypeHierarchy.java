package edu.wayne.ograph.analysis;

import org.eclipse.jdt.core.IJavaProject;

import edu.cmu.cs.crystal.util.TypeHierarchy;

//Does not appear to be in use. REMOVE. 
@Deprecated
public class OwnershipDomainsTypeHierarchy implements TypeHierarchy {

	public OwnershipDomainsTypeHierarchy(IJavaProject javaProject) {

	}

	@Override
	public boolean isSubtypeCompatible(String subType, String superType) {
		return false;
	}

	@Override
	public boolean existsCommonSubtype(String t1, String t2) {
		return false;
	}

	@Override
	public boolean existsCommonSubtype(String t1, String t2, boolean skipCheck1, boolean skipCheck2) {
		return false;
	}

}
