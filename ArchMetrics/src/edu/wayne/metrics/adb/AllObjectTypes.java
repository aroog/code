package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IObject;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import ast.Type;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.utils.CSVConst;

/**
 * Compute the list of types instantiated in the program
// TODO: Include transitive super-types?
// TODO: Define options for what to include/exclude
 * 
 * TODO: Use TypeSelector interface
 * 
 * Note: These numbers may be different from Number of Classes, Number of Interfaces,
 * which may only count types defined in the application, and exclude those from the library.
 * Here, we count the types referenced in the OOG.
 *
 * TODO: Maybe work in these supertypes into the other metrics, like the one that deal with 
 * types and trivial clusters.
 * 
 * TODO: Rename: AllObjects
 * 
 * TODO: Add: InstantiatedTypes, not just InstantiatableTypes.
 * TODO: Add a note on how we deal with library types.
 * 
 * TODO: why not generate some of these numbers from the other metrics that traverse all the objects, e.g., WA.
 */
public class AllObjectTypes extends ObjectSetStrategyBase {
	private static String HEADER = "Type,IsAbstract,IsInterface,IsInstantiatable";
	
	private boolean includeSuperClasses = true;
	// NOTE: In order to include interfaces, include Superclasses must be also set to true!
	private boolean includeInterfaces = true;


	public AllObjectTypes() {
	    super();
	    
	    this.shortName = "All";
    }

	@Override
    public String getHeader() {
	    return HEADER;
    }
	
	@Override
    public String getHeaderShort() {
	    return HEADER;
    }

	// TODO: LOW. Reference underlying IObject?
	private class ObjectTypeInfo implements ObjectInfo {

		String type;
		
		boolean isAbstract;
		boolean isInterface;
		boolean isInstantiatable;
		
		private void calculate() {
			// Update the isInstantiatableFlag
			isInstantiatable = !isAbstract && !isInterface;
		}
		
		@Override
        public void writeTo(Writer writer)  throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.type);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(Boolean.toString(this.isAbstract));
			writer.append(CSVConst.COMMA);
			writer.append(Boolean.toString(this.isInterface));
			writer.append(CSVConst.COMMA);
			writer.append(Boolean.toString(this.isInstantiatable));
        }
		
		@Override
        public void writeShortTo(Writer writer)  throws IOException {
			// TODO: Implement me
        }		

		// Implement value equality
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof ObjectTypeInfo)) {
				return false;
			}

			ObjectTypeInfo key = (ObjectTypeInfo) o;
			return this.type.equals(key.type);
		}

		// Always override hashcode when you override equals
		@Override
		public int hashCode() {
			int result = 17;

			result = 37 * result + (type == null ? 0 : type.hashCode());
			return result;
		}
		
		@Override
        public DataPoint[] getDataPoints() {
	        return new DataPoint[0];
        }
	}

	public void compute(Set<IObject> allObjects) {
		// store this
		this.allObjects = allObjects;
		this.objectInfos = new HashSet<ObjectInfo>(); 
		
		Crystal crystal = Crystal.getInstance();
		
		for (IObject object : allObjects) {
			Type objectType = object.getC();
			
			String typeName = objectType.getFullyQualifiedName();
			
			Set<String> allSuperClasses = new HashSet<String>();
			
			// TODO: Include transitive supertypes, starting from the current one
			// TODO: Exclude maybe abstract classes and interfaces, things that cannot be instantiated?
			// NOTE: Here, we include interfaces.
			
			if (includeSuperClasses) {
				Util.getSuperClasses(objectType, allSuperClasses, includeInterfaces);
			}
			else {
				allSuperClasses.add(typeName);
			}
			
			for (String type : allSuperClasses) {
				ObjectTypeInfo typeInfo = new ObjectTypeInfo();
				
				ITypeBinding typeBinding = crystal.getTypeBindingFromName(type);
				if (typeBinding != null ) {
					typeInfo.isAbstract = Modifier.isAbstract(typeBinding.getModifiers());
					typeInfo.isInterface = typeBinding.isInterface();
				}

				typeInfo.type = type;
				// Update calculated fields
				typeInfo.calculate();
				this.objectInfos.add(typeInfo);
			}
		}
	}

	@Override
    public DataPoint[] getDataPoints() {
		int numAllTypes = 0;
		int numInstantiatiableTypes = 0;
		int numAbstractClasses = 0;
		int numInterfaces = 0;
		
		for(ObjectInfo info : this.objectInfos ) {
			if (info instanceof ObjectTypeInfo ) {
				ObjectTypeInfo typeInfo = (ObjectTypeInfo)info;

				numAllTypes++;
				numInstantiatiableTypes += typeInfo.isInstantiatable ? 1 : 0;
				numAbstractClasses += typeInfo.isAbstract ? 1 : 0;
				numInterfaces += typeInfo.isInterface ? 1 : 0;
			}
			
		}
		
		return new DataPoint[] { new DataPoint("AllTypes", Integer.valueOf(numAllTypes)),
		        new DataPoint("InstantiatiableTypes", Integer.valueOf(numInstantiatiableTypes)),
		        new DataPoint("AbstractClasses", Integer.valueOf(numAbstractClasses)),
		        new DataPoint("Interfaces", Integer.valueOf(numInterfaces)),
		        new DataPoint("NumObjects", Integer.valueOf(this.allObjects.size()))};
    }
}
