package edu.wayne.metrics.adb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oog.itf.IObject;
import edu.wayne.metrics.utils.CSVOutputUtils;

/**
 * EXPERIMENTAL: Buckets of instances that are in the same domain, but that are of types that are in the different packages.
 * Measures differences between the code structure packages and the runtime structure tiers.
 * 
 * NOTE: It is important that the domain here is an actual domain, on the OOG, not a formal domain parameter.
 * 
 * TODO: Rename: Tier->Domain in classname? 
 * TODO: Rename: tier->domain in header
 * 
 * TODO: HIGH. XXX. Show different sub-keys, sub-values, i.e., the name of the type.
 * - Requires extending the interface		
 * 
 * TODO: HIGH. XXX. Convert this to sets of pairs.
 * TODO: HIGH. XXX. "Same package": does not include "children packages"
 * 
 */
public class SameTierDifferentPackage extends ClusterKeyValueBase<String, IObject, PrimaryValue<String, IObject>> {
	private static final String HEADER = "IsOutlier,ClusterSize,Tier,Package,Types";

	private static final String HEADER_SHORT = "ClusterSize,Key";

	private Map<Pair<String, String>, PrimaryValue<String, IObject>> map = new HashMap<Pair<String,String>, PrimaryValue<String,IObject>>();

	public SameTierDifferentPackage() {
	    super();
	    
	    // this.shortName = "STDP";
	    // TODO: Rename: "1DnP" not a valid R identifier.
	    // TODO: Rename: "nP1D", or "PnD1"
	    this.shortName = "1DnP";
	    this.generateShortOutput = true;	    
    }

	@Override
    public String getHeader() {
	    return HEADER;
    }
	
	@Override
    public String getHeaderShort() {
	    return HEADER_SHORT;
    }	
	
	public boolean satisfiesMetric(IObject o1, IObject o2) {
		if (o2 == null) {
			return false;
		}
		ADBTriplet tt1 = ADBTriplet.getTripletFrom(o1);
		ADBTriplet tt2 = ADBTriplet.getTripletFrom(o2);
		if (tt1.getDomainStringD().compareTo(tt2.getDomainStringD()) == 0
		        && isPackageDifferent(tt1.getTypeA(), tt2.getTypeA()) ) {
			return true;
		}
		return false;
	}

	
	private boolean isPackageDifferent(String type1, String type2) {
		return Util.getPackageName(type1).compareTo(Util.getPackageName(type2)) != 0;
    }

	@Override
	public PrimaryValue<String, IObject> getValue(IObject o) {
		StringBuffer builder = new StringBuffer();
		ADBTriplet tt = ADBTriplet.getTripletFrom(o);
		builder.append(Util.getPackageName(tt.getTypeA()));
		String packName = builder.toString();

		// Lookup (tier, package)
		Pair<String, String> key = new Pair<String,String>(getKey(o), packName);
		PrimaryValue<String, IObject> entry = map.get(key);
		if (entry == null) {
			// Output: package name only;
			// NOTE: if including package.Type or just fully qualified type, then no longer counting packages!
			entry = new PrimaryValue<String, IObject>(packName);
			map.put(key, entry);
		}
		entry.addSecondaryValue(o);

		return entry;
	}

	@Override
	public String getKey(IObject o) {
		ADBTriplet tt = ADBTriplet.getTripletFrom(o);
		StringBuffer builder = new StringBuffer();
		builder.append(tt.getDomainStringD());
		return builder.toString();
	}

	@Override
	public String toStringValue(PrimaryValue<String, IObject> val) {
		StringBuffer builder = new StringBuffer();
		builder.append(val.first);
		builder.append(",");
		
		Set<String> vals = new HashSet<String>();
		for (IObject second : val.second) {
			ADBTriplet tt = ADBTriplet.getTripletFrom(second);
			vals.add(tt.getShortTypeA());
		}
		
		builder.append(CSVOutputUtils.sanitize(vals.toString()));
		return builder.toString();
	}
}
