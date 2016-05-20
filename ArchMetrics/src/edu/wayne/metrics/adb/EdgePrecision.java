package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oog.itf.IEdge;
import oog.itf.IElement;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ast.FieldDeclaration;
import ast.Type;
import ast.TypeDeclaration;
import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.qual.Q_PTEP;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.ograph.OObject;
import edu.wayne.ograph.OPTEdge;

// TODO: HIGH. XXX. Why not using fields() in EdgePrecision? Make the implementation match the formal definition.
// - Extract aux. judgement fields()
// TODO: HIGH. Investigate special cases EdgePrecision: 
// - How to deal with the current type; include in the list?
// - How to deal with java.lang.Object; include in the list?

// TODO: HIGH. Careful: this is just PTEdgePrecision. Can we also define a DFEdgePrecision?
// TODO: HIGH. Exclude fields of array type, like char[], byte[], etc.
// TODO: HIGH. Exclude noise data where PTEP_F = 1.0
public class EdgePrecision extends EdgeMetricBase {

	// DONE. Rename: "PrecisionFactor" -> "F"
	private static final String FACTOR = "F";
	// TODO: HIGH. Add: IsField,
	private static String HEADER = "VariableType,VariableName,DomainD,DomainD_IsPublic,DomainD_IsFormal,TypeB,Type_NumSubClasses,Type_PossibleSubClasses,Type_NumConcreteClasses,Type_ConcreteClasses,PrecisionRatio,PrecisionFactor";
	
	// Swapped the column names to be consistent through all metrics
	private static String SHORT_HEADER = "ClusterSize,key";

	public EdgePrecision() {
	    super();
	    
	    this.shortName = "PTEP";
	    this.generateShortOutput = true;
    }

	public String getHeader() {
		return HEADER;
	}
	
	@Override
    public String getHeaderShort() {
	    return SHORT_HEADER;
    }
	
	// TODO: Hold on to underlying IEdge. But it is not just one. It's a set of IEdges...
	public class EdgePrecisionInfo implements EdgeInfo {

		private String typeA;
		private String domainD;
		private String typeB;
		
		// TODO: HIGH. Delete unused fields.
		
		// true if the domainD is public; false if it is private. 
		boolean isDomainPublic;

		// true if the domainD is formal; false if it is locally declared. 
		private boolean isDomainFormal;
		
		// DONE. Convert this to a Set<String>, instead of one string.
		// - It is important to avoid duplicates
		// DONE. The list of transitive subclasses of TypeA
		// NOTE: This is what the Eclipse Type Hierarchy would show you.
		private Set<String> allSubClasses = new HashSet<String>();

		// The concrete classes that TypeA can really refer to in the OOG
		private Set<String> concreteClasses = new HashSet<String>();

		// If TypeA is not an interface or an abstract class:
		// -- The list of SubClasses would be empty; the list of ConcreteClasses would be the same as TypeA
		
		// The name of the field
		// Unclear why we need this on an A,D,B triplet;
		// A,D,B identifies an object ... fieldnames due to field declarations, which are related to edges.
		private String fieldName;
		
		// TODO: HIGH. Clean-up unused fields
		private boolean isField;
		
		private boolean isLocalVariable;
		
		private boolean isMethodParam;
		private float precisionFactor;
		private float precisionRatio;
		
		public EdgePrecisionInfo(String typeA, String domain, String typeB) {
			this.typeA = typeA;
			this.domainD = domain;
			this.typeB = typeB;
		}

		
		public String getTypeA() {
			return this.typeA;
		}
		
		public String getTypeB() {
			return this.typeB;
		}

		public String getDomainD() {
	    	return this.domainD;
	    }
		
		public Set<String> getAllSubClasses() {
	    	return this.allSubClasses;
	    }

		public Set<String> getConcreteClasses() {
	    	return this.concreteClasses;
	    }

		public String getFieldName() {
	    	return fieldName;
	    }

		public void setFieldName(String fieldName) {
	    	this.fieldName = fieldName;
	    }

		public int getNumSubClasses() {
	    	return this.allSubClasses.size();
	    }
		
		public int getNumConcreteClasses() {
	    	return this.concreteClasses.size();
	    }

		public boolean isDomainPublic() {
	    	return isDomainPublic;
	    }

		public void setDomainPublic(boolean isDomainPublic) {
	    	this.isDomainPublic = isDomainPublic;
	    }
		
		public boolean isDomainFormal() {
	    	return isDomainFormal;
	    }

		public void setDomainFormal(boolean isDomainFormal) {
	    	this.isDomainFormal = isDomainFormal;
	    }
		
		public void calculate() {
			// Update the calculated fields
			int numConcreteClasses = getNumConcreteClasses();
			int numSubClasses = getNumSubClasses();
			
			if ( numConcreteClasses > numSubClasses ) {
				int debug = 0; debug++;
			}
			
			// TODO: HIGH. Adjustment. To avoid negative precision number.
			// Still due to arrays,  e.g., Object[]
			 if ( numSubClasses < numConcreteClasses ) {
				 System.err.println("ArchMetrics: Edge Precision: negative value being adjusted! Please investigate.");
				 // HACK: Investigate!
				 numSubClasses = numConcreteClasses;
			 }
			
			// Update precisionRatio
			precisionRatio = (float) numConcreteClasses / (float) numSubClasses;
			precisionFactor = 1.0f - precisionRatio;
		}
		
		public float getPrecisionRatio() {
        	return precisionRatio;
        }

		public float getPrecisionFactor() {
        	return precisionFactor;
        }

		@Override
		// TODO: HIGH. Fix implementation of equals, hashCode: take into  account all fields	
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof EdgePrecisionInfo)) {
				return false;
			}

			EdgePrecisionInfo key = (EdgePrecisionInfo) o;
			return this.typeA.equals(key.typeA) && this.domainD.equals(key.domainD) && this.typeB.equals(key.typeB);
		}

		@Override
		// TODO: HIGH. Fix implementation of equals, hashCode: take into  account all fields	
	    public int hashCode() {
		    final int prime = 31;
		    int result = 1;
		    result = prime * result + ((typeA == null) ? 0 : typeA.hashCode());
		    result = prime * result + ((domainD == null) ? 0 : domainD.hashCode());
		    result = prime * result + ((typeB == null) ? 0 : typeB.hashCode());
		    return result;
	    }
		
		@Override
		public String toString() {
			StringBuffer builder = new StringBuffer();
			builder.append("<");
			builder.append(getTypeA());
			builder.append(",");
			builder.append(getDomainD());
			builder.append(",");
			builder.append(getTypeB());
			builder.append(">");
			return builder.toString();
		}		
		
		public void writeTo(Writer writer) throws IOException {
			
			// Update calculated fields before writing them
			this.calculate();
			
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.getTypeA());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.getFieldName());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.getDomainD());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(Boolean.valueOf(this.isDomainPublic()).toString());
			writer.append(CSVConst.COMMA);
			writer.append(Boolean.valueOf(this.isDomainFormal()).toString());
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.getTypeB());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(this.getNumSubClasses()));
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.getAllSubClasses().toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(this.getNumConcreteClasses()));
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.getConcreteClasses().toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(Float.toString(getPrecisionRatio()));
			writer.append(CSVConst.COMMA);
			writer.append(Float.toString(getPrecisionFactor()));
	    }

		@Override
		public void writeShortTo(Writer writer) throws IOException {
			// Update calculated fields before writing them
			this.calculate();
			
			// Swapped to be consistent with all metrics
			writer.append(Float.toString(getPrecisionFactor()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.getTypeA());
			writer.append(CSVConst.DOUBLE_QUOTES);
			
			
	    }		
	
		@Override
        public DataPoint[] getDataPoints() {
			// Update calculated fields
			this.calculate();
			
			// Return an array of datapoints;
			List<DataPoint> dataPoints = new ArrayList<DataPoint>();
			// NOTE: Careful, the names must match whatever getColumnHeaders() returns			
			dataPoints.add(new DataPoint(FACTOR, getPrecisionFactor()));
			
			// CUT: for testing only.
			// dataPoints.add(new DataPoint("DEBUG", 100));
			
			return dataPoints.toArray(new DataPoint[0]);
		}
	}
	
	// TODO: Not using allEdges
	public void compute(Set<IEdge> allEdges) {
		edgeInfos = new HashSet<EdgeInfo>();
		
    	Crystal crystal = Crystal.getInstance();
    	
		ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
		Map<FieldDeclaration, Set<IElement>> fieldDeclMap = instance.getFieldDeclMap();

		for (Entry<FieldDeclaration, Set<IElement>> entry : fieldDeclMap.entrySet()) {
			FieldDeclaration key = entry.getKey();

			// TODO: Should we not check the annotation on the field?
			// - Maybe exclude 'shared'? 
			// - Or count them separately? Since 'shared' is not much better than no annotations.
			Type toType = key.fieldType;
			
			// Exclude primitive types. There are no corresponding objects in the OOG!
			// NOTE: This also excludes unresolvable types that cannot be converted to ITypeBindings.
			// TODO: HIGH. Look into cases where getTypeBindingFromName returns null when it should not.
			ITypeBinding toTypeBinding = crystal.getTypeBindingFromName(toType.getFullyQualifiedName());
			if (toTypeBinding == null || toTypeBinding.isPrimitive() ) {
				continue;
			}
			
			// DONE: Check for nulls
			TypeDeclaration enclosingTypeDecl = key.enclosingType;
			if (enclosingTypeDecl!=null){
				Type enclosingType = enclosingTypeDecl.type;
				
				String domain = "";
				// XXX. Check that FieldDeclaration != null for most cases
				// Only cases where FieldDeclaration may be null is for fields from AliasXML files
				if (key != null) {
					String annotation = key.annotation;
					domain = Util.getDomainString(annotation);
				}

				// DONE. Set the domain for the field decl. instead of using ""
				EdgePrecisionInfo edgeInfo = new EdgePrecisionInfo(toType.toString(), domain, enclosingType.toString());
				// Set the various field
				edgeInfo.setFieldName( key.fieldName );
				//edgeInfo.setDomainFormal(toDomain.isFormal());
				//edgeInfo.setDomainPublic(toDomain.isPublic());

				// Compute the real, concrete classes
				// TODO: Have EdgePrecisionInfo hold on to entry.getValue() which returns a Set<IElement>
				// which is filtered to consider only IEdges
				// Or generate one EdgePrecisionInfo object for each IEdge?
				getConcreteClasses(key, entry.getValue(), edgeInfo.concreteClasses);

				// Compute the possible subclasses
				Util.getSubClasses(toType, edgeInfo.allSubClasses);
				
				if ( edgeInfo.concreteClasses.size() > edgeInfo.allSubClasses.size() ) {
					int debug = 0; debug++;
					System.err.println("EdgePrecision: no subclasses for " + toType.getFullyQualifiedName());
					// TODO: Exclude bad data points from being added to the Set of EdgeInfo objects?
					continue;
				}
				if ( edgeInfo.concreteClasses.size() == 0 ) {
					// TODO: HIGH. What about the number of concrete Classes being zero?
					// TODO: Exclude bad data points from being added to the Set of EdgeInfo objects?
					// Bad rows messes up the precision number. Leads to PrecisionFactor = 1.
					System.err.println("EdgePrecision: no concrete classes for " + toType.getFullyQualifiedName());
					continue;
				}

				edgeInfos.add(edgeInfo);
			}
			else{
				System.err.println("EdgePrecision: cannot find enclosing type decl for "+key);
			}
		}
	}
	
	// NOTE: Use reverse traceability information, from the OOG
	private static void getConcreteClasses(FieldDeclaration fieldRef, Set<IElement> elements, Set<String> concreteClasses) {

		// DO NOT add the field type, which could be an interface
		Type toType = fieldRef.fieldType;
		
		for (IElement element : elements) {
			if (element instanceof OPTEdge) {
				OPTEdge edge = (OPTEdge) element;

				OObject runtimeObject = edge.getOdst();
				Type declaredType = runtimeObject.getC();

				String fullyQualifiedName = toType.getFullyQualifiedName();
				if ( fullyQualifiedName.endsWith("[]") ) {
					int debug = 0; debug++;
				}
				// Check that the types are compatible!
				// TODO: Is this check needed? Shouldn't the OOG construction already take care of that?
				// Or is our reverse traceability map broken, in that it associates unrelated things?
				if (Util.isSubtypeCompatible(declaredType, toType)) {
					concreteClasses.add(declaredType.toString());
				}
			}
		}
   }
	
	@Override
	public String[] getColumnHeaders() {
		// NOTE: Major metric name is: "PTEP".
		// DONE. Rename: PrecisionFactor -> just "F", to get "EP_F"?
		return new String[] { FACTOR};
	}
	
	@Override
	public void visitOutliers(Writer writer, Set<EdgeInfo> outliers) throws IOException {
		Q_PTEP qVisit = new Q_PTEP(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}
}
