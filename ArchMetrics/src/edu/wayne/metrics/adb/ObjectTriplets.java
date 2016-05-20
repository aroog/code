package edu.wayne.metrics.adb;

import java.util.Set;

import oog.itf.IObject;

/**
 * Default Object metric, to generate all the object triplets.
 * Basically, puts all the triplets into one bucket!
 * 
 * NOTE: Does not work well with TripletPair. Generates too many TripletPairs!
 * TODO: LOW. Create a different base class for this kind of metric.
 * Or change the base class, to use ObjectSetStrategyBase
 *
 */
@Deprecated
public class ObjectTriplets extends ClusterMetricBase {

	@Override
	public String getKey(IObject tt) {
		return "";
	}

	@Override
	public boolean satisfiesMetric(IObject tt1, IObject tt2) {
		return true;
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

					if (satisfiesMetric(o1, o2)) {
						TripletPairAlt pair = new TripletPairAlt(o1, o2);
						mmap.put(key1, pair);
					}
				}
			}
		}
    }
}
