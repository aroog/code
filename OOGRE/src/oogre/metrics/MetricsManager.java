package oogre.metrics;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import oogre.actions.CSVConst;
import oogre.actions.CustomWriter;
import oogre.refinements.tac.TM;

// Map each TM to a MetricsInfo
public class MetricsManager {

	// XXX. Is OK to cache MetricsInfo per TM? The main goal is that there will frequent calls to compare.
	// And we don't want to recompute every time.
	// But this won't work if the same TM object changes across invocations.
	private Hashtable<TM, MetricsInfo> metrics = new Hashtable<TM, MetricsInfo>();

	private StringBuilder writer;

	// XXX. What about other?
	private static final String HEADER_METRICS_ROW = ",,,,";
	private static final String HEADER_METRICS = "shared,owned,PD,p,owner";
	
	
	public MetricsManager(StringBuilder writer) {
		this.writer = writer;
    }

	public MetricsInfo computeMetrics(TM tm) {
		MetricsInfo mInfo = metrics.get(tm);
//		MetricsInfo mInfo = null;
		if (mInfo == null) {
			mInfo = new MetricsInfo();
			mInfo.analyzeTM(tm);
			metrics.put(tm, mInfo);
		}
		return mInfo;
	}

	private int compareBetween(TM tm1, TM tm2) {
		// Sanity check; 
		// Avoid call to expensive TM.equals()
		if(tm1 == tm2) {
			return MetricsInfo.EQUAL;
		}

		MetricsInfo m1 = computeMetrics(tm1);
		MetricsInfo m2 = computeMetrics(tm2);
		int metricsComparison = m1.compareTo(m2);
		if(metricsComparison == MetricsInfo.EQUAL) {
			// Here, two TMs have the same solution Index, have the same numbers we care about, but are different objects...
			// Calling TM.hashCode() is very expensive: computes hashCode of map, then hashcode of sets, etc.
			metricsComparison = tm1.hashCode() - tm2.hashCode();
		}
		return metricsComparison;
    }
	
	// XXX. Avoid extra work here. Why not keep savedTMs as a sorted set?
	public TM getTMToSave(Set<TM> savedTMs) {
		TM retTM = null;

			// For TreeSet, the ordering maintained by a set (whether or not an explicit comparator is provided) must be consistent with equals
			// if it is to correctly implement the Setinterface. 		
			SortedSet<TM> tms = new TreeSet<TM>(new Comparator<TM>() {
			@Override
			// compare must be consistent with equals if it is to correctly 
			public int compare(TM tm1, TM tm2) {
				return compareBetween(tm1, tm2);
			}


		});
		tms.addAll(savedTMs);

		if (tms.size() > 0) {
			TM last = tms.last();
			TM first = tms.first();

			MetricsInfo metricsFirst = null;
			if (first != null) {
				metricsFirst = computeMetrics(first);
			}

			MetricsInfo metricsLast = null;
			if (last != null) {
				metricsLast = computeMetrics(last);
			}

			// int debug = compareBetween(first, last);
			// int debug2 = metricsFirst.compareTo(metricsLast);
			
			// Convention: Pick the lowest ranked in TM; that will be the one the fewest shared, most owned, etc.
			retTM = first;

			System.err.println("SaveAnnotations: there are " + savedTMs.size() + " possible TMs to save");
			
			writer.append(CSVConst.NEWLINE);
			writer.append(CSVConst.NEWLINE);
			writer.append("TMs");
			writer.append(CSVConst.COMMA);
			writer.append("BestTM");
			writer.append(HEADER_METRICS_ROW);
			writer.append(CSVConst.COMMA);
			writer.append("WorstTM");
			writer.append(HEADER_METRICS_ROW);
			writer.append(CSVConst.NEWLINE);
			writer.append("ToSave");
			writer.append(CSVConst.COMMA);
			writer.append(HEADER_METRICS);
			writer.append(CSVConst.COMMA);
			writer.append(HEADER_METRICS);
			writer.append(CSVConst.NEWLINE);
			writer.append(savedTMs.size());
			writer.append(CSVConst.COMMA);
			metricsFirst.displayStats("first TM", writer);
			writer.append(CSVConst.COMMA);
			metricsLast.displayStats("last TM", writer);
			writer.append(CSVConst.NEWLINE);
		}

		return retTM;
	}

}
