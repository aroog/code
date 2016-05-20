package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

import edu.wayne.metrics.qual.Q_Which_A_In_Which_B;

import oog.itf.IObject;



public class Which_A_In_Which_B_Raw extends ClusterMetricBase {
	
	public Which_A_In_Which_B_Raw() {
	    super();
	    
	    //this.shortName = "WAIWBR";
	    this.shortName = "WAWBR";
	    this.generateShortOutput = true;	    
    }

	// TODO: HIGH. This is no longer being called by compute()
	// Which_A_In_Which_B_Raw: A1 = A2, d1 xx d2, B1 != B2 (xx = dont care)
	// Rationale: distinguish between different instances of the same type that are in different contexts
	// NOTE: The domain could be the same (due to inheritance) or not. So do not exclude D1 != D2;
	// NOTE: Could use the full type here, not just the raw type.
	@Override
	public boolean satisfiesMetric(IObject o1, IObject o2) {
		if ( o2 == null ) {
			return false;
		}
		
		ADBTriplet tt1 = ADBTriplet.getTripletFrom(o1);
		ADBTriplet tt2 = ADBTriplet.getTripletFrom(o2);

		
		String typeA1 = tt1.getRawTypeA();
		String typeA2 = tt2.getRawTypeA();
		Set<String> typeB1s = tt1.getRawTypeBs();
		Set<String> typeB2s = tt2.getRawTypeBs();
		
		for (String typeB1 : typeB1s) {
			for (String typeB2 : typeB2s) {
				if (typeB1 == typeB2)
					continue;
				
				if ( typeA1.compareTo(typeA2) == 0 
						&& typeB1.compareTo(typeB2) != 0 ) {
					// Why return the first one? Return all of them!
					return true;
				}		
			}
		}
	    return false;
    }

	// NOTE: Use the same type (full or raw) as satisfiesMetric
	@Override	
	public String getKey(IObject o) {
		ADBTriplet tt = ADBTriplet.getTripletFrom(o);

		StringBuffer builder = new StringBuffer();
		builder.append(tt.getRawTypeA());
		return builder.toString();
	}

	@Override
    protected void doCompute(Set<IObject> allObjects) {
		if (allObjects != null) {
			for (IObject o1 : allObjects) {
				String key1 = getKey(o1);
				mmap.put(key1, null);

				for (IObject o2 : allObjects) {
					if (o1 == o2)
						continue; // Do not compare to self

					ADBTriplet tt1 = ADBTriplet.getTripletFrom(o1);
					ADBTriplet tt2 = ADBTriplet.getTripletFrom(o2);

					
					String typeA1 = tt1.getRawTypeA();
					String typeA2 = tt2.getRawTypeA();
					Set<String> typeB1s = tt1.getRawTypeBs();
					Set<String> typeB2s = tt2.getRawTypeBs();
					
					for(String typeB1 : typeB1s ) {
						for (String typeB2 : typeB2s ) {
							
							if ( typeA1.compareTo(typeA2) == 0 
									&& typeB1.compareTo(typeB2) != 0 ) {
								// Why return the first one? Return all of them!
								TripletPairAlt pair = new TripletPairAlt(o1, o2);
								mmap.put(key1, pair);							}		
						}
					}					
				}
			}
		}
    }
	// Added a method to visit the outliers 
	@Override
    public void visitOutliers(Writer writer, Set<Collection<TripletPairAlt>> outliers)  throws IOException {
		Q_Which_A_In_Which_B qVisit = new Q_Which_A_In_Which_B(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
    }
}
