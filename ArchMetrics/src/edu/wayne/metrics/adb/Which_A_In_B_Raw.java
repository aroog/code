package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

import edu.wayne.metrics.qual.Q_Which_A_In_B;


import oog.itf.IObject;

// TOSUM: Seems buggy:
// 	java.util.List<minidraw.framework.Figure>	owned	java.util.List<minidraw.boardgame.BoardFigure>	MAPS	Classification is unknown	
// is being marked as: Classification is unknown
// Also maybe relax the classification: look for just containers; instead of container of a general type?
// TOMAR: The classification is unknown as I am looking only for Containers of elements of General types. 
//I think we will add another classification container of elements of Class and like you said, RENAME container of elements of general types. 
public class Which_A_In_B_Raw extends ClusterMetricBase {
	
	public Which_A_In_B_Raw() {
	    super();
	    
	    // this.shortName = "WAIBR";
	    this.shortName = "WABR";
	    this.generateShortOutput = true;	    
    }

	// TODO: HIGH. This is no longer being called by compute()
	@Override
	// Which_A_In_B_Raw: A1 = A2, d1 != d2, B1 = B2
	// NOTES:
	// - Use the raw type, not the full type, which may include generics:
	// -- If we consider only the raw type, we often find a few instances that satisfy this condition. 
	// -- With the full type, fewer or no triplets satisfy this condition.
	// - Use the raw domain name (without the qualifier of the declaring class "Class::Domain").
	// Rationale: distinguish between objects in different domains of the same parent object.
	//            how multiple domains (public, private) can express design intent.	
	public boolean satisfiesMetric(IObject o1, IObject o2) {
		if (o2 == null) {
			return false;
		}
		
		ADBTriplet tt1 = ADBTriplet.getTripletFrom(o1);
		ADBTriplet tt2 = ADBTriplet.getTripletFrom(o2);

		String typeA1 = tt1.getRawTypeA();
		String typeA2 = tt2.getRawTypeA();
		String d1 = tt1.getRawDomainD();
		String d2 = tt2.getRawDomainD();
		Set<String> typeB1s = tt1.getRawTypeBs();
		Set<String> typeB2s = tt2.getRawTypeBs();
		
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
	
	// NOTE: Do not use the full type here (see satisfiesMetric)
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
					String d1 = tt1.getRawDomainD();
					String d2 = tt2.getRawDomainD();
					Set<String> typeB1s = tt1.getRawTypeBs();
					Set<String> typeB2s = tt2.getRawTypeBs();
					
					for(String typeB1 : typeB1s ) {
						for(String typeB2 : typeB2s ) {
							
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
	public void visitOutliers(Writer writer, Set<Collection<TripletPairAlt>> outliers) throws IOException {
		Q_Which_A_In_B qVisit = new Q_Which_A_In_B(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}
}
