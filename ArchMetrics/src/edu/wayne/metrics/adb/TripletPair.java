package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;

import edu.wayne.metrics.utils.CSVConst;


/**
 * Unordered pair of triplets.
 * NOT implemented as a set (Presumably more efficient)
 * 
 * TODO: Convert to use IObjects
 */
public class TripletPair {
	private static TripletPair nullPair = null;

	public static TripletPair getNullPair() {
		if (nullPair == null)
			nullPair = new TripletPair();
		return nullPair;
	}	

	private ADBTriplet first = null;
	private ADBTriplet second = null;
	
	private TripletPair() {
	}

	public TripletPair(ADBTriplet first, ADBTriplet second) {
		this.first = first;
		this.second = second;
	}

	// Implement value equality
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof TripletPair)) {
			return false;
		}

		TripletPair key = (TripletPair) o;
		return ( this.first.equals(key.first) && this.second.equals(key.second) )  || 
				// Order does not matter
			   ( this.first.equals(key.second) && this.second.equals(key.first) );
	}

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;
		// Make hashCode commutative, with respect to first and second fields, by using multiplication, which is also commutative
		result *= (first == null ? 0 : first.hashCode()) * (second == null ? 0 : second.hashCode());
		return result;
	}

	/**
	 * TOAND: TODO: LOW. Maybe output  <First, Second>, i.e., <<A1,D1,B1>, <A2,D2,B2>>,
	 * But this contains ",", which messes up the CSV output. 
	 * For now, output differently.
	 */
	public void writeTo(Writer writer) throws IOException {
			// TODO:
			// triplet.writeTo(writer);
			// or
			// CUT: Output as two triplets, back to back
			// writer.append("<");
			writer.append(first.toLongString());
			writer.append(CSVConst.COMMA);
			// writer.append(">");
			
			// TODO:
			// triplet.writeTo(writer);
			// or
			// CUT: Output as two triplets, back to back
			// writer.append("<");
			writer.append(second.toLongString());
			// writer.append(">");			
			
			
			// More detailed ouptut
			writer.append(CSVConst.COMMA);
			writer.append(CSVConst.COMMA);
			writer.append(first.toObjectString());
			writer.append(CSVConst.COMMA);
			writer.append(second.toObjectString());
			
    }	
}
