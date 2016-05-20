package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

import edu.wayne.metrics.qual.Q_Which_A;

import oog.itf.IObject;
import ast.Type;

/**
 * EXPERIMENTAL: How many objects of type A of the same class A.
 * Which_A_Raw uses the raw type A.
 */
public class Which_A_Raw extends ObjectMetricBase<String, IObject> {
	private static final String HEADER = "IsOutlier,Type_A_Raw,Triplet";
	private static final String SHORT_HEADER = "ClusterSize,Key";

	public Which_A_Raw() {
	    super();
	    this.generateShortOutput = true;
	    this.shortName = "WAR";
    }

	@Override
    public String getHeader() {
	    return HEADER;
    }
	
	@Override
    public String getHeaderShort() {
	    return SHORT_HEADER;
    }

	@Override
    public String toStringValue(IObject val) {
	    return ADBTriplet.getTripletFrom(val).toShortString();
    }

	@Override
	public void compute(Set<IObject> allObjects) {
		
		this.mmap = ClusterInfoFactory.create();

		for (IObject tt1 : allObjects) {
			
			for (IObject tt2 : allObjects) {
				if (tt1 == tt2 ) {
					continue;
				}
				
				// Skip objects of different types?
				Type c1 = tt1.getC();
				Type c2 = tt2.getC();
				
				String f1 = c1.getFullyQualifiedName();
				String f2 = c2.getFullyQualifiedName();
				
				String raw1 = Util.getRawTypeName(f1);
				String raw2 = Util.getRawTypeName(f2);
				
				// Add the trivial clusters
				mmap.put(raw1, tt1);

				if (raw1.compareTo(raw2) == 0) {
					mmap.put(raw2, tt2);
				}			
				
				// TODO: Why do we need to use the traceability?
				
//				for (BaseTraceability trace1 : tt1.getTraceability()) {
//					ClassInstanceCreation newC1 = ReverseTraceabilityMap.getObjectCreation(trace1);
//					if (newC1 == null) {
//						continue;
//					}
//					
//					TypeDeclaration typeDecl1 = newC1.typeDeclaration;
//					String aC1 = typeDecl1 == null ? "" : typeDecl1.getFullyQualifiedName();
//					
//					if ( typeDecl1 == null ) {
//						int debug = 0; 
//						debug++;
//					}
//					// TODO: Why should newC1.typeDeclaration return null?
//					// TODO: HIGH. XXX. We need a method that returns the full generic type.
//
//					for (BaseTraceability trace2 : tt2.getTraceability()) {
//						ClassInstanceCreation newC2 = ReverseTraceabilityMap.getObjectCreation(trace2);
//						if (newC2 == null) {
//							continue;
//						}
//						TypeDeclaration typeDecl2 = newC2.typeDeclaration;
//						if (typeDecl2 == null ) {
//							int debug = 0; 
//							debug++;
//						}
//						String aC2 = typeDecl2 == null? "" : typeDecl2.getFullyQualifiedName();
//
//						// Ignore empty strings.
//						if (aC1.length() > 0 && aC1.compareTo(aC2) == 0) {
//							mmap.put(aC1, ADBTriplet.getTripletFrom(tt1).toLongString());
//							mmap.put(aC2, ADBTriplet.getTripletFrom(tt2).toLongString());
//						}
//					}
//				}
			}
		}
	}
	@Override
	public void visitOutliers(Writer writer, Set<Collection<IObject>> outliers) throws IOException {
		Q_Which_A qVisit = new Q_Which_A(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}
}
