package edu.wayne.summary.strategies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IObject;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ast.Type;
import ast.TypeInfo;

import secoog.EdgeType;
import edu.wayne.ograph.OGraph;
import edu.wayne.summary.Crystal;
import edu.wayne.summary.traceability.ReverseTraceabilityMap;

// XXX. Implement filtering to speed things up further..
public abstract class SummaryBase<T> implements SummaryStrategy<T> {

	@Override
	public Set<IObject> getObjects() {
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
		return 	instance.getAllObjects();
	}

	private Set<String> allClasses = new HashSet<String>();
	
	// XXX. Get rid of field allInterfaces. This was previously used when computing MCBI. But that was incorrect.
	private Set<String> allInterfaces = new HashSet<String>();

	private static boolean preCompute = false;
	
	private Set<Info<T>> setMIC;
	private Map<String, Set<Info<T>>> mapMIRC = new HashMap<String, Set<Info<T>>>();
	private Map<String, Set<Info<T>>> mapMIM = new HashMap<String, Set<Info<T>>>();
	
	// For fields, the String is Class:FieldType:FieldName
	// For local variables and method parameters: the String is Class:Method:VariableType:VariableName
	// Or split into two separate maps?
	// Or do it the correct O-O way (no string parsing): use MiniAST objects
	// private Map<String, Set<Info<T>>> mapCBI = new HashMap<String, Set<Info<T>>>();
	
	@Override
	public Set<IEdge> getEdges(Set<EdgeType> types) {
		return OGraphSingleton.getInstance().getEdges(types);
	}
	protected abstract Set<Info<T>> computeClasses();
	protected abstract Set<Info<T>> computeMethodsOfClass(String javaClass);
	protected abstract Set<Info<T>> computeClassesRelatedToClass(String javaClass);
	protected abstract Set<Info<T>> computeClassesBehindInterface(String enclosingType, String fieldType, String fieldName);
	
	
	@Override
	public void compute() throws JavaModelException {
		setMIC = computeClasses();
		
		getAllClassesAndInterfaces();
		
		if (preCompute) {
			// XXX. Once interfaces are added to allClasses, consolidate the two loops
			// Right now, abstract classes are processed twice, since they are both in allClasses and allInterfaces
			for (String itf : allInterfaces) {
				mapMIRC.put(itf, computeClassesRelatedToClass(itf));
			}
			for (String clazz : allClasses) {
				mapMIRC.put(clazz, computeClassesRelatedToClass(clazz));
				mapMIM.put(clazz, computeMethodsOfClass(clazz));
			}
		}
	}
	private void getAllClassesAndInterfaces() throws JavaModelException {
		Crystal crystal = Crystal.getInstance();
		TypeInfo typeInfo = TypeInfo.getInstance();
		if (typeInfo == null)
			return;
		
		Iterator<ICompilationUnit> compilationUnitIterator = crystal.getCompilationUnitIterator();
		if (compilationUnitIterator == null )
			return;
		
		while(compilationUnitIterator.hasNext()){
			ICompilationUnit cu = compilationUnitIterator.next();
			for(IType type :cu.getTypes()){
				String fullyQualifiedName = type.getFullyQualifiedName();
				
				Type archSumType = typeInfo.getType(fullyQualifiedName);
				// Because the OGraph will contain types for things that are created, used, etc. 
				// Some Type objects may not be already created.
				// This could cause problems when trying to use Type.getSubClasses(), etc.
				// Here, we traverse the types in the project rather than the OGraph to create the Type objects 
				
				// If this Type object was not already created then create it!
				// Make sure that initTypeStructures has been done by now
				if(archSumType == null ) {
					// archSumType = new Type(fullQualifiedName);
					ITypeBinding typeBinding = crystal.getTypeBindingFromName(fullyQualifiedName);
					if (typeBinding != null ) {
						archSumType = Type.createFrom(typeBinding);
					}
					else {
						System.err.println("Unexpected null");
					}
				}
				
				// TODO: HIGH. The rest of this work is not necessary, is it? Could cut. 
				
				if(type.isInterface()){
					allInterfaces.add(fullyQualifiedName);
					//Interfaces only
				}else if(Flags.isAbstract(type.getFlags())){
					//Abstract Classes added to interfaces set and classes set
					// XXX. This is fishy.
					allInterfaces.add(fullyQualifiedName);
					allClasses.add(fullyQualifiedName);

				}else{
					//Classes added to classes set
					allClasses.add(fullyQualifiedName);
				}
			}
		}
	}
	
	// XXX. This seems inefficient. Use information from Type in MiniAST OOG.
	@Override
	public Set<String> getConcreteClasses(String itf){
		Set<String> concreteClasses = new HashSet<String>();
		for(String clazz: allClasses){
			if(Utils.isSubtypeCompatible(clazz, itf)){
				concreteClasses.add(clazz);
			}
		}
		return concreteClasses;
		
	}
	
	@Override
	public Set<Info<T>> getMostImportantClasses() {
		return setMIC;
	}

	@Override
	public Set<Info<T>> getMostImportantRelatedClass(String javaClass) {
		Set<Info<T>> retVal = mapMIRC.get(javaClass);
		if (retVal == null ) {
			retVal = computeClassesRelatedToClass(javaClass);
			mapMIRC.put(javaClass, retVal);
		}
		return retVal;
	}

	@Override
	public Set<Info<T>> getMostImportantMethods(String javaClass) {
		Set<Info<T>> retVal = mapMIM.get(javaClass);
		if (retVal == null ) {
			retVal = computeMethodsOfClass(javaClass);
			mapMIM.put(javaClass, retVal);
		}
		return retVal;
	}

	@Override
	public Set<String> getAllClasses() {
		return allClasses;
	}
	@Override
	public Set<String> getAllInterfaces() {
		return allInterfaces;
	}

	@Override
	// XXX. Not using fieldType anymore!
	public Set<Info<T>> getClassesBehindInterface(String enclosingType, String fieldType, String fieldName) {
		// XXX. First read the cache. If not there, compute it.
		Set<Info<T>> retValue = computeClassesBehindInterface(enclosingType, fieldType, fieldName);
		if (retValue != null ) {
			// XXX. Cache the result before returning it
			// XXX. Construct the right key for this lookup
			// retValue = mapCBI.put(...);
		}
		return retValue;
	}

	// Read the graph from the singleton
	protected OGraph getGraph() {
	    return OGraphSingleton.getInstance().getGraph();
    }
}
