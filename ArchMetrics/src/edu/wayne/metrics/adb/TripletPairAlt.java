package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IObject;


/**
 * Unordered pair of triplets.
 * Implemented as a set.
 * TODO: Use TripletPair
 * TODO: HIGH. Rename class. Just an ObjectPair
 */
public class TripletPairAlt {
	private static TripletPairAlt nullPair = null;

	public static TripletPairAlt getNullPair() {
		if (nullPair == null)
			nullPair = new TripletPairAlt();
		return nullPair;
	}	

	// TODO: Rename field; just an object pair
	private Set<IObject> triplets = new HashSet<IObject>();
	
	private TripletPairAlt() {
	}

	public TripletPairAlt(IObject first, IObject second) {
		this.triplets.add(first);
		this.triplets.add(second);
	}

	// Implement value equality
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof TripletPairAlt)) {
			return false;
		}

		TripletPairAlt key = (TripletPairAlt) o;
		return this.triplets.equals(key.triplets);
	}

	// Always override hashcode when you override equals
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (triplets == null ? 0 : triplets.hashCode());
		return result;
	}

	/**
	 * TOAND: TODO: LOW. Maybe output  <First, Second>, i.e., <<A1,D1,B1>, <A2,D2,B2>>,
	 * But this contains ",", which messes up the CSV output. 
	 * For now, output differently.
	 */
	public void writeTo(Writer writer) throws IOException {
		for(IObject object : triplets ) {
			ADBTriplet triplet  = ADBTriplet.getTripletFrom(object);
			// TODO:
			// triplet.writeTo(writer);
			// or
			// CUT: Output as two triplets, back to back
			// writer.append("<");
			writer.append(triplet.toLongString());
			writer.append(",");
			// writer.append(">");
		}
    }
	
	public IObject getFirst() {
		IObject object = null;
		IObject[] array = this.triplets.toArray(new IObject[0]);
		if(array != null && array.length == 2 ) {
			object = array[0];
		}
		return object;
	}
	
	public IObject getSecond() {
		IObject object = null;
		IObject[] array = this.triplets.toArray(new IObject[0]);
		if(array != null && array.length == 2 ) {
			object = array[1];
		}
		return object;
	}	
}
