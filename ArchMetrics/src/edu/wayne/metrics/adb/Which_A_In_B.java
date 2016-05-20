package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;
import edu.wayne.metrics.qual.Q_Which_A_In_B;
import oog.itf.IObject;

// TODO: HIGH. XXX. Some of the callers may expect an unqualified domain
public class Which_A_In_B extends ClusterMetricBase {

	public Which_A_In_B() {
	    super();
	    
	    // this.shortName = "WAIB";
	    this.shortName = "WAB";
	    this.generateShortOutput = true;	    
    }

	// TODO: HIGH. XXX. This is no longer being called by compute()
	@Override
	// Which_A_In_B: A1 = A2, d1 != d2, B1 = B2
	// NOTES:
	// - Use the full type, which may include generics:
	// -- If we consider only the  type, we often find a few instances that satisfy this condition. 
	// -- With the full type, fewer or no triplets satisfy this condition.
	// - Use the  domain name (without the qualifier of the declaring class "Class::Domain").
	// Rationale: distinguish between objects in different domains of the same parent object.
	//            how multiple domains (public, private) can express design intent.	
	public boolean satisfiesMetric(IObject o1, IObject o2) {
		if (o2 == null) {
			return false;
		}
		
		ADBTriplet tt1 = ADBTriplet.getTripletFrom(o1);
		ADBTriplet tt2 = ADBTriplet.getTripletFrom(o2);
		
		String typeA1 = tt1.getTypeA();
		String typeA2 = tt2.getTypeA();
		String d1 = tt1.getRawDomainD();
		String d2 = tt2.getRawDomainD();
		Set<String> typeB1s = tt1.getTypeBs();
		Set<String> typeB2s = tt2.getTypeBs();
		
		for (String typeB1 : typeB1s) {
			for (String typeB2 : typeB2s) {
				if (typeB1 == typeB2)
					continue;

				if (typeA1.compareTo(typeA2) == 0
						&& d1.compareTo(d2) != 0
						&& typeB1.compareTo(typeB2) == 0) {
					// Why return the first one? Return all of them!
					return true;
				}
			}
		}
		return false;
	}
	
	// NOTE: use the full type here (see satisfiesMetric)
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
					String d1 = tt1.getRawDomainD();
					String d2 = tt2.getRawDomainD();
					Set<String> typeB1s = tt1.getTypeBs();
					Set<String> typeB2s = tt2.getTypeBs();
					for(String typeB1 : typeB1s) {
						for (String typeB2 : typeB2s ) {
							
							if (typeA1.compareTo(typeA2) == 0
									&& d1.compareTo(d2) != 0
									&& typeB1.compareTo(typeB2) == 0) {
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
		// Added the visitor here
		Q_Which_A_In_B qVisit = new Q_Which_A_In_B(writer, outliers, shortName);
		qVisit.visit();
		
		qVisit.display();
    }
}
