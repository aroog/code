package edu.wayne.metrics.adb;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;
import oog.itf.IObject;
import ast.BaseTraceability;
import ast.ClassInstanceCreation;
import edu.wayne.metrics.qual.Q_HMN;
import edu.wayne.metrics.traceability.ReverseTraceabilityMap;

/**
 * How many new C expressions can be mapped to the same object  
 * 
 * different O's, same new C
 * 
 * TODO: HIGH. Fix the header...
 * TODO: HIGH. Fix the display of this metric. Output is hard to parse.
 * TODO: HIGH. Probably change the base class for this one.
 * TODO: Instead of Oid, just display A,D,B triplet?
 * TODO: LOW. String generates a lot of data!
 */
public class HowManyNewCToObject extends ObjectMetricBase<IObject,ClassInstanceCreation> {
	private static final String HEADER = "IsOutlier,ClassInstanceCreation,Triplet";
	private static final String SHORT_HEADER = "ClusterSize,Key";
	
	public HowManyNewCToObject() {
	    super();
	    
	    this.generateShortOutput = true;
	    // this.shortName = "NCTO";
	    // this.shortName = "1OnN";
	    // TODO: HIGH. Rename: too similar to HMO.
	    this.shortName = "HMN";
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
    public String toStringKey(IObject key) {
		ADBTriplet triplet = ADBTriplet.getTripletFrom(key);
		return triplet.toShortString();
    }

	@Override
    public String toStringValue(ClassInstanceCreation val) {
	    return val.complexExpression;
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
				
				mmap.put(tt1, newC1);
			
			}
		}
		if (errors>0)
			System.err.println("HowManyObjectsToNewC: Encountered bad data for " + errors + " objects out of " +allObjects.size() );
	}
	
	@Override
  	public void visitOutliers(Writer writer, Set<Collection<ClassInstanceCreation>> outliers)  throws IOException {
		Q_HMN qVisit = new Q_HMN(writer, outliers, shortName);
		qVisit.visit();
		qVisit.display();
    }
	
}
