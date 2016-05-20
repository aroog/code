package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oog.itf.IObject;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import ast.TypeDeclaration;
import edu.wayne.metrics.qual.Q_SO;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.ObjectsUtils;

/**
 * Metric: ScatteringObjects
 * different new C (in different enclosing types), same O
 * TODO: HIGH. Check distinct enclosingtypes! Put them into a Set.
 * TODO: HIGH. Do we need 2 versions? ScatteringObjectsDistinct?
 * 
 * DONE. Compute Scattering and ScatteringFactor
 */
// NOTE: Major Name: "ScatteredObjects". Minor Name: "Factor".  ShortName: "SO_F".
// NOTE: Alt. Major Name: "ObjectScattering". Minor Name: "Factor".  ShortName: "OS_F".
public class ScatteringObjects extends ObjectSetStrategyBase {
	private static final String FACTOR = "F"; // DONE: Rename: ScatteringFactor -> F
	private static final String HEADER = "ADB,EnclosingTypes,Scattering,ScatteringFactor";
	// Swapped the column names to be consistent with others
	private static String SHORT_HEADER = "ClusterSize,key";

	public ScatteringObjects() {
	    super();
	    
	    // this.shortName = "OS";
	    
	    this.shortName = "SO";
	    this.generateShortOutput = true;
    }

	@Override
    public String getHeader() {
	    return HEADER;
    }
	
	@Override
    public String getHeaderShort() {
	    return SHORT_HEADER;
    }	

	// Made public to access ObjectInfo
	public class ScatteredObjectInfo implements ObjectInfo {

		String triplet;
		Set<String> allExpressions;
		int scattering;
		float scatteringFactor;
		// Added the triplet
		ADBTriplet aTriplet;
		
		
		// Added getters to pull what is needed
		public Set<String> getAllExpressions() {
			return allExpressions;
		}

		public ADBTriplet getTriplet() {
			return aTriplet;
		}
		
		@Override
        public void writeTo(Writer writer)  throws IOException {
			this.calculate();
			
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.triplet);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.allExpressions.toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(this.scattering));
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Float.toString(this.scatteringFactor));
			writer.append(CSVConst.DOUBLE_QUOTES);
        }
		
		@Override
        public void writeShortTo(Writer writer)  throws IOException {
			this.calculate();
			
			// Swapped the column
			writer.append(Float.toString(this.scatteringFactor));
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.triplet);
			writer.append(CSVConst.DOUBLE_QUOTES);		
			
        }		
		
		@Override
        public DataPoint[] getDataPoints() {
			// Update calculated fields
			this.calculate();
			
			// Return an array of datapoints;
			List<DataPoint> dataPoints = new ArrayList<DataPoint>();
			// NOTE: Careful, the names must match whatever getColumnHeaders() returns
			dataPoints.add(new DataPoint(FACTOR, this.scatteringFactor));
			return dataPoints.toArray(new DataPoint[0]);
		}

		private void calculate() {
			this.scattering = allExpressions.size();
			this.scatteringFactor = 1.0f - (1.0f/scattering);
        }
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof ScatteredObjectInfo)) {
				return false;
			}

			ScatteredObjectInfo key = (ScatteredObjectInfo) o;
			return this.triplet.equals(key.triplet) && this.allExpressions.equals(key.allExpressions);
		}

		// Always override hashcode when you override equals
		@Override
		public int hashCode() {
			int result = 17;

			result = 37 * result + (triplet == null ? 0 : triplet.hashCode());
			result = 37 * result + (allExpressions == null ? 0 : allExpressions.hashCode());
			return result;
		}			
		
	}

	@Override
	public  void compute(Set<IObject> allObjects) {
		// store this
		this.allObjects = allObjects;
		this.objectInfos = new HashSet<ObjectInfo>(); 
		
		if (allObjects != null) {
			for (IObject tt1 : allObjects) {
				String key = ADBTriplet.getTripletFrom(tt1).toShortString();
				
				for (BaseTraceability trace1 : tt1.getTraceability()) {
					ClassInstanceCreation newC1 = ReverseTraceabilityMap.getObjectCreation(trace1);
					if (newC1 == null) {
						continue;
					}
					// Use a set since we want to compute *distinct* cases
					Set<String> set = new HashSet<String>();
					for (BaseTraceability trace2 : tt1.getTraceability()) {
						if ( trace2 == trace1 ) {
							continue;
						}
						
						ClassInstanceCreation newC2 = ReverseTraceabilityMap.getObjectCreation(trace2);
						if (newC2 == null) {
							continue;
						}

						// TODO: HIGH. XXX. Check the different enclosingtypes/declaringTypes.
						// Same O, different new C
						if (newC1 != newC2) {
							// The newC1.complexExpression may be the same; in that case, they will get merged
							// This is because our ClassInstanceCreation does not have any information about its context!
							// We need the BaseTraceability to figure out the full meaning
							// or to get the enclosingType
//							String string1 = newC1.complexExpression + "|" + trace1.toString();
//							String string2 = newC2.complexExpression + "|" + trace2.toString();

							TypeDeclaration enclosingTypeC1 = ObjectsUtils.getEnclosingType(newC1);
							TypeDeclaration enclosingTypeC2 = ObjectsUtils.getEnclosingType(newC2);
							if (enclosingTypeC1 != null && enclosingTypeC2 != null) {
								String string1 = enclosingTypeC1.getFullyQualifiedName();
								String string2 = enclosingTypeC2.getFullyQualifiedName();
								
								set.add(string1);
								set.add(string2);
							}
							else {
								int debug = 0; debug++;
							}
						}
					}
					
					if (set.size() > 0) {
						ScatteredObjectInfo scatteredObject = new ScatteredObjectInfo();
						// Populated Triplet for the given object
						scatteredObject.aTriplet = ADBTriplet.getTripletFrom(tt1);
						scatteredObject.triplet = key;
						scatteredObject.allExpressions = set;
						this.objectInfos.add(scatteredObject);
					}
				}
			}
		}
	}

	@Override
	public String[] getColumnHeaders() {
		// NOTE: the major name is "ObjectScattering (OS)" or "ScatteredObjects (SO)".
		// DONE. Rename: "ScatteringFactor" -> just "F"
		return new String[] {FACTOR};
	}
	
	public void visitOutliers(Writer writer, Set<ObjectInfo> outliers) throws IOException {
		Q_SO qVisit = new Q_SO(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}
	
}
