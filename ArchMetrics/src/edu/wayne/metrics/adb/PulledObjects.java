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
import ast.Type;
import ast.TypeDeclaration;
import edu.cmu.cs.aliasjava.parser.DomainParams;
import edu.wayne.metrics.qual.Q_PulledObjects;
import edu.wayne.metrics.qual.Q_Which_A;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.ObjectsUtils;

//TODO: Major Name: "ObjectPulling". Minor Name: "Percentage".  ShortName: "OPP".
public class PulledObjects extends ObjectSetStrategyBase {
	private static String HEADER = "O_id,InstanceName,TypeDisplayName,DeclaredInType,PulledToType";

	public PulledObjects() {
	    super();
	    
	    this.shortName = "PO";
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
	// Made the class public in order to access the underlying data
	public class PulledObjectInfo implements ObjectInfo {

		String O_id;
		String instanceName;
		String typeDisplayName;
		String declaredInType;
		String pulledToType;
		Type declaredType;
		Type pulledintoType;
		Type objectType;
		
		

		@Override
        public void writeTo(Writer writer)  throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.O_id);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.instanceName);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.typeDisplayName);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.declaredInType);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.pulledToType);
			writer.append(CSVConst.DOUBLE_QUOTES);
        }

		//TOSUM: Added getters for all projected variables that I need, I needed the type of the declared and pulled objects
		public Type getDeclaredType() {
			return declaredType;
		}

		public Type getPulledintoType() {
			return pulledintoType;
		}

		public Type getObjectType() {
			return objectType;
		}
		
		@Override
        public void writeShortTo(Writer writer)  throws IOException {
			// TODO: Implement me
        }
		
		@Override
        public DataPoint[] getDataPoints() {
	        return new DataPoint[0];
        }
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof PulledObjectInfo)) {
				return false;
			}

			PulledObjectInfo key = (PulledObjectInfo) o;
			return this.O_id.equals(key.O_id) && this.declaredInType.equals(key.declaredInType) && this.pulledToType.equals(key.pulledToType);
		}

		// Always override hashcode when you override equals
		@Override
		public int hashCode() {
			int result = 17;

			result = 37 * result + (O_id == null ? 0 : O_id.hashCode());
			result = 37 * result + (instanceName == null ? 0 : instanceName.hashCode());
			result = 37 * result + (typeDisplayName == null ? 0 : typeDisplayName.hashCode());
			result = 37 * result + (declaredInType == null ? 0 : declaredInType.hashCode());
			result = 37 * result + (pulledToType == null ? 0 : pulledToType.hashCode());
			return result;
		}		
	}

	// DONE. Compute % pulled objects: | pulledObjects | / | allObjects |
	private int numPulled = 0;
	private int numNotPulled = 0;
	
	// PulledObjects: measure effect of "pulling" of objects from formal domain parameters, to actual domains
	// 1. Get EnclosingType(A) --> C
	// (may need to reach into traceability info to get this; or OOG_DM may need to expose additional info)
	// 2. Get domain of instance of type A domainof(A)--> D_formal;
	//    D_formal \in params(C) (params returns list of formal parameters; D_formal must be declared on C) 
	// 3. D in domains(B) (domains(...) returns list of locally declared (private and public) domains, excluding parameters)
	// 4. D_formal is bound to D
	// Rationale: Locate "pulled" objects of type A. A field could be declared in a formal domain, D_formal.
	// where the domain D is locally declared on B;
	// 
	// NOTE: Take into account inheritance; could separately measure effect of inheritance;
	// Since inheritance is also a form of de-localized information.
	// Could split inheritance into a separate metric.
	
	// <A,D,B> in the OOG ==> D is a domain declared on B
	// In the code/annotations, the domain of A can be a formal domain parameter that is bound to B::D 
	
	// NOTE: The current implementation is hackish since the traceability information does not have the formal domain parameter.
	// Instead, we're just relying on the enclosing type in the traceability information being different from the declaring type of the domain.
	// TODO: HIGH: This is hackish because the types could be different, not because of formal/actual bindings
	// but because of inheritance (the domain is introduced at declaration point).
	// NOTE: For PulledObjects, we ignore the second argument to satisfiesMetric!

	
	// To show: <A,Df,Bf> --> <A,Da,Ba>
	// O -> <A,Da,Ba> 
	//   -> Da --> Ca::da
	//   - Cf \in declaringTypes(A)
	//   -   Ca != Cf ...
	// <A,Df,Bf> --> 
	// 
	public void compute(Set<IObject> allObjects) {
		// store this
		this.allObjects = allObjects;
		this.objectInfos = new HashSet<ObjectInfo>(); 
		
		for (IObject object : allObjects) {
			
			ADBTriplet tt1 = ADBTriplet.getTripletFrom(object);

			ClassTable CT = ClassTable.getInstance();

			for(String typeNameB : tt1.getTypeBs() ) {
			// Lookup TypeDeclaration from a Type Name
			TypeDeclaration typeB = CT.getTypeDeclaration(typeNameB);
			if (typeB == null){
				int debug = 0;
				debug++;
				System.err.println("PulledObjects: cannot find TypeDeclaration in Class Table: "+typeNameB);
				// HACK: This should never happen!
				continue;
			}
			// Important: get the unqualified domain name, not "C::d"
			String domainD = tt1.getRawDomainD();

			Set<BaseTraceability> set = tt1.getObjectA().getTraceability();
			if (set != null) {
				for (BaseTraceability traceability : set) {
					AstNode expression = traceability.getExpression();
					if (expression instanceof ClassInstanceCreation) {
						ClassInstanceCreation newAExpression = (ClassInstanceCreation) expression;
						TypeDeclaration typeA = newAExpression.typeDeclaration;
						if (typeA == null) {
							int debug = 0;
							debug++;
							System.err.println("PulledObjects: Unexpected null ClassInstanceCreation.typeDeclaration: "+expression);
							// HACK: This should never happen!
							continue;
						}
						String typeAString = typeA.type.getFullyQualifiedName();

						// Sanity check: make sure that we are looking at a 'new A()' that created the <A,D,B>
						if (typeAString.compareTo(tt1.getTypeA()) == 0) {

							String formalD = Util.getDomainString(newAExpression.annotation);

							// Get the declaring type
							TypeDeclaration typeC = ObjectsUtils.getEnclosingType(newAExpression);
							if (typeC == null){
								int debug = 0;
								debug++;
								System.err.println("PulledObjects: Unexpected null enclosing type"+expression);
								// HACK: This should never happen!
								continue;
							}
							
							if (typeC.hasDomainParam(formalD) && typeC != typeB // Can we use reference inequality here?
							        // TODO: Check if domainD is qualified as C::D, before passing as argument to hasDomain
							        && typeB.hasDomain(domainD)) {
								
								// Yes, it is a pulled object!
								numPulled++;
								
								PulledObjectInfo objectInfo = new PulledObjectInfo();
								objectInfo.O_id = object.getO_id();
								objectInfo.instanceName = object.getInstanceDisplayName();
								objectInfo.typeDisplayName = object.getTypeDisplayName();
								objectInfo.objectType = object.getC();
								objectInfo.declaredInType = typeC.getFullyQualifiedName();
								objectInfo.declaredType = typeC.type;
								objectInfo.pulledintoType = typeB.type;
								objectInfo.pulledToType = typeB.getFullyQualifiedName();
								objectInfos.add(objectInfo);								
							}
							else if (typeC.hasDomain(formalD) && typeC == typeB // Can we use reference inequality here?
							        // TODO: Check if domainD is qualified as C::D, before passing as argument to hasDomain
							        && typeB.hasDomain(domainD)) {
								// The object was not pulled
								numNotPulled++;
							}
						}
					}
				}
			}
			}
		}
	}

	@Override
    public DataPoint[] getDataPoints() {
		// TODO: HIGH. XXX. The denominator is wrong. The numAllObjects should be obtained from the annotations, not
		// from the OGraph since the OGraph does not have formals.
		// Need to count all the fields of a reference type. Also, what about variables.
		// TODO: Maybe use the ReverseTraceability map
//		int numAllObjects = this.allObjects.size();
//		int numPulledObjects = this.objectInfos.size();
//		float pctPulledObjects = (float)numPulledObjects/(float)numAllObjects * 100;

		float pctPulledObjects = (float) numPulled / ((float) numPulled + numNotPulled) * 100;
		
		// NOTE: Major metric name is: "PO".
		return new DataPoint[] {
				// DONE. Rename: "NumAllObjects" -> NO or NAO
				new DataPoint("NAO", numPulled + numNotPulled),
				// DONE. Rename: "NumPulledObjects" -> NPO? Or just "N"? "PO_N"
				new DataPoint("N", numPulled),
				// DONE. Rename: "%PulledObjects" -> PPO? Or just "P"? "PO_P"
				new DataPoint("P", pctPulledObjects) };
    }
	
		
	public void visitOutliers(Writer writer, Set<ObjectInfo> outliers) throws IOException {
		// Not just outliers here
		outliers = objectInfos;		
		Q_PulledObjects qVisit = new Q_PulledObjects(writer, outliers);		
		qVisit.visit();
		qVisit.display();
	}
	
}
