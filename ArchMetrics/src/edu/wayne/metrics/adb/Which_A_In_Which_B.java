package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

import oog.itf.IObject;
import edu.wayne.metrics.qual.Q_Which_A_In_Which_B;


public class Which_A_In_Which_B extends ClusterMetricBase {
	
	public Which_A_In_Which_B() {
	    super();

	    // this.shortName = "WAIWB";
	    this.shortName = "WAWB";
	    this.generateShortOutput = true;	    
    }

	// TODO: HIGH. This is no longer being called by compute()
	// Which_A_In_Which_B: A1 = A2, D1 xx D2, B1 != B2 (xx = dont care)
	// Rationale: distinguish between different instances of the same type that are in different contexts.
	// NOTE: Same as Which_A_In_B_Raw, but uses the full type (full or raw)
	// Unlike Which_A_In_Which_B_Raw, this uses the full type, to see the effect of generic types.
	// NOTE: The domain could be the same (due to inheritance) or not. So do not exclude D1 != D2;
	// NOTE: Use the full, generic type here (instead of the raw type); you still get interesting results!
	@Override	
	public boolean satisfiesMetric(IObject o1, IObject o2) {
		if ( o2 == null ) {
			return false;
		}
		
		ADBTriplet tt1 = ADBTriplet.getTripletFrom(o1);
		ADBTriplet tt2 = ADBTriplet.getTripletFrom(o2);

		
		String typeA1 = tt1.getTypeA();
		String typeA2 = tt2.getTypeA();
		Set<String> typeB1s = tt1.getTypeBs();
		Set<String> typeB2s = tt2.getTypeBs();
		
		for(String typeB1 : typeB1s ) {
			for(String typeB2 : typeB2s ) {
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

	// NOTE: Same as Metric2, but uses the full type (full or raw)
	@Override	
	public String getKey(IObject o) {
		ADBTriplet tt = ADBTriplet.getTripletFrom(o);

		StringBuffer builder = new StringBuffer();
		builder.append(tt.getTypeA());
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

					String typeA1 = tt1.getTypeA();
					String typeA2 = tt2.getTypeA();
					Set<String> typeB1s = tt1.getTypeBs();
					Set<String> typeB2s = tt2.getTypeBs();

					for (String typeB1 : typeB1s) {
						for (String typeB2 : typeB2s) {
							
							if ( typeA1.compareTo(typeA2) == 0 
									&& typeB1.compareTo(typeB2) != 0 ) {
								// Why return the first one? Return all of them!
								TripletPairAlt pair = new TripletPairAlt(o1, o2);
								mmap.put(key1, pair);
							}		
						}
					}					
				}
			}
		}
    }

	@Override
    public void visitOutliers(Writer writer, Set<Collection<TripletPairAlt>> outliers)  throws IOException {
		Q_Which_A_In_Which_B qVisit = new Q_Which_A_In_Which_B(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
    }
}