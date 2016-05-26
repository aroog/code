package oogre.metrics;

import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import oogre.actions.CSVConst;
import oogre.refinements.tac.OType;
import oogre.refinements.tac.RankingStrategy;
import oogre.refinements.tac.TM;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;


/*
 * XXX. Count separately owner and alpha? For now, adding them up.
 * 
 * XXX. Do not mix count and weights: messes up our reporting...
 * 
 */
public class MetricsInfo implements Comparable<MetricsInfo> {

    public static final int BEFORE = -1;
    public static final int EQUAL = 0;
    public static final int AFTER = 1;
    
	private static final int FIELD_O_MULTIPLIER = 2^5; 
	private static final int FIELD_A_MULTIPLIER = 2^4;
	
	private static final int PARAM_O_MULTIPLIER = 2^3;
	private static final int PARAM_A_MULTIPLIER = 2^3;

	private static final int RET_O_MULTIPLIER = 2^2;
	private static final int RET_A_MULTIPLIER = 2^2;

	private static final int OWNED_MULTIPLIER = 10;
	private static final int PD_MULTIPLIER = 10;
	private static final int SHARED_MULTIPLIER = 100;

	private static final int DEFAULT_MULTIPLIER = 1;
	
	private static final int INDEX_LENT = 0;
	private static final int INDEX_UNIQUE = 1;
	private static final int INDEX_SHARED = 2;
	private static final int INDEX_OWNED = 3;
	private static final int INDEX_PD = 4;
	private static final int INDEX_P = 5;
	private static final int INDEX_ND = 6;
	private static final int INDEX_OWNER = 7;
	private static final int INDEX_OTHER = 8;
	
	// XXX. Maybe count n.d separately than n.any
	// XXX. What does 'other' include? n.any?
	//[lent, unique, shared, owned, PD, p, n.d, owner, other]
	int[] fieldMetric =        {0,0,0,0,0,0,0,0,0};
	int[] varMetric =          {0,0,0,0,0,0,0,0,0};
	int[] methodParamsMetric = {0,0,0,0,0,0,0,0,0};
	int[] returnTypeMetric =   {0,0,0,0,0,0,0,0,0};
	
	private int noFields;
	private int noLocalVars;
	private int noMethodParams;
	private int noReturnTypes;

	private int totalLent;
	private int totalUnique;
	private int totalShared;
	private int totalOwned;
	private int totalPD;
	private int totalP;
	private int totalOwner;
	private int totalND;
	private int totalOther;
	
	private Random random = new Random();
	// Turn-off randomization to simplify testing
	// May end up with with equal TMs
	private static final boolean NO_RANDOMIZE= true;

	
	private RankingStrategy ranking = RankingStrategy.getInstance();
	
	public MetricsInfo() {
	    super();
    }

	public void analyzeTM(TM tm) {

		for (Entry<Variable, Set<OType>> entry : tm.entrySet()) {
			Variable au = entry.getKey();
			
			if(au instanceof SourceVariable){
				SourceVariable srcVariable = (SourceVariable)au;
			
			//Set<OType> value = entry.getValue();
			
			// Measure the types that would be picked, instead of the whole set of typings, across the TM.
			OType value = ranking.pickFromSet(entry.getValue());
			
			String variableKind ="";
			if(srcVariable.getBinding().isField()){
				variableKind = "f";
			}
			else if(srcVariable.getBinding().isParameter()){
				variableKind = "p";
			}
			else{
				variableKind = "v";
			}
			
			switch (variableKind) {
			case "f":
				noFields++;
				getMetrics(fieldMetric, value, FIELD_O_MULTIPLIER, FIELD_A_MULTIPLIER, DEFAULT_MULTIPLIER);
				break;
			// XXX. Should we worry only about fields for now?				
			case "v":
				noLocalVars++;
				// Use DEFAULT_MULTIPLIER for local vars
				getMetrics(varMetric, value, DEFAULT_MULTIPLIER, DEFAULT_MULTIPLIER, DEFAULT_MULTIPLIER);
				break;
			case "p":
				noMethodParams++;
				getMetrics(methodParamsMetric, value, PARAM_O_MULTIPLIER, PARAM_A_MULTIPLIER, DEFAULT_MULTIPLIER);
				break;
			default:
				noReturnTypes++;
				getMetrics(returnTypeMetric, value, RET_O_MULTIPLIER, RET_A_MULTIPLIER, DEFAULT_MULTIPLIER);
//				break;
//			default:
				// XXX. What else to count?
				// XXX. Count temporaries?
			}
		}
		}
		
		computeStats();
	}
	
	private void computeStats() {
		this.totalLent =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_LENT);
		this.totalUnique =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_UNIQUE);
		this.totalShared =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_SHARED);
		this.totalOwned =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_OWNED);
		this.totalPD =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_PD);
		this.totalP =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_P);
		this.totalOwner =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_OWNER);
		this.totalND =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_ND);
		this.totalOther =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, INDEX_OTHER);
	}
	
	private int getSumColumn(int[] fieldMetric, int[] varMetric, int[] methodParamsMetric, int[] returnTypeMetric, int i) {
		int totalColumn = 0;
		totalColumn += fieldMetric[i]; // XXX. Weight fields more heavily...
		totalColumn += varMetric[i];
		totalColumn += methodParamsMetric[i]; // maybe exclude this one too.
		totalColumn += returnTypeMetric[i]; // exclude for now
		return totalColumn;
	}
	
	private void getMetrics(int[] entry, Set<OType> value) {
		for (OType oType : value){
			getMetrics(entry, oType, 1, 1, 1);
		}
	}

	private void getMetrics(int[] entry, OType oType) {
        getMetrics(entry, oType, 1, 1, 1);
    }

	private void getMetrics(int[] entry, OType oType, int weight) {
        getMetrics(entry, oType, weight, 1, 1);
    }

	private void getMetrics(int[] entry, OType oType, int weightO, int weightA) {
        getMetrics(entry, oType, weightO, weightA, 1);
    }

	private void getMetrics(int[] entry, OType oType, int weightO, int weightA, int weightI) {
		// Add up owner and alpha to the same entries
		trackPart(entry, oType.getOwner(), weightO);
		if(oType.getAlpha() != null)
			trackPart(entry, oType.getAlpha(), weightA);
		// NOTE: Do not count the inner; it is determined by others...
		if(oType.getInner() != null)
			trackPart(entry, oType.getInner(), weightI);
    }

	private void trackPart(int[] entry, String part) {
        trackPart(entry, part, 1);
    }

	private void trackPart(int[] entry, String part, int weight) {
		if (part.equals("shared")) {
			entry[INDEX_SHARED] += 1 * weight * SHARED_MULTIPLIER  * (1 + (NO_RANDOMIZE? 0: random.nextInt(2)));
		}
		else if (part.equals("p")) {
			entry[INDEX_P]+= 1 * weight;
		}
		else if (part.equals("owner")) {
			entry[INDEX_OWNER]+= 1 * weight * (1 + random.nextInt(2));
		}
		else if (part.equals("this.owned")) {
			entry[INDEX_OWNED] += 1 * weight * OWNED_MULTIPLIER  * (1 + (NO_RANDOMIZE? 0: random.nextInt(2)));
		}
		// XXX. How are we going to distinguish between "PD" and "n.PD"?
		else if (part.equals("this.PD")) {
			entry[INDEX_PD] += 1 * weight * PD_MULTIPLIER  * (1 + (NO_RANDOMIZE? 0: random.nextInt(2)));
		}
		else  {
			entry[INDEX_OTHER]+= 1 * weight;
		}
    }

	@Override
	/**
	 * Returns a negative integer, zero, or a positive integer if this object is less than, equal to, or greater than the specified object.
	 * 
	 *  TM1 > TM2 <=> utility(TM1) > utility(TM2) => utility(TM1) - utility(TM2) > 0  
	 */
    public int compareTo(MetricsInfo o) {
	    
	    // CUT: [does not work]
		//  return getUtility() - o.getUtility();
	    
		if(this == o ) {
			return EQUAL;
		}
		
		// Look at criteria in order: owned, PD, owner, shared, p, ... 
		
		// More owned is better
		if (this.getTotalOwned() > o.getTotalOwned()) {
			return BEFORE;
		}
		if (this.getTotalOwned() < o.getTotalOwned()) {
			return AFTER;
		}
		
		// More PD is better
		if(this.getTotalPD() > o.getTotalPD()) {
			return BEFORE;
		}
		if(this.getTotalPD() < o.getTotalPD()) {
			return AFTER;
		}		
			
		// LESS peer is better
		if(this.getTotalOwner() < o.getTotalOwner()) {
			return BEFORE;
		}
		if(this.getTotalOwner() > o.getTotalOwner()) {
			return AFTER;
		}

		// LESS shared is better
		if(this.getTotalShared() < o.getTotalShared()) {
			return BEFORE;
		}
		if(this.getTotalShared() > o.getTotalShared()) {
			return AFTER;
		}
		
		// More parametric code is better
		if(this.getTotalP() > o.getTotalP() ) {
			return BEFORE;
		}		
		if(this.getTotalP() < o.getTotalP() ) {
			return AFTER;
		}		
		
		// MORE n.d is better
		if(this.getTotalND() > o.getTotalND()) {
			return BEFORE;
		}		
		if(this.getTotalND() < o.getTotalND()) {
			return AFTER;
		}		
		
		// XXX. What if we still can't decide?!
		return EQUAL;
    }

	// Return negative value to rank lower; we pick the lowest element
	private int getUtility() {
		// v1. Minimize shared
		// return this.totalShared - o.totalShared;
		
		// v2. Maximize hierarchy: owned + PD
	    return -(this.totalOwned + this.totalPD);
    }
	
	// XXX. Useful to display percentages. But percentages not needed for comparison;
	// Since we are comparing TMs with the same number of AUs
	// TODO: Make it possible to write this to a different writer
	public void displayStats(String nameTM, StringBuilder writer) {
		
		System.err.println("*******************************************************");
		System.err.println("Statistics for " + nameTM);
		System.err.println("Total shared = " + this.totalShared );
		System.err.println("Total owned = " + this.totalOwned );
		System.err.println("Total PD = " + this.totalPD );
		System.err.println("Total p = " + this.totalP );
		System.err.println("Total owner = " + this.totalOwner );
		System.err.println("*******************************************************");

		// XXX. Also display percentages instead of raw numbers?
		writer.append(this.totalShared );
		writer.append(CSVConst.COMMA);
		writer.append(this.totalOwned );
		writer.append(CSVConst.COMMA);
		writer.append(this.totalPD );
		writer.append(CSVConst.COMMA);
		writer.append(this.totalP );
		writer.append(CSVConst.COMMA);
		writer.append(this.totalOwner);
	}

	public int getTotalShared() {
    	return totalShared;
    }

	public int getTotalOwned() {
    	return totalOwned;
    }

	public int getTotalPD() {
    	return totalPD;
    }

	public int getTotalP() {
    	return totalP;
    }

	public int getTotalOwner() {
    	return totalOwner;
    }

	public int getTotalND() {
    	return totalND;
    }

	public int getTotalOther() {
    	return totalOther;
    }

}
