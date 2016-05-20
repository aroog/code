package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IObject;
import ast.AstNode;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import ast.ClassTable;
import ast.TypeDeclaration;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.ObjectsUtils;

/**
 * TODO: This is a weird metric: counting domains that are just in the annotations; not really in the code structure.
 * Could still deal with understandability.
 * Except that we rarely have developers trace to domains in the code.
 * Maybe they just have to understand what the C::d means.
 * 
 * DONE. Filter out duplicates, since we are traversing objects and looking at their domains. So we might encounter domains multiple times.
	Do so by implementing value equality on the InheritedDomainInfo, putting them into a set.
 */
public class InheritedDomains extends ObjectSetStrategyBase {
	private static String HEADER = "D_id,d,DeclaredInType,InheritedToType";
	
	// Use a Set of D_ids to compute the total number of domains.
	private Set<String> allDomains = new HashSet<String>();
	
	public InheritedDomains() {
	    super();
	    
	    this.shortName = "InhD";	    
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
	private class InheritedDomainInfo implements ObjectInfo {

		String D_id;
		String d;
		String declaredInType;
		String inheritedToType;
		
		@Override
        public void writeTo(Writer writer)  throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.D_id);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.d);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.declaredInType);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.inheritedToType);
			writer.append(CSVConst.DOUBLE_QUOTES);
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
			if (!(o instanceof InheritedDomainInfo)) {
				return false;
			}

			InheritedDomainInfo key = (InheritedDomainInfo) o;
			return this.D_id.equals(key.D_id) && this.d.equals(key.d) && this.declaredInType.equals(key.declaredInType)
			        && this.inheritedToType.equals(key.inheritedToType);
		}

		// Always override hashcode when you override equals
		@Override
		public int hashCode() {
			int result = 17;

			result = 37 * result + (D_id == null ? 0 : D_id.hashCode());
			result = 37 * result + (d == null ? 0 : d.hashCode());
			result = 37 * result + (declaredInType == null ? 0 : declaredInType.hashCode());
			result = 37 * result + (inheritedToType == null ? 0 : inheritedToType.hashCode());
			return result;
		}


		@Override
        public DataPoint[] getDataPoints() {
	        return new DataPoint[0];
        }
	
	}	

	// InheritedDomains: measure effect of inheritance
	// but because of inheritance (the domain is introduced at declaration point)
	// NOTE: For InheritedDomains, we ignore the second argument to satisfiesMetric!
	@Override	
	public void compute(Set<IObject> allObjects) {
		// store this
		this.allObjects = allObjects;
		this.objectInfos = new HashSet<ObjectInfo>();

		ClassTable CT = ClassTable.getInstance();

		for (IObject object : allObjects) {
			ADBTriplet tt1 = ADBTriplet.getTripletFrom(object);

			for(String typeNameB : tt1.getTypeBs() ) {
			// Lookyp TypeDeclaration from a Type Name
			TypeDeclaration typeB = CT.getTypeDeclaration(typeNameB);

			String domainD = tt1.getRawDomainD();
			String D_id = object.getParent().getD_id();
			
			this.allDomains.add(D_id);

			Set<BaseTraceability> set = tt1.getObjectA().getTraceability();
			if (set != null) {
				for (BaseTraceability traceability : set) {
					AstNode expresssion = traceability.getExpression();
					if (expresssion instanceof ClassInstanceCreation) {
						ClassInstanceCreation newAExpression = (ClassInstanceCreation) expresssion;
						TypeDeclaration typeA = newAExpression.typeDeclaration;

						if (typeA == null) {
							int debug = 0;
							debug++;
							System.err.println("Unexpected null ClassInstanceCreation.typeDeclaration");
							// HACK: This should never happen!
							continue;
						}
						String typeAString = typeA.type.getFullyQualifiedName();
						// Sanity check: make sure that we are looking at a 'new A()' that created the <A,D,B>
						if (typeAString.compareTo(tt1.getTypeA()) == 0) {

							// Get the declaring type
							TypeDeclaration typeC = ObjectsUtils.getEnclosingType(newAExpression);
							
							if (typeC == null || typeB == null) {
								// TODO: HIGH. XXX. why should this happen?
								continue;
							}
							if ( typeC.type == typeB.type ) {
								// This means, it is not inherited
								// Have to handle this special case, since subtyping is reflexive
								continue;
							}

							if (typeC.hasDomain(domainD) && Util.isSubtypeCompatible(typeB.type, typeC.type)) {
								// Yes, domainD is an inherited domain!
								
								InheritedDomainInfo objectInfo = new InheritedDomainInfo();
								objectInfo.D_id = D_id;
								objectInfo.d = domainD;
								objectInfo.declaredInType = typeC.getFullyQualifiedName();
								objectInfo.inheritedToType = typeB.getFullyQualifiedName();
								objectInfos.add(objectInfo);								
							}
						}
					}
				}
			}
		}
		}
	}

	// DONE. Compute % inherited domains: | inheritedDomains | / | allDomains |
	@Override
    public DataPoint[] getDataPoints() {
		int numInherited = objectInfos.size();
		int numAllDomains = allDomains.size();
		
		float pctInheritedDomains = (float) numInherited / ((float) numAllDomains) * 100;
		
		// DONE. Rename: "%InheritedDomains" -> just "P"
		return new DataPoint[] { new DataPoint("P", pctInheritedDomains) };
    }		
}
