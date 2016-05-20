package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IObject;
import ast.Type;
import edu.wayne.metrics.qual.Q_TMO;
import edu.wayne.metrics.utils.CSVConst;

/**
 * EXPERIMENTAL: Measures the number of distinct types that are merged by an object.
 * Measures the effect of collapsing the inheritance hierarchy.
 * 
 * TODO: HIGH. Should we include java.lang.Object in the list of types?
 * - Maybe easier to exclude it
 * TODO: HIGH. Look into possible bug in dealing with array objects, e.g., int[]; is that not a subtype of Object?
 * 
 * 
 * TODO: Compute some kind of percentage: %objects that use no inheritance, vs. % of objects that do.
 * 
 * TODO: Count things about abstract types.
 * 
 * Add number of columns:
 * -- MergedAllTypes, MergedConcreteClasses, MergedAbstractClasses, MergedInterfaces 
 * 
 * Add options to include/exclude things
 * -- includeAbstractClasses?
 * -- includeInterfaces?
 *  
 * TODO: Could come up with a version that uses raw types
 */
public class HowManyTypesMergedByObject extends ObjectSetStrategyBase {
	// DONE. Rename: "HowMany" -> "N"
	private static final String HOW_MANY = "N";
	// TODO: Output is kind of hard to read. Just display the type, instead of the triplets
	private static String HEADER = "Triplet,MergedTypes,HowMany";
	private static String SHORT_HEADER = "ClusterSize,Key";

	public HowManyTypesMergedByObject() {
	    super();
	    
	    this.generateShortOutput = true;
	    // this.shortName = "TMBO";
	    this.shortName = "TMO";
    }

	@Override
    public String getHeader() {
	    return HEADER;
    }

	@Override
    public String getHeaderShort() {
	    return SHORT_HEADER;
    }
	
	// TODO: LOW. Reference underlying IObject?
	// Made this class public to get the String triplets
	
	public class MergedTypesInfo implements ObjectInfo {

		String triplet;

		ADBTriplet aTriplet;
		public ADBTriplet getTriplet() {
			return aTriplet;
		}

		Set<String> allSuperClasses = new HashSet<String>();
		
		@Override
        public void writeTo(Writer writer)  throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.triplet);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(allSuperClasses.toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(allSuperClasses.size()));
			writer.append(CSVConst.DOUBLE_QUOTES);
        }
		
		@Override
		// Write only: ClusterSize,Key
        public void writeShortTo(Writer writer)  throws IOException {
			writer.append(Integer.toString(allSuperClasses.size()));
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			// TODO: HIGH. Escape this string, to avoid messing up R.
			writer.append(this.triplet);
			writer.append(CSVConst.DOUBLE_QUOTES);
        }		

		@Override
        public DataPoint[] getDataPoints() {
			// NOTE: Careful, the names must match whatever getColumnHeaders() returns
			return new DataPoint[]{ new DataPoint(HOW_MANY, (float)allSuperClasses.size()) };
        }
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof MergedTypesInfo)) {
				return false;
			}

			MergedTypesInfo key = (MergedTypesInfo) o;
			return this.triplet.equals(key.triplet) && this.allSuperClasses.equals(key.allSuperClasses);
		}

		// Always override hashcode when you override equals
		@Override
		public int hashCode() {
			int result = 17;
			
			result = 37 * result + (triplet == null ? 0 : triplet.hashCode());
			result = 37 * result + (allSuperClasses == null ? 0 : allSuperClasses.hashCode());
			return result;
		}			
				
	}
	
	public void compute(Set<IObject> allObjects) {
		// store this
		this.allObjects = allObjects;
		
		this.objectInfos = new HashSet<ObjectInfo>(); 
		
		for (IObject object : allObjects) {
			
			// TODO: Skip the MainObject here to? Not necessarily. This is unlike HowManyTypesInObjectSubTree.
			// The root class can still an interesting inheritance hierarchy
			
			Type c = object.getC();
			
			MergedTypesInfo objectInfo = new MergedTypesInfo();
			ADBTriplet tripletFrom = ADBTriplet.getTripletFrom(object);
			objectInfo.triplet = tripletFrom.toShortString();
			objectInfo.aTriplet = tripletFrom;
			// NOTE: Here, we do not include interfaces...
			// TODO: Why does it make sense to exclude interfaces?
			Util.getSuperClasses(c, objectInfo.allSuperClasses, false);
			objectInfos.add(objectInfo);
		}
	}

	@Override
	public String[] getColumnHeaders() {
		return new String[] { HOW_MANY};
	}
	
	@Override
	public void visitOutliers(Writer writer, Set<ObjectInfo> outliers) throws IOException {
		Q_TMO qVisit = new Q_TMO(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}

}
