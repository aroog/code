package util.typehierarchy;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import edu.cmu.cs.crystal.util.TypeHierarchy;



public class TypeHierarchyFactory {
	private static TypeHierarchyFactory s_instance = null;
	private CachedTypeHierarchyEx hierarchy;
	private TypeHierarchyFactory() {		
	}
	
	public static TypeHierarchyFactory getInstance(){
		if (s_instance==null){
			s_instance = new TypeHierarchyFactory();
		}
		return s_instance;
	}

	public void registerProject(IJavaProject project) throws JavaModelException{
		hierarchy = new CachedTypeHierarchyEx(project);
	}
	
	public TypeHierarchy getHierarchy() {
		return hierarchy;
	}
	
	// XXX. If we want to preserve the type hierarchy across invocations, do not reset this!
	public static void reset() {
		if (s_instance.hierarchy != null ) {
			s_instance.hierarchy.clear();
			s_instance.hierarchy = null;
		}
		s_instance = null;
	}
}
