package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IObject;
import edu.wayne.metrics.utils.CSVConst;

/**
 * EXPERIMENTAL: Measures the number of distinct types that are in the sub-structure of each object.
 * Uses the transitive descendants of an object.
 * 
 * Note: this is interesting. An object has such as an instance of "MoveCommand" has no children.
 * In the code, the class has fields. All the fields are in domain parameters.
 * So the object just has relations to other objects.
 * This measures the size of an object.
 * 
 * DONE: Convert this to use ObjectInfo, to fix the output:
 * - Add datapoint: HowMany
 * DONE. Generate datapoints automatically to be able to plot: objectSize.
 * 
 * TODO: Could come up with a version that uses raw types
 * 
 */
// TODO: LOW. Rename "HowManyTypesInObjectSubTree" --> "ObjectSizeInTypes"?
public class HowManyTypesInObjectSubTree extends ObjectSetStrategyBase {
	// DONE. Rename: "HowMany" -> "N"
	private static final String HOW_MANY = "N";
	private static String HEADER = "Triplet,ChildTypes,HowMany";
	private static final String SHORT_HEADER = "ClusterSize,Key";
	
	public HowManyTypesInObjectSubTree() {
	    super();
	    
	    this.generateShortOutput = true;
	    // this.shortName = "TIOS";
	    this.shortName = "TOS";
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
	private class ChildTypesInfo implements ObjectInfo {

		String triplet;

		Set<String> childTypes = new HashSet<String>();
		
		@Override
        public void writeTo(Writer writer)  throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(this.triplet);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(childTypes.toString());
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(childTypes.size()));
			writer.append(CSVConst.DOUBLE_QUOTES);
        }
		
		@Override
		// Write only: ClusterSize,Key		
        public void writeShortTo(Writer writer)  throws IOException {
			writer.append(CSVConst.DOUBLE_QUOTES);
			writer.append(Integer.toString(childTypes.size()));
			writer.append(CSVConst.DOUBLE_QUOTES);			
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.DOUBLE_QUOTES);
			// TODO: HIGH. Escape this string, to avoid messing up R.
			writer.append(this.triplet);
			writer.append(CSVConst.DOUBLE_QUOTES);
        }		

		@Override
        public DataPoint[] getDataPoints() {
			// NOTE: Careful, the names must match whatever getColumnHeaders() returns
			return new DataPoint[]{ new DataPoint(HOW_MANY, (float)childTypes.size()) };
        }
	}
	
	@Override
	public void compute(Set<IObject> allObjects) {
		
		this.objectInfos = new HashSet<ObjectInfo>(); 

		for (IObject tt1 : allObjects) {

			// DONE. Skip the main object
			if (tt1.isMainObject()) {
				continue;
			}
			
			String key = ADBTriplet.getTripletFrom(tt1).toShortString();
			
			Set<String> childTypes = new HashSet<String>();
			for (IObject desc : tt1.getDescendants()) {
				childTypes.add(desc.getC().getFullyQualifiedName());
			}
			
			ChildTypesInfo objectInfo = new ChildTypesInfo();
			objectInfo.triplet = key;
			objectInfo.childTypes = childTypes;
			objectInfos.add(objectInfo);
		}
	}
	
	@Override
	public String[] getColumnHeaders() {
		return new String[] { HOW_MANY };
	}
}
