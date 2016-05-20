package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

import oog.itf.IObject;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import edu.wayne.metrics.qual.Q_HMO;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;


/**
 * How many objects can be traced to the same new C expression:
 * 
 * different O's, same new C
 * 
 * TODO: HIGH. Fix the header...
 * TODO: HIGH. Fix the display of this metric. Output is hard to parse.
 * TODO: HIGH. Probably change the base class for this one.
 * TODO: Instead of Oid, just display A,D,B triplet?
 * TODO: LOW. String generates a lot of data!
 */
public class HowManyObjectsToNewC extends ObjectMetricBase<ClassInstanceCreation,IObject> {
	private static final String HEADER = "IsOutlier,ClassInstanceCreation,Triplet";
	private static final String SHORT_HEADER = "ClusterSize,Key";

	public HowManyObjectsToNewC() {
	    super();
	    
	    this.generateShortOutput = true;
	    // this.shortName = "OTNC";
	    this.shortName = "HMO";
    }

	@Override
    public String toStringValue(IObject val) {
		ADBTriplet triplet = ADBTriplet.getTripletFrom(val);
		return  triplet.toShortString();
    }

	@Override
    public String toStringKey(ClassInstanceCreation key) {
	    return key.complexExpression;
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
	public void compute(Set<IObject> allObjects) {
		
		this.mmap = ClusterInfoFactory.create();

		long errors = 0;
		for (IObject tt1 : allObjects) {
			
			Set<BaseTraceability> traceabilityList1 = tt1.getTraceability();
			for (BaseTraceability trace1 : traceabilityList1) {
				ClassInstanceCreation newC1 = ReverseTraceabilityMap.getObjectCreation(trace1);
				if (newC1 == null) {
					int debug = 0; debug++;
					errors++;
					continue;
				}
				
				// Add trivial clusters too
				mmap.put(newC1, tt1);
				
				for (IObject tt2 : allObjects) {
					if (tt1 == tt2) {
						continue;
					}
					
					Set<BaseTraceability> traceability2 = tt2.getTraceability();
					for (BaseTraceability trace2 : traceability2) {
						ClassInstanceCreation newC2 = ReverseTraceabilityMap.getObjectCreation(trace2);
						// TODO: HIGH. It is crucial here that we have the *same* ClassInstanceCreation object
						// due to creating the ClassInstanceCreation object from the *same* ASTNode node
						if (newC2 == null) {
							int debug = 0; debug++;
							continue;
						}
						else if (newC1 == newC2) {
							mmap.put(newC2, tt2);
						}
					}
				}
			}
		}
		if (errors>0)
			System.err.println("HowManyObjectsToNewC: Encountered bad data for " + errors + " objects out of " +allObjects.size() );
	}
	
	@Override
	public void visitOutliers(Writer writer, Set<Collection<IObject>> outliers) throws IOException {
		Q_HMO qVisit = new Q_HMO(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
	}
}
